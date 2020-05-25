// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.tc.alejandrogg.hammer.lib;

/**
 * Clase de métodos estáticos utilitarios para desempaquetar bits a bytes y
 * viceversa.
 *
 * @author Alejandro González García
 */
public final class EmpaquetamientoBits {
	// No permitir instanciar esta clase
	private EmpaquetamientoBits() {}

	/**
	 * Desempaqueta los {@code numBits} bits de menor peso del byte especificado a
	 * bytes independientes, en un nuevo array.
	 *
	 * @param bits    El byte que contiene los bits a desempaquetar.
	 * @param numBits El número de bits a desempaquetar del byte.
	 * @return El descrito array, con los bits desempaquetados a bytes.
	 * @throws IllegalArgumentException Si {@code numBits} no está en el intervalo
	 *                                  [0, 8].
	 */
	public static byte[] bitsABytes(final byte bits, final int numBits) {
		final byte[] resultado;

		if (numBits < 0 || numBits > 8) {
			throw new IllegalArgumentException("El número de bits no es válido");
		}

		resultado = new byte[numBits];
		for (int i = 0; i < numBits; ++i) {
			resultado[numBits - 1 - i] = (byte) ((bits >>> i) & 1);
		}

		return resultado;
	}

	/**
	 * Empaqueta los {@code bytes} bits de menor peso de un byte a un byte. Esta
	 * operación es la recíproca de {@link #bitsABytes(byte, int)}.
	 *
	 * @param bytes Los bits, desempaquetados a bytes, a convertir de nuevo a un
	 *              byte.
	 * @return El byte correspondiente.
	 * @throws IllegalArgumentException Si {@code bytes} es nulo o de longitud mayor
	 *                                  a 8.
	 */
	public static byte bytesABits(final byte[] bytes) {
		if (bytes == null || bytes.length > 8) {
			throw new IllegalArgumentException("El array de bytes no puede ser nulo o de longitud mayor que 8");
		}

		byte bits = 0;
		for (int i = 0; i < bytes.length; ++i) {
			bits |= bytes[i] << (bytes.length - 1 - i);
		}

		return bits;
	}
}
