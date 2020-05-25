// SPDX-License-Identifier: GPL-3.0-or-later

package es.uvigo.esei.tc.alejandrogg.hammer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Random;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.IntegerConverter;

/**
 * Clase principal de la aplicación.
 *
 * @author Alejandro González García
 */
public final class Hammer {
	@Parameter(
		description = "Fichero de entrada (\"-\" para usar el flujo de entrada estándar)",
		required = true,
		converter = ConversorCadenaEntrada.class,
		validateValueWith = ConversorCadenaEntrada.class
	)
	private InputStream entrada = null;

	@Parameter(
		description = "Fichero de salida al que codificar la entrada, usando un código Hamming ampliado (3, 2). Se puede indicar \"-\" para usar el flujo de salida estándar",
		names = { "-c", "--codificar" },
		converter = ConversorFlujoSalida.class
	)
	private OutputStream flujoSalidaCodificacion = null;

	@Parameter(
		description = "Número de errores por byte a introducir en la codificación",
		names = { "-e", "--errores" },
		converter = IntegerConverter.class,
		validateValueWith = ValidadorNumeroErrores.class
	)
	private Integer numeroErrores = 0;

	@Parameter(
		description = "Introduce errores por byte a la codificación que siguen, aproximadamente, una distribución normal de media 0 y desviación típica 1",
		names = { "-n", "--errores-normales" }
	)
	private boolean erroresNormales = false;

	@Parameter(
		description = "Introduce errores con una distribución similar a -n, excepto que en ningún caso excederán la capacidad de detección del código. Es decir, la probabilidad de introducir más de dos errores en un byte es cero",
		names = { "-l", "--errores-normales-limitados" }
	)
	private boolean erroresNormalesLimitados = false;

	@Parameter(
		description = "Fichero de salida al que decodificar la entrada, previamente codificada con -c. Se puede indicar \"-\" para usar el flujo de salida estándar",
		names = { "-d", "--decodificar" },
		converter = ConversorFlujoSalida.class
	)
	private OutputStream flujoSalidaDecodificacion = null;

	@Parameter(
		description = "Suprime la salida de la mayor parte del texto por el flujo de errores",
		names = { "-q", "-s", "--quiet", "--silencioso" }
	)
	private boolean modoSilencioso = false;

	@Parameter(
		description = "Muestra los parámetros aceptados por la aplicación",
		names = { "-h", "--help", "--ayuda" },
		help = true
	)
	private boolean mostrarAyuda = false;

	private final Random prng = new Random();

	private final byte[] posicionesError = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 };

	private int erroresIntroducidos = 0;
	

	/**
	 * Punto de entrada de la aplicación, ejecutado por la JVM.
	 *
	 * @param args Los argumentos recibidos del entorno de ejecución.
	 */
	public static void main(final String... args) {
		new Hammer().run(args);
	}

	/**
	 * Ejecuta la aplicación.
	 *
	 * @param args Los argumentos recibidos del entorno de ejecución.
	 */
	public void run(final String... args) {
		final JCommander jCommander = JCommander.newBuilder()
			.addObject(this)
			.programName(getClass().getSimpleName() + ".jar")
			.build();

		jCommander.setUsageFormatter(new FormateadorSintaxisParametros(jCommander));

		try {
			jCommander.parse(args);

			if (!mostrarAyuda) {
				int byteLeido;
				int bytesLeidos = 0;

				// No permitir ciertas combinaciones de argumentos ambigüas o sin sentido
				if (flujoSalidaCodificacion != null && flujoSalidaDecodificacion != null) {
					throw new ParameterException("");
				}

				if (erroresNormales && erroresNormalesLimitados) {
					throw new ParameterException("");
				}

				if (flujoSalidaCodificacion == null && flujoSalidaDecodificacion == null) {
					throw new ParameterException("");
				}

				if (modoSilencioso) {
					System.setErr(new PrintStream(new OutputStream() {
						@Override
						public void write(int b) throws IOException {
							// Descartar byte
						}
					}));
				}

				if (flujoSalidaCodificacion != null) {
					final HammingAmpliado3_2 codigo = new HammingAmpliado3_2();

					// Codificar cada byte del flujo de entrada al flujo de salida
					while ((byteLeido = entrada.read()) != -1) {
						for (int i = 0; i < 2; ++i) {
							final byte nibbleActual = (byte) (byteLeido >>> (4 * i));

							flujoSalidaCodificacion.write(
								introducirErrores(codigo.codificarNibble(nibbleActual))
							);
						}

						++bytesLeidos;
					}

					System.err.printf(
						"> Se han codificado %d bytes de entrada en %d bytes, introduciendo un total de %d errores (%.2f errores/byte)" + System.lineSeparator(),
						bytesLeidos, bytesLeidos * 2, erroresIntroducidos, erroresIntroducidos / (double) (bytesLeidos * 2)
					);
				} else {
					final HammingAmpliado3_2 codigo = new HammingAmpliado3_2();

					// Decodificar cada byte del flujo de entrada al flujo de salida
					int bytesRecuperados = 0;
					byte palabraFuenteActual = 0;
					boolean saltarByte = false;

					while ((byteLeido = entrada.read()) != -1) {
						if (!saltarByte) {
							try {
								final int nibbleActual = bytesLeidos % 2;

								// Añadir este nibble a la palabra fuente actual
								palabraFuenteActual |= codigo.decodificarPalabraCodigo((byte) byteLeido) << (4 * nibbleActual);

								if (nibbleActual % 2 == 1) {
									// Nibble completo si el siguiente byte a leer es par.
									// Reestablecer la palabra fuente actual a 0, tras
									// escribirla en el flujo de salida
									flujoSalidaDecodificacion.write(palabraFuenteActual);
									palabraFuenteActual = 0;
									++bytesRecuperados;
								}
							} catch (final DecodificacionImposibleException exc) {
								// Si este nibble era el primero de un byte, ignorar el siguiente byte,
								// que contiene el segundo nibble del byte (no podemos recuperar el byte
								// original)
								if (bytesLeidos % 2 == 0) {
									saltarByte = true;
								}

								palabraFuenteActual = 0;
							}
						} else {
							saltarByte = false;
						}

						++bytesLeidos;
					}

					System.err.printf(
						"> Se han decodificado %d bytes de entrada en %d bytes, pudiendo recuperarse el %.2f%% del mensaje original" + System.lineSeparator(),
						bytesLeidos, bytesRecuperados, (200 * bytesRecuperados) / (double) bytesLeidos
					);
				}
			} else {
				jCommander.usage();
			}
		} catch (final ParameterException exc) {
			System.err.println("Algún parámetro falta o es incorrecto");
			jCommander.usage();
		} catch (final Exception exc) {
			System.err.println("! Ha ocurrido un error durante la ejecución de la aplicación. Detalles:");
			exc.printStackTrace();
		} finally {
			if (entrada != null) {
				try {
					entrada.close();
				} catch (final IOException ignorada) {}
			}

			if (flujoSalidaCodificacion != null) {
				try {
					flujoSalidaCodificacion.close();
				} catch (final IOException ignorada) {}
			}

			if (flujoSalidaDecodificacion != null) {
				try {
					flujoSalidaDecodificacion.close();
				} catch (final IOException ignorada) {}
			}
		}
	}

	/**
	 * Introduce errores a la palabra código especificada, según lo deseado por el
	 * usuario, incrementando el contador de errores introducidos apropiadamente.
	 *
	 * @param palabraCodigo La palabra código a corromper.
	 * @return La palabra código, potencialmente corrupta.
	 */
	private byte introducirErrores(byte palabraCodigo) {
		reordenarPosicionesError(posicionesError);

		final int numeroErrores;
		if (erroresNormales) {
			numeroErrores = (int) Math.abs(Math.floor(prng.nextGaussian()));
		} else if (erroresNormalesLimitados) {
			numeroErrores = ((int) Math.abs(Math.floor(prng.nextGaussian()))) % 3;
		} else {
			numeroErrores = this.numeroErrores;
		}

		for (int i = 0; i < posicionesError.length && i < numeroErrores; ++i) {
			// Negar bit (introducir error) en la posición indicada
			palabraCodigo ^= 1 << posicionesError[i];
			++erroresIntroducidos;
		}

		return palabraCodigo;
	}

	/**
	 * Reordena aleatoriamente las posiciones de error para un byte contenidas en el
	 * array. El array recibido como parámetro es modificado directamente.
	 * <p>
	 * La implementación de este método sigue el algoritmo de Fisher–Yates,
	 * documentado en <a href=
	 * "https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia</a>.
	 * </p>
	 *
	 * @param posiciones El array de posiciones a reordenar.
	 */
	private void reordenarPosicionesError(final byte[] posiciones) {
		for (int i = posiciones.length - 1; i >= 1; --i) {
			final byte j = (byte) prng.nextInt(i + 1);

			byte temp = posiciones[i];
			posiciones[i] = posiciones[j];
			posiciones[j] = temp;
		}
	}

	public static final class ConversorCadenaEntrada implements IStringConverter<InputStream>, IValueValidator<InputStream> {
		@Override
		public InputStream convert(final String value) {
			try {
				return "-".equals(value) ? System.in : new BufferedInputStream(new FileInputStream(value));
			} catch (final FileNotFoundException exc) {
				return null;
			}
		}

		@Override
		public void validate(final String name, final InputStream value) throws ParameterException {
			if (value == null) {
				throw new ParameterException("");
			}
		}
	}

	public static final class ConversorFlujoSalida implements IStringConverter<OutputStream>, IValueValidator<OutputStream> {
		@Override
		public OutputStream convert(final String value) {
			try {
				return "-".equals(value) ? System.out : new BufferedOutputStream(new FileOutputStream(value));
			} catch (final FileNotFoundException exc) {
				return null;
			}
		}

		@Override
		public void validate(final String name, final OutputStream value) throws ParameterException {
			if (value == null) {
				throw new ParameterException("");
			}
		}
	}

	public static final class ValidadorNumeroErrores implements IValueValidator<Integer> {
		@Override
		public void validate(final String name, final Integer value) throws ParameterException {
			if (value != null && value < 0 && value <= 8) {
				throw new ParameterException("");
			}
		}
	}
}
