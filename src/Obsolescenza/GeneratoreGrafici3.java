package Obsolescenza;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class GeneratoreGrafici3 {

    public static void main(String[] args) {
        Path filePulitoPath = Paths.get("DISPO_GAP_PULITO.csv");

        System.out.println(" AVVIO SISTEMA ANALISI FASCIA TECNOLOGICA CND ");

        Map<String, Map<String, Integer>> macchinariPerRegioneECnd = new HashMap<>();

        // Lettura del file CSV dei dispositivi
        try (BufferedReader br = new BufferedReader(new FileReader(filePulitoPath.toFile()))) {
            String riga = br.readLine(); // Scarta la riga di intestazione
            if (riga == null) {
                System.err.println("ERRORE: Il file risulta vuoto.");
                return;
            }

            while ((riga = br.readLine()) != null) {
                riga = riga.replace("\uFEFF", "").replace("\"", "").trim();
                if (riga.isEmpty()) {
                    continue;
                }

                String[] campi = riga.split(";", -1);

                if (campi.length >= 13) {
                    String regione = campi[1].trim().toUpperCase();
                    String codiceCnd = campi[10].trim().toUpperCase();
                    int quantita = parseQuantita(campi[12]);

                    if (!regione.isEmpty() && !codiceCnd.isEmpty() && quantita > 0) {
                        macchinariPerRegioneECnd
                                .computeIfAbsent(regione, k -> new HashMap<>())
                                .put(codiceCnd, macchinariPerRegioneECnd.get(regione).getOrDefault(codiceCnd, 0) + quantita);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Errore durante la lettura del file apparecchiature: " + e.getMessage());
            return;
        }

        // Esecuzione dell'analisi e generazione del grafico
        StrategiaAnalisi strategia = new AnalisiFasciaTecnologica();
        strategia.eseguiAnalisiEGrafico(macchinariPerRegioneECnd);
    }

    private static int parseQuantita(String val) {
        try {
            return Integer.parseInt(val.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}