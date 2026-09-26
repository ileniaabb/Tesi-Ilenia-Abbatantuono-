

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Legge il file CSV del Censimento Nazionale delle Grandi Apparecchiature
 * Biomedicali (Ministero della Salute) e lo converte in una lista di
 * oggetti {@link Dispositivo}.
 *
 * Il file usa il punto e virgola come delimitatore e la quotatura in
 * stile CSV per i campi che contengono caratteri speciali (es. la
 * colonna descrizione_cnd puo' contenere virgolette doppie per indicare
 * una virgoletta letterale, come in:
 * "GAMMA CAMERE ... CON ACQUISIZIONE ""TOTAL BODY"""). Per questo ogni
 * riga non viene divisa con un semplice split sul delimitatore, ma con
 * un piccolo tokenizzatore che rispetta le virgolette.
 *
 * La lettura avviene una riga alla volta: si legge, si tokenizza, si
 * costruisce il Dispositivo e lo si aggiunge alla lista, senza caricare
 * prima l'intero file in una singola stringa. Sul dataset attuale
 * (poco meno di 7.000 righe) la differenza di memoria fra le due
 * strategie sarebbe comunque trascurabile: si preferisce questo
 * approccio perche' e' il piu' diretto, non perche' l'altro fosse un
 * problema.
 *
 * Le righe che non possono essere interpretate (es. tipo_apparecchiatura
 * con sigla sconosciuta) vengono scartate e segnalate, non fanno fallire
 * l'intera importazione: e' una scelta deliberata, coerente con il fatto
 * che il dataset presenta gia' di suo alcune anomalie note.
 */
public class DispositivoCsvReader {

    private static final char DELIMITATORE = ';';

    private final List<String> erroriRiscontrati = new ArrayList<>();

    /**
     * Legge il file indicato e restituisce la lista dei dispositivi
     * correttamente interpretati. Eventuali righe scartate sono
     * disponibili dopo la chiamata tramite {@link #getErroriRiscontrati()}.
     */
    public List<Dispositivo> leggi(Path percorsoCsv) throws IOException {
        erroriRiscontrati.clear();
        List<Dispositivo> dispositivi = new ArrayList<>();

        try (BufferedReader lettore = Files.newBufferedReader(percorsoCsv, StandardCharsets.UTF_8)) {
        	// UTF8 Forza la codifica a UTF-8 per leggere correttamente accenti e caratteri speciali italiani.
            String rigaIntestazione = lettore.readLine();
            if (rigaIntestazione == null) {
                return dispositivi; // file vuoto
            }
            Map<String, Integer> indiceColonna = costruisciIndiceColonne(tokenizzaRiga(rigaIntestazione));
            /*poichè l'ordine delle colonne potrebbe cambiare, creiamo una mappa in cui il programma legge la rigaIntestazione 
             * e associa ad ogni colonna un numero*/

            String riga;
            int numeroRiga = 1;
            while ((riga = lettore.readLine()) != null) {
                numeroRiga++;

                if (riga.isBlank()) {
                    continue;
                }

                List<String> campi = tokenizzaRiga(riga);
                try {
                    dispositivi.add(costruisciDispositivo(campi, indiceColonna));
                } catch (RuntimeException e) {
                    // riga scartata ma segnalata, invece di interrompere l'importazione
                    erroriRiscontrati.add("Riga " + numeroRiga + " scartata: " + e.getMessage());
                }
            }
        }

        return dispositivi;
    }

    /**
     * Righe scartate durante l'ultima chiamata a {@link #leggi(Path)},
     * con il relativo motivo. Lista vuota se non ci sono state anomalie
     * di parsing.
     */
    public List<String> getErroriRiscontrati() {
        return erroriRiscontrati;
    }

    private Map<String, Integer> costruisciIndiceColonne(List<String> intestazione) {
        Map<String, Integer> indice = new HashMap<>();
        for (int i = 0; i < intestazione.size(); i++) {
            // trim indispensabile: la colonna "azienda_sanitaria_ASL" nel
            // file originale ha uno spazio finale nell'intestazione
            indice.put(intestazione.get(i).trim(), i);
        }
        return indice;
    }

    private Dispositivo costruisciDispositivo(List<String> campi, Map<String, Integer> indiceColonna) {
        String siglaTipo = valore(campi, indiceColonna, "tipo_apparecchiatura");
        TipoApparecchiatura tipo = TipoApparecchiatura.daSigla(siglaTipo);

        return new Dispositivo(
                valore(campi, indiceColonna, "codice_regione"),
                valore(campi, indiceColonna, "regione"),
                valore(campi, indiceColonna, "codice_azienda_sanitaria_asl"),
                valore(campi, indiceColonna, "azienda_sanitaria_ASL"),
                valore(campi, indiceColonna, "codice_struttura"),
                valore(campi, indiceColonna, "denominazione_struttura"),
                valore(campi, indiceColonna, "indirizzo_struttura"),
                parseDouble(valore(campi, indiceColonna, "latitudine")),
                parseDouble(valore(campi, indiceColonna, "longitudine")),
                tipo,
                valore(campi, indiceColonna, "cod_classificazione_cnd"),
                valore(campi, indiceColonna, "descrizione_cnd"),
                parseInteger(valore(campi, indiceColonna, "num_apparecchiature")),
                parseInteger(valore(campi, indiceColonna, "num_app_disponibili")),
                valore(campi, indiceColonna, "caratteristiche")
        );
    }

    private String valore(List<String> campi, Map<String, Integer> indiceColonna, String nomeColonna) {
        Integer indice = indiceColonna.get(nomeColonna);
        if (indice == null || indice >= campi.size()) {
            return null;
        }
        String valoreGrezzo = campi.get(indice).trim();
        return valoreGrezzo.isEmpty() ? null : valoreGrezzo;
    }

    private Double parseDouble(String valore) {
        if (valore == null) {
            return null;
        }
        try {
            return Double.parseDouble(valore.replace(',', '.'));// in italia siamo abituati a scrivere i decimali dopo la virgola, in java i decimali vanno dopo il punto
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String valore) {
        if (valore == null) {
            return null;
        }
        try {
            return Integer.parseInt(valore.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Tokenizza una singola riga del CSV in campi, rispettando la
     * quotatura: un campo tra virgolette puo' contenere il delimitatore,
     * e una coppia di virgolette consecutive dentro un campo quotato
     * rappresenta una virgoletta letterale.
     *
     * Non gestisce il caso di un campo con un a-capo al suo interno:
     * su questo dataset non si presenta (verificato: il numero di righe
     * fisiche del file corrisponde esattamente al numero di record), e
     * BufferedReader.readLine() tratta comunque ogni a-capo come fine
     * riga a prescindere dal contesto.
     */
    private List<String> tokenizzaRiga(String riga) {
        List<String> campi = new ArrayList<>();
        StringBuilder campoCorrente = new StringBuilder();
        boolean dentroVirgolette = false;

        int lunghezza = riga.length();
        int i = 0;

        while (i < lunghezza) {
            char carattere = riga.charAt(i);

            if (dentroVirgolette) {
                if (carattere == '"') {
                    boolean prossimaEVirgoletta = (i + 1 < lunghezza) && riga.charAt(i + 1) == '"';
                    if (prossimaEVirgoletta) {
                        campoCorrente.append('"');
                        i += 2;
                    } else {
                        dentroVirgolette = false;
                        i++;
                    }
                } else {
                    campoCorrente.append(carattere);
                    i++;
                }
                continue;
            }

            if (carattere == '"') {
                dentroVirgolette = true;
                i++;
            } else if (carattere == DELIMITATORE) {
                campi.add(campoCorrente.toString());
                campoCorrente.setLength(0);
                i++;
            } else {
                campoCorrente.append(carattere);
                i++;
            }
        }

        campi.add(campoCorrente.toString());
        return campi;
    }
}
