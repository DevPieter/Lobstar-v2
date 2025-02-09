package nl.devpieter.lobstar.enums;

public enum ServerType {
    Unknown(-1),
    Generic(0),
    Lobby(1);

    private final int value;

    ServerType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ServerType fromInt(int value) {
        for (ServerType type : values()) if (type.getValue() == value) return type;
        return Unknown;
    }
}