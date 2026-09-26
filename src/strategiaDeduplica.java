import java.util.List;

public interface strategiaDeduplica {
	/**
     * Riceve la lista dei dispositivi bonificati e restituisce la lista
     * deduplicata secondo una specifica regola di dominio.
     */
    List<Dispositivo> aggrega(List<Dispositivo> lista); //aggrega è il metodo lista è il nome
}
