import java.net.URI;
import java.net.URL;

public class ImageDownloader {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso: java ImageDownloader <URL>");
            return;
        }
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
			
            System.out.println("Protocolo: " + url.getProtocol());
            System.out.println("Servidor: " + url.getHost());
            System.out.println("Puerto: " + url.getPort());
            System.out.println("Ruta: " + url.getPath());
        } catch (IllegalArgumentException e) {
            System.err.println("URL inválida: " + e.getMessage());
        } catch (java.net.MalformedURLException e) {
            System.err.println("URL no admitida: " + e.getMessage());
        }
    }
} 
