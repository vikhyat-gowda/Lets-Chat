package common.packets;

public enum PacketType {
    CONNECT("/c/"), MESSAGE("/m/"), DISCONNECT("/d/"), PING("/i/"),ENDCHAR("/e/");

    private final String value;

    PacketType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
