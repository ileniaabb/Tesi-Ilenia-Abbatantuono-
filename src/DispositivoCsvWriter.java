


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Classe responsabile dell'esportazione dei dispositivi su un nuovo file CSV.
 * Riceve i dati elaborati/puliti e li scrive mantenendo la struttura
 * e il formato del dataset ministeriale originale.
 */
public class DispositivoCsvWriter {

    private static final char DELIMITATORE = ';';
    private static final String INTESTAZIONE = 
            "codice_regione ;regione ;codice_azienda_sanitaria_asl ;azienda_sanitaria_ASL ;" +
            "codice_struttura ;denominazione_struttura ;indirizzo_struttura ;latitudine ;longitudine ;" +
            "tipo_apparecchiatura ;cod_classificazione_cnd ;descrizione_cnd ;num_apparecchiature ;" +
            "num_app_disponibili ;caratteristiche ";

    /**
     * Scrive la lista di dispositivi fornita nel file CSV specificato.
     *
     * @param dispositivi lista dei dispositivi da salvare (es. puliti/corretti)
     * @param percorsoDestinazione percorso del nuovo file CSV da creare
     * @throws IOException se si verificano errori durante la scrittura su disco
     */
    public void scrivi(List<Dispositivo> dispositivi, Path percorsoDestinazione) throws IOException {
        // Crea la cartella padre se non esiste (es. cartella "dati_puliti")
        if (percorsoDestinazione.getParent() != null) {
            Files.createDirectories(percorsoDestinazione.getParent());
        }

        try (BufferedWriter scrittore = Files.newBufferedWriter(percorsoDestinazione, StandardCharsets.UTF_8)) {
            // Write riga di intestazione
            scrittore.write(INTESTAZIONE);
            scrittore.newLine();

            // Write ogni record
            for (Dispositivo d : dispositivi) {
                scrittore.write(convertiInRigaCsv(d));
                scrittore.newLine();
            }
        }
    }

    /**
     * Trasforma un oggetto Dispositivo in una riga formattata in CSV.
     */
    private String convertiInRigaCsv(Dispositivo d) {
        StringBuilder riga = new StringBuilder();

        appendCampo(riga, d.getCodiceRegione());
        appendCampo(riga, d.getRegione());
        appendCampo(riga, d.getCodiceAziendaSanitariaAsl());
        appendCampo(riga, d.getAziendaSanitariaAsl());
        appendCampo(riga, d.getCodiceStruttura());
        appendCampo(riga, d.getDenominazioneStruttura());
        appendCampo(riga, d.getIndirizzoStruttura());
        appendCampo(riga, d.getLatitudine() != null ? d.getLatitudine().toString() : "");
        appendCampo(riga, d.getLongitudine() != null ? d.getLongitudine().toString() : "");
        
        // Sigla enum (es. TAC, RMN, MMI)
        appendCampo(riga, d.getTipoApparecchiatura() != null ? d.getTipoApparecchiatura().name() : "");
        
        appendCampo(riga, d.getCodClassificazioneCnd());
        appendCampo(riga, d.getDescrizioneCnd());
        appendCampo(riga, d.getNumApparecchiature() != null ? d.getNumApparecchiature().toString() : "");
        
        // Se c'era l'incongruenza (disponibili > totale), qui applichiamo la regola di pulizia
        // salvando direttamente il numero totale come numero disponibile
        Integer disponibiliPuliti = d.haIncongruenzaNumerica() ? d.getNumApparecchiature() : d.getNumAppDisponibili();
        appendCampo(riga, disponibiliPuliti != null ? disponibiliPuliti.toString() : "");
        
        // Campo caratteristiche (ultimo campo, non serve aggiungere il delimitatore dopo)
        riga.append(gestisciVirgolette(d.getCaratteristiche()));

        return riga.toString();
    }

    private void appendCampo(StringBuilder sb, String valore) {
        sb.append(gestisciVirgolette(valore)).append(DELIMITATORE);
    }

    /**
     * Se il testo contiene punti e virgola o virgolette (es. la descrizione CND "TOTAL BODY"),
     * lo racchiude tra virgolette doppie escapando quelle interne, mantenendo
     * lo stesso comportamento del parser DispositivoCsvReader.
     */
    private String gestisciVirgolette(String valore) {
        if (valore == null) {
            return "";
        }
        if (valore.contains(String.valueOf(DELIMITATORE)) || valore.contains("\"")) {
            return "\"" + valore.replace("\"", "\"\"") + "\"";
        }
        return valore;
    }
}