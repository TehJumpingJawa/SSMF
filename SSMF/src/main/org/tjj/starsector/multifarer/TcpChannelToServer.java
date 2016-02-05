package org.tjj.starsector.multifarer;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.tjj.starsector.multifarer.messages.client.TcpMessageToServer;
import org.tjj.starsector.multifarer.messages.client.tcp.ClientConnected;
import org.tjj.starsector.multifarer.messages.server.tcp.PeerStateChange;

public class TcpChannelToServer extends ChannelManager<TcpMessageToServer, MultifarerClient, SocketChannel> {

	public TcpChannelToServer(MultifarerClient client, SelectionKey key, SocketChannel channel) {
		super(client,key, channel);
	}

	@Override
	public void connect() throws IOException {
		if(channel.finishConnect()) {
			clearInterestOps(key, SelectionKey.OP_CONNECT);
			setInterestOps(key, SelectionKey.OP_READ);
			
			queueMessage(new ClientConnected(channel.socket().getLocalAddress().getHostAddress()));
		}
		else {
			throw new IOException("Connect failed");
		}
	}
	
	public void updatePeer(PeerStateChange state) {
		
		manager.updatePeer(state);
	}
}
