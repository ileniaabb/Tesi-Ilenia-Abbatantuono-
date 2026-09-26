import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
public class SommaQuantita implements strategiaDeduplica {
	@Override
    public List<Dispositivo> aggrega(List<Dispositivo> lista) {
        Map<String, Dispositivo> mappaAggregata = new LinkedHashMap<>();

        for (Dispositivo d : lista) {
            String chiave = String.format("%s_%s_%s",
                    d.getCodiceStruttura(),
                    d.getCodClassificazioneCnd(),
                    d.getTipoApparecchiatura());

            if (mappaAggregata.containsKey(chiave)) {
                Dispositivo esistente = mappaAggregata.get(chiave);

                int nuovoTotale = (esistente.getNumApparecchiature() != null ? esistente.getNumApparecchiature() : 0)
                                + (d.getNumApparecchiature() != null ? d.getNumApparecchiature() : 0);
                int nuoveDisponibili = (esistente.getNumAppDisponibili() != null ? esistente.getNumAppDisponibili() : 0)
                                     + (d.getNumAppDisponibili() != null ? d.getNumAppDisponibili() : 0);

                Dispositivo aggregato = new Dispositivo(
                        esistente.getCodiceRegione(),
                        esistente.getRegione(),
                        esistente.getCodiceAziendaSanitariaAsl(),
                        esistente.getAziendaSanitariaAsl(),
                        esistente.getCodiceStruttura(),
                        esistente.getDenominazioneStruttura(),
                        esistente.getIndirizzoStruttura(),
                        esistente.getLatitudine(),
                        esistente.getLongitudine(),
                        esistente.getTipoApparecchiatura(),
                        esistente.getCodClassificazioneCnd(),
                        esistente.getDescrizioneCnd(),
                        nuovoTotale,
                        nuoveDisponibili,
                        esistente.getCaratteristiche()
                );
                mappaAggregata.put(chiave, aggregato);
            } else {
                mappaAggregata.put(chiave, d);
            }
        }

        return new ArrayList<>(mappaAggregata.values());
    }
}
