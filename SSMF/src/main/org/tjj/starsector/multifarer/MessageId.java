package org.tjj.starsector.multifarer;

import org.tjj.starsector.multifarer.messages.Message;
import org.tjj.starsector.multifarer.messages.client.tcp.RequestUdpPortTest;
import org.tjj.starsector.multifarer.messages.client.tcp.ClientConnected;
import org.tjj.starsector.multifarer.messages.client.tcp.UdpPortStatusUpdate;
import org.tjj.starsector.multifarer.messages.server.tcp.CompletedUdpPortTest;
import org.tjj.starsector.multifarer.messages.server.tcp.PeerStateChange;
import org.tjj.starsector.multifarer.messages.server.udp.UdpPortTest;

public enum MessageId {

	REQUEST_UDP_PORT_TEST(RequestUdpPortTest.class),
	UDP_PORT_TEST(UdpPortTest.class),
	COMPLETED_UDP_PORT_TEST(CompletedUdpPortTest.class),
	UDP_PORT_STATUS_UPDATE(UdpPortStatusUpdate.class),
	CLIENT_STATE_CHANGED(PeerStateChange.class),
	CLIENT_CONNECTED(ClientConnected.class);
	
	public final Class<? extends Message<?>> message;

	public final Message.Size size;
	
	private MessageId(Class<? extends Message<?>> message) {
		this.message = message;
		size = Message.computeSize(message);
	}
	
	public static MessageId [] values = values();
	
	public static MessageId getByOrdinal(int x) throws InvalidEnumOrdinalException {
		if(x<0 || x>=values.length) {
			throw new InvalidEnumOrdinalException(MessageId.class, x);
		}
		return values[x];
	}
}
