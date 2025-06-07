package nl.devpieter.lobstar.enums;

public enum MotdGetType {
    Default(0),
    Server(1),
    Custom(2);

    private final int value;

    MotdGetType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MotdGetType fromInt(int value) {
        for (MotdGetType type : values()) if (type.getValue() == value) return type;
        return Default;
    }
}
