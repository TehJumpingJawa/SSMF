package org.tjj.starsector.multifarer;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import org.tjj.starsector.multifarer.messages.client.UdpMessageToPeer;

public class UdpChannelToPeer extends ChannelManager<UdpMessageToPeer, MultifarerClient, DatagramChannel> {

	public UdpChannelToPeer(MultifarerClient client, SelectionKey key, DatagramChannel channel) {
		super(client,key, channel);
	}
	
	public void connect() throws IOException {
		throw new UnsupportedOperationException();
	}
}
