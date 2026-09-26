public class SimilaritaLevenshtein {

    /**
     * Calcola la similarità percentuale tra 0.0 e 1.0 basata sulla distanza di Levenshtein.
     */
    public static double calcolaSimilarita(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        String str1 = s1.toUpperCase().trim();
        String str2 = s2.toUpperCase().trim();
        
        if (str1.equals(str2)) return 1.0;

        int distanza = distanzaLevenshtein(str1, str2);
        int maxLen = Math.max(str1.length(), str2.length());

        if (maxLen == 0) return 1.0;
        
        // Converte la distanza assoluta in una percentuale da 0.0 a 1.0
        return 1.0 - ((double) distanza / maxLen);
    }

    /**
     * Calcola la distanza di Levenshtein (numero assoluto di modifiche)
     */
    public static int distanzaLevenshtein(String s1, String s2) {
        if (s1 == null || s2 == null) return Integer.MAX_VALUE;
        String str1 = s1.toUpperCase().trim();
        String str2 = s2.toUpperCase().trim();

        int[] dp = new int[str2.length() + 1];

        for (int j = 0; j <= str2.length(); j++) {
            dp[j] = j;
        }

        for (int i = 1; i <= str1.length(); i++) {
            int lastDiagonal = dp[0];
            dp[0] = i;
            for (int j = 1; j <= str2.length(); j++) {
                int oldDiagonal = dp[j];
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[j] = lastDiagonal;
                } else {
                    dp[j] = 1 + Math.min(lastDiagonal, Math.min(dp[j], dp[j - 1]));
                }
                lastDiagonal = oldDiagonal;
            }
        }

        return dp[str2.length()];
    }
}