package com.github.tutertlob.im920wireless.util;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.tutertlob.im920wireless.packet.AckPacket;
import com.github.tutertlob.im920wireless.packet.CommandPacket;
import com.github.tutertlob.im920wireless.packet.DataPacket;
import com.github.tutertlob.im920wireless.packet.Im920Packet;
import com.github.tutertlob.im920wireless.packet.NoticePacket;

public final class Im920 {

	private static final Logger logger = Logger.getLogger(Im920.class.getName());

	private Im920Interface im920Interface;

	public static final byte IM920_MODULE_COMMAND = 1;

	private byte sequence = 0;

	public Im920(Im920Interface im920interface) {
		this.im920Interface = im920interface;
	}

	public Im920Packet readPacket() throws InterruptedException {
		ByteBuffer[] frame = this.im920Interface.takeReceivedFrame();
		Im920Packet packet = Im920Packet.frameOf(frame);
		return packet;
	}

	public void send(Im920Packet packet) {
		packet.setSeqNum(getNextFrameID());

		im920Interface.sendDataAsync(packet.getPacketBytes());
	}

	public void sendData(byte[] data, boolean fragment) {
		if (data == null) {
			String msg = "Argument data is null.";
			logger.log(Level.WARNING, msg);
			throw new NullPointerException(msg);
		}

		ByteBuffer buf;
		for (buf = ByteBuffer.wrap(data); buf.remaining() > DataPacket.DATA_MAX_LENGTH;) {
			byte[] chopped = new byte[DataPacket.DATA_MAX_LENGTH];
			buf.get(chopped, 0, chopped.length);
			DataPacket packet = new DataPacket(chopped);
			packet.setFragment(true);

			send(packet);
		}
		byte[] chopped = new byte[buf.remaining()];
		buf.get(chopped, 0, chopped.length);
		DataPacket packet = new DataPacket(chopped);
		packet.setFragment(fragment);
		send(packet);
	}

	public void sendCommand(byte cmd, String param) {
		CommandPacket packet = new CommandPacket(cmd, param);
		send(packet);
	}

	public void sendCommandWithAck(byte cmd, String param) {
		CommandPacket packet = new CommandPacket(cmd, param);
		packet.setAckRequest(true);
		send(packet);
	}

	public void sendAck(byte cmd, String response) {
		AckPacket packet = new AckPacket(cmd, response);
		send(packet);
	}

	public void sendNotice(String notice) {
		NoticePacket packet = new NoticePacket(notice);
		send(packet);
	}

	private synchronized byte getNextFrameID() {
		return (byte) sequence++;
	}

}
