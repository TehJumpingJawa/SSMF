package org.tjj.starsector.multifarer;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import org.tjj.starsector.multifarer.messages.server.UdpMessageToClient;
import org.tjj.starsector.multifarer.messages.server.tcp.CompletedUdpPortTest;
import org.tjj.starsector.multifarer.messages.server.udp.UdpPortTest;

public class UdpChannelToClient extends ChannelManager<UdpMessageToClient, MultifarerMaster, DatagramChannel> implements Runnable {

	private final TcpChannelToClient owner;

	private final long uniqueId;
	
	public UdpChannelToClient(MultifarerMaster master, TcpChannelToClient owner, SelectionKey key, DatagramChannel channel, long uniqueId) {
		super(master,key, channel);
		this.owner = owner;
		this.uniqueId = uniqueId;
		new Thread(this).start();
	}
	
	public void connect() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void run() {
		
		final int DURATION = 5000;
		final int PACKETS = 50;
		
		int loops = PACKETS;
		
		
		try {
			while(loops-->0) {

				queueMessage(new UdpPortTest(uniqueId));
				Thread.sleep(DURATION/PACKETS);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		owner.queueMessage(new CompletedUdpPortTest(channel.socket().getPort()));
	}

}
