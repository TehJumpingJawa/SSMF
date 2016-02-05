package org.tjj.starsector.multifarer;

public class InvalidEnumOrdinalException extends Exception {

	public InvalidEnumOrdinalException(Class<? extends Enum<?>> clazz, int ordinal) {
		super("Ordinal value '" + ordinal + "' not valid for Enum class '" + clazz.getName() +"'");
	}
}
