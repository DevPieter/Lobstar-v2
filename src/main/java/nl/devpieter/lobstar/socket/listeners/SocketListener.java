package nl.devpieter.lobstar.socket.listeners;

import nl.devpieter.sees.Sees;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class SocketListener<T> implements ISocketListener<T> {

    protected final Sees sees = Sees.getInstance();

    @Override
    public Class<?> getActionType() {
        Type[] genericInterfaces = getClass().getGenericInterfaces();
        if (genericInterfaces.length == 0) return null;

        Type genericInterface = genericInterfaces[0];
        if (!(genericInterface instanceof ParameterizedType)) return null;

        Type actualTypeArgument = ((ParameterizedType) genericInterface).getActualTypeArguments()[0];
        if (actualTypeArgument instanceof Class<?>) {
            return (Class<?>) actualTypeArgument;
        } else if (actualTypeArgument instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) actualTypeArgument).getRawType();
        }

        return null;
    }
}
