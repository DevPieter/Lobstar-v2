package nl.devpieter.lobstar.socket;

import com.microsoft.signalr.*;
import io.reactivex.rxjava3.core.Completable;
import nl.devpieter.lobstar.ConfigManager;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.lobstar.socket.utils.CastUtils;
import nl.devpieter.sees.Listener.Listener;
import org.slf4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class SocketManager implements Listener {

    private static SocketManager INSTANCE;

    private final ConfigManager configManager = ConfigManager.getInstance();
    private final Logger logger = Lobstar.getInstance().getLogger();

    private final List<ISocketListener<?>> socketListeners = new ArrayList<>();

    private HubConnection hubConnection;
    private boolean disconnecting;
    private boolean reconnecting;

    private SocketManager() {
    }

    public static SocketManager getInstance() {
        if (INSTANCE == null) INSTANCE = new SocketManager();
        return INSTANCE;
    }

    public void addListener(ISocketListener<?> listener) {
        if (this.isConnected() || this.isConnecting()) throw new IllegalStateException("Cannot add listener while connected or connecting");
        this.socketListeners.add(listener);
    }

    public Completable connect() {
        if (this.isConnected()) return Completable.complete();
        if (this.isConnecting()) return Completable.error(new IllegalStateException("Already connecting"));

        this.disconnecting = false;
        this.reconnecting = false;

        HttpHubConnectionBuilder builder = HubConnectionBuilder.create(this.configManager.getString("api_base_url") + "/hub/plugin");
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
            this.logger.error("Socket connection closed, reconnecting in 10 seconds");

            this.reconnecting = true;
            this.attemptReconnect();
        });

        return hubConnection.start();
    }

    private void attemptReconnect() {
        Completable.timer(10, TimeUnit.SECONDS)
                .andThen(this.connect())
                .subscribe(() -> this.reconnecting = false, throwable -> {
                    this.logger.error("Failed to reconnect to socket, retrying in 10 seconds");
                    this.attemptReconnect();
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
