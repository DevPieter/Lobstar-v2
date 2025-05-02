package nl.devpieter.lobstar.socket.listeners;

import java.lang.reflect.Type;
import java.util.List;

public interface ISocketListener<T> {

    String getTarget();

    List<Type> getTypes();

    T getAction();

    Class<?> getActionType();
}
