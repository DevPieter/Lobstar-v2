package nl.devpieter.lobstar.socket.listeners;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public interface ISocketListener<T> {
    
    String getTarget();

    List<Type> getTypes();

    T getAction();

    default Class<?> getActionType() {
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
