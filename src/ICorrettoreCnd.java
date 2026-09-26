public interface ICorrettoreCnd {
    String correggiCodice(String codCndInput, TipoApparecchiatura tipo, String descrizioneInput);
    String ottieniDescrizioneCanonica(String codCnd, String descrizioneFallback);
    
}
