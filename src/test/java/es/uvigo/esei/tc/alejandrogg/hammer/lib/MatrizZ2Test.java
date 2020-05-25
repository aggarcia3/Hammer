// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.tc.alejandrogg.hammer.lib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import es.uvigo.esei.tc.alejandrogg.hammer.lib.MatrizZ2;

/**
 * Pruebas para {@link MatrizZ2}.
 *
 * @author Alejandro González García
 */
public class MatrizZ2Test {
	private static MatrizZ2 a = null;
	private static MatrizZ2 b = null;
	private static MatrizZ2 c = null;

	@BeforeClass
	public static void crearMatrices() {
		a = new MatrizZ2(new byte[][] {
			{ 1, 1, 0, 0 }
		});

		b = new MatrizZ2(new byte[][] {
			{ 1, 0, 0, 0, 0, 1, 1 },
			{ 0, 1, 0, 0, 1, 0, 1 },
			{ 0, 0, 1, 0, 1, 1, 0 },
			{ 0, 0, 0, 1, 1, 1, 1 }
		});

		c = new MatrizZ2(new byte[][] {
			{ 0, 1, 0, 1 }
		});
	}

	@Test
	public void getNumeroFilasA() {
		assertEquals(1, a.getNumeroFilas());
	}

	@Test
	public void getNumeroFilasB() {
		assertEquals(4, b.getNumeroFilas());
	}

	@Test
	public void getNumeroColumnasA() {
		assertEquals(4, a.getNumeroColumnas());
	}

	@Test
	public void getNumeroColumnasB() {
		assertEquals(7, b.getNumeroColumnas());
	}

	@Test
	public void getElementoValidoA() {
		assertEquals(1, a.getElemento(0, 1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void getElementoInvalidoA() {
		a.getElemento(0, 27);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getElementoInvalidoB() {
		b.getElemento(544, 0);
	}

	@Test
	public void setElementoA() {
		a.setElemento(0, 0, 0);
		assertEquals(0, a.getElemento(0, 0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void setElementoInvalidoA() {
		a.setElemento(0, 27, 0);
	}

	@Test
	public void setElementoB() {
		b.setElemento(2, 6, 1);
		assertEquals(1, b.getElemento(2, 6));
	}

	@Test(expected = IllegalArgumentException.class)
	public void setElementoInvalidoB() {
		b.setElemento(544, 0, 0);
	}

	@Test
	public void getFilaA() {
		// Este test se ejecuta después de setElementoA()
		assertArrayEquals(
			new byte[] { 0, 1, 0, 0 },
			a.getFila(0)
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getFilaInvalidaA() {
		a.getFila(64);
	}

	@Test
	public void getFilaB() {
		// Este test se ejecuta después de setElementoB()
		assertArrayEquals(
			new byte[] { 0, 0, 1, 0, 1, 1, 1 },
			b.getFila(2)
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getFilaInvalidaB() {
		b.getFila(542);
	}

	@Test
	public void testMultiplicacion() {
		assertEquals(
			new MatrizZ2(new byte[][] {
				{ 1, 1, 0, 0, 1, 1, 0 }
			}),
			a.multiplicarPor(b)
		);
	}

	@Test
	public void testSuma() {
		assertEquals(
			new MatrizZ2(new byte[][] {
				{ 1, 0, 0, 1 }
			}),
			a.sumar(c)
		);
	}
}
