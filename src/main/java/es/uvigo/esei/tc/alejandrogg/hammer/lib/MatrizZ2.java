// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.tc.alejandrogg.hammer.lib;

import java.util.Arrays;

/**
 * Una matriz cuyos elementos están en Z<sub>2</sub>.
 *
 * @author Alejandro González García
 */
public final class MatrizZ2 {
	private final byte[][] elementos;
	private final int filas;
	private final int columnas;

	/**
	 * Crea una nueva matriz en Z<sub>2</sub>, a partir de los bytes que la
	 * componen. Se asume que los bytes toman valores en { 0, 1 }.
	 *
	 * @param elementos Los elementos de la matriz a crear, donde el primer índice
	 *                  representa las filas de la matriz, y el segundo índice
	 *                  localiza un elemento de una columna dentro de una fila.
	 * @throws IllegalArgumentException Si {@code elementos} es nulo, la dimensión
	 *                                  de la matriz es nula, o no es rectangular.
	 */
	public MatrizZ2(final byte[][] elementos) {
		if (elementos == null) {
			throw new IllegalArgumentException("El array de elementos de una matriz binaria no puede ser nulo");
		}

		if (elementos.length <= 0) {
			throw new IllegalArgumentException("La matriz debe de tener al menos una fila");
		}

		int ultimoNumeroColumnas = Integer.MIN_VALUE;
		for (final byte[] fila : elementos) {
			if (fila == null || fila.length <= 0 || (ultimoNumeroColumnas != Integer.MIN_VALUE && ultimoNumeroColumnas != fila.length)) {
				throw new IllegalArgumentException("El número de columnas debe de ser mayor o igual que 0 y común a todas las filas");
			}

			ultimoNumeroColumnas = fila.length;
		}

		this.elementos = elementos;
		this.filas = elementos.length;
		this.columnas = elementos[0].length;
	}

	/**
	 * Obtiene el número de filas de la matriz.
	 * @return El número de filas de la matriz.
	 */
	public int getNumeroFilas() {
		return filas;
	}

	/**
	 * Obtiene el número de columnas de la matriz.
	 * @return El número de columnas de la matriz.
	 */
	public int getNumeroColumnas() {
		return columnas;
	}

	/**
	 * Obtiene el elemento (n, k) de la matriz, donde la primera fila y columna son
	 * la 0.
	 *
	 * @param n El número de fila del elemento a obtener.
	 * @param m El número de columna del elemento a obtener.
	 * @return El elemento deseado.
	 * @throws IllegalArgumentException Si el número de fila o columna es inválido.
	 */
	public byte getElemento(final int n, final int m) {
		if (n < 0 || n >= filas) {
			throw new IllegalArgumentException("El número de fila es inválido");
		}

		if (m < 0 || m >= columnas) {
			throw new IllegalArgumentException("El número de columna es inválido");
		}

		return elementos[n][m];
	}

	/**
	 * Establece el valor del elemento (n, k) de la matriz, donde la primera fila y
	 * columna son la 0.
	 *
	 * @param n El número de fila del elemento a establecer.
	 * @param m El número de columna del elemento a establecer.
	 * @param v El valor del elemento, 0 ó 1.
	 * @throws IllegalArgumentException Si el número de fila o columna es inválido,
	 *                                  o el valor no es 0 ó 1.
	 */
	public void setElemento(final int n, final int m, final byte v) {
		if (n < 0 || n >= filas) {
			throw new IllegalArgumentException("El número de fila es inválido");
		}

		if (m < 0 || m >= columnas) {
			throw new IllegalArgumentException("El número de columna es inválido");
		}

		if (v != 0 && v != 1) {
			throw new IllegalArgumentException("El valor del elemento no es válido");
		}

		elementos[n][m] = v;
	}

	/**
	 * Establece el valor del elemento (n, k) de la matriz, donde la primera fila y
	 * columna son la 0.
	 *
	 * @param n El número de fila del elemento a establecer.
	 * @param m El número de columna del elemento a establecer.
	 * @param v El valor del elemento, 0 ó 1.
	 * @throws IllegalArgumentException Si el número de fila o columna es inválido,
	 *                                  o el valor no es 0 ó 1.
	 */
	public void setElemento(final int n, final int m, final int v) {
		setElemento(n, m, (byte) v);
	}

	/**
	 * Obtiene la fila especificada de esta matriz, siendo la primera fila la 0.
	 * Cualquier cambio que se haga al array devuelto se propagará a la matriz.
	 *
	 * @param n El número de fila a obtener.
	 * @return La devandicha fila.
	 * @throws IllegalArgumentException Si el número de fila es inválido.
	 */
	public byte[] getFila(final int n) {
		if (n < 0 || n >= filas) {
			throw new IllegalArgumentException("El número de fila es inválido");
		}

		return elementos[n];
	}

	/**
	 * Calcula el producto de esta matriz con otra matriz en Z<sub>2</sub>. Los
	 * elementos de la nueva matriz resultante, devuelta por el método, están
	 * también en Z<sub>2</sub>.
	 *
	 * @param matriz La matriz por la que multiplicar.
	 * @return La matriz resultado de la multiplicación.
	 * @throws IllegalArgumentException Si las matrices no son compatibles (el
	 *                                  número de columnas de esta matriz no iguala
	 *                                  al número de filas de la matriz parámetro),
	 *                                  o {@code matriz} es nula.
	 */
	public MatrizZ2 multiplicarPor(final MatrizZ2 matriz) {
		byte[][] elementosResultado;

		if (matriz == null) {
			throw new IllegalArgumentException("La matriz binaria por la que se multiplica no puede ser nula");
		}

		if (this.columnas != matriz.filas) {
			throw new IllegalArgumentException("El número de columnas de esta matriz debe de coincidir con el número de filas de la matriz operando");
		}

		elementosResultado = new byte[this.filas][matriz.columnas];

		for (int i = 0; i < this.filas; ++i) {
			for (int j = 0; j < matriz.columnas; ++j) {
				for (int k = 0; k < this.columnas; ++k) {
					// XOR equivale a suma módulo 2 (el contenido inicial de cada elemento es 0)
					elementosResultado[i][j] ^= (byte) (elementos[i][k] * matriz.elementos[k][j]);
				}
			}
		}

		return new MatrizZ2(elementosResultado);
	}

	/**
	 * Calcula la suma de esta matriz con otra matriz en Z<sub>2</sub>. La matriz
	 * resultante tiene la misma dimensión que esta matriz, siendo sus elementos el
	 * resultado de sumar dos a dos los elementos de la misma posición de las
	 * matrices operando. Para Z<sub>2</sub>, la operación suma equivale a la
	 * operación resta.
	 *
	 * @param matriz La matriz que sumar.
	 * @return Una nueva matriz resultado de la operación.
	 * @throws IllegalArgumentException Si las matrices no son compatibles (su
	 *                                  dimensión no coincide), o {@code matriz} es
	 *                                  nula.
	 */
	public MatrizZ2 sumar(final MatrizZ2 matriz) {
		byte[][] elementosResultado;

		if (matriz == null) {
			throw new IllegalArgumentException("La matriz binaria por la que se multiplica no puede ser nula");
		}

		if (this.filas != matriz.filas && this.columnas != matriz.columnas) {
			throw new IllegalArgumentException("El número de filas y columnas de las matrices deben de coincidir");
		}

		elementosResultado = new byte[filas][columnas];

		for (int i = 0; i < filas; ++i) {
			for (int j = 0; j < columnas; ++j) {
				elementosResultado[i][j] = (byte) (this.elementos[i][j] ^ matriz.elementos[i][j]);
			}
		}

		return new MatrizZ2(elementosResultado);
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof MatrizZ2 &&
			Arrays.deepEquals(this.elementos, ((MatrizZ2) obj).elementos);
	}

	@Override
	public int hashCode() {
		return Arrays.deepHashCode(elementos);
	}

	@Override
	public String toString() {
		return Arrays.deepToString(elementos);
	}
}
