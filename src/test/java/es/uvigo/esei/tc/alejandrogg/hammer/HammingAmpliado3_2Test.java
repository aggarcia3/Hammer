// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.tc.alejandrogg.hammer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Test;

import es.uvigo.esei.tc.alejandrogg.hammer.lib.EmpaquetamientoBits;
import es.uvigo.esei.tc.alejandrogg.hammer.lib.MatrizZ2;

/**
 * Pruebas para {@link HammingAmpliado3_2}.
 *
 * @author Alejandro González García
 */
public class HammingAmpliado3_2Test {
	@Test
	public void testCodificacionNibble() {
		assertEquals(
			0b1100_1100,
			Byte.toUnsignedInt(new HammingAmpliado3_2().codificarNibble((byte) 0b1100))
		);
	}

	@Test
	public void testDecodificacionNibble() throws DecodificacionImposibleException {
		assertEquals(
			0b1100,
			new HammingAmpliado3_2().decodificarPalabraCodigo((byte) 0b1100_1100)
		);
	}

	@Test
	public void testCodificacionDecodificacion() throws DecodificacionImposibleException {
		final HammingAmpliado3_2 codigo = new HammingAmpliado3_2();

		System.setErr(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				// Descartar byte
			}
		}));

		for (int nibble = 0; nibble < 16; ++nibble) {
			assertEquals(
				nibble,
				codigo.decodificarPalabraCodigo(codigo.codificarNibble((byte) nibble))
			);
		}
	}

	@Test
	public void testCodificacionDecodificacionErrorSimple() throws DecodificacionImposibleException {
		final HammingAmpliado3_2 codigo = new HammingAmpliado3_2();

		for (int posError = 0; posError < 8; ++posError) {
			for (int nibble = 0; nibble < 16; ++nibble) {
				byte palabraCodigo = codigo.codificarNibble((byte) nibble);

				// Introducir un error simple
				palabraCodigo ^= 1 << posError;

				assertEquals(
					nibble,
					codigo.decodificarPalabraCodigo(palabraCodigo)
				);
			}
		}
	}

	@Test
	public void testCodificacionDecodificacionErrorDoble() {
		final HammingAmpliado3_2 codigo = new HammingAmpliado3_2();
		final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
		boolean todasDecodificacionesFallan = true;

		System.setErr(new PrintStream(stderr));

		for (int error = 0; error < 256 && todasDecodificacionesFallan; ++error) {
			if (Integer.bitCount(error) == 2) { // Dos errores
				for (int nibble = 0; nibble < 16 && todasDecodificacionesFallan; ++nibble) {
					final MatrizZ2 palabraCodigo = new MatrizZ2(
						new byte[][] { EmpaquetamientoBits.bitsABytes(codigo.codificarNibble((byte) nibble), 8) }
					);

					// Introducir un error doble
					final byte palabraConError = EmpaquetamientoBits.bytesABits(palabraCodigo.sumar(
						new MatrizZ2(new byte[][] { EmpaquetamientoBits.bitsABytes((byte) error, 8) })
					).getFila(0));

					try {
						stderr.reset();

						codigo.decodificarPalabraCodigo(palabraConError);

						// Aunque no se lance una excepción, debe de avisar de que se han detectado errores
						todasDecodificacionesFallan = !new String(stderr.toByteArray())
							.contains("error de transmisión de 2 bit");
					} catch (DecodificacionImposibleException ignorada) {}
				}
			}
		}

		assertTrue(todasDecodificacionesFallan);
	}
}
