# urlconnection-Java

ESTUDIANTE: Santiago Beltran Astorga  
EXPEDIENTE: 225203551  

Aplicación de consola en Java que descarga el código HTML de una página web, busca las etiquetas `<img>` y guarda las imágenes encontradas en la carpeta `./imagenes/` usando únicamente las clases estándar de Java (`URL`, `URLConnection` y `HttpURLConnection`).

## Descripción

El programa realiza los siguientes pasos:

1. Valida que se reciba una URL correcta con protocolo `http` o `https`.
2. Se conecta al servidor mediante una petición HTTP `GET` y descarga el documento HTML.
3. Busca las etiquetas `<img>` en el HTML para extraer las rutas (`src`) de las imágenes, convirtiendo las rutas relativas en completas y evitando duplicados.
4. Descarga cada imagen por separado verificando que realmente sea una imagen (`image/png`, `image/jpeg`, `image/gif`, `image/webp`), que no pese más de 10 MB y sin sobrescribir archivos que ya existan.

## Requisitos

* **Java (JDK):** Versión 17 o superior.
* **Conexión a Internet** (o un servidor local para hacer pruebas).
* No requiere librerías externas.

## Instrucciones de compilación y ejecución

### Compilar

Desde la carpeta raíz del proyecto, ejecuta:

```bash
javac -d out src/main/java/ImageDownloader.java
```

### Ejecutar

Ejecuta el programa pasando la URL de la página web como argumento:

```bash
java -cp out ImageDownloader <URL>
```

**Ejemplo:**

```bash
java -cp out ImageDownloader https://books.toscrape.com
```

*(También se puede ejecutar directamente con `java src/main/java/ImageDownloader.java <URL>`).*

## Preguntas de reflexión

1. **¿Qué responsabilidad desempeña la aplicación dentro de una arquitectura cliente-servidor?**
   Es el **cliente**: envía peticiones HTTP al servidor para pedir el HTML y las imágenes, y recibe las respuestas para guardarlas en la computadora.

2. **¿Qué diferencias existen entre `URL`, `URLConnection` y `HttpURLConnection`?**
   * `URL`: Guarda la dirección del recurso.
   * `URLConnection`: Prepara y configura la conexión a esa dirección.
   * `HttpURLConnection`: Es la versión para HTTP/HTTPS que permite usar `GET` y leer el código de estado (como `200` o `404`).

3. **¿Qué sucede al invocar `openConnection()`?**
   Crea el objeto de conexión para configurarlo, pero todavía no se conecta a Internet.

4. **¿Qué operaciones provocan que la conexión se establezca realmente?**
   Cuando se llama a `connect()` o a métodos que piden datos al servidor, como `getInputStream()`, `getResponseCode()` o `getContentType()`.

5. **¿Por qué una página HTML puede requerir múltiples conexiones?**
   Porque el HTML solo trae texto con las direcciones (`src`) de las imágenes; por eso hay que hacer una conexión para el HTML y otra aparte por cada imagen.

6. **¿Cuál es la diferencia entre `getInputStream()` y `getContentType()`?**
   `getContentType()` dice qué tipo de archivo es (por ejemplo, `image/jpeg`), mientras que `getInputStream()` descarga los bytes del archivo.

7. **¿Por qué distinguir recursos textuales y binarios?**
   El texto (HTML) usa codificación de caracteres (como `UTF-8`) y las imágenes son bytes puros; si una imagen se lee como texto, se daña.

8. **¿Para qué sirven los tiempos máximos de conexión y lectura?**
   Para evitar que el programa se quede congelado si el servidor no responde o se corta la conexión.

9. **¿Qué limitaciones tiene analizar HTML mediante expresiones regulares?**
   Pueden fallar si el HTML tiene etiquetas comentadas, atributos sin comillas o imágenes que se cargan con JavaScript.

10. **¿Cómo podría implementarse la descarga concurrente usando únicamente la API estándar de Java?**
    Usando `ExecutorService` (`Executors.newFixedThreadPool(...)`) para ejecutar varias descargas al mismo tiempo en distintos hilos.