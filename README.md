# Hammer
_Hammer_ es una aplicación de línea de comandos escrita en Java 8 (o posterior) que codifica cualquier flujo de bytes a un fichero de acuerdo a un código Hamming ampliado (3, 2) sistemático, y luego permite decodificarlo, corrigiendo errores que pudiese haber. El funcionamiento de la codificación consiste en dividir cada byte de entrada en dos nibbles (grupos de 4 bits), que se codifican sucesivamente con el código Hamming comentado, correspondiéndole a cada nibble (vector fuente) un vector código de un byte (por tanto, un byte de entrada pasa a ocupar dos). Por las propiedades del código, se pueden corregir errores de un bit, y detectar errores de dos bits, en la recepción de cada nibble.

![Diagrama de codificación de Hammer](https://github.com/aggarcia3/Hammer/raw/master/Diagrama%20codificaci%C3%B3n%20Hammer.svg)

## Ejemplos de codificación y decodificación
Antes de nada, es conveniente conocer las opciones y modos de funcionamiento ofrecidos por Hammer, que se pueden visualizar ejecutando `Hammer.jar` con una máquina virtual de Java. Normalmente, basta con ejecutar el siguiente comando en un terminal, si `Hammer.jar` se encuentra en el directorio de trabajo actual:

```bash
$ java -jar Hammer.jar –-ayuda
```

La salida del anterior comando debería de resultar autoexplicativa. A continuación, se comentan algunos ejemplos de tareas de codificación y decodificación, definidas a partir de los argumentos pasados a Hammer.

### Ejemplo 1: codificar el fichero de pruebas `Sonido.raw` a otro fichero llamado `Sonido2.raw`, introduciendo un único error por vector código (byte) en una posición aleatoria

```bash
$ java -jar Hammer.jar –c Sonido2.raw –e 1 "Ficheros de pruebas/Sonido.raw"
```

`Sonido.raw` es un fichero de audio PCM de 8 bits y un único canal en bruto, que contiene un tono cuya frecuencia asciende desde 20 Hz hasta 22000 Hz en diez segundos. Está muestreado a 44100 Hz, por lo que su tamaño es exactamente 441000 bytes. Un fichero equivalente a éste, pero codificado en un formato con cabeceras y compatible con la mayoría de reproductores multimedia, se encuentra en `Ficheros de pruebas/Sonido.wav`. Si se omite el parámetro -e, no se introducirán errores.

### Ejemplo 2: decodificar el fichero `Sonido2.raw` generado en el Ejemplo 1

```bash
$ java -jar Hammer.jar –d "Sonido decodificado.raw" Sonido2.raw
```

Como se ha introducido un único error por byte, Hammer es capaz de recuperar el sonido original, aunque detecta errores de recepción y avisa de su presencia en el terminal. Puede comprobarse con comandos como `md5sum` que, en efecto, `Ficheros de pruebas/Sonido.raw` y `Sonido decodificado.raw` son idénticos. Como normalmente mostrar mucho texto en un terminal interactivo es lento, puede suprimirse el aviso de detección de errores con la opción `-q`.

### Ejemplo 3: codificar el fichero de pruebas `Sonido.raw` a otro fichero llamado `Sonido3.raw`, introduciendo un número de errores por byte resultado de muestrear una distribución normal estándar, pero que nunca supera la capacidad de detección del código

```bash
$ java -jar Hammer.jar –c Sonido3.raw –l "Ficheros de pruebas/Sonido.raw"
```

`Sonido3.raw` puede ser decodificado de manera análoga a la comentada en el Ejemplo 2. No obstante, en esta ocasión lo más posible es que se hayan introducido varios errores incorregibles, que provocan que la palabra fuente asociada sea descartada durante la decodificación. El resultado sería similar al disponible en `Ficheros de pruebas/Sonido codificado corrupto recuperado.wav` y, aunque hay un ruido audible, especialmente en frecuencias altas, el audio sigue siendo reconocible.

### Ejemplo 4: codificar el fichero de pruebas `The Hitchhiker's Guide to the Galaxy.txt` a otro fichero llamado `Historia.txt`, introduciendo un número de errores por byte resultado de muestrear una distribución normal estándar, que puede superar la capacidad de detección y corrección del código

```bash
$ java -jar Hammer.jar –c Historia.txt –n "Ficheros de pruebas/The Hitchhiker's Guide to the Galaxy.txt"
```

Si se decodifica `Historia.txt` (la correspondiente pareja de ficheros ya codificados y decodificados se encuentra en el directorio `Ficheros de pruebas`), pueden identificarse algunos pasajes de texto y partes de palabras originales. Sin embargo, como el número de errores ha sido mayor que 2 en algunos bytes, algunos caracteres se han decodificado incorrectamente, por unos que no tienen nada que ver con los originales. Cabe esperar este resultado, pues el código no está diseñado para lidiar con este nivel de errores.

## Estructura del código fuente

El código fuente está organizado como un proyecto de Maven. Con Maven instalado, pueden ejecutarse las pruebas y generar el JAR ejecutable con el comando `mvn package`. También se importar a cualquier IDE o editor de texto que soporte Maven, como Eclipse, NetBeans o Visual Studio Code.
