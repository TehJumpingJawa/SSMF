package org.tjj.starsector.multifarer;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

import org.tjj.starsector.multifarer.messages.client.tcp.ClientConnected;
import org.tjj.starsector.multifarer.messages.server.TcpMessageToClient;
import org.tjj.starsector.multifarer.messages.server.tcp.PeerStateChange;

public class TcpChannelToClient extends ChannelManager<TcpMessageToClient, MultifarerMaster, SocketChannel> {

	/**
	 * This client's public IP address
	 */
	private final String publicAddress;
	/**
	 * This client's public udpPort that peers should address packets to.
	 */
	private int publicUdpPort = -1;
	/**
	 * status of the client's UDP port.
	 */
	private UdpPortStatus udpPortStatus = UdpPortStatus.UNKNOWN;
	private ClientState clientState = ClientState.CONNECTING;
	
	/**
	 * null while clientStatus==ClientStatus.CONNECTING
	 * non-null after this point.
	 * This client's local IP address. Needed for LAN play.
	 *  
	 */
	private String localAddress;
	
	public TcpChannelToClient(MultifarerMaster master, SelectionKey key, SocketChannel channel) {
		super(master,key, channel);
		publicAddress = channel.socket().getInetAddress().getHostAddress();
	}
	
	private UdpChannelToClient test;
	
	public void setTestChannel(UdpChannelToClient test) {
		this.test = test;
	}

	public void updateUdpPortStatus(int udpPort, UdpPortStatus portState) throws IOException {
		if(clientState==ClientState.CONNECTING) {
			throw new InvalidClientStateException();
		}
		this.publicUdpPort = udpPort;
		this.udpPortStatus = portState;
		
		//broadcast the status change to all clients (except this one)
		manager.broadcastMessage(this, new PeerStateChange(publicAddress, publicUdpPort, udpPortStatus, localAddress, clientState));
	}
	
	@Override
	public void connect() throws IOException {
		throw new UnsupportedOperationException();
	}

	public void connectUdp(int port, long uniqueId) throws IOException {
		manager.connectUdp(this, port, uniqueId);
		
	}

	public final String getLocalAddress() {
		return localAddress;
	}

	public final ClientState getClientStatus() {
		return clientState;
	}
	
	public void clientConnected(ClientConnected message) {
		if(clientState!=ClientState.CONNECTING) {
			throw new InvalidClientStateException();
		}
		
		this.localAddress = message.localAddress;
		clientState = ClientState.CONNECTED;
		
		
		
		manager.broadcastMessage(this, new PeerStateChange(publicAddress, publicUdpPort, udpPortStatus, localAddress, clientState));

		//inform this peer of the status of all other peers.
		// this should probably be encapsulated somewhere.... probably in the manager.
		List<TcpChannelToClient> peers = manager.tcpChannelsToClients;
		for (TcpChannelToClient peer : peers) {
			if(peer!=this) {
				this.queueMessage(new PeerStateChange(peer.publicAddress, peer.publicUdpPort, peer.udpPortStatus, peer.localAddress, peer.clientState));
			}
		}

	}
}
