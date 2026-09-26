
package Obsolescenza;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PipelinePugliaHighTech {

    public enum Fascia {
        ALTA,
        MEDIA,
        BASSA,
        NON_DEFINITA
    }

    public static void main(String[] args) {
        Path inputFile = Paths.get("DISPO_GAP_PULITO.csv");
        Path outputFile = Paths.get("PUGLIA_HIGH_TECH.csv");
        String absolutePathOutputFile = outputFile.toAbsolutePath().toString();

        Set<String> cndQualificati = caricaAnagraficaCndPuglia();
        Map<String, Fascia> mappaturaFasce = inizializzaMappaturaFasce();

        Map<String, Integer> totalePerProvinciaAlta = new HashMap<>();
        Map<String, Integer> totalePerProvinciaMedia = new HashMap<>();
        Map<String, Integer> totalePerProvinciaBassa = new HashMap<>();
        int totaleRegionaleAlta = 0;
        int totaleRegionaleMedia = 0;
        int totaleRegionaleBassa = 0;

        System.out.println("AVVIO PIPELINE ELABORAZIONE PUGLIA CON CLASSIFICAZIONE PUNTUALE");

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile.toFile()));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile.toFile()))) {

            String headerLine = br.readLine();
            if (headerLine == null) {
                System.err.println("ERRORE: File di input vuoto.");
                return;
            }

            headerLine = headerLine.replace("\uFEFF", "").replace("\"", "").trim();
            String[] headers = headerLine.split(";", -1);

            Map<String, Integer> colMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                colMap.put(headers[i].trim().toLowerCase(), i);
            }

            int idxRegione = colMap.getOrDefault("regione", 1);
            int idxAsl = colMap.getOrDefault("azienda_sanitaria_asl", 3);
            int idxStruttura = colMap.getOrDefault("denominazione_struttura", 5);
            int idxIndirizzo = colMap.getOrDefault("indirizzo_struttura", 6);
            int idxLat = colMap.getOrDefault("latitudine", 7);
            int idxLng = colMap.getOrDefault("longitudine", 8);
            int idxTipologia = colMap.getOrDefault("tipo_apparecchiatura", 9);
            int idxCnd = colMap.getOrDefault("cod_classificazione_cnd", 10);

            bw.write("Tipologia;Struttura;Fascia;Provincia;Latitudine;Longitudine");
            bw.newLine();

            String riga;
            Pattern provPattern = Pattern.compile("\\((BA|BR|BT|FG|LE|TA)\\)", Pattern.CASE_INSENSITIVE);
            Pattern aslPattern = Pattern.compile("\\b(BA|BR|BT|FG|LE|TA)\\b", Pattern.CASE_INSENSITIVE);

            while ((riga = br.readLine()) != null) {
                riga = riga.replace("\uFEFF", "").replace("\"", "").trim();
                if (riga.isEmpty()) continue;

                String[] campi = riga.split(";", -1);

                String regione = estraiCampo(campi, idxRegione).toUpperCase();
                if (!regione.contains("PUGLIA")) continue;

                String codiceCnd = estraiCampo(campi, idxCnd).toUpperCase();
                if (!isTipologiaTarget(codiceCnd, cndQualificati)) continue;

                String tipologia = estraiCampo(campi, idxTipologia);
                String struttura = estraiCampo(campi, idxStruttura);
                String indirizzo = estraiCampo(campi, idxIndirizzo);
                String asl = estraiCampo(campi, idxAsl);
                String lat = estraiCampo(campi, idxLat);
                String lng = estraiCampo(campi, idxLng);

                String provincia = "ALTRO/ND";
                Matcher mIndirizzo = provPattern.matcher(indirizzo);
                if (mIndirizzo.find()) {
                    provincia = mIndirizzo.group(1).toUpperCase();
                } else {
                    Matcher mAsl = aslPattern.matcher(asl);
                    if (mAsl.find()) {
                        provincia = mAsl.group(1).toUpperCase();
                    }
                }

                Fascia fascia = mappaturaFasce.getOrDefault(codiceCnd, Fascia.NON_DEFINITA);
                if (fascia == Fascia.NON_DEFINITA) continue; 

                if (fascia == Fascia.ALTA) {
                    totaleRegionaleAlta++;
                    totalePerProvinciaAlta.put(provincia, totalePerProvinciaAlta.getOrDefault(provincia, 0) + 1);
                } else if (fascia == Fascia.MEDIA) {
                    totaleRegionaleMedia++;
                    totalePerProvinciaMedia.put(provincia, totalePerProvinciaMedia.getOrDefault(provincia, 0) + 1);
                } else if (fascia == Fascia.BASSA) {
                    totaleRegionaleBassa++;
                    totalePerProvinciaBassa.put(provincia, totalePerProvinciaBassa.getOrDefault(provincia, 0) + 1);
                }

                bw.write(String.format("%s;%s;%s;%s;%s;%s",
                        tipologia, struttura, fascia.name(), provincia, lat, lng));
                bw.newLine();
            }

            System.out.println("\n[FILE CREATO CON SUCCESSO]");
            System.out.println("PERCORSO ASSOLUTO (Absolute Path): " + absolutePathOutputFile + "\n");

            stampaReport(totaleRegionaleAlta, totalePerProvinciaAlta, totaleRegionaleMedia, totalePerProvinciaMedia, totaleRegionaleBassa, totalePerProvinciaBassa);

        } catch (IOException e) {
            System.err.println("Errore durante l'elaborazione I/O: " + e.getMessage());
        }
    }

    private static String estraiCampo(String[] campi, int index) {
        if (index >= 0 && index < campi.length) {
            return campi[index].trim();
        }
        return "";
    }

    private static Set<String> caricaAnagraficaCndPuglia() {
        Set<String> set = new HashSet<>();
        set.add("Z110306"); // TAC
        set.add("Z110501"); // RMN
        set.add("Z110101"); // Acceleratori Lineari
        set.add("Z1102");   // Medicina Nucleare / PET
        return set;
    }

    private static boolean isTipologiaTarget(String codiceCnd, Set<String> cndQualificati) {
        for (String prefisso : cndQualificati) {
            if (codiceCnd.startsWith(prefisso)) return true;
        }
        return false;
    }

    private static Map<String, Fascia> inizializzaMappaturaFasce() {
        Map<String, Fascia> mappa = new HashMap<>();

        // TOMOGRAFI COMPUTERIZZATI (TAC - Z110306)
        mappa.put("Z11030601", Fascia.BASSA); // <= 2 Strati
        mappa.put("Z11030602", Fascia.MEDIA); // > 2 e < 16 Strati
        mappa.put("Z11030603", Fascia.MEDIA); // >= 16 e < 64 Strati
        mappa.put("Z11030604", Fascia.ALTA);  // >= 64 Strati
        mappa.put("Z11030605", Fascia.ALTA);  // >= 64 e < 128 Strati
        mappa.put("Z11030606", Fascia.ALTA);  // >= 128 e < 256 Strati
        mappa.put("Z11030607", Fascia.ALTA);  // >= 256 Strati

        // RISONANZE MAGNETICHE (RM - Z110501)
        mappa.put("Z11050101", Fascia.BASSA); // Settoriali / Estremità
        mappa.put("Z11050102", Fascia.BASSA); // Magnete aperto <= 0.5T
        mappa.put("Z11050103", Fascia.MEDIA); // Magnete aperto > 0.5T
        mappa.put("Z11050104", Fascia.BASSA); // Magnete chiuso <= 0.5T o <= 2T
        mappa.put("Z11050105", Fascia.ALTA);  // Magnete chiuso 1.5T - 3T / 2T - 4T
        mappa.put("Z11050106", Fascia.ALTA);  // > 3T o > 4T

     // ACCELERATORI LINEARI (Z110101)
        mappa.put("Z11010101", Fascia.BASSA); // Energia singola (tecnologia superata/limitata per oncologia complessa)
        mappa.put("Z11010102", Fascia.MEDIA); // Energia media e multipla
        mappa.put("Z11010103", Fascia.ALTA);  // Energia alta e multipla (massima precisione radioterapica)
        mappa.put("Z11010104", Fascia.ALTA);  // Intraoperatori (IORT, altissima specializzazione)
        
     // MEDICINA NUCLEARE E PET (Z1102)
        mappa.put("Z11020101", Fascia.BASSA); // Gamma camere mobili
        mappa.put("Z11020102", Fascia.BASSA); // Gamma camere fisse singola testata - senza Total Body
        mappa.put("Z11020103", Fascia.MEDIA); // Gamma camere fisse singola testata - con Total Body
        mappa.put("Z11020104", Fascia.MEDIA); // Gamma camere fisse testata multipla - senza Total Body
        mappa.put("Z11020105", Fascia.ALTA);  // Gamma camere fisse testata multipla - con Total Body
        mappa.put("Z11020201", Fascia.ALTA);  // Sistemi TC / Gamma Camera
        mappa.put("Z11020301", Fascia.ALTA);  // Sistemi TC / PET (Massima specializzazione metabolica)
        
        return mappa;
    }

    private static void stampaReport(int tAlta, Map<String, Integer> pAlta, int tMed, Map<String, Integer> pMed, int tBas, Map<String, Integer> pBas) {
        System.out.println("   REPORT APPARECCHIATURE PUGLIA PER FASCIA     ");
        System.out.printf("TOTALE REGIONALE ALTA:   %d\n", tAlta);
        System.out.printf("TOTALE REGIONALE MEDIA:  %d\n", tMed);
        System.out.printf("TOTALE REGIONALE BASSA:  %d\n", tBas);
        System.out.println("-------------------------------------------------");
        System.out.println("Dettaglio FASCIA ALTA per Provincia:");
        pAlta.forEach((prov, conta) -> System.out.printf(" - %s: %3d\n", prov, conta));
        System.out.println("Dettaglio FASCIA MEDIA per Provincia:");
        pMed.forEach((prov, conta) -> System.out.printf(" - %s: %3d\n", prov, conta));
        System.out.println("Dettaglio FASCIA BASSA per Provincia:");
        pBas.forEach((prov, conta) -> System.out.printf(" - %s: %3d\n", prov, conta));
        
    }
}