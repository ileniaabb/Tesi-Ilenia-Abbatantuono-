package grafic;

import java.util.HashMap;
import java.util.Map;

import java.util.HashMap;
import java.util.Map;

public class CalcoloTassoPer100k implements Strategy {

    @Override
    public Map<String, Double> calcola(Map<String, Integer> macchinari, Map<String, Long> popolazione) {
        Map<String, Double> risultato = new HashMap<>();

        for (Map.Entry<String, Integer> entry : macchinari.entrySet()) {
            String regione = entry.getKey();
            int numMacchinari = entry.getValue();
            long pop = popolazione.getOrDefault(regione, 0L);

            if (pop > 0) {
                double tassoPer100k = ((double) numMacchinari / pop) * 100000.0;
                risultato.put(regione, tassoPer100k);
            } else {
                risultato.put(regione, 0.0);
            }
        }
        return risultato;
    }
}