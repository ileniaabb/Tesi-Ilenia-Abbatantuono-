package grafic;

import java.util.HashMap;
import java.util.Map;

public class CalcoloMediaNazionale implements Strategy {

    @Override
    public Map<String, Double> calcola(Map<String, Integer> macchinari, Map<String, Long> popolazione) {
        Map<String, Double> risultato = new HashMap<>();

        long sommaMacchinariTotale = 0;
        long sommaMacchinariSenzaPuglia = 0;
        int numRegioniTotale = macchinari.size();
        int numRegioniSenzaPuglia = 0;

        long popTotaleItalia = 0;
        long popTotaleSenzaPuglia = 0;
        long macchTotaleValidi = 0;
        long macchSenzaPugliaValidi = 0;

        for (Map.Entry<String, Integer> entry : macchinari.entrySet()) {
            String regione = entry.getKey();
            int macch = entry.getValue();

            sommaMacchinariTotale += macch;

            if (!regione.contains("PUGLIA")) {
                sommaMacchinariSenzaPuglia += macch;
                numRegioniSenzaPuglia++;
            }

            long pop = popolazione.getOrDefault(regione, 0L);
            if (pop > 0) {
                popTotaleItalia += pop;
                macchTotaleValidi += macch;

                if (!regione.contains("PUGLIA")) {
                    popTotaleSenzaPuglia += pop;
                    macchSenzaPugliaValidi += macch;
                }
            }
        }

        double mediaNonPesataTotale = numRegioniTotale > 0 ? (double) sommaMacchinariTotale / numRegioniTotale : 0.0;
        double mediaNonPesataSenzaPuglia = numRegioniSenzaPuglia > 0 ? (double) sommaMacchinariSenzaPuglia / numRegioniSenzaPuglia : 0.0;

        double mediaPesataTotale100k = popTotaleItalia > 0 ? ((double) macchTotaleValidi / popTotaleItalia) * 100000.0 : 0.0;
        double mediaPesataSenzaPuglia100k = popTotaleSenzaPuglia > 0 ? ((double) macchSenzaPugliaValidi / popTotaleSenzaPuglia) * 100000.0 : 0.0;

        risultato.put("MEDIA_NON_PESATA_TOTALE", mediaNonPesataTotale);
        risultato.put("MEDIA_NON_PESATA_SENZA_PUGLIA", mediaNonPesataSenzaPuglia);
        risultato.put("MEDIA_PESATA_TOTALE_100K", mediaPesataTotale100k);
        risultato.put("MEDIA_PESATA_SENZA_PUGLIA_100K", mediaPesataSenzaPuglia100k);

        return risultato;
    }
}