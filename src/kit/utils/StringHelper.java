package kit.utils;

public class StringHelper {

    public static boolean hasSubstrings(String str, String[] searches, boolean caseInsensitive)
    {
        for(String search : searches)
        {
                String iStr = caseInsensitive ? str.toLowerCase() : str;
                String iSearch = caseInsensitive ? search.toLowerCase() : search;
                if(iStr.indexOf(iSearch) != -1)
                {
                    return true;
                }
        }
        return false;
    }
    public static double strippedSimilarity(String a, String b) {
        String aStripped = strip(a);
        String bStripped = strip(b);
        return similarity(aStripped,bStripped);
    }

    public static String strip(String str) {
        String[] toRemove = new String[]{"™", " ", ".", ":", "-", "®",".exe"};
        for (String search : toRemove) {
            str = str.replace(search, "");
        }
        str = str.trim();
        str = str.toLowerCase();
        return str;
    }

    /**
     * Calculates the similarity (a number within 0 and 1) between two strings.
     */
    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0; /* both strings are zero length */
        }
    /* // If you have Apache Commons Text, you can use it to calculate the edit distance:
    LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    return (longerLength - levenshteinDistance.apply(longer, shorter)) / (double) longerLength; */
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    // Example implementation of the Levenshtein Edit Distance
    // See http://rosettacode.org/wiki/Levenshtein_distance#Java
    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }
}
