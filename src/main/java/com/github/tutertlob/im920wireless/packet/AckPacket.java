package com.github.tutertlob.im920wireless.packet;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AckPacket extends Im920Packet {

	private static final Logger logger = Logger.getLogger(AckPacket.class.getName());

	private static final int COMMAND_I = PACKET_PAYLOAD_I;

	private static final int COMMAND_SIZE = 1;

	public static final int RESPONSE_MAX_LENGTH = PAYLOAD_MAX_LENGTH - COMMAND_SIZE;

	private byte cmd = 0;

	private String response;

	private byte[] payload = null;

	public AckPacket() {
		super(Type.ACK);
		response = "";
	}

	public AckPacket(ByteBuffer body) {
		super(Type.ACK, body);
		body.position(COMMAND_I);
		setCommand(body.get());
		byte[] response = new byte[body.remaining()];
		body.get(response);
		setResponse(new String(response, StandardCharsets.US_ASCII));
	}

	public AckPacket(byte cmd, String response) {
		super(Type.ACK);
		setCommand(cmd);
		setResponse(response);
	}

	public byte getCommand() {
		return cmd;
	}

	public String getResponse() {
		return response;
	}

	public void setCommand(byte cmd) {
		this.cmd = cmd;
		payload = null;
		body = null;
	}

	public void setResponse(String response) {
		if (response == null) {
			String msg = "Argument response is null";
			logger.log(Level.WARNING, msg);
			throw new NullPointerException(msg);
		}

		if (response.length() > RESPONSE_MAX_LENGTH) {
			String msg = "Argument string is too long. Upto " + RESPONSE_MAX_LENGTH + " is permitted.";
			logger.log(Level.WARNING, msg);
			throw new IllegalArgumentException(msg);
		}

		this.response = response;
		payload = null;
		body = null;
	}

	@Override
	public int getPayloadLength() {
		return COMMAND_SIZE + response.length();
	}

	@Override
	public byte[] getPayload() {
		if (payload == null) {
			ByteBuffer buf = ByteBuffer.allocate(getPayloadLength());
			buf.put(cmd);
			buf.put(response.getBytes(StandardCharsets.US_ASCII));
			payload = buf.array();

			return payload;
		} else {
			return payload;
		}
	}

	@Override
	public String toString() {
		String hdr = super.toString();
		String body = String.format("\nCommand:0x%02X, Response: %s", getCommand(), getResponse());

		return hdr + body;
	}
}
