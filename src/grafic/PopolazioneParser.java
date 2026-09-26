package grafic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PopolazioneParser {

    public static Map<String, Long> caricaESommaPopolazione(Path path) {
        Map<String, Long> popMap = new HashMap<>();

        if (!path.toFile().exists()) {
            System.err.println("ERRORE: File popolazione non trovato in " + path.toAbsolutePath());
            return popMap;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String riga;
            while ((riga = br.readLine()) != null) {
                // Rimuove il carattere invisibile UTF-8 BOM e le virgolette
                riga = riga.replace("\uFEFF", "").replace("\"", "").trim();
                if (riga.isEmpty()) continue;

                String sep = riga.contains(";") ? ";" : ",";
                String[] parti = riga.split(sep);

                if (parti.length < 2) continue;

                // Cerchiamo la colonna col nome della regione e quella col numero di abitanti reale
                String nomeRegione = "";
                long popolazioneRilevata = 0;

                for (String campo : parti) {
                    campo = campo.trim();
                    
                    // Identifica la regione
                    if (nomeRegione.isEmpty()) {
                        String regNorm = normalizzaNomeRegione(campo);
                        if (!regNorm.isEmpty()) {
                            nomeRegione = regNorm;
                        }
                    }

                    // Identifica un valore numerico coerente con la popolazione di una regione (tra 100.000 e 15.000.000)
                    String soloCifre = campo.replaceAll("[^0-9]", "");
                    if (!soloCifre.isEmpty()) {
                        try {
                            long val = Long.parseLong(soloCifre);
                            // Un valore di popolazione regionale plausibile è compreso tra 100mila e 15milioni
                            if (val >= 100_000L && val <= 15_000_000L) {
                                popolazioneRilevata = val;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }

                // Se abbiamo trovato sia la regione sia un valore valido di popolazione
                if (!nomeRegione.isEmpty() && popolazioneRilevata > 0) {
                    // Se la regione è unica ma non era ancora memorizzata oppure ha un valore superiore (evita aggregati parziali)
                    if (popolazioneRilevata > popMap.getOrDefault(nomeRegione, 0L)) {
                        popMap.put(nomeRegione, popolazioneRilevata);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante il parsing del file popolazione: " + e.getMessage());
        }

        return popMap;
    }

    public static String normalizzaNomeRegione(String testo) {
        if (testo == null) return "";
        String reg = testo.toUpperCase()
                .replace("-", " ")
                .replace("VALLE D'AOSTE", "")
                .replace("SÜDTIROL", "")
                .replace(" / ", " ")
                .trim();

        if (reg.contains("PUGLIA")) return "PUGLIA";
        if (reg.contains("LOMBARDIA")) return "LOMBARDIA";
        if (reg.contains("LAZIO")) return "LAZIO";
        if (reg.contains("CAMPANIA")) return "CAMPANIA";
        if (reg.contains("SICILIA")) return "SICILIA";
        if (reg.contains("VENETO")) return "VENETO";
        if (reg.contains("EMILIA")) return "EMILIA ROMAGNA";
        if (reg.contains("PIEMONTE")) return "PIEMONTE";
        if (reg.contains("TOSCANA")) return "TOSCANA";
        if (reg.contains("CALABRIA")) return "CALABRIA";
        if (reg.contains("SARDEGNA")) return "SARDEGNA";
        if (reg.contains("LIGURIA")) return "LIGURIA";
        if (reg.contains("MARCHE")) return "MARCHE";
        if (reg.contains("ABRUZZO")) return "ABRUZZO";
        if (reg.contains("FRIULI")) return "FRIULI VENEZIA GIULIA";
        if (reg.contains("UMBRIA")) return "UMBRIA";
        if (reg.contains("BASILICATA")) return "BASILICATA";
        if (reg.contains("MOLISE")) return "MOLISE";
        if (reg.contains("AOSTA") || reg.contains("VALLE")) return "VALLE D'AOSTA";
        if (reg.contains("BOLZANO")) return "PROV. AUTON. BOLZANO";
        if (reg.contains("TRENTO")) return "PROV. AUTON. TRENTO";

        return "";
        }
}
