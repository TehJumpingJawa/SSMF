package org.tjj.starsector.multifarer.messages.client.tcp;

import java.io.IOException;

import org.tjj.starsector.multifarer.MessageId;
import org.tjj.starsector.multifarer.UdpPortStatus;
import org.tjj.starsector.multifarer.TcpChannelToClient;
import org.tjj.starsector.multifarer.messages.client.TcpMessageToServer;
/**
 * Client command, instructing the server to begin a udp port test on the specified port.
 * 
 * @author TehJumpingJawa
 *
 */
public final class RequestUdpPortTest extends TcpMessageToServer {

	public final int port;
	public final long uniqueId;

	public RequestUdpPortTest(int port, long uniqueId) {
		this.port = port;
		this.uniqueId = uniqueId;
	}

	
	@Override
	public MessageId getId() {
		return MessageId.REQUEST_UDP_PORT_TEST;
	}



	@Override
	public void execute(TcpChannelToClient c) throws IOException {
		
		c.updateUdpPortStatus(port, UdpPortStatus.UNKNOWN);
		c.connectUdp(port, uniqueId);
	}
}
