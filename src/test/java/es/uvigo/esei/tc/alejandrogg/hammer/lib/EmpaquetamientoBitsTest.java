// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.tc.alejandrogg.hammer.lib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import es.uvigo.esei.tc.alejandrogg.hammer.lib.EmpaquetamientoBits;

/**
 * Pruebas para {@link HammingAmpliado3_2}.
 *
 * @author Alejandro González García
 */
public class EmpaquetamientoBitsTest {
	@Test
	public void testBitsABytes() {
		assertArrayEquals(
			new byte[] { 1, 0, 1, 0 },
			EmpaquetamientoBits.bitsABytes((byte) 0b1010, 4)
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBitsABytesInvalido() {
		EmpaquetamientoBits.bitsABytes((byte) 3, 564);
	}

	@Test
	public void testBytesABits() {
		assertEquals(
			0b1100,
			EmpaquetamientoBits.bytesABits(new byte[] { 1, 1, 0, 0 })
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBytesABitsNulo() {
		EmpaquetamientoBits.bytesABits(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBytesABitsInvalido() {
		EmpaquetamientoBits.bytesABits(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 });
	}
}
