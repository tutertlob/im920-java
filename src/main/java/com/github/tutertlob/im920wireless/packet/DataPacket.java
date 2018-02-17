package com.github.tutertlob.im920wireless.packet;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.nio.ByteBuffer;
import javax.xml.bind.DatatypeConverter;

import java.lang.IllegalArgumentException;
import java.lang.NullPointerException;

public class DataPacket extends Im920Packet {

	private static final Logger logger = Logger.getLogger(DataPacket.class.getName());

	private static final int DATA_I = PACKET_PAYLOAD_I;

	public static final int DATA_MAX_LENGTH = PAYLOAD_MAX_LENGTH;
	
	private byte[] data;

	public DataPacket() {
		super(Type.DATA);
		data = new byte[0];
	}

	public DataPacket(ByteBuffer body) {
		super(Type.DATA, body);
		body.position(DATA_I);
		data = new byte[body.remaining()];
		body.get(data);
	}

	public DataPacket(byte[] data) {
		super(Type.DATA);
		setData(data);
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		if (data == null) {
			String msg = "Data is null";
			logger.log(Level.WARNING, msg);
			throw new NullPointerException(msg);
		}
		if (data.length > DATA_MAX_LENGTH) {
			String msg = "The data is too large. Upto " + DATA_MAX_LENGTH + " is acceptable.";
			logger.log(Level.WARNING, msg);
			throw new IllegalArgumentException(msg);
		}
		
		this.data = data;
		
		body = null;
	}
	
	@Override
	public int getPayloadLength() {
		return data.length;
	}
	
	@Override
	public byte[] getPayload() {
		return getData();
	}
		
	@Override
	public String toString() {
		String hdr = super.toString();
		String body = String.format("\nData: %s", DatatypeConverter.printHexBinary(getData()));
		
		return hdr + body;
	}
}
