package nl.devpieter.lobstar.enums;

public enum HostnameCheckType {
    Exact(0),
    Contains(1),
    StartsWith(2),
    EndsWith(3),
    Regex(4);

    private final int value;

    HostnameCheckType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static HostnameCheckType fromInt(int value) {
        for (HostnameCheckType type : values()) if (type.getValue() == value) return type;
        return Exact;
    }
}
