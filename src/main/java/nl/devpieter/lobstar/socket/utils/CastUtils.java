package nl.devpieter.lobstar.socket.utils;

import com.microsoft.signalr.*;

import java.lang.reflect.Type;

public class CastUtils {

    public static Class<Object> c2c(Type type) {
        if (type instanceof Class<?>) {
            return (Class<Object>) type;
        } else {
            throw new IllegalArgumentException("Type is not a Class");
        }
    }

    public static <T> Action1<T> ca1(Object action) {
        return (Action1<T>) action;
    }

    public static <T1, T2> Action2<T1, T2> ca2(Object action) {
        return (Action2<T1, T2>) action;
    }

    public static <T1, T2, T3> Action3<T1, T2, T3> ca3(Object action) {
        return (Action3<T1, T2, T3>) action;
    }

    public static <T1, T2, T3, T4> Action4<T1, T2, T3, T4> ca4(Object action) {
        return (Action4<T1, T2, T3, T4>) action;
    }

    public static <T1, T2, T3, T4, T5> Action5<T1, T2, T3, T4, T5> ca5(Object action) {
        return (Action5<T1, T2, T3, T4, T5>) action;
    }

    public static <T1, T2, T3, T4, T5, T6> Action6<T1, T2, T3, T4, T5, T6> ca6(Object action) {
        return (Action6<T1, T2, T3, T4, T5, T6>) action;
    }

    public static <T1, T2, T3, T4, T5, T6, T7> Action7<T1, T2, T3, T4, T5, T6, T7> ca7(Object action) {
        return (Action7<T1, T2, T3, T4, T5, T6, T7>) action;
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8> Action8<T1, T2, T3, T4, T5, T6, T7, T8> ca8(Object action) {
        return (Action8<T1, T2, T3, T4, T5, T6, T7, T8>) action;
    }
}
