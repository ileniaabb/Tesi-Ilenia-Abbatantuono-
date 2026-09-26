import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MantenimentoPrimaOccorrenza implements strategiaDeduplica {

	@Override
	public List<Dispositivo> aggrega(List<Dispositivo> lista) {
		 Map<String, Dispositivo> mappaAggregata = new LinkedHashMap<>();

         for (Dispositivo d : lista) {
             // Chiave composita: Struttura + CND + Tipo
             String chiave = String.format("%s_%s_%s",
                     d.getCodiceStruttura(), //inseriti in un'unica stringa
                     d.getCodClassificazioneCnd(),
                     d.getTipoApparecchiatura());

                 Dispositivo esistente = mappaAggregata.get(chiave);
                 /* Mette l'elemento nella mappa solo se la chiave NON esiste ancora.
                  Se la chiave esiste già, putIfAbsent ignora il nuovo dispositivo 'd' 
                  mantenendo intatto quello inserito per primo.*/
                 mappaAggregata.putIfAbsent(chiave, d);
                 
             }
         return new ArrayList<>(mappaAggregata.values());
	}

}
