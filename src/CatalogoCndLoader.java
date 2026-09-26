import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CatalogoCndLoader {

    public static Map<String, String> caricaCatalogoDaCsv(String percorsoFileCsv) {
        Map<String, String> catalogo = new HashMap<>();
        Path path = Paths.get(percorsoFileCsv);

        if (!path.toFile().exists()) {
            System.err.println("ERRORE: File catalogo CND non trovato in " + path.toAbsolutePath());
            return catalogo;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String riga;
            while ((riga = br.readLine()) != null) {
                // Rimuove eventuali caratteri BOM o virgolette
                riga = riga.replace("\uFEFF", "").replace("\"", "").trim();
                if (riga.isEmpty() || riga.startsWith("#")) continue; // Ignora righe vuote o commenti

                // Separatore ';' (o ',' a seconda del tuo file)
                String[] parti = riga.split(";");
                if (parti.length >= 2) {
                    String descrizione = parti[0].trim().toUpperCase();
                    String codiceCnd = parti[1].trim().toUpperCase();
                    catalogo.put(descrizione, codiceCnd);
                }
            }
            System.out.println("Catalogo CND caricato con successo: " + catalogo.size() + " voci inserite.");
        } catch (IOException e) {
            System.err.println("Errore durante la lettura del catalogo CND: " + e.getMessage());
        }

        return catalogo;
    }
}