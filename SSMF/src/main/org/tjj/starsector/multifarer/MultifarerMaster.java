package org.tjj.starsector.multifarer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.support.igd.PortMappingListener;
import org.fourthline.cling.support.model.PortMapping;
import org.fourthline.cling.support.model.PortMapping.Protocol;
import org.tjj.starsector.multifarer.messages.server.TcpMessageToClient;

public class MultifarerMaster implements Runnable, SelectorManager {

	public static final int MASTER_PORT = 25252;
	
	private final int port;
	
	private final Thread selectorThread;
	
	private MultifarerMaster(int port) {
		this.port = port;
		(selectorThread = new Thread(this)).start();
	}
	
	public static void main(String[] args) {

		new MultifarerMaster(MASTER_PORT);
	}

	public void wakeup() {
		if(Thread.currentThread()!=selectorThread) {
			selector.wakeup();
		}
	}
	
	private boolean running = false;
	private ShutdownHook shutdown;
	
	List<TcpChannelToClient> tcpChannelsToClients = new ArrayList<>();
	
	private Selector selector;
	
	@Override
	public void run() {
		
		shutdown = openPort(MASTER_PORT, Protocol.TCP, "Multifarer Master");		
		
		try {
			
			selector = Selector.open();
			
			ServerSocketChannel ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);
			ssc.bind(new InetSocketAddress(port));
			
			ssc.register(selector, SelectionKey.OP_ACCEPT);
			running = true;
			while (running) {
				
				int keyCount = selector.select();
				
				Set<SelectionKey> keys = selector.selectedKeys();
				
				for (Iterator<SelectionKey> iterator = keys.iterator(); iterator.hasNext();) {
					SelectionKey key = iterator.next();
					
					if(key.isAcceptable()) {
						SocketChannel sc = ssc.accept();
						
						sc.configureBlocking(false);
						SelectionKey clientKey = sc.register(selector, SelectionKey.OP_READ, null);
						TcpChannelToClient newClient = new TcpChannelToClient(this, clientKey, sc);
						clientKey.attach(newClient);

						tcpChannelsToClients.add(newClient);
					}
					
					if(key.isReadable()) {
						ChannelManager<?,?,?> c = (ChannelManager<?,?,?>)key.attachment();
						c.read();
					}

					if(key.isWritable()) {
						ChannelManager<?,?,?> c = (ChannelManager<?,?,?>)key.attachment();
						c.write();
					}
					
					iterator.remove();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);	
		}
	}
	
	public void connectUdp(TcpChannelToClient c, int port, long uniqueId) throws IOException {
		DatagramChannel dc = DatagramChannel.open();
		dc.configureBlocking(false);
		dc.connect(new InetSocketAddress(c.channel.socket().getInetAddress(), port));
		SelectionKey key = dc.register(selector, 0, null);
		UdpChannelToClient testChannel = new UdpChannelToClient(this, c, key, dc, uniqueId);
		key.attach(testChannel);
		c.setTestChannel(testChannel);
	}

//	public void remove(Client client) {
//		if(!clients.remove(client.connection.getInetAddress().getHostAddress(), client)) {
//			System.err.println("Unexpected: " + client.connection.getInetAddress().getHostAddress() + " was not associated with " + client);
//		}
//	}
	
	/**
	 * open port on upnp internet gateway
	 */
	private ShutdownHook openPort(int port, PortMapping.Protocol protocol, String name) {
		ShutdownHook hook;
		String localAddress;
		try {
			localAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		PortMapping desiredMapping = new PortMapping(port, localAddress, protocol, name);
		
		UpnpService upnpService = new UpnpServiceImpl(new PortMappingListener(desiredMapping));

		Runtime.getRuntime().addShutdownHook(new Thread(hook = new ShutdownHook(upnpService)));
		upnpService.getControlPoint().search();
		
		return hook;
	}	
	/**
	 * broadcasts a message to every client but the originator.
	 * 
	 * @param source
	 * @param udpPortStatusUpdate
	 * @throws IOException
	 */
	public void broadcastMessage(TcpChannelToClient source, TcpMessageToClient message) {
		for (TcpChannelToClient destination : tcpChannelsToClients) {
			if(source!=destination) {
				destination.queueMessage(message);
			}
		}
		
	}

}
