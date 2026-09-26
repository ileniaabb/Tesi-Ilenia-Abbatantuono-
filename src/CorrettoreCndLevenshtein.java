import java.util.HashMap;
import java.util.Map;

public class CorrettoreCndLevenshtein implements ICorrettoreCnd {

    // Soglia minima di similarità (85%)
    private static final double SOGLIA_SIMILARITA = 0.85;

    private static final Map<String, String> CATALOGO_DESC_CODICE = 
            CatalogoCndLoader.caricaCatalogoDaCsv("catalogo_cnd.csv");

    public static String cercaCodiceDaDescrizione(String descrizione) {
        if (descrizione == null) return null;
        return CATALOGO_DESC_CODICE.get(descrizione.trim().toUpperCase());
    }

    private final Map<String, String> catalogoCodiceToDesc = new HashMap<>();

    public CorrettoreCndLevenshtein() {
        for (Map.Entry<String, String> entry : CATALOGO_DESC_CODICE.entrySet()) {
            catalogoCodiceToDesc.put(entry.getValue(), entry.getKey());
        }
    }

    @Override
    public String correggiCodice(String codCndInput, TipoApparecchiatura tipo, String descrizioneInput) {
        if (codCndInput != null) {
            String cndPulito = codCndInput.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
            if (catalogoCodiceToDesc.containsKey(cndPulito)) {
                return cndPulito;
            }
        }

        String codiceRiallineato = riallineaCodiceCnd(descrizioneInput);
        if (codiceRiallineato != null) {
            return codiceRiallineato;
        }

        return codCndInput != null ? codCndInput.trim().toUpperCase() : null;
    }

    /**
     * Trova il codice CND basandosi sulla similarità Levenshtein.
     */
    public String riallineaCodiceCnd(String descrizioneGrezza) {
        if (descrizioneGrezza == null || descrizioneGrezza.isBlank()) {
            return null;
        }

        String migliorCodice = null;
        double massimaSimilarita = SOGLIA_SIMILARITA;

        for (Map.Entry<String, String> entry : CATALOGO_DESC_CODICE.entrySet()) {
            String descrizioneUfficiale = entry.getKey();
            
            // Usa il metodo della tua classe utility 
            double sim = SimilaritaLevenshtein.calcolaSimilarita(descrizioneGrezza, descrizioneUfficiale);

            if (sim > massimaSimilarita) {
                massimaSimilarita = sim;
                migliorCodice = entry.getValue();
            }
        }

        return migliorCodice;
    }

    @Override
    public String ottieniDescrizioneCanonica(String codCnd, String descrizioneFallback) {
        if (codCnd == null) {
            return descrizioneFallback;
        }
        return catalogoCodiceToDesc.getOrDefault(codCnd, descrizioneFallback);
    }
}