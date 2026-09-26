/**
 * Tipologie di grandi apparecchiature sanitarie censite nel dataset
 * ministeriale (fonte: Ministero della Salute, Censimento Nazionale
 * delle Grandi Apparecchiature Biomedicali).
 *
 * Ogni costante riporta anche la sigla utilizzata nel file CSV originale
 * nella colonna "tipo_apparecchiatura".
 */
public enum TipoApparecchiatura {

    MMI("Mammografo"),
    TAC("Tomografo Assiale Computerizzato"),
    RMN("Risonanza Magnetica Nucleare"),
    ANG("Angiografo"),
    ACC("Acceleratore Lineare"),
    GCC("Gamma Camera"),
    ROB("Robot Chirurgico"),
    PET("Tomografo ad Emissione di Positroni"),
    GTT("Gamma Knife / Tomoterapia");

    private final String descrizioneEstesa;

    TipoApparecchiatura(String descrizioneEstesa) {
        this.descrizioneEstesa = descrizioneEstesa;
    }

    public String getDescrizioneEstesa() {
        return descrizioneEstesa;
    }

    /**
     * Indica se, per questa tipologia di apparecchiatura, il campo
     * "caratteristiche" del dataset ha un significato applicabile.
     *
     * Dall'analisi del dataset (estrazione del 15/07/2026) risulta che
     * il campo è valorizzato esclusivamente per TAC (tavolo portapaziente
     * bariatrico) e RMN (tavolo portapaziente per HIFU), mentre per tutte
     * le altre tipologie non esiste un equivalente e il campo è sempre
     * vuoto per costruzione, non per errore.
     *
     * Questo metodo centralizza la regola di dominio, cosi' la logica di
     * validazione non deve mai dedurre l'applicabilita' dal solo fatto
     * che il campo sia vuoto o meno.
     */
    public boolean isCaratteristicaApplicabile() {
        return this == TAC || this == RMN; /*restituisce true solo se la caratteristica è TAC o RMN*/
    }

    /**
     * Converte la sigla testuale letta dal CSV (es. "TAC", "RMN") nella
     * costante enum corrispondente.
     *
     * @throws IllegalArgumentException se la sigla non corrisponde a
     *         nessuna tipologia nota: utile in fase di parsing per
     *         intercettare valori imprevisti nel dataset.
     */
    public static TipoApparecchiatura daSigla(String sigla) {
        if (sigla == null) {
            throw new IllegalArgumentException("Sigla tipo apparecchiatura nulla");
        }
        try {
            return TipoApparecchiatura.valueOf(sigla.trim().toUpperCase());// converte tutti i caratteri in  maiuscole
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Sigla tipo apparecchiatura non riconosciuta: '" + sigla + "'", e); /*enum ha un metodo statico valueOf che permette di confrontare
            il valore passato al metodo con l'array nascosto di enum dichiarati*/
        }
    }
}
