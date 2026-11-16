import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageDownloader {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso: java ImageDownloader <URL>");
            return;
        }
        HttpURLConnection connection = null;
        try {
            URI uri = URI.create(args[0]);
            URL url = uri.toURL();

            String protocolo = url.getProtocol();
            if (!"http".equalsIgnoreCase(protocolo) && !"https".equalsIgnoreCase(protocolo)) {
                System.err.println("Protocolo no admitido: " + protocolo);
                return;
            }
            if (url.getHost() == null || url.getHost().isBlank()) {
                System.err.println("Servidor inválido.");
                return;
            }

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);

            int codigo = connection.getResponseCode();
            String tipoContenido = connection.getContentType();
            System.out.println("Código HTTP: " + codigo);
            System.out.println("Tipo de contenido: " + tipoContenido);

            Charset charset = StandardCharsets.UTF_8;
            if (tipoContenido != null && tipoContenido.toLowerCase().contains("charset=")) {
                String encoding = tipoContenido.substring(tipoContenido.toLowerCase().indexOf("charset=") + 8).split(";")[0].trim();
                charset = Charset.forName(encoding);
            }

            StringBuilder html = new StringBuilder();
            try (InputStream input = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    html.append(linea).append("\n");
                }
            }

            String contenido = html.toString();
            Set<String> imagenes = extraerImagenes(contenido);
            System.out.println("Imágenes encontradas: " + imagenes.size());

            Path directorio = Path.of("imagenes");
            Files.createDirectories(directorio);

            int numero = 1;
            for (String img : imagenes) {
                URL urlImagen = resolverImagen(url, img);
                if (urlImagen != null) {
                    descargarImagen(urlImagen, directorio, numero++);
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static Set<String> extraerImagenes(String html) {
        Set<String> imagenes = new LinkedHashSet<>();
        Pattern imgPattern = Pattern.compile("<img\\b[^>]*>", Pattern.CASE_INSENSITIVE);
        Pattern srcPattern = Pattern.compile("\\bsrc\\s*=\\s*([\"'])(.*?)\\1", Pattern.CASE_INSENSITIVE);

        Matcher imgMatcher = imgPattern.matcher(html);
        while (imgMatcher.find()) {
            String etiqueta = imgMatcher.group();
            Matcher srcMatcher = srcPattern.matcher(etiqueta);
            if (srcMatcher.find()) {
                imagenes.add(srcMatcher.group(2));
            }
        }
        return imagenes;
    }

    public static URL resolverImagen(URL urlPagina, String referencia) {
        if (referencia == null || referencia.isBlank()) {
            return null;
        }
        String lower = referencia.trim().toLowerCase();
        if (lower.startsWith("data:") || lower.startsWith("javascript:")) {
            return null;
        }
        try {
            URL resuelta = new URL(urlPagina, referencia.trim());
            String protocolo = resuelta.getProtocol();
            if (!"http".equalsIgnoreCase(protocolo) && !"https".equalsIgnoreCase(protocolo)) {
                return null;
            }
            return resuelta;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static boolean descargarImagen(URL urlImagen, Path directorio, int numero) {
        HttpURLConnection connection = null;
        Path destino = null;
        long maxBytes = 10L * 1024 * 1024;
        try {
            connection = (HttpURLConnection) urlImagen.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);

            int codigo = connection.getResponseCode();
            if (codigo != HttpURLConnection.HTTP_OK) {
                System.err.println("Error HTTP " + codigo + " en: " + urlImagen);
                return false;
            }

            String tipo = connection.getContentType();
            String ext = null;
            if (tipo != null) {
                String mime = tipo.split(";")[0].trim().toLowerCase();
                if (mime.equals("image/png")) ext = "png";
                else if (mime.equals("image/jpeg")) ext = "jpg";
                else if (mime.equals("image/gif")) ext = "gif";
                else if (mime.equals("image/webp")) ext = "webp";
            }
            if (ext == null) {
                System.err.println("No es imagen admitida (" + tipo + "): " + urlImagen);
                return false;
            }

            if (connection.getContentLengthLong() > maxBytes) {
                System.err.println("Excede 10 MB: " + urlImagen);
                return false;
            }

            String nombre = String.format("imagen_%03d.%s", numero, ext);
            destino = directorio.resolve(nombre);
            int intento = 1;
            while (Files.exists(destino)) {
                nombre = String.format("imagen_%03d_%d.%s", numero, intento++, ext);
                destino = directorio.resolve(nombre);
            }

            try (InputStream input = connection.getInputStream();
                 OutputStream output = Files.newOutputStream(destino)) {
                byte[] buffer = new byte[8192];
                int leidos;
                long total = 0;
                while ((leidos = input.read(buffer)) != -1) {
                    total += leidos;
                    if (total > maxBytes) {
                        throw new IOException("Supera el límite de 10 MB");
                    }
                    output.write(buffer, 0, leidos);
                }
            }

            System.out.println("Descargada: " + nombre);
            return true;
        } catch (Exception e) {
            if (destino != null) {
                try {
                    Files.deleteIfExists(destino);
                } catch (IOException ignored) {}
            }
            System.err.println("Error al descargar " + urlImagen + ": " + e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}