package org.tjj.starsector.multifarer.messages.client.tcp;

import java.io.IOException;

import org.tjj.starsector.multifarer.ClientState;
import org.tjj.starsector.multifarer.MessageId;
import org.tjj.starsector.multifarer.TcpChannelToClient;
import org.tjj.starsector.multifarer.messages.client.TcpMessageToServer;
/**
 * 
 * This message is sent immediately after connection.
 *  
 * @author TehJumpingJawa
 *
 */
public class ClientConnected extends TcpMessageToServer {

	/**
	 * The client's local address.
	 * This address is used if two clients try to play each other on the same local network.
	 */
	public final String localAddress;

	public ClientConnected(String localAddress) {
		this.localAddress = localAddress;
	}
	
	@Override
	public MessageId getId() {
		return MessageId.CLIENT_CONNECTED;
	}

	@Override
	public void execute(TcpChannelToClient c) throws IOException {
		c.clientConnected(this);
	}
}
