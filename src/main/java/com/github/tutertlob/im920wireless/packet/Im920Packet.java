package com.github.tutertlob.im920wireless.packet;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

public abstract class Im920Packet {

	private static final Logger logger = Logger.getLogger(Im920Packet.class.getName());

	private int nodeId = 0;

	private int moduleId = 0;

	private int rssi = 0;

	private boolean fragmented = false;

	private boolean acknoledgement = false;

	protected Type type;

	private byte seq = 0;

	protected ByteBuffer body = null;

	public static final int PACKET_MAX_LENGTH = 64;

	public static final int PACKET_HEADER_SIZE = 3;

	public static final int PAYLOAD_MAX_LENGTH = PACKET_MAX_LENGTH - PACKET_HEADER_SIZE;

	protected static final int PACKET_LENGTH_I = 0;

	protected static final int PACKET_FLAG_I = 1;

	protected static final int PACKET_TYPE_I = 1;

	protected static final int PACKET_SEQ_NUM_I = 2;

	protected static final int PACKET_PAYLOAD_I = PACKET_HEADER_SIZE;

	protected static final byte PACKET_LENGTH_MASK = 0x3F;

	protected static final byte PACKET_FLAG_MASK = 0x18;

	protected static final byte PACKET_FLAG_MASK_FRAG = 0x10;

	protected static final byte PACKET_FLAG_MASK_ACK = 0x08;

	protected static final byte PACKET_TYPE_MASK = 0x07;

	public enum Type {

		DATA((byte) 0), COMMAND((byte) 1), ACK((byte) 2), NOTICE((byte) 3);

		private final byte id;

		Type(byte id) {
			this.id = id;
		}

		byte id() {
			return this.id;
		}

		private static final Map<Byte, Type> m = new HashMap<Byte, Type>();

		static {
			for (Type t : Type.values())
				m.put(t.id, t);
		}

		public static boolean isValid(Byte i) {
			return m.get(Byte.valueOf(i)) != null;
		}

		public static Type valueOf(Byte i) {
			if (!isValid(i)) {
				String msg = String.format("Invalid packet type id %d", i);
				logger.log(Level.WARNING, msg);
				throw new IllegalArgumentException(msg);
			}
			return m.get(Byte.valueOf(i));
		}
	}

	protected Im920Packet(Type type) {
		this.type = type;
	}

	protected Im920Packet(Type type, ByteBuffer body) {
		assert type == getPacketType(body);

		this.type = type;
		this.setFragment(isFragmented(body));
		this.setAckRequest(isAckRequested(body));
		this.setSeqNum((byte) getSeqNum(body));
	}

	public static final Im920Packet frameOf(ByteBuffer body) {
		body.rewind();
		Type type = getPacketType(body);

		Im920Packet packet;

		switch (type) {
		case DATA:
			packet = new DataPacket(body);
			break;
		case COMMAND:
			packet = new CommandPacket(body);
			break;
		case ACK:
			packet = new AckPacket(body);
			break;
		case NOTICE:
			packet = new NoticePacket(body);
			break;
		default:
			String msg = String.format("Undefined packet type %s", type);
			logger.log(Level.WARNING, msg);
			throw new IllegalArgumentException(msg);
		}

		return packet;
	}

	public static final Im920Packet frameOf(ByteBuffer[] frame) {
		if (frame.length != 2) {
			String msg = "The argument is an invalid IM920 frame.";
			logger.log(Level.WARNING, msg);
			throw new IllegalArgumentException(msg);
		}

		ByteBuffer header = frame[0];
		ByteBuffer body = frame[1];

		header.rewind();
		body.rewind();

		Im920Packet packet = frameOf(body);

		packet.setNodeId(header.get());
		packet.setModuleId(header.getShort());
		packet.setRssi(header.get());

		return packet;
	}

	public int getPacketLength() {
		int length = PACKET_HEADER_SIZE + getPayloadLength();

		assert length <= PACKET_MAX_LENGTH;

		return length;
	}

	public static final int getPayloadLength(ByteBuffer body) {
		return body.get(PACKET_LENGTH_I) & PACKET_LENGTH_MASK;
	}

	public abstract int getPayloadLength();

	public static final Type getPacketType(ByteBuffer body) {
		byte id = (byte) (body.get(PACKET_TYPE_I) & PACKET_TYPE_MASK);
		return Type.valueOf(id);
	}

	public final Type getPacketType() {
		return type;
	}

	public final byte[] getPacketBytes() {
		if (isPacketUpdated()) {
			body = ByteBuffer.allocate(getPacketLength());

			byte payloadLength = (byte) getPayloadLength();
			body.put(payloadLength);

			byte flag = 0;
			if (this.acknoledgement)
				flag |= (byte) PACKET_FLAG_MASK_ACK;
			if (this.fragmented)
				flag |= (byte) PACKET_FLAG_MASK_FRAG;
			flag |= type.id();
			body.put(flag);

			body.put(seq);

			body.put(getPayload());
		}

		return body.array();
	}

	public abstract byte[] getPayload();

	public static final int getSeqNum(ByteBuffer body) {
		return body.get(PACKET_SEQ_NUM_I);
	}

	public final int getSeqNum() {
		return seq;
	}

	public static final boolean isFragmented(ByteBuffer body) {
		return (body.get(PACKET_FLAG_I) & PACKET_FLAG_MASK_FRAG) != 0 ? true : false;
	}

	public final boolean isFragmented() {
		return fragmented;
	}

	public final void setFragment(boolean fragment) {
		this.fragmented = fragment;
		body = null;
	}

	public static final boolean isAckRequested(ByteBuffer body) {
		return (body.get(PACKET_FLAG_I) & PACKET_FLAG_MASK_ACK) != 0 ? true : false;
	}

	public final boolean isAckRequested() {
		return acknoledgement;
	}

	public final void setAckRequest(boolean request) {
		this.acknoledgement = request;
		body = null;
	}

	public final void setSeqNum(byte num) {
		this.seq = num;
		body = null;
	}

	public final int getNodeId() {
		return nodeId;
	}

	public final int getModuleId() {
		return moduleId;
	}

	public final int getRssi() {
		return rssi;
	}

	private final void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	private final void setModuleId(int moduleId) {
		this.moduleId = moduleId;
	}

	private final void setRssi(int rssi) {
		this.rssi = rssi;
	}

	public boolean isPacketUpdated() {
		return body == null ? true : false;
	};

	@Override
	public String toString() {
		String hdr = String.format(
				"NodeId:0x%02X, ModuleId: 0x%04X, RSSI:%d, Payload size: %d, Flag(frag/ack): %b/%b, Seq: 0x%02X, Packet type: %s\nBytes: ",
				nodeId, moduleId, rssi, getPayloadLength(), isFragmented(), isAckRequested(), getSeqNum(), type);

		String payload = DatatypeConverter.printHexBinary(getPacketBytes());
		StringBuffer str = new StringBuffer(hdr).append(payload);

		return str.toString();
	}
}
