package com.github.tutertlob.im920wireless.packet;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import java.lang.IllegalArgumentException;
import java.lang.NullPointerException;

public class NoticePacket extends Im920Packet {

	private static final Logger logger = Logger.getLogger(NoticePacket.class.getName());

	private static final int NOTICE_I = PACKET_PAYLOAD_I;
	
	public static final int NOTICE_MAX_LENGTH = PAYLOAD_MAX_LENGTH;
	
	private String payload; 

	public NoticePacket() {
		super(Type.NOTICE);
		payload = "";
	}

	public NoticePacket(ByteBuffer body) {
		super(Type.NOTICE, body);
		body.position(NOTICE_I);
		byte[] notice = new byte[body.remaining()];
		body.get(notice);
		setNotice(new String(notice, StandardCharsets.US_ASCII));
	}

	public NoticePacket(String notice) {
		super(Type.NOTICE);
		setNotice(notice);
	}

	public String getNotice() {
		return new String(getPayload(), StandardCharsets.US_ASCII);
	}

	public void setNotice(String notice) {
		if (notice == null) {
			String msg = "Argument notice is null.";
			logger.log(Level.WARNING, msg);
			throw new NullPointerException(msg);
		}
		if (notice.length() > NOTICE_MAX_LENGTH) {
			String msg = "Argument string is too long. " + NOTICE_MAX_LENGTH + " of characters were sent.";
			logger.log(Level.WARNING, msg);
			throw new IllegalArgumentException(msg);
		}
		
		payload = notice;
		
		body = null;
	}
	
	@Override
	public int getPayloadLength() {
		return payload.length();
	}
	
	@Override
	public byte[] getPayload() {
		return payload.getBytes(StandardCharsets.US_ASCII);
	}
	
	public String toString() {
		String hdr = super.toString();
		String body = String.format("\nNotice: %s", getNotice());
		
		return hdr + body;
	}
}
