package nl.devpieter.lobstar.socket;

import com.microsoft.signalr.*;
import io.reactivex.rxjava3.core.Completable;
import nl.devpieter.lobstar.Lobstar;
import nl.devpieter.lobstar.managers.ConfigManager;
import nl.devpieter.lobstar.socket.listeners.ISocketListener;
import nl.devpieter.lobstar.socket.utils.CastUtils;
import nl.devpieter.sees.Listener.Listener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class SocketManager implements Listener {

    private final ConfigManager configManager = ConfigManager.getInstance();
    private final List<ISocketListener<?>> socketListeners = new ArrayList<>();

    private HubConnection hubConnection;
    private boolean reconnecting;

    private final Logger logger;

    public SocketManager(@NotNull Lobstar lobstar) {
        this.logger = lobstar.getLogger();
    }

    public void addListener(ISocketListener<?> listener) {
        if (isConnected() || isConnecting()) throw new IllegalStateException("Cannot add listener while connected or connecting");
        socketListeners.add(listener);
    }

    public Completable connect() {
        if (isConnected()) return Completable.complete();
        if (isConnecting()) return Completable.error(new IllegalStateException("Already connecting"));

        HttpHubConnectionBuilder builder = HubConnectionBuilder.create(configManager.getString("socket_url"));
        builder.withHeader("X-API-KEY", configManager.getString("api_key"));

        hubConnection = builder.build();

        Map<Class<?>, BiConsumer<String, ISocketListener<?>>> actionMap = new HashMap<>();
        actionMap.put(Action1.class, (target, listener) -> {
            Type[] types = listener.getTypes().toArray(new Type[0]);
            hubConnection.on(target, CastUtils.ca1(listener.getAction()), CastUtils.c2c(types[0]));
        });
        actionMap.put(Action2.class, (target, listener) -> {
            Type[] types = listener.getTypes().toArray(new Type[0]);
            hubConnection.on(target, CastUtils.ca2(listener.getAction()), CastUtils.c2c(types[0]), CastUtils.c2c(types[1]));
        });
        actionMap.put(Action3.class, (target, listener) -> {
            Type[] types = listener.getTypes().toArray(new Type[0]);
            hubConnection.on(target, CastUtils.ca3(listener.getAction()), CastUtils.c2c(types[0]), CastUtils.c2c(types[1]), CastUtils.c2c(types[2]));
        });
        actionMap.put(Action4.class, (target, listener) -> {
            Type[] types = listener.getTypes().toArray(new Type[0]);
            hubConnection.on(target, CastUtils.ca4(listener.getAction()), CastUtils.c2c(types[0]), CastUtils.c2c(types[1]), CastUtils.c2c(types[2]), CastUtils.c2c(types[3]));
        });

        for (ISocketListener<?> listener : socketListeners) {
            BiConsumer<String, ISocketListener<?>> consumer = actionMap.get(listener.getActionType());
            if (consumer == null) continue;

            consumer.accept(listener.getTarget(), listener);
        }

        hubConnection.onClosed(exception -> {
            logger.error("Socket connection closed, reconnecting in 10 seconds");
            if (reconnecting) return;

            reconnecting = true;
            attemptReconnect();
        });

        return hubConnection.start();
    }

    private void attemptReconnect() {
        Completable.timer(10, TimeUnit.SECONDS)
                .andThen(connect())
                .subscribe(() -> reconnecting = false, throwable -> {
                    logger.error("Failed to reconnect to socket, retrying in 10 seconds");
                    attemptReconnect();
                });
    }

    public Completable disconnect() {
        if (hubConnection == null) return Completable.complete();
        return hubConnection.stop();
    }

    public boolean isConnecting() {
        return hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.CONNECTING;
    }

    public boolean isConnected() {
        return hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.CONNECTED;
    }

    public void send(String method, Object... args) {
        this.hubConnection.send(method, args);
    }
}
