

package Obsolescenza;

import java.util.Map;

public interface StrategiaAnalisi {
    /**
     * Esegue l'analisi sui dati aggregati e genera il grafico PNG.
     * 
     * @param macchinariPerRegione Mappa: Regione -> (Codice CND -> Quantità)
     */
    void eseguiAnalisiEGrafico(Map<String, Map<String, Integer>> macchinariPerRegione);
    
    /**
     * Restituisce il nome identificativo della strategia.
     */
    String getNomeStrategia();
}