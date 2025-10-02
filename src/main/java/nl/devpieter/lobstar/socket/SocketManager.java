package nl.devpieter.lobstar.socket;

import com.microsoft.signalr.*;
import io.reactivex.rxjava3.core.Completable;
import nl.devpieter.lobstar.ConfigManager;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.managers.RetryManager;
import nl.devpieter.lobstar.models.common.Retry;
import nl.devpieter.lobstar.models.common.RetryHolder;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.lobstar.socket.utils.CastUtils;
import nl.devpieter.sees.Listener.Listener;
import org.slf4j.Logger;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class SocketManager implements Listener {

    private static SocketManager INSTANCE;

    private final ConfigManager configManager = ConfigManager.getInstance();
    private final RetryManager retryManager = RetryManager.getInstance();
    private final Logger logger = Lobstar.getInstance().getLogger();

    private final List<ISocketListener<?>> socketListeners = new ArrayList<>();

    private HubConnection hubConnection;
    private boolean disconnecting;
    private boolean reconnecting;

    private final RetryHolder reconnectRetry = retryManager.register("socket-reconnect", new Retry(
            Duration.ofSeconds(2),
            Duration.ofSeconds(30),
            1.5
    ), this::attemptReconnect);

    private SocketManager() {
    }

    public static SocketManager getInstance() {
        if (INSTANCE == null) INSTANCE = new SocketManager();
        return INSTANCE;
    }

    public void addListener(ISocketListener<?> listener) {
        if (this.isConnected() || this.isConnecting())
            throw new IllegalStateException("Cannot add listener while connected or connecting");
        this.socketListeners.add(listener);
    }

    public Completable connect() {
        if (this.isConnected()) return Completable.complete();
        if (this.isConnecting()) return Completable.error(new IllegalStateException("Already connecting"));

        this.disconnecting = false;
        this.reconnecting = false;

        HttpHubConnectionBuilder builder = HubConnectionBuilder.create(this.configManager.getString("api_base_url") + "/api/hub/plugin");
        builder.withHeader("X-API-KEY", this.configManager.getString("api_key"));

        this.hubConnection = builder.build();

        Map<Class<?>, BiConsumer<String, ISocketListener<?>>> actionMap = new HashMap<>();
        actionMap.put(Action1.class, (target, listener) -> {
            Type[] types = listener.getTypes().toArray(new Type[0]);
            this.hubConnection.on(target, CastUtils.ca1(listener.getAction()), CastUtils.c2c(types[0]));
        });
        actionMap.put(Action2.class, (target, listener) -> {
            Type[] types = listener.getTypes().toArray(new Type[0]);
            this.hubConnection.on(target, CastUtils.ca2(listener.getAction()), CastUtils.c2c(types[0]), CastUtils.c2c(types[1]));
        });
        actionMap.put(Action3.class, (target, listener) -> {
            Type[] types = listener.getTypes().toArray(new Type[0]);
            this.hubConnection.on(target, CastUtils.ca3(listener.getAction()), CastUtils.c2c(types[0]), CastUtils.c2c(types[1]), CastUtils.c2c(types[2]));
        });
        actionMap.put(Action4.class, (target, listener) -> {
            Type[] types = listener.getTypes().toArray(new Type[0]);
            this.hubConnection.on(target, CastUtils.ca4(listener.getAction()), CastUtils.c2c(types[0]), CastUtils.c2c(types[1]), CastUtils.c2c(types[2]), CastUtils.c2c(types[3]));
        });

        for (ISocketListener<?> listener : this.socketListeners) {
            BiConsumer<String, ISocketListener<?>> consumer = actionMap.get(listener.getActionType());
            if (consumer != null) consumer.accept(listener.getTarget(), listener);
        }

        this.hubConnection.onClosed(exception -> {
            if (this.disconnecting || this.reconnecting) return;
            this.logger.error("Disconnected from socket, attempting to reconnect in a bit", exception);

            this.reconnecting = true;
            this.retryManager.retry(this.reconnectRetry);
        });

        return hubConnection.start();
    }

    private void attemptReconnect() {
        this.reconnecting = true;
        this.connect().subscribe(() -> {
            this.logger.info("Reconnected to socket");
            this.reconnecting = false;
            this.reconnectRetry.retry().reset();
        }, throwable -> {
            this.logger.error("Failed to reconnect to socket, will try again");
            this.retryManager.retry(this.reconnectRetry);
        });
    }

    public Completable disconnect() {
        if (this.hubConnection == null) return Completable.complete();

        this.disconnecting = true;
        return this.hubConnection.stop();
    }

    public boolean isConnecting() {
        return this.hubConnection != null && this.hubConnection.getConnectionState() == HubConnectionState.CONNECTING;
    }

    public boolean isConnected() {
        return this.hubConnection != null && this.hubConnection.getConnectionState() == HubConnectionState.CONNECTED;
    }

    public void send(String method, Object... args) {
        this.hubConnection.send(method, args);
    }
}
