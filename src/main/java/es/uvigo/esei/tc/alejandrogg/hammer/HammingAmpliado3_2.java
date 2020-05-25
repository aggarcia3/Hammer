// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.tc.alejandrogg.hammer;

import es.uvigo.esei.tc.alejandrogg.hammer.lib.EmpaquetamientoBits;
import es.uvigo.esei.tc.alejandrogg.hammer.lib.MatrizZ2;

/**
 * Representa un código Hamming ampliado (3, 2), un código lineal sistemático
 * (4, 8, 4) capaz de corregir errores de un bit, y detectar errores de dos
 * bits.
 *
 * @author Alejandro González García
 */
final class HammingAmpliado3_2 {
	/**
	 * La matriz generadora del código.
	 */
	private static final MatrizZ2 G = new MatrizZ2(new byte[][] {
		{ 1, 0, 0, 0, 0, 1, 1, 1 },
		{ 0, 1, 0, 0, 1, 0, 1, 1 },
		{ 0, 0, 1, 0, 1, 1, 0, 1 },
		{ 0, 0, 0, 1, 1, 1, 1, 0 }
	});

	/**
	 * La matriz control de paridad del código, traspuesta.
	 */
	private static final MatrizZ2 H_TR = new MatrizZ2(new byte[][] {
		{ 0, 1, 1, 1 },
		{ 1, 0, 1, 1 },
		{ 1, 1, 0, 1 },
		{ 1, 1, 1, 0 },
		{ 1, 0, 0, 0 },
		{ 0, 1, 0, 0 },
		{ 0, 0, 1, 0 },
		{ 0, 0, 0, 1 }
	});

	/**
	 * La tabla de síndromes de los representantes de los vectores código. Cada
	 * posición del array contiene el representante del correspondiente síndrome.
	 * Por ejemplo, para el síndrome (0, 0, 1, 0), que se interpreta como un 2, el
	 * representante sería SINDROMES[2], (0, 0, 0, 0, 0, 0, 1, 0).
	 * <p>
	 * Esta tabla de síndromes ha sido calculada con la función "syndtable" del
	 * paquete "communications" de GNU Octave, a partir de la matriz control de
	 * paridad.
	 * </p>
	 */
	private static final MatrizZ2[] TABLA_SINDROMES = new MatrizZ2[] {
		new MatrizZ2(new byte[][] { { 0, 0, 0, 0, 0, 0, 0, 0 } }),
		new MatrizZ2(new byte[][] { { 0, 0, 0, 0, 0, 0, 0, 1 } }),
		new MatrizZ2(new byte[][] { { 0, 0, 0, 0, 0, 0, 1, 0 } }),
		new MatrizZ2(new byte[][] { { 1, 0, 0, 0, 0, 1, 0, 0 } }),
		new MatrizZ2(new byte[][] { { 0, 0, 0, 0, 0, 1, 0, 0 } }),
		new MatrizZ2(new byte[][] { { 1, 0, 0, 0, 0, 0, 1, 0 } }),
		new MatrizZ2(new byte[][] { { 1, 0, 0, 0, 0, 0, 0, 1 } }),
		new MatrizZ2(new byte[][] { { 1, 0, 0, 0, 0, 0, 0, 0 } }),
		new MatrizZ2(new byte[][] { { 0, 0, 0, 0, 1, 0, 0, 0 } }),
		new MatrizZ2(new byte[][] { { 1, 0, 0, 1, 0, 0, 0, 0 } }),
		new MatrizZ2(new byte[][] { { 1, 0, 1, 0, 0, 0, 0, 0 } }),
		new MatrizZ2(new byte[][] { { 0, 1, 0, 0, 0, 0, 0, 0 } }),
		new MatrizZ2(new byte[][] { { 1, 1, 0, 0, 0, 0, 0, 0 } }),
		new MatrizZ2(new byte[][] { { 0, 0, 1, 0, 0, 0, 0, 0 } }),
		new MatrizZ2(new byte[][] { { 0, 0, 0, 1, 0, 0, 0, 0 } }),
		new MatrizZ2(new byte[][] { { 1, 0, 0, 0, 1, 0, 0, 0 } })
	};

	/**
	 * Codifica un nibble (palabra de 4 bits), devolviendo una palabra código de 8
	 * bits (un byte).
	 *
	 * @param nibble El nibble a codificar, correspondiente a los 4 bits de menor peso del byte.
	 * @return La palabra código correspondiente al nibble.
	 */
	public byte codificarNibble(final byte nibble) {
		return EmpaquetamientoBits.bytesABits(
			new MatrizZ2(
				new byte[][] { EmpaquetamientoBits.bitsABytes(nibble, 4) }
			).multiplicarPor(G).getFila(0)
		);
	}

	/**
	 * Decodifica una palabra código de 8 bits (un byte) al nibble original,
	 * corrigiendo errores de un bit.
	 * <p>
	 * Si se detectan errores de uno o dos bits, se mostrará un mensaje por el flujo
	 * de salida estándar.
	 * </p>
	 *
	 * @param palabra La palabra código a decodificar.
	 * @return El nibble decodificado, donde los 4 bits de menor peso contienen el
	 *         nibble.
	 * @throws DecodificacionImposibleException Si no se ha podido encontrar el
	 *                                          síndrome correspondiente a la
	 *                                          palabra código recibida,
	 *                                          posiblemente porque han ocurrido
	 *                                          demasiados errores en la
	 *                                          transmisión.
	 */
	public byte decodificarPalabraCodigo(final byte palabra) throws DecodificacionImposibleException {
		final MatrizZ2 r = new MatrizZ2(
			new byte[][] { EmpaquetamientoBits.bitsABytes(palabra, 8) }
		);

		// Al interpretar el síndrome como un número binario, tenemos el índice del representante
		final int indiceRepresentante = Byte.toUnsignedInt(EmpaquetamientoBits.bytesABits(
			r.multiplicarPor(H_TR).getFila(0)
		));

		// Considerar representante = error. Entonces, las posiciones a 1 del vector representan
		// errores detectados
		final MatrizZ2 error = TABLA_SINDROMES[indiceRepresentante];
		final byte[] vectorError = error.getFila(0);

		// Obtener el número de errores detectados contando los bits a 1
		int erroresDetectados = 0;
		for (int i = 0; i < vectorError.length && erroresDetectados < 2; ++i) {
			erroresDetectados += vectorError[i];
		}

		if (erroresDetectados > 0) {
			System.err.printf(
				"! Se ha detectado un error de transmisión de %d bit(s)" + System.lineSeparator(),
				erroresDetectados
			);

			// Código 2-detector, 1-corrector
			if (erroresDetectados > 1) {
				throw new DecodificacionImposibleException();
			}
		}

		// c = r - error
		final byte c = EmpaquetamientoBits.bytesABits(
			r.sumar(error).getFila(0)
		);

		// Se trata de un código sistemático, donde los primeros 4 bits son la
		// palabra fuente. Por tanto, obtener la palabra fuente a partir de
		// la palabra código se reduce a quedarse con los 4 bits más significativos
		return (byte) (((int) c & 0xFF) >>> 4);
	}
}
