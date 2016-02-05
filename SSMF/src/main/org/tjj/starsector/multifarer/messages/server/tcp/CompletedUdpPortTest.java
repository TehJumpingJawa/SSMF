package org.tjj.starsector.multifarer.messages.server.tcp;

import java.io.IOException;

import org.tjj.starsector.multifarer.MessageId;
import org.tjj.starsector.multifarer.TcpChannelToServer;
import org.tjj.starsector.multifarer.messages.server.TcpMessageToClient;

/**
 * Server message, notifying a client that the udp port test has concluded.
 * 
 * @author TehJumpingJawa
 *
 */
public final class CompletedUdpPortTest extends TcpMessageToClient {

	public final int portChecked;
	
	public CompletedUdpPortTest(int portChecked) {
		this.portChecked = portChecked;
	}

	@Override
	public MessageId getId() {
		return MessageId.COMPLETED_UDP_PORT_TEST;
	}


	@Override
	public void execute(TcpChannelToServer c) throws IOException {
		//TODO client needs to ...do stuff.
		
		
		
	}
}
