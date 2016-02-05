package org.tjj.starsector.multifarer.messages.server.tcp;

import java.io.IOException;

import org.tjj.starsector.multifarer.ClientState;
import org.tjj.starsector.multifarer.MessageId;
import org.tjj.starsector.multifarer.UdpPortStatus;
import org.tjj.starsector.multifarer.TcpChannelToServer;
import org.tjj.starsector.multifarer.Utils;
import org.tjj.starsector.multifarer.messages.server.TcpMessageToClient;

/**
 *  Server command, notifying a client of another client's (peer) state.
 *  
 * @author TehJumpingJawa
 *
 */
public class PeerStateChange extends TcpMessageToClient {

	public final String publicAddress;
	public final int publicUdpPort;
	public final UdpPortStatus udpPortStatus;
	public final String localAddress;
	public final ClientState clientState;  	

	public PeerStateChange(String publicAddress, int publicUdpPort, UdpPortStatus udpPortStatus, String localAddress, ClientState clientState) {
		this.publicAddress = publicAddress;
		this.publicUdpPort = publicUdpPort;
		this.udpPortStatus = udpPortStatus;
		this.localAddress = localAddress;
		this.clientState = clientState;
	}
	
	@Override
	public MessageId getId() {
		return MessageId.CLIENT_STATE_CHANGED;
	}

	@Override
	public void execute(TcpChannelToServer c) throws IOException {
		
		c.updatePeer(this);
	}
}
