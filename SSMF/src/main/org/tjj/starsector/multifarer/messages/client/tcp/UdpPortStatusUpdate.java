package org.tjj.starsector.multifarer.messages.client.tcp;

import java.io.IOException;

import org.tjj.starsector.multifarer.MessageId;
import org.tjj.starsector.multifarer.UdpPortStatus;
import org.tjj.starsector.multifarer.TcpChannelToClient;
import org.tjj.starsector.multifarer.messages.client.TcpMessageToServer;

/**
 *  Client command, notifying the server of the status of this client's udp port.
 *  
 * @author TehJumpingJawa
 *
 */
public class UdpPortStatusUpdate extends TcpMessageToServer {

	public final int port;
	public final UdpPortStatus udpPortStatus;

	public UdpPortStatusUpdate(int port, UdpPortStatus udpPortStatus) {
		this.port = port;
		this.udpPortStatus = udpPortStatus;
	}
	
	@Override
	public MessageId getId() {
		return MessageId.UDP_PORT_STATUS_UPDATE;
	}

	@Override
	public void execute(TcpChannelToClient c) throws IOException {
		c.updateUdpPortStatus(port, udpPortStatus);
		
	}
}
