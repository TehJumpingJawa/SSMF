package org.tjj.starsector.multifarer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.util.LinkedList;
import java.util.Queue;

import org.tjj.starsector.multifarer.messages.Message;

import sun.reflect.ReflectionFactory;

public abstract class ChannelManager<MessageType extends Message<?>, OwnerType extends SelectorManager, ChannelType extends ByteChannel> {

	// used for deserializing class data.
	static ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();
	
	protected final OwnerType manager;
	protected final ChannelType channel;

	protected final SelectionKey key;

	private final Queue<ByteBuffer> sendQueue = new LinkedList<>();
	private final ByteBuffer receiveBuffer = ByteBuffer.allocateDirect(1024);

	public ChannelManager(OwnerType manager, SelectionKey key, ChannelType channel) {
		this.channel = channel;
		this.manager = manager;
		this.key = key;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final void read() throws IOException {
		final int bytesRead = channel.read(receiveBuffer);

		if (bytesRead == -1) {
			// do something special
			System.out.println("End of Stream");
		} else {

			receiveBuffer.flip();
			// mark the start of the message we're going to read.
			// in case we need to reset due to insufficient bytes.
			receiveBuffer.mark();

			while (receiveBuffer.remaining() > 0) {
				
				final MessageId messageId;
				try {
					messageId = MessageId.getByOrdinal(receiveBuffer.get());
				} catch (InvalidEnumOrdinalException e) {
					throw new IOException(e);
				} 
				
				final Message.Size messageSize = messageId.size;

				final int size;
				
				if(messageSize.isVariable) {
					if(receiveBuffer.remaining()> Short.SIZE/8) {
						size = receiveBuffer.getShort();
					}
					else {
						System.out.println("Not enough data to read message length for " + messageId);
						receiveBuffer.reset();
						break;
					}
				}
				else {
					size = messageSize.primitives;
				}
				
				if (size <= receiveBuffer.remaining()) {

					Message i;
					try {
						final Constructor<Message> superConstructor = Message.class.getConstructor();
						final Constructor<?> c = reflectionFactory.newConstructorForSerialization(messageId.message, superConstructor);
						
						i = (Message)c.newInstance();
						// then populate it using reflection.
						i.fromByteBuffer(receiveBuffer);

						
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException | NoSuchMethodException e) {
						throw new IOException(e);
					}
					
					i.execute(this);

					receiveBuffer.mark();
				} else {

					System.out.println("Not enough data to read message body for " + messageId + "; size=" + size + ", remaining=" + receiveBuffer.remaining());
					// not enough data to read a complete message.
					receiveBuffer.reset();

					break;
				}
			}
			receiveBuffer.compact();
		}
	}

	public final void write() throws IOException {
		ByteBuffer data;
		while ((data = sendQueue.peek()) != null) {

			final int bytesWritten = channel.write(data);
			if (data.remaining() > 0) {
				// unwritten bytes, so the channel must be clogged.
				// end writing for now.
				// but keep the interestOp set, as we still want to write at the 1st opportunity.
				return;
			}
			sendQueue.poll();
		}
		// all data has been flushed to the channel
		// so we're no-longer interested in write ops.
		clearInterestOps(key, SelectionKey.OP_WRITE);

	}

	public abstract void connect() throws IOException;

	public synchronized final void queueMessage(MessageType message) {

		sendQueue.add(message.toByteBuffer());
		setInterestOps(key, SelectionKey.OP_WRITE);
		manager.wakeup();
	}
	
	public static void clearInterestOps(SelectionKey key, int interestOps) {
		key.interestOps(key.interestOps() & ~interestOps);
	}

	public static void setInterestOps(SelectionKey key, int interestOps) {
		key.interestOps(key.interestOps() | interestOps);
	}
	
}
