
import java.util.Objects;

/**
 * Rappresenta una riga del dataset ministeriale sulle grandi
 * apparecchiature sanitarie (Censimento Nazionale delle Grandi
 * Apparecchiature Biomedicali).
 *
 * I nomi dei campi seguono le colonne del CSV originale:
 * codice_regione, regione, codice_azienda_sanitaria_asl,
 * azienda_sanitaria_ASL, codice_struttura, denominazione_struttura,
 * indirizzo_struttura, latitudine, longitudine, tipo_apparecchiatura,
 * cod_classificazione_cnd, descrizione_cnd, num_apparecchiature,
 * num_app_disponibili, caratteristiche.
 */
public class Dispositivo {

    /**
     * I tre stati possibili del campo "caratteristiche", da usare al
     * posto di un semplice controllo su null/stringa vuota: distingue
     * un'assenza strutturale (non applicabile a questa tipologia) da
     * un'assenza anomala (dato che ci si aspettava e manca), evitando
     * che la pipeline di pulizia tratti allo stesso modo due situazioni
     * di natura completamente diversa.
     */
    public enum StatoCaratteristica {
        PRESENTE,
        NON_APPLICABILE,
        MANCANTE;
    }

    private final String codiceRegione;
    private final String regione;
    private final String codiceAziendaSanitariaAsl;
    private final String aziendaSanitariaAsl;
    private final String codiceStruttura;
    private final String denominazioneStruttura;
    private final String indirizzoStruttura;
    private final Double latitudine;
    private final Double longitudine;
    private final TipoApparecchiatura tipoApparecchiatura;
    private final String codClassificazioneCnd;
    private final String descrizioneCnd;
    private final Integer numApparecchiature;
    private final Integer numAppDisponibili;
    private final String caratteristiche;

    public Dispositivo(String codiceRegione,
                        String regione,
                        String codiceAziendaSanitariaAsl,
                        String aziendaSanitariaAsl,
                        String codiceStruttura,
                        String denominazioneStruttura,
                        String indirizzoStruttura,
                        Double latitudine,
                        Double longitudine,
                        TipoApparecchiatura tipoApparecchiatura,
                        String codClassificazioneCnd,
                        String descrizioneCnd,
                        Integer numApparecchiature,
                        Integer numAppDisponibili,
                        String caratteristiche) {
        this.codiceRegione = codiceRegione;
        this.regione = regione;
        this.codiceAziendaSanitariaAsl = codiceAziendaSanitariaAsl;
        this.aziendaSanitariaAsl = aziendaSanitariaAsl;
        this.codiceStruttura = codiceStruttura;
        this.denominazioneStruttura = denominazioneStruttura;
        this.indirizzoStruttura = indirizzoStruttura;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.tipoApparecchiatura = Objects.requireNonNull(tipoApparecchiatura,
                "tipoApparecchiatura non puo' essere nullo");
        this.codClassificazioneCnd = codClassificazioneCnd;
        this.descrizioneCnd = descrizioneCnd;
        this.numApparecchiature = numApparecchiature;
        this.numAppDisponibili = numAppDisponibili;
        // una stringa vuota dopo il trim viene normalizzata a null,
        // cosi' il controllo di presenza piu' sotto resta un semplice
        // != null, senza dover ripetere ovunque il controllo su blank
        this.caratteristiche = (caratteristiche == null || caratteristiche.trim().isEmpty())
                ? null
                : caratteristiche.trim(); /* controlla se caratteristiche è vuoto, oppure se togliendo gli spazi rimane un campo 
                vuoto:"", se almeno una di queste due condizioni è vera, l'intera condizione è vera: this.car..=null
                altrimenti this.caratteristiche= caratteristiche.trim().*/
    }

    /**
     * Calcola lo stato del campo "caratteristiche" applicando la regola
     * di dominio definita in {@link TipoApparecchiatura#isCaratteristicaApplicabile()},
     * invece di limitarsi a un controllo di nullita'.
     */
    public StatoCaratteristica statoCaratteristica() {
        boolean applicabile = tipoApparecchiatura.isCaratteristicaApplicabile();
        boolean valorizzato = caratteristiche != null;

        if (!applicabile) {
            // per questa tipologia il campo non ha significato: il
            // valore vuoto e' corretto per costruzione, non e' un'anomalia
            return StatoCaratteristica.NON_APPLICABILE;
        }
        return valorizzato ? StatoCaratteristica.PRESENTE : StatoCaratteristica.MANCANTE;
    }

    /**
     * Coerenza logica tra apparecchiature totali e disponibili: un
     * valore di disponibili superiore al totale e' un'anomalia da
     * segnalare in fase di validazione (vedi verifica sul dataset:
     * 199 record con questa incongruenza sull'estrazione del 15/07/2026).
     */
    public boolean haIncongruenzaNumerica() {
        if (numApparecchiature == null || numAppDisponibili == null) {
            return true;
        }
        else return numAppDisponibili > numApparecchiature;// se è vero restituisce true
    }

    public boolean haCoordinateMancanti() {
        return latitudine == null || longitudine == null;
    }

    // --- getters ---

    public String getCodiceRegione() {
        return codiceRegione;
    }

    public String getRegione() {
        return regione;
    }

    public String getCodiceAziendaSanitariaAsl() {
        return codiceAziendaSanitariaAsl;
    }

    public String getAziendaSanitariaAsl() {
        return aziendaSanitariaAsl;
    }

    public String getCodiceStruttura() {
        return codiceStruttura;
    }

    public String getDenominazioneStruttura() {
        return denominazioneStruttura;
    }

    public String getIndirizzoStruttura() {
        return indirizzoStruttura;
    }

    public Double getLatitudine() {
        return latitudine;
    }

    public Double getLongitudine() {
        return longitudine;
    }

    public TipoApparecchiatura getTipoApparecchiatura() {
        return tipoApparecchiatura;
    }

    public String getCodClassificazioneCnd() {
        return codClassificazioneCnd;
    }

    public String getDescrizioneCnd() {
        return descrizioneCnd;
    }

    public Integer getNumApparecchiature() {
        return numApparecchiature;
    }

    public Integer getNumAppDisponibili() {
        return numAppDisponibili;
    }

    public String getCaratteristiche() {
        return caratteristiche;
    }

    @Override
    public String toString() {
        return "Dispositivo{" +
                "regione='" + regione + '\'' +
                ", struttura='" + denominazioneStruttura + '\'' +
                ", tipo=" + tipoApparecchiatura +
                ", numApparecchiature=" + numApparecchiature +
                ", numAppDisponibili=" + numAppDisponibili +
                ", statoCaratteristica=" + statoCaratteristica() +
                '}';
    }
}