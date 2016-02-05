package org.tjj.starsector.multifarer.messages;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.tjj.starsector.multifarer.ChannelManager;
import org.tjj.starsector.multifarer.MessageId;

public abstract class Message<DestinationType extends ChannelManager<?,?,?>> {

	@SuppressWarnings("rawtypes")
	// the field accessors used for writing to the final fields of this class' subclasses.
	private static HashMap<Class<? extends Message>, Field[]> fieldAccessors = new HashMap<>();
	
	public Message() {
	}
	
	private static Charset DEFAULT_ENCODING = Charset.forName("UTF-8");
	
	public int getSize() {
		return getId().size.primitives;
	}

	public abstract MessageId getId();

	public abstract void execute(DestinationType c) throws IOException;

	@SuppressWarnings("rawtypes")
	private static Field[] getFieldAccessors(Class<? extends Message> clazz) {
		Field[] fields = fieldAccessors.get(clazz);
		if (fields == null) {
			try {
				fields = clazz.getDeclaredFields();
//				Field modifiersField = Field.class.getDeclaredField("modifiers");
//				modifiersField.setAccessible(true);

				for (Field field : fields) {
					// make the field's modifiers non-final (apparently not necessary?!)
//					modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
					// and accessible
					field.setAccessible(true);
				}

				fieldAccessors.put(clazz, fields);
			} catch (IllegalArgumentException | SecurityException e) {

				throw new RuntimeException(e);
			}

		}
		return fields;
	}
	
	public final ByteBuffer toByteBuffer() {

		Field [] fields = getFieldAccessors(getClass());

		try {
			Size messageSize = getId().size;
			
			
			int dataSize = messageSize.primitives;
			
			EncodedString head = null;
			EncodedString tail = null;
			
			if(messageSize.isVariable) {
				for (Field field : fields) {
					if(field.getType()==String.class) {
						String s = (String)field.get(this);
						EncodedString es = new EncodedString(s);
						if(head==null) {
							head = es;
							tail = es;
						}
						else {
							tail.next = es; 
							tail = es;
						}
						// string length limited to 255 bytes.
						dataSize+=1+es.bytes.length;
					}
				}
			}
			
			final int messageIdBytes = 1;
			final int lengthBytes = messageSize.isVariable?2:0; 
			
			ByteBuffer buffer = ByteBuffer.allocate(messageIdBytes + lengthBytes + dataSize);
			
			buffer.put((byte)getId().ordinal());
			
			if(messageSize.isVariable) {
				buffer.putShort((short)dataSize);
			}
			
			for (Field field : fields) {
				Class<?> fieldType = field.getType();
				if(fieldType==byte.class) {
					buffer.put(field.getByte(this));
				}
				else if(fieldType==boolean.class) {
					buffer.put(field.getBoolean(this)?(byte)1:0);
				}
				else if(fieldType==short.class) {
					buffer.putShort(field.getShort(this));
				}
				else if(fieldType==int.class) {
					buffer.putInt(field.getInt(this));
				}
				else if(fieldType==long.class) {
					buffer.putLong(field.getLong(this));
				}
				else if(fieldType==float.class) {
					buffer.putFloat(field.getFloat(this));
				}
				else if(fieldType==double.class) {
					buffer.putDouble(field.getDouble(this));
				}
				else if(fieldType==String.class) {
					buffer.put((byte)head.bytes.length);
					buffer.put(head.bytes);
					head = head.next;
				} else if(fieldType.isEnum()) {
					final Enum<?> value = (Enum<?>)field.get(this);
					buffer.put((byte)value.ordinal());
				}
				else {
					throw new RuntimeException("Unsupported field type");
				}
			}
			
			buffer.flip();
			return buffer;
		}
		catch(IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public final void fromByteBuffer(ByteBuffer b) throws IllegalArgumentException, IllegalAccessException {
		Field [] fields = getFieldAccessors(getClass());
		for (Field field : fields) {
			Class<?> fieldType = field.getType();
			if(fieldType==boolean.class) {
				field.setBoolean(this, b.get()!=1);
			}
			else if(fieldType==byte.class) {
				field.setByte(this, b.get());
			}
			else if(fieldType==short.class) {
				field.setShort(this, b.getShort());
			}
			else if(fieldType==int.class) {
				field.setInt(this, b.getInt());
			}
			else if(fieldType==long.class) {
				field.setLong(this, b.getLong());
			}
			else if(fieldType==float.class) {
				field.setFloat(this, b.getFloat());
			}
			else if(fieldType==double.class) {
				field.setDouble(this, b.getDouble());
			}
			else if(fieldType==String.class) {
				
				int byteLength = b.get()&0xFF;
				byte [] bytes = new byte[byteLength];
				b.get(bytes);
				
				field.set(this, new String(bytes, DEFAULT_ENCODING));
			}	
			else if(fieldType.isEnum()) {
				field.set(this, fieldType.getEnumConstants()[b.get()]);
			}
			else {
				throw new RuntimeException("Unsupported field type");				
			}
		}
	}
	
	public final static Size computeSize(Class<? extends Message<?>> clazz) {
		Field [] fields = getFieldAccessors(clazz);
		int size = 0;
		boolean isVariable = false;
		for (Field field : fields) {
			Class<?> fieldType = field.getType();
			if(fieldType==boolean.class || fieldType==byte.class) {
				size+=Byte.SIZE/8;
			}
			else if(fieldType==short.class) {
				size+=Short.SIZE/8;
			}
			else if(fieldType==int.class) {
				size+=Integer.SIZE/8;
			}
			else if(fieldType==long.class) {
				size+=Long.SIZE/8;
			}
			else if(fieldType==float.class) {
				size+=Float.SIZE/8;
			}
			else if(fieldType==double.class) {
				size+=Double.SIZE/8;
			}
			else if(fieldType==String.class) {
				isVariable = true;
			}
			else if(fieldType.isEnum()) {
				if(fieldType.getEnumConstants().length>255) {
					throw new RuntimeException("Enum is too large to serialize into a byte!"); 
				}
				size+=Byte.SIZE/8;
			}
			else {
				throw new RuntimeException("Unsupported field type");
			}
		}
		
		return new Size(size, isVariable);
	}
	
	public static class Size {
		public final int primitives;
		public final boolean isVariable;
		
		public Size(int primitives, boolean isVariable) {
			this.primitives = primitives;
			this.isVariable = isVariable;
		}
	}
	
	public static class EncodedString {
		
		public final byte[] bytes;
		public EncodedString next;
		public EncodedString(String s) {
			this.bytes = s.getBytes(DEFAULT_ENCODING);
		}
	}
	
}
