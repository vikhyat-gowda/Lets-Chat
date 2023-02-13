package common.packets;

public class ProcessPacket {
    public static byte[] encodePacket(PacketType packetType, String message) {
        String s =  packetType.getValue() + message + PacketType.ENDCHAR.getValue();
        return s.getBytes();
    }

    public static String decodePacket(String packet, String...splitValues) {
        return packet.split(String.join("|", splitValues))[1];
    }
}
