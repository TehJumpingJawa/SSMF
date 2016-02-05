package org.tjj.starsector.multifarer;

import java.lang.reflect.Field;

public class Utils {

	
	public static String classToString(Object obj) {
		
		StringBuilder sb = new StringBuilder();
		
		Class<?> clazz = obj.getClass();
		
		sb.append(clazz.getSimpleName()).append(" ");
		
		Field[] fields = clazz.getDeclaredFields();
		
		for (Field field : fields) {
			sb.append(field.getName());
			sb.append("=");
			try {
				sb.append(field.get(obj).toString());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			sb.append(", ");
		}
		
		sb.setLength(sb.length()-2);
		
		return sb.toString();
		
	}
}
