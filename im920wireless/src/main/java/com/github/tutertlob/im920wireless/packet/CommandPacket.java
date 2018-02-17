package com.github.tutertlob.im920wireless.packet;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.lang.String;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import java.lang.IllegalArgumentException;
import java.lang.NullPointerException;

public class CommandPacket extends Im920Packet {

	private static final Logger logger = Logger.getLogger(CommandPacket.class.getName());

	private static final int COMMAND_I = PACKET_PAYLOAD_I;
	
	private static final int COMMAND_SIZE = 1;

	public static final int PARAM_MAX_LENGTH = PAYLOAD_MAX_LENGTH - COMMAND_SIZE;
	
	private byte cmd = 0;
	
	private String param;
	
	private byte[] payload = null;

	public CommandPacket() {
		super(Type.COMMAND);
		param = "";
	}

	public CommandPacket(ByteBuffer body) {		
		super(Type.COMMAND, body);
		body.position(COMMAND_I);
		setCommand(body.get());
		byte[] param = new byte[body.remaining()];
		body.get(param);
		setCommandParam(new String(param, StandardCharsets.US_ASCII));
	}

	public CommandPacket(byte cmd, String param) {
		super(Type.COMMAND);
		setCommand(cmd);
		setCommandParam(param);
	}

	public byte getCommand() {
		return cmd;
	}

	public String getCommandParam() {
		return param;
	}

	public void setCommand(byte cmd) {
		this.cmd = cmd;
		payload = null;
		body = null;
	}

	public void setCommandParam(String param) {
		if (param == null) {
			String msg = "Argument param is null.";
			logger.log(Level.WARNING, msg);
			throw new NullPointerException(msg);
		}
		if (param.length() > PARAM_MAX_LENGTH) {
			String msg = "Argument string is too long. Upto " + PARAM_MAX_LENGTH + " is permitted.";
			logger.log(Level.WARNING, msg);
			throw new IllegalArgumentException(msg);
		}
		
		this.param = param;
		
		payload = null;
		body = null;
	}
	
	@Override
	public int getPayloadLength() {
		return COMMAND_SIZE + param.length();
	}
	
	@Override
	public byte[] getPayload() {
		if (payload == null) {
			ByteBuffer buf = ByteBuffer.allocate(getPayloadLength());
			buf.put(cmd);
			buf.put(param.getBytes(StandardCharsets.US_ASCII));
			payload = buf.array();
			
			return payload;
		} else {
			return payload;
		}
	}
	
	@Override
	public String toString() {
		String hdr = super.toString();
		String body = String.format("\nCommand:0x%02X, Param: %s", getCommand(), getCommandParam());
		
		return hdr + body;
	}
}
