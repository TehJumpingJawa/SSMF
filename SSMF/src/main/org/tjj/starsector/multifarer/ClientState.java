package org.tjj.starsector.multifarer;

public enum ClientState {
	CONNECTING,
	CONNECTED,
	AWAITING_OPPONENT,
	JOINING_GAME,
	IN_GAME,
	WATCHING_GAME,
	DISCONNECTED;
	
	private static ClientState [] values = ClientState.values();
	
	public static ClientState getByOrdinal(int x) throws InvalidEnumOrdinalException {
		if(x<0 || x>=values.length) {
			throw new InvalidEnumOrdinalException(ClientState.class, x);
		}
		return values[x];
	}
}
