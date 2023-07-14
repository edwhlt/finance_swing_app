package fr.hedwin.sql.utils;

public class StringSimilarity {
    public static double calculateSimilarity(String str1, String str2) {
        int len1 = str1.length();
        int len2 = str2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int substitution = dp[i - 1][j - 1] + 1;
                    int deletion = dp[i - 1][j] + 1;
                    int insertion = dp[i][j - 1] + 1;
                    dp[i][j] = Math.min(substitution, Math.min(deletion, insertion));
                }
            }
        }

        int maxLen = Math.max(len1, len2);
        double similarity = 1.0 - ((double) dp[len1][len2] / maxLen);
        return similarity;
    }
}