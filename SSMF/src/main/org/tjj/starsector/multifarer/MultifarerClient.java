package org.tjj.starsector.multifarer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.support.igd.PortMappingListener;
import org.fourthline.cling.support.model.PortMapping;
import org.fourthline.cling.support.model.PortMapping.Protocol;
import org.tjj.starsector.multifarer.messages.server.tcp.PeerStateChange;

public class MultifarerClient implements Runnable, SelectorManager {

	public static final int DEFAULT_UDP_LISTEN_PORT = 8000;
	
	private int inboundUdpPort;
	
	private final InetSocketAddress masterServer = new InetSocketAddress("127.0.0.1", MultifarerMaster.MASTER_PORT);
	
	private final Thread selectorThread;
	
	private MultifarerClient(int port) {
		
		inboundUdpPort = port;
		(selectorThread = new Thread(this)).start();
	}
	
	public static void main(String[] args) {

		final int udpListenPort;
		
		if(args.length==1) {
			udpListenPort = Integer.valueOf(args[0]);
		}
		else {
			udpListenPort = DEFAULT_UDP_LISTEN_PORT;
		}
		
		new MultifarerClient(udpListenPort);
	}

	public void wakeup() {
		if(Thread.currentThread()!=selectorThread) {
			selector.wakeup();
		}
	}
	
	private boolean running = false;
	private ShutdownHook shutdown;
	
	private Selector selector;
	
	private Map<MultiKey<String>, PeerStateChange> peers = new HashMap<>();
	
	public void updatePeer(PeerStateChange state) {
		MultiKey<String> key = new MultiKey<String>(state.publicAddress, state.localAddress);
		if(state.clientState==ClientState.DISCONNECTED) {
			PeerStateChange removedPeer = peers.remove(key);
			
			if(removedPeer!=null) {
				System.out.println("Peer disconnected: " + Utils.classToString(removedPeer));
			}
		}
		else {
			peers.put(key, state);
		}
	}
	
	
	@Override
	public void run() {
		
		shutdown = openPort(inboundUdpPort, Protocol.UDP, "Multifarer P2P");
		
		try {
			
			selector = Selector.open();
			
			DatagramChannel peerToPeer = DatagramChannel.open();
			peerToPeer.configureBlocking(false);
			peerToPeer.bind(new InetSocketAddress(inboundUdpPort));

			SelectionKey udpKey = peerToPeer.register(selector, SelectionKey.OP_READ);
			UdpChannelToPeer udp = new UdpChannelToPeer(this, udpKey, peerToPeer);
			udpKey.attach(udp);
			
			
			SocketChannel toServer = SocketChannel.open();
			toServer.configureBlocking(false);
			toServer.connect(masterServer);
			SelectionKey tcpKey = toServer.register(selector, SelectionKey.OP_CONNECT);
			TcpChannelToServer tcp = new TcpChannelToServer(this, tcpKey, toServer);
			tcpKey.attach(tcp);
			
			running = true;
			while (running) {
				
				int keyCount = selector.select();
				
				Set<SelectionKey> keys = selector.selectedKeys();
				
				for (Iterator<SelectionKey> iterator = keys.iterator(); iterator.hasNext();) {
					SelectionKey key = iterator.next();
					
					if(key.isReadable()) {
						ChannelManager<?,?,?> c = (ChannelManager<?,?,?>)key.attachment();
						c.read();
					}

					if(key.isWritable()) {
						ChannelManager<?,?,?> c = (ChannelManager<?,?,?>)key.attachment();
						c.write();
					}
					
					if(key.isConnectable()) {
						ChannelManager<?,?,?> c = (ChannelManager<?,?,?>)key.attachment();
						c.connect();
					}
					
					
					
					iterator.remove();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);	
		}
	}
	
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
}
