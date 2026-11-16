import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
            System.out.println(contenido.substring(0, Math.min(500, contenido.length())));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}