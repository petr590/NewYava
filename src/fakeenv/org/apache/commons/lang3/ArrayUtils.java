package org.apache.commons.lang3;

import java.lang.reflect.Array;

public class ArrayUtils {

	@SafeVarargs
	public static <T> T[] addAll(T[] array1, T... array2) {
		if (array1 == null) {
			return clone(array2);
		}
		
		if (array2 == null) {
			return clone(array1);
		}

		@SuppressWarnings("unchecked")
		Class<T> type1 = (Class<T>)array1.getClass().getComponentType();
		T[] joinedArray = newInstance(type1, array1.length + array2.length);

		System.arraycopy(array1, 0, joinedArray, 0, array1.length);

		try {
			System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
			return joinedArray;
		} catch (ArrayStoreException ex) {
			Class<?> type2 = array2.getClass().getComponentType();
			
			if (!type1.isAssignableFrom(type2)) {
				throw new IllegalArgumentException("Cannot store " + type2.getName() + " in an array of " + type1.getName(), ex);
			} else {
				throw ex;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] clone(T[] array) {
        return array != null ? (T[])array.clone() : null;
    }

	@SuppressWarnings("unchecked")
    public static <T> T[] newInstance(Class<T> componentType, int length) {
        return (T[])Array.newInstance(componentType, length);
    }
}
