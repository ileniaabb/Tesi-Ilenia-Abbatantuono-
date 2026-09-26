package grafic;

import java.util.Map;

	public interface Strategy {
	    /**
	     * Calcola la metrica specifica basandosi su macchinari e popolazione
	     */
	    Map<String, Double> calcola(Map<String, Integer> macchinari, Map<String, Long> popolazione);
	}

