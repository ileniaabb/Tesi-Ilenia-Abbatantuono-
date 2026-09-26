import java.util.HashMap;
import java.util.Map;

public class CorrettoreCndImpl implements ICorrettoreCnd{
	private final Map<String, String> descrizioniCanoniche;

    public CorrettoreCndImpl() {
        this.descrizioniCanoniche = new HashMap<>();
        inizializzaDescrizioni();
    }

    private void inizializzaDescrizioni() {
        descrizioniCanoniche.put("Z11010101", "ACCELERATORI LINEARI AD ENERGIA SINGOLA");
        descrizioniCanoniche.put("Z11010102", "ACCELERATORI LINEARI AD ENERGIA MEDIA E MULTIPLA");
        descrizioniCanoniche.put("Z11010103", "ACCELERATORI LINEARI AD ENERGIA ALTA E MULTIPLA");
        descrizioniCanoniche.put("Z11010104", "ACCELERATORI LINEARI INTRAOPERATORI");
        descrizioniCanoniche.put("Z11020101", "GAMMA CAMERE MOBILI");
        descrizioniCanoniche.put("Z11020102", "GAMMA CAMERE IN STAZIONE FISSA A SINGOLA TESTATA - SENZA ACQUISIZIONE \"TOTAL BODY\"");
        descrizioniCanoniche.put("Z11020103", "GAMMA CAMERE IN STAZIONE FISSA A SINGOLA TESTATA - CON ACQUISIZIONE \"TOTAL BODY\"");
        descrizioniCanoniche.put("Z11020104", "GAMMA CAMERE IN STAZIONE FISSA A TESTATA MULTIPLA - SENZA ACQUISIZIONE \"TOTAL BODY\"");
        descrizioniCanoniche.put("Z11020105", "GAMMA CAMERE IN STAZIONE FISSA A TESTATA MULTIPLA - CON ACQUISIZIONE \"TOTAL BODY\"");
        descrizioniCanoniche.put("Z11020201", "SISTEMI TC/GAMMA CAMERA");
        descrizioniCanoniche.put("Z11020301", "SISTEMI TC/PET");
        descrizioniCanoniche.put("Z11030102", "ANGIOGRAFI FISSI PER STUDI ANGIOGRAFICI E CARDIOLOGICI");
        descrizioniCanoniche.put("Z11030103", "ANGIOGRAFI BIPLANARI");
        descrizioniCanoniche.put("Z11030201", "MAMMOGRAFI CONVENZIONALI");
        descrizioniCanoniche.put("Z11030202", "MAMMOGRAFI DIGITALI");
        descrizioniCanoniche.put("Z11030601", "TOMOGRAFI COMPUTERIZZATI - INFERIORE O UGUALE A 2 STRATI");
        descrizioniCanoniche.put("Z11030602", "TOMOGRAFI COMPUTERIZZATI - SUPERIORE A 2 STRATI ED INFERIORE A 16 STRATI");
        descrizioniCanoniche.put("Z11030603", "TOMOGRAFI COMPUTERIZZATI - SUPERIORE O UGUALE A 16 STRATI ED INFERIORE A 64 STRATI");
        descrizioniCanoniche.put("Z11030604", "TOMOGRAFI COMPUTERIZZATI - SUPERIORE O UGUALE A 64 STRATI");
        descrizioniCanoniche.put("Z11030605", "TOMOGRAFI COMPUTERIZZATI - SUPERIORE O UGUALE A 64 STRATI ED INFERIORE A 128 STRATI");
        descrizioniCanoniche.put("Z11030606", "TOMOGRAFI COMPUTERIZZATI - SUPERIORE O UGUALE A 128 STRATI ED INFERIORE A 256 STRATI");
        descrizioniCanoniche.put("Z11030607", "TOMOGRAFI COMPUTERIZZATI - SUPERIORE O UGUALE A 256 STRATI");
        descrizioniCanoniche.put("Z11050101", "TOMOGRAFI SETTORIALI (PER ESAMI TOMOGRAFICI DELLE ESTREMITA')");
        descrizioniCanoniche.put("Z11050102", "TOMOGRAFI A MAGNETE APERTO CON INTENSITA' DI CAMPO MAGNETICO INFERIORE O UGUALE A 0.5T");
        descrizioniCanoniche.put("Z11050103", "TOMOGRAFI A MAGNETE APERTO CON INTENSITA' DI CAMPO MAGNETICO SUPERIORE A 0.5T");
        descrizioniCanoniche.put("Z11050104", "TOMOGRAFI A MAGNETE CHIUSO CON INTENSITA' DI CAMPO INFERIORE O UGUALE A 2T");
        descrizioniCanoniche.put("Z11050105", "TOMOGRAFI A MAGNETE CHIUSO CON INTENSITA' DI CAMPO SUPERIORE A 2T E INFERIORE O UGUALE A 4T");
        descrizioniCanoniche.put("Z11050106", "TOMOGRAFI PER STUDI SPECIALI E RICERCA (INTENSITA' DI CAMPO SUPERIORE A 3T)");
        descrizioniCanoniche.put("Z12020101", "SISTEMI ROBOTIZZATI PER CHIRURGIA ENDOSCOPICA");
    }

    @Override
    public String correggiCodice(String codCndInput, TipoApparecchiatura tipo, String descrizioneInput) {
        if (codCndInput == null) {
        	return null;
        }
        
        String cndPulito = codCndInput.trim().toUpperCase().replaceAll("[^A-Z0-9]", ""); // tutto ciò che NON è da  a Z o da 0 a 9 viene eliminato
        if (descrizioniCanoniche.containsKey(cndPulito)) {
            return cndPulito;
        }

        if (!cndPulito.startsWith("Z") && cndPulito.length() == 8) { //aggiunge la z iniziale se manca
            cndPulito = "Z" + cndPulito;
        }
        cndPulito = cndPulito.replace('O', '0'); //rimpiazza o con 0

        if (descrizioniCanoniche.containsKey(cndPulito)) {
            return cndPulito;
        }

        return cndPulito;
    }

    @Override
    public String ottieniDescrizioneCanonica(String codCnd, String descrizioneFallback) {
        return descrizioniCanoniche.getOrDefault(codCnd, descrizioneFallback);
    }
}
