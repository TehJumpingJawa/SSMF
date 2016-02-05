package org.tjj.starsector.multifarer.messages.server.udp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.tjj.starsector.multifarer.MessageId;
import org.tjj.starsector.multifarer.UdpChannelToPeer;
import org.tjj.starsector.multifarer.messages.server.UdpMessageToClient;

/**
 * Server command sent to the client, testing whether the port is accessible.
 * The contained uniqueId was provided to the server by the client when it
 * issued the initial RequestUdpPortTest command
 * 
 * @author TehJumpingJawa
 *
 */
public class UdpPortTest extends UdpMessageToClient {

	private long uniqueIdentifier;
	
	public UdpPortTest(DataInputStream dis) throws IOException {
		uniqueIdentifier = dis.readLong();
	}
	
	public UdpPortTest(long uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}
	
	@Override
	public MessageId getId() {
		return MessageId.UDP_PORT_TEST;
	}

	@Override
	public void execute(UdpChannelToPeer c) throws IOException {
//		c.

	}
}
