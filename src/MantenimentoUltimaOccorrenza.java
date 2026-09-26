import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MantenimentoUltimaOccorrenza implements strategiaDeduplica {
	@Override
	public List<Dispositivo> aggrega(List<Dispositivo> lista) {
		 Map<String, Dispositivo> mappaAggregata = new LinkedHashMap<>();

         for (Dispositivo d : lista) {
             // Chiave composita: Struttura + CND + Tipo
             String chiave = String.format("%s_%s_%s",
                     d.getCodiceStruttura(), //inseriti in un'unica stringa
                     d.getCodClassificazioneCnd(),
                     d.getTipoApparecchiatura());
             
             //Usando put, ogni duplicato successivo sovrascrive il precedente.
             // Alla fine dell'iterazione la mappa conterra' l'ULTIMA occorrenza trovata.
             mappaAggregata.put(chiave, d);
             }
		 return new ArrayList<>(mappaAggregata.values());
	}
}