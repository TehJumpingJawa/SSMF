package org.tjj.starsector.multifarer;


public enum UdpPortStatus {

	UNKNOWN,
	OPEN,
	CLOSED;
	
	private static UdpPortStatus [] values = UdpPortStatus.values();
	
	public static UdpPortStatus getByOrdinal(int x) throws InvalidEnumOrdinalException {
		if(x<0 || x>=values.length) {
			throw new InvalidEnumOrdinalException(UdpPortStatus.class, x);
		}
		return values[x];
	}
	
}
