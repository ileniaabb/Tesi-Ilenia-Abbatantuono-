import java.io.IOException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestratore per il processo di lettura, bonifica, deduplicazione e scrittura del dataset.
 * Utilizza il pattern Strategy per delegare l'algoritmo di deduplicazione/aggregazione.
 */
public class PulitoreDataset {

    private final DispositivoCsvReader reader;
    private final DispositivoCsvWriter writer;
    private final normalizzazioni normalizzazioni;
   
    private final ICorrettoreCnd icorrettoreCnd;
	private strategiaDeduplica strategiaDeduplica;


    public PulitoreDataset(strategiaDeduplica strategiaDeduplica, normalizzazioni normalizzazioni, ICorrettoreCnd icorrettoreCnd) {
        this.reader = new DispositivoCsvReader();
        this.writer = new DispositivoCsvWriter();
        this.strategiaDeduplica = strategiaDeduplica;
		this.normalizzazioni = normalizzazioni;
		this.icorrettoreCnd= icorrettoreCnd;
        
    }
 // Costruttore con servizi di default
    public PulitoreDataset(strategiaDeduplica strategiaDeduplica) {
        this(strategiaDeduplica, 
             new StandardizzatoreTestoImpl(), 
             new CorrettoreCndImpl());
    }

    public void setStrategiaDeduplica(strategiaDeduplica strategiaDeduplica) {
        this.strategiaDeduplica = strategiaDeduplica;
    }

    /**
     * Esegue l'intera pipeline: Lettura -> Bonifica -> Aggregazione (Strategy) -> Scrittura.
     *
     * @param percorsoInput  Path del CSV originale
     * @param percorsoOutput Path del CSV pulito
     * @return Numero di record scritti nel file finale
     * @throws IOException In caso di problemi di I/O sui file
     */
    public int convertiInFilePulito(Path percorsoInput, Path percorsoOutput) throws IOException {
        List<Dispositivo> dispositiviGrezzi = reader.leggi(percorsoInput);

        List<Dispositivo> dispositiviBonificati = new ArrayList<>();
        for (Dispositivo d : dispositiviGrezzi) {
            dispositiviBonificati.add(bonificaDispositivo(d));
        }

        List<Dispositivo> dispositiviPuliti = strategiaDeduplica.aggrega(dispositiviBonificati);

        writer.scrivi(dispositiviPuliti, percorsoOutput);

        return dispositiviPuliti.size();
    }

    private Dispositivo bonificaDispositivo(Dispositivo d) {
        // 1. Pulizia testo e caratteri accentati
        String regione = normalizzazioni.correggiCaratteriAccentati(normalizzazioni.pulisciTesto(d.getRegione()));
        String asl = normalizzazioni.correggiCaratteriAccentati(normalizzazioni.pulisciTesto(d.getAziendaSanitariaAsl()));
        String struttura = normalizzazioni.correggiCaratteriAccentati(normalizzazioni.pulisciTesto(d.getDenominazioneStruttura()));
        String indirizzo = normalizzazioni.pulisciIndirizzo(d.getIndirizzoStruttura());

        // 2. Correzione CND
        String codCndCorretto = icorrettoreCnd.correggiCodice(
                d.getCodClassificazioneCnd(),
                d.getTipoApparecchiatura(),
                d.getDescrizioneCnd());

        // 3. Descrizione Canonica CND
        String descCnd = icorrettoreCnd.ottieniDescrizioneCanonica(
                codCndCorretto,
                normalizzazioni.pulisciTesto(d.getDescrizioneCnd()));

        

        // 5. Incongruenze numeriche senza creare un metodo
        Integer numTot = d.getNumApparecchiature();
        Integer numDisp = d.getNumAppDisponibili();
        if (numTot == null || numTot < 0) numTot = 0;
        if (numDisp == null || numDisp < 0) numDisp = 0;
        if (numDisp > numTot) numDisp = numTot;

        // 6. Caratteristiche
        String caratt = d.getCaratteristiche();
        if (!d.getTipoApparecchiatura().isCaratteristicaApplicabile()) {
            caratt = null;
        } else {
            caratt = normalizzazioni.pulisciTesto(caratt);
        }

        return new Dispositivo(
                normalizzazioni.pulisciCodice(d.getCodiceRegione()),
                regione,
                normalizzazioni.pulisciCodice(d.getCodiceAziendaSanitariaAsl()),
                asl,
                normalizzazioni.pulisciCodice(d.getCodiceStruttura()),
                struttura,
                indirizzo,
               d.getLatitudine(),
               d.getLongitudine(),
                d.getTipoApparecchiatura(),
                codCndCorretto,
                descCnd,
                numTot,
                numDisp,
                caratt );
    }
}

    

  

