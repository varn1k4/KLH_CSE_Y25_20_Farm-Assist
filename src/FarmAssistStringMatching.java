import java.io.*;
import java.util.*;
 
public class FarmAssistStringMatching {
 
    // =========================================================
    // AGRICULTURAL VOCABULARY
    // Used by Edit Distance for automatic spelling correction
    // =========================================================
 
    static String[] agriculturalTerms = {
 
        "rice", "wheat", "tomato", "soil", "water", "irrigation",
        "fertilizer", "fertilizers", "pest", "pests", "disease", "diseases",
        "cultivation", "harvesting", "harvest", "seed", "seeds", "crop", "crops",
        "yield", "farmer", "farmers", "farming", "agriculture", "agricultural",
        "nitrogen", "phosphorus", "potassium", "weed", "weeds", "fungicide",
        "fungicides", "insecticide", "insecticides", "pesticide", "pesticides",
        "plant", "plants", "root", "roots", "leaf", "leaves", "stem", "growth",
        "temperature", "rainfall", "climate", "sunlight", "organic", "compost",
        "mulching", "drainage", "fertility", "production", "productivity",
        "farmland", "banana", "nitrogen", "sustainable", "technology"
    };
 
 
    // =========================================================
    // BUILD LPS ARRAY FOR KMP
    // =========================================================
 
    public static int[] buildLPS(String pattern) {
 
        int[] lps = new int[pattern.length()];
        int length = 0;
        int i = 1;
 
        while (i < pattern.length()) {
 
            char current = Character.toLowerCase(pattern.charAt(i));
            char previous = Character.toLowerCase(pattern.charAt(length));
 
            if (current == previous) {
 
                length++;
                lps[i] = length;
                i++;
 
            } else {
 
                if (length != 0) {
                    length = lps[length - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
 
        return lps;
    }
 
 
    // =========================================================
    // KMP STRING MATCHING ALGORITHM (WHOLE-WORD MATCHES ONLY)
    //
    // A raw KMP search will happily report a "match" for "roce"
    // inside the word "process" or "production". That is what
    // was breaking the spelling-correction feature: the program
    // thought it had an exact match, so it never tried to fix
    // the typo. This version keeps the same KMP core but only
    // keeps matches that sit on a whole-word boundary (i.e. not
    // sandwiched inside a bigger word).
    // =========================================================
 
    public static ArrayList<Integer> KMPSearch(String text, String pattern) {
 
        ArrayList<Integer> positions = new ArrayList<>();
 
        if (pattern == null || pattern.length() == 0 || text == null) {
            return positions;
        }
 
        int[] lps = buildLPS(pattern);
 
        int i = 0;
        int j = 0;
 
        while (i < text.length()) {
 
            char textChar = Character.toLowerCase(text.charAt(i));
            char patternChar = Character.toLowerCase(pattern.charAt(j));
 
            if (textChar == patternChar) {
                i++;
                j++;
            }
 
            if (j == pattern.length()) {
 
                int start = i - j;
                int end = i;
 
                boolean leftBoundaryOk =
                        (start == 0) ||
                        !Character.isLetterOrDigit(text.charAt(start - 1));
 
                boolean rightBoundaryOk =
                        (end == text.length()) ||
                        !Character.isLetterOrDigit(text.charAt(end));
 
                if (leftBoundaryOk && rightBoundaryOk) {
                    positions.add(start);
                }
 
                j = lps[j - 1];
 
            } else if (i < text.length() &&
                    Character.toLowerCase(text.charAt(i)) != Character.toLowerCase(pattern.charAt(j))) {
 
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
 
        return positions;
    }
 
 
    // =========================================================
    // EDIT DISTANCE (Damerau-Levenshtein Distance)
    //
    // Plain Levenshtein distance treats a swapped pair of letters
    // (e.g. "siol" vs "soil") as TWO edits (two substitutions),
    // which pushed it past the allowed threshold and made the
    // correction fail. Damerau-Levenshtein adds one extra rule:
    // swapping two adjacent letters counts as a single edit, just
    // like a real typo. This is the standard algorithm used by
    // spell checkers for exactly this reason.
    // =========================================================
 
    public static int editDistance(String word1, String word2) {
 
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();
 
        int m = word1.length();
        int n = word2.length();
 
        int[][] dp = new int[m + 1][n + 1];
 
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
 
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
 
                int cost = (word1.charAt(i - 1) == word2.charAt(j - 1)) ? 0 : 1;
 
                int insertion = dp[i][j - 1] + 1;
                int deletion = dp[i - 1][j] + 1;
                int substitution = dp[i - 1][j - 1] + cost;
 
                dp[i][j] = Math.min(insertion, Math.min(deletion, substitution));
 
                // Adjacent transposition, e.g. "siol" -> "soil"
                if (i > 1 && j > 1 &&
                        word1.charAt(i - 1) == word2.charAt(j - 2) &&
                        word1.charAt(i - 2) == word2.charAt(j - 1)) {
 
                    dp[i][j] = Math.min(dp[i][j], dp[i - 2][j - 2] + 1);
                }
            }
        }
 
        return dp[m][n];
    }
 
 
    // =========================================================
    // FIND CLOSEST AGRICULTURAL WORD
    // =========================================================
 
    public static String findClosestWord(String query) {
 
        query = query.toLowerCase().trim();
 
        String closestWord = null;
        int smallestDistance = Integer.MAX_VALUE;
 
        for (String word : agriculturalTerms) {
 
            int distance = editDistance(query, word);
 
            if (distance < smallestDistance) {
                smallestDistance = distance;
                closestWord = word;
            }
        }
 
        int allowedDistance;
 
        if (query.length() <= 4) {
            allowedDistance = 1;
        } else {
            allowedDistance = 2;
        }
 
        if (smallestDistance <= allowedDistance) {
            return closestWord;
        }
 
        return null;
    }
 
 
    // =========================================================
    // READ FILE
    // =========================================================
 
    public static String readFile(String filePath) {
 
        StringBuilder content = new StringBuilder();
 
        try {
 
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
 
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append("\n");
            }
 
            reader.close();
 
        } catch (IOException e) {
            System.out.println("Error reading file: " + filePath);
        }
 
        return content.toString();
    }
 
 
    // =========================================================
    // GET ALL TEXT FILES FROM DATA FOLDER
    // =========================================================
 
    public static File[] getDataFiles() {
 
        File dataFolder = new File("data");
 
        if (!dataFolder.exists()) {
 
            System.out.println();
            System.out.println("ERROR: 'data' folder was not found.");
            System.out.println("Create a folder named 'data' inside your project folder.");
 
            return new File[0];
        }
 
        File[] files = dataFolder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        });
 
        if (files == null) {
            return new File[0];
        }
 
        Arrays.sort(files, Comparator.comparing(File::getName));
 
        return files;
    }
 
 
    // =========================================================
    // GET MATCHING SENTENCES FROM A BLOCK OF TEXT
    // =========================================================
 
    public static ArrayList<String> getMatchingSentences(String text, String query) {
 
        ArrayList<String> matches = new ArrayList<>();
 
        String[] sentences = text.split("(?<=[.!?])\\s+");
 
        for (String sentence : sentences) {
 
            ArrayList<Integer> hits = KMPSearch(sentence, query);
 
            if (!hits.isEmpty()) {
                matches.add(sentence.trim());
            }
        }
 
        return matches;
    }
 
 
    // =========================================================
    // SEARCH THE WHOLE CORPUS FOR A TERM AND PRINT THE RESULT
    //
    // Returns the total number of whole-word matches found.
    //
    // Many of the "general_notes_*.txt" filler files repeat the
    // exact same sentence dozens of times, so printing one line
    // per match would flood the screen. Instead this groups by
    // the sentence text: each distinct sentence is shown once,
    // and only the top MAX_SENTENCES are printed to keep the
    // output clean. File names are intentionally not shown --
    // only the actual information matters to the user.
    // =========================================================
 
    private static final int MAX_SENTENCES = 8;
 
    public static int searchAndDisplay(
            Map<String, String> corpus,
            String term) {
 
        int totalMatches = 0;
 
        // Use a Set so an identical sentence appearing in many
        // duplicate files is only ever shown once.
        LinkedHashSet<String> uniqueSentences = new LinkedHashSet<>();
 
        for (String content : corpus.values()) {
 
            ArrayList<Integer> positions = KMPSearch(content, term);
 
            if (positions.isEmpty()) {
                continue;
            }
 
            totalMatches += positions.size();
            uniqueSentences.addAll(getMatchingSentences(content, term));
        }
 
        System.out.println();
 
        if (totalMatches == 0) {
            System.out.println("No information about \"" + term + "\" was found.");
            return 0;
        }
 
        System.out.println("Information about \"" + term + "\":");
        System.out.println();
 
        int shown = 0;
 
        for (String sentence : uniqueSentences) {
 
            if (shown >= MAX_SENTENCES) {
                break;
            }
 
            System.out.println("  * " + sentence);
            shown++;
        }
 
        if (uniqueSentences.size() > MAX_SENTENCES) {
            System.out.println("  ... and " + (uniqueSentences.size() - MAX_SENTENCES) + " more points.");
        }
 
        return totalMatches;
    }
 
 
    // =========================================================
    // LOAD EVERY FILE IN THE DATA FOLDER ONCE
    // =========================================================
 
    public static Map<String, String> loadCorpus(File[] files) {
 
        Map<String, String> corpus = new LinkedHashMap<>();
 
        for (File file : files) {
            corpus.put(file.getName(), readFile(file.getPath()));
        }
 
        return corpus;
    }
 
 
    // =========================================================
    // MAIN METHOD
    // =========================================================
 
    public static void main(String[] args) {
 
        Scanner scanner = new Scanner(System.in);
 
        System.out.println("===============================================");
        System.out.println("   FARM ASSIST -- Agricultural Search System");
        System.out.println("===============================================");
 
        File[] files = getDataFiles();
 
        if (files.length == 0) {
            System.out.println("No .txt files were found inside the 'data' folder.");
            scanner.close();
            return;
        }
 
        Map<String, String> corpus = loadCorpus(files);
 
        System.out.println("Corpus loaded successfully.");
 
        while (true) {
 
            System.out.println();
            System.out.print("Enter a word or phrase to search (or 'exit'): ");
 
            String query = scanner.nextLine().trim();
 
            if (query.equalsIgnoreCase("exit")) {
                System.out.println();
                System.out.println("Thank you for using Farm Assist.");
                break;
            }
 
            if (query.isEmpty()) {
                System.out.println("Please enter a search term.");
                continue;
            }
 
            // -----------------------------------------------
            // STEP 1: try an exact (whole-word) KMP search
            // -----------------------------------------------
 
            int exactMatches = 0;
 
            for (String content : corpus.values()) {
                exactMatches += KMPSearch(content, query).size();
            }
 
            if (exactMatches > 0) {
                searchAndDisplay(corpus, query);
                continue;
            }
 
            // -----------------------------------------------
            // STEP 2: no exact match -> try edit-distance fix
            // -----------------------------------------------
 
            String corrected = findClosestWord(query);
 
            if (corrected == null) {
 
                System.out.println();
                System.out.println("No match found for \"" + query + "\", and no close "
                        + "agricultural term was found either.");
                System.out.println("Try words like: rice, wheat, tomato, soil, "
                        + "irrigation, fertilizer, disease, pest.");
                continue;
            }
 
            System.out.println();
            System.out.println("Did you mean \"" + corrected + "\"?");
 
            searchAndDisplay(corpus, corrected);
        }
 
        scanner.close();
    }
}
