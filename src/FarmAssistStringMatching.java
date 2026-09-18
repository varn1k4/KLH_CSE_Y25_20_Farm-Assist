import java.io.*;
import java.util.*;

public class FarmAssistStringMatching {

    // =========================================================
    // AGRICULTURAL VOCABULARY
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
        "farmland", "banana", "sustainable", "technology"
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
    // KMP STRING MATCHING ALGORITHM
    // WHOLE-WORD MATCHES ONLY
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
                    Character.toLowerCase(text.charAt(i))
                    != Character.toLowerCase(pattern.charAt(j))) {

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
    // DAMERAU-LEVENSHTEIN EDIT DISTANCE
    // =========================================================

    public static int editDistance(String word1, String word2) {

        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        int m = word1.length();
        int n = word2.length();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++)
            dp[i][0] = i;

        for (int j = 0; j <= n; j++)
            dp[0][j] = j;

        for (int i = 1; i <= m; i++) {

            for (int j = 1; j <= n; j++) {

                int cost =
                        (word1.charAt(i - 1) == word2.charAt(j - 1))
                        ? 0 : 1;

                int insertion = dp[i][j - 1] + 1;
                int deletion = dp[i - 1][j] + 1;
                int substitution = dp[i - 1][j - 1] + cost;

                dp[i][j] = Math.min(
                        insertion,
                        Math.min(deletion, substitution)
                );

                // Adjacent transposition
                if (i > 1 && j > 1 &&
                        word1.charAt(i - 1) == word2.charAt(j - 2) &&
                        word1.charAt(i - 2) == word2.charAt(j - 1)) {

                    dp[i][j] =
                            Math.min(dp[i][j], dp[i - 2][j - 2] + 1);
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

            BufferedReader reader =
                    new BufferedReader(new FileReader(filePath));

            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append("\n");
            }

            reader.close();

        } catch (IOException e) {

            System.out.println(
                    "Error reading file: " + filePath
            );
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
            System.out.println(
                    "ERROR: 'data' folder was not found."
            );

            System.out.println(
                    "Create a folder named 'data' inside your project folder."
            );

            return new File[0];
        }

        File[] files = dataFolder.listFiles(
                new FilenameFilter() {

                    public boolean accept(File dir, String name) {

                        return name.toLowerCase().endsWith(".txt");
                    }
                }
        );

        if (files == null) {
            return new File[0];
        }

        Arrays.sort(
                files,
                Comparator.comparing(File::getName)
        );

        return files;
    }


    // =========================================================
    // GET MATCHING SENTENCES
    // =========================================================

    public static ArrayList<String> getMatchingSentences(
            String text,
            String query) {

        ArrayList<String> matches =
                new ArrayList<>();

        String[] sentences =
                text.split("(?<=[.!?])\\s+");

        for (String sentence : sentences) {

            ArrayList<Integer> hits =
                    KMPSearch(sentence, query);

            if (!hits.isEmpty()) {
                matches.add(sentence.trim());
            }
        }

        return matches;
    }


    // =========================================================
    // SEARCH THE WHOLE CORPUS
    // =========================================================

    private static final int MAX_SENTENCES = 8;

    public static int searchAndDisplay(
            Map<String, String> corpus,
            String term) {

        int totalMatches = 0;

        LinkedHashSet<String> uniqueSentences =
                new LinkedHashSet<>();

        for (String content : corpus.values()) {

            ArrayList<Integer> positions =
                    KMPSearch(content, term);

            if (positions.isEmpty()) {
                continue;
            }

            totalMatches += positions.size();

            uniqueSentences.addAll(
                    getMatchingSentences(content, term)
            );
        }

        System.out.println();

        if (totalMatches == 0) {

            System.out.println(
                    "No information about \"" +
                    term + "\" was found."
            );

            return 0;
        }

        System.out.println(
                "Information about \"" + term + "\":"
        );

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

            System.out.println(
                    "  ... and " +
                    (uniqueSentences.size() - MAX_SENTENCES) +
                    " more points."
            );
        }

        return totalMatches;
    }


    // =========================================================
    // LOAD EVERY FILE IN DATA FOLDER
    // =========================================================

    public static Map<String, String> loadCorpus(
            File[] files) {

        Map<String, String> corpus =
                new LinkedHashMap<>();

        for (File file : files) {

            corpus.put(
                    file.getName(),
                    readFile(file.getPath())
            );
        }

        return corpus;
    }


    // =========================================================
    // IRRIGATION WATER-DISTRIBUTION NETWORK
    // MAXIMUM FLOW
    // =========================================================

    static String[] networkNodeNames = {

            "Water Source",
            "Canal A",
            "Canal B",
            "Field 1",
            "Field 2",
            "Field 3",
            "Farm Outlet"
    };


    // Capacity in liters/minute

    static int[][] networkCapacity = {

            // Source CanalA CanalB Field1 Field2 Field3 Outlet

            { 0, 20, 15, 0, 0, 0, 0 }, // Water Source

            { 0, 0, 0, 10, 8, 0, 0 },  // Canal A

            { 0, 0, 0, 0, 10, 12, 0 }, // Canal B

            { 0, 0, 0, 0, 0, 0, 10 }, // Field 1

            { 0, 0, 0, 0, 0, 0, 15 }, // Field 2

            { 0, 0, 0, 0, 0, 0, 12 }, // Field 3

            { 0, 0, 0, 0, 0, 0, 0 }   // Farm Outlet
    };


    // =========================================================
    // BFS FOR EDMONDS-KARP
    // =========================================================

    public static boolean bfsFindPath(
            int[][] residual,
            int source,
            int sink,
            int[] parent) {

        int n = residual.length;

        boolean[] visited =
                new boolean[n];

        Queue<Integer> queue =
                new LinkedList<>();

        queue.add(source);

        visited[source] = true;

        parent[source] = -1;

        while (!queue.isEmpty()) {

            int current =
                    queue.poll();

            for (int next = 0; next < n; next++) {

                if (!visited[next] &&
                        residual[current][next] > 0) {

                    parent[next] = current;

                    visited[next] = true;

                    queue.add(next);
                }
            }
        }

        return visited[sink];
    }


    // =========================================================
    // EDMONDS-KARP MAXIMUM FLOW
    // =========================================================

    public static int maxFlow(
            int[][] capacity,
            int source,
            int sink,
            int[][] residualOut) {

        int n = capacity.length;

        int[][] residual =
                new int[n][n];

        for (int i = 0; i < n; i++) {

            residual[i] =
                    Arrays.copyOf(capacity[i], n);
        }

        int[] parent =
                new int[n];

        int totalFlow = 0;

        while (bfsFindPath(
                residual,
                source,
                sink,
                parent)) {

            // Find bottleneck

            int pathFlow =
                    Integer.MAX_VALUE;

            for (int v = sink;
                 v != source;
                 v = parent[v]) {

                int u = parent[v];

                pathFlow =
                        Math.min(
                                pathFlow,
                                residual[u][v]
                        );
            }


            // Update residual capacities

            for (int v = sink;
                 v != source;
                 v = parent[v]) {

                int u = parent[v];

                residual[u][v] -= pathFlow;

                residual[v][u] += pathFlow;
            }

            totalFlow += pathFlow;
        }


        // Copy residual graph

        for (int i = 0; i < n; i++) {

            residualOut[i] =
                    residual[i];
        }

        return totalFlow;
    }


    // =========================================================
    // RUN IRRIGATION NETWORK
    // =========================================================

    public static void runIrrigationFlow() {

        int source = 0;

        int sink =
                networkNodeNames.length - 1;

        int n =
                networkCapacity.length;

        int[][] residual =
                new int[n][n];


        int totalFlow =
                maxFlow(
                        networkCapacity,
                        source,
                        sink,
                        residual
                );


        System.out.println();

        System.out.println(
                "==============================================="
        );

        System.out.println(
                "       IRRIGATION NETWORK -- MAXIMUM FLOW"
        );

        System.out.println(
                "==============================================="
        );

        System.out.println();

        System.out.println(
                "Maximum deliverable water: " +
                totalFlow +
                " liters/minute"
        );

        System.out.println();


        // =====================================================
        // DISPLAY FLOW USED
        // =====================================================

        System.out.println(
                "Flow used on each canal:"
        );

        for (int u = 0; u < n; u++) {

            for (int v = 0; v < n; v++) {

                if (networkCapacity[u][v] > 0) {

                    int used =
                            networkCapacity[u][v]
                            - residual[u][v];

                    System.out.println(
                            "  " +
                            networkNodeNames[u] +
                            " -> " +
                            networkNodeNames[v] +
                            " : " +
                            used +
                            " / " +
                            networkCapacity[u][v] +
                            " L/min"
                    );
                }
            }
        }


        // =====================================================
        // FIND BOTTLENECK / MIN CUT
        // =====================================================

        boolean[] reachable =
                new boolean[n];

        Queue<Integer> queue =
                new LinkedList<>();

        queue.add(source);

        reachable[source] = true;

        while (!queue.isEmpty()) {

            int current =
                    queue.poll();

            for (int next = 0; next < n; next++) {

                if (!reachable[next] &&
                        residual[current][next] > 0) {

                    reachable[next] = true;

                    queue.add(next);
                }
            }
        }


        System.out.println();

        System.out.println(
                "Bottleneck canal(s) limiting total water flow:"
        );

        boolean anyBottleneck = false;


        for (int u = 0; u < n; u++) {

            for (int v = 0; v < n; v++) {

                if (networkCapacity[u][v] > 0 &&
                        reachable[u] &&
                        !reachable[v]) {

                    System.out.println(
                            "  " +
                            networkNodeNames[u] +
                            " -> " +
                            networkNodeNames[v] +
                            " (full at " +
                            networkCapacity[u][v] +
                            " L/min)"
                    );

                    anyBottleneck = true;
                }
            }
        }


        if (!anyBottleneck) {

            System.out.println(
                    "  None -- the network is not capacity-limited."
            );
        }
    }


    // =========================================================
    // RUN CROP SEARCH
    // KMP + EDIT DISTANCE
    // =========================================================

    public static void runCorpusSearch(
            Scanner scanner) {

        File[] files =
                getDataFiles();


        if (files.length == 0) {

            System.out.println(
                    "No .txt files were found inside the 'data' folder."
            );

            return;
        }


        Map<String, String> corpus =
                loadCorpus(files);


        System.out.println(
                "Corpus loaded successfully."
        );


        while (true) {

            System.out.println();

            System.out.print(
                    "Enter a word or phrase to search (or 'back'): "
            );


            String query =
                    scanner.nextLine().trim();


            if (query.equalsIgnoreCase("back") ||
                    query.equalsIgnoreCase("exit")) {

                break;
            }


            if (query.isEmpty()) {

                System.out.println(
                        "Please enter a search term."
                );

                continue;
            }


            // =================================================
            // STEP 1: EXACT KMP SEARCH
            // =================================================

            int exactMatches = 0;


            for (String content : corpus.values()) {

                exactMatches +=
                        KMPSearch(
                                content,
                                query
                        ).size();
            }


            if (exactMatches > 0) {

                searchAndDisplay(
                        corpus,
                        query
                );

                continue;
            }


            // =================================================
            // STEP 2: EDIT DISTANCE CORRECTION
            // =================================================

            String corrected =
                    findClosestWord(query);


            if (corrected == null) {

                System.out.println();

                System.out.println(
                        "No match found for \"" +
                        query +
                        "\", and no close agricultural term was found either."
                );

                System.out.println(
                        "Try words like: rice, wheat, tomato, soil, " +
                        "irrigation, fertilizer, disease, pest."
                );

                continue;
            }


            System.out.println();

            System.out.println(
                    "Did you mean \"" +
                    corrected +
                    "\"?"
            );


            searchAndDisplay(
                    corpus,
                    corrected
            );
        }
    }


    // =========================================================
    // MAIN METHOD
    // =========================================================

    public static void main(String[] args) {

        Scanner scanner =
                new Scanner(System.in);


        System.out.println(
                "==============================================="
        );

        System.out.println(
                "            FARM ASSIST -- MAIN MENU"
        );

        System.out.println(
                "==============================================="
        );


        while (true) {

            System.out.println();

            System.out.println(
                    "1. Search Crop Information"
            );

            System.out.println(
                    "2. Analyze Irrigation Supply Network"
            );

            System.out.println(
                    "3. Exit"
            );

            System.out.print(
                    "Choose an option: "
            );


            String choice =
                    scanner.nextLine().trim();


            if (choice.equals("1")) {

                runCorpusSearch(scanner);

            }

            else if (choice.equals("2")) {

                runIrrigationFlow();

            }

            else if (choice.equals("3") ||
                    choice.equalsIgnoreCase("exit")) {

                System.out.println();

                System.out.println(
                        "Thank you for using Farm Assist."
                );

                break;
            }

            else {

                System.out.println(
                        "Please choose 1, 2, or 3."
                );
            }
        }


        scanner.close();
    }
}
