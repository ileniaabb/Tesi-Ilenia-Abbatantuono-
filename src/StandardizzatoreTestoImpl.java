
public class StandardizzatoreTestoImpl implements normalizzazioni{
	@Override
    public String pulisciTesto(String testo) {
        if (testo == null) {
        	return null;
        }
        String pulito = testo.replaceAll("\\s+", " ").trim();// in primis rimuove gli spazi multipli,tabulazione e a capo,
        //sostituiti con un solo spazio, e rimuove gli spazi finali e iniziali
        return pulito.isEmpty() ? null : pulito;
    }

    @Override
    public String correggiCaratteriAccentati(String testo) { //poi rimpiazza i caratteri trasposti male con quelli giusti
        if (testo == null) return null;
        return testo.replace("Ã¨", "è").replace("Ã©", "é").replace("Ã ", "à")
        		.replace("Ã€", "À").replace("Ã¬", "ì").replace("Ã²", "ò")
        		.replace("Ã¹", "ù").replace("ÃƒÂ€", "À").replace("â€™", "'");
    }

    @Override
    public String pulisciIndirizzo(String indirizzo) {
        if (indirizzo == null) {
        	return null;
        }
        String pulito = correggiCaratteriAccentati(indirizzo);
        pulito = pulito.replaceAll("\\s+,", ",").replaceAll(",([a-zA-Z0-9])", ", $1")// inserisce uno spazio dopo la virgola, se cè un testo vicino
                       .replaceAll("\\s+", " ").trim();
        return pulito.isEmpty() ? null : pulito;
    }

    @Override
    public String pulisciCodice(String codice) {
        if (codice == null) return null;
        String pulito = codice.trim();
        return pulito.isEmpty() ? null : pulito;
    }
}

