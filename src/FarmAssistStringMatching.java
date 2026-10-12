import java.io.*;
import java.util.*;
import java.math.BigInteger;
 
/**
 * Farm Assist
 * Original features: KMP crop search + spelling correction, irrigation max-flow.
 * Added: Vertex Cover 2-approximation demonstration.
 * Added: Randomised Quicksort demonstration.
 *
 * Compile: javac FarmAssistStringMatching.java
 * Run:     java FarmAssistStringMatching
 */
public class FarmAssistStringMatching {
    // ==================== AGRICULTURAL VOCABULARY ====================
    static String[] agriculturalTerms = {
        "rice", "wheat", "tomato", "soil", "water", "irrigation",
        "fertilizer", "fertilizers", "pest", "pests", "disease",
        "diseases", "cultivation", "harvesting", "harvest", "seed",
        "seeds", "crop", "crops", "yield", "farmer", "farmers",
        "farming", "agriculture", "agricultural", "nitrogen",
        "phosphorus", "potassium", "weed", "weeds", "fungicide",
        "fungicides", "insecticide", "insecticides", "pesticide",
        "pesticides", "plant", "plants", "root", "roots", "leaf",
        "leaves", "stem", "growth", "temperature", "rainfall",
        "climate", "sunlight", "organic", "compost", "mulching",
        "drainage", "fertility", "production", "productivity",
        "farmland", "banana", "sustainable", "technology"
    };
 
    // ==================== KMP STRING MATCHING ====================
    public static int[] buildLPS(String pattern) {
        int[] lps = new int[pattern.length()];
        int length = 0, i = 1;
        while (i < pattern.length()) {
            char current = Character.toLowerCase(pattern.charAt(i));
            char previous = Character.toLowerCase(pattern.charAt(length));
            if (current == previous) {
                lps[i++] = ++length;
            } else if (length != 0) {
                length = lps[length - 1];
            } else {
                lps[i++] = 0;
            }
        }
        return lps;
    }
 
    public static ArrayList<Integer> KMPSearch(String text, String pattern) {
        ArrayList<Integer> positions = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty()) return positions;
        int[] lps = buildLPS(pattern);
        int i = 0, j = 0;
        while (i < text.length()) {
            if (Character.toLowerCase(text.charAt(i)) ==
                Character.toLowerCase(pattern.charAt(j))) {
                i++;
                j++;
            }
            if (j == pattern.length()) {
                int start = i - j, end = i;
                boolean left = start == 0 ||
                    !Character.isLetterOrDigit(text.charAt(start - 1));
                boolean right = end == text.length() ||
                    !Character.isLetterOrDigit(text.charAt(end));
                if (left && right) positions.add(start);
                j = lps[j - 1];
            } else if (i < text.length() &&
                Character.toLowerCase(text.charAt(i)) !=
                Character.toLowerCase(pattern.charAt(j))) {
                if (j != 0) j = lps[j - 1];
                else i++;
            }
        }
        return positions;
    }
 
    // Optimal-string-alignment edit distance (supports adjacent transposition).
    public static int editDistance(String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();
        int m = word1.length(), n = word2.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = word1.charAt(i - 1) == word2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(dp[i][j - 1] + 1,
                    Math.min(dp[i - 1][j] + 1, dp[i - 1][j - 1] + cost));
                if (i > 1 && j > 1 &&
                    word1.charAt(i - 1) == word2.charAt(j - 2) &&
                    word1.charAt(i - 2) == word2.charAt(j - 1)) {
                    dp[i][j] = Math.min(dp[i][j], dp[i - 2][j - 2] + 1);
                }
            }
        }
        return dp[m][n];
    }
 
    public static String findClosestWord(String query) {
        query = query.toLowerCase().trim();
        String closest = null;
        int smallest = Integer.MAX_VALUE;
        for (String word : agriculturalTerms) {
            int d = editDistance(query, word);
            if (d < smallest) { smallest = d; closest = word; }
        }
        int allowed = query.length() <= 4 ? 1 : 2;
        return smallest <= allowed ? closest : null;
    }
 
    public static String readFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) content.append(line).append("\n");
        } catch (IOException e) {
            System.out.println("Error reading file: " + filePath);
        }
        return content.toString();
    }
 
    public static File[] getDataFiles() {
        File folder = new File("data");
        if (!folder.exists()) {
            System.out.println("ERROR: 'data' folder was not found.");
            System.out.println("Create a folder named 'data' inside your project folder.");
            return new File[0];
        }
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (files == null) return new File[0];
        Arrays.sort(files, Comparator.comparing(File::getName));
        return files;
    }
 
    public static ArrayList<String> getMatchingSentences(String text, String query) {
        ArrayList<String> matches = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?])\\s+");
        for (String sentence : sentences) {
            if (!KMPSearch(sentence, query).isEmpty()) matches.add(sentence.trim());
        }
        return matches;
    }
 
    private static final int MAX_SENTENCES = 8;
 
    public static int searchAndDisplay(Map<String, String> corpus, String term) {
        int total = 0;
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String content : corpus.values()) {
            total += KMPSearch(content, term).size();
            unique.addAll(getMatchingSentences(content, term));
        }
        System.out.println();
        if (total == 0) {
            System.out.println("No information about \"" + term + "\" was found.");
            return 0;
        }
        System.out.println("Information about \"" + term + "\":\n");
        int shown = 0;
        for (String sentence : unique) {
            if (shown >= MAX_SENTENCES) break;
            System.out.println(" * " + sentence);
            shown++;
        }
        if (unique.size() > MAX_SENTENCES)
            System.out.println(" ... and " + (unique.size() - MAX_SENTENCES) + " more points.");
        return total;
    }
 
    public static Map<String, String> loadCorpus(File[] files) {
        Map<String, String> corpus = new LinkedHashMap<>();
        for (File file : files) corpus.put(file.getName(), readFile(file.getPath()));
        return corpus;
    }
 
    public static void runCorpusSearch(Scanner scanner) {
        File[] files = getDataFiles();
        if (files.length == 0) {
            System.out.println("No .txt files were found inside the 'data' folder.");
            return;
        }
        Map<String, String> corpus = loadCorpus(files);
        System.out.println("Corpus loaded successfully (" + files.length + " files).");
        while (true) {
            System.out.print("\nEnter a word or phrase to search (or 'back'): ");
            String query = scanner.nextLine().trim();
            if (query.equalsIgnoreCase("back") || query.equalsIgnoreCase("exit")) break;
            if (query.isEmpty()) { System.out.println("Please enter a search term."); continue; }
            int exact = 0;
            for (String content : corpus.values()) exact += KMPSearch(content, query).size();
            if (exact > 0) { searchAndDisplay(corpus, query); continue; }
            String corrected = findClosestWord(query);
            if (corrected == null) {
                System.out.println("No match found for \"" + query +
                    "\", and no close agricultural term was found either.");
                System.out.println("Try: rice, wheat, tomato, soil, irrigation, fertilizer, disease, pest.");
            } else {
                System.out.println("Did you mean \"" + corrected + "\"?");
                searchAndDisplay(corpus, corrected);
            }
        }
    }
 
    // ==================== GENERIC EDMONDS-KARP MAX FLOW ====================
    static String[] networkNodeNames = {
        "Water Source", "Canal A", "Canal B", "Field 1", "Field 2", "Field 3", "Farm Outlet"
    };
    static int[][] networkCapacity = {
        {0,20,15,0,0,0,0},
        {0,0,0,10,8,0,0},
        {0,0,0,0,10,12,0},
        {0,0,0,0,0,0,10},
        {0,0,0,0,0,0,15},
        {0,0,0,0,0,0,12},
        {0,0,0,0,0,0,0}
    };
 
    public static boolean bfsFindPath(int[][] residual, int source, int sink, int[] parent) {
        int n = residual.length;
        boolean[] visited = new boolean[n];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);
        visited[source] = true;
        parent[source] = -1;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v = 0; v < n; v++) {
                if (!visited[v] && residual[u][v] > 0) {
                    parent[v] = u;
                    visited[v] = true;
                    queue.add(v);
                }
            }
        }
        return visited[sink];
    }
 
    public static int maxFlow(int[][] capacity, int source, int sink, int[][] residualOut) {
        int n = capacity.length;
        int[][] residual = new int[n][n];
        for (int i = 0; i < n; i++) residual[i] = Arrays.copyOf(capacity[i], n);
        int[] parent = new int[n];
        int total = 0;
        while (bfsFindPath(residual, source, sink, parent)) {
            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v])
                pathFlow = Math.min(pathFlow, residual[parent[v]][v]);
            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                residual[u][v] -= pathFlow;
                residual[v][u] += pathFlow;
            }
            total += pathFlow;
        }
        for (int i = 0; i < n; i++) residualOut[i] = Arrays.copyOf(residual[i], n);
        return total;
    }
 
    public static boolean[] residualReachable(int[][] residual, int source) {
        boolean[] seen = new boolean[residual.length];
        Queue<Integer> q = new LinkedList<>();
        q.add(source); seen[source] = true;
        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v = 0; v < residual.length; v++)
                if (!seen[v] && residual[u][v] > 0) { seen[v] = true; q.add(v); }
        }
        return seen;
    }
 
    public static void runIrrigationFlow() {
        int n = networkCapacity.length, source = 0, sink = n - 1;
        int[][] residual = new int[n][n];
        int flow = maxFlow(networkCapacity, source, sink, residual);
        System.out.println("\n===============================================");
        System.out.println(" IRRIGATION NETWORK -- MAXIMUM FLOW");
        System.out.println("===============================================");
        System.out.println("Maximum deliverable water: " + flow + " liters/minute\n");
        System.out.println("Flow used on each canal:");
        for (int u = 0; u < n; u++) for (int v = 0; v < n; v++) {
            if (networkCapacity[u][v] > 0) {
                int used = networkCapacity[u][v] - residual[u][v];
                System.out.println(" " + networkNodeNames[u] + " -> " + networkNodeNames[v] +
                    " : " + used + " / " + networkCapacity[u][v] + " L/min");
            }
        }
        boolean[] reachable = residualReachable(residual, source);
        System.out.println("\nBottleneck canal(s) in the minimum cut:");
        boolean found = false;
        for (int u = 0; u < n; u++) for (int v = 0; v < n; v++) {
            if (networkCapacity[u][v] > 0 && reachable[u] && !reachable[v]) {
                System.out.println(" " + networkNodeNames[u] + " -> " + networkNodeNames[v] +
                    " (capacity " + networkCapacity[u][v] + " L/min)");
                found = true;
            }
        }
        if (!found) System.out.println(" None found.");
    }
 
    // ==================== VERTEX COVER 2-APPROXIMATION ====================
    public static void vertexCoverApproximation(Scanner sc) {
        System.out.println("\nVERTEX COVER 2-APPROXIMATION");
        System.out.print("Number of vertices (0..100): ");
        int n = readInt(sc, 0, 100);
        if (n < 1) { System.out.println("Need at least one vertex."); return; }
        System.out.print("Number of edges: ");
        int m = readInt(sc, 0, 500);
        int[][] edges = new int[m][2];
        for (int i = 0; i < m; i++) {
            System.out.print("Edge " + (i + 1) + " endpoints (0.." + (n - 1) + "): ");
            edges[i][0] = readInt(sc, 0, n - 1);
            edges[i][1] = readInt(sc, 0, n - 1);
        }
        boolean[] covered = new boolean[m];
        LinkedHashSet<Integer> cover = new LinkedHashSet<>();
        List<String> matching = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            if (covered[i]) continue;
            int u = edges[i][0], v = edges[i][1];
            if (u == v) { cover.add(u); covered[i] = true; continue; }
            cover.add(u); cover.add(v);
            matching.add("(" + u + "," + v + ")");
            for (int j = 0; j < m; j++)
                if (edges[j][0] == u || edges[j][1] == u ||
                    edges[j][0] == v || edges[j][1] == v) covered[j] = true;
        }
        System.out.println("Matching edges selected: " + matching);
        System.out.println("Approximate vertex cover: " + cover);
        System.out.println("Cover size = " + cover.size() +
            "; matching size = " + matching.size() +
            "; guarantee: cover size <= 2 * OPT (for standard loop-free graphs).");
    }
 
    // ==================== RANDOMISED QUICKSORT ====================
    public static void randomizedQuickSortDemo() {
        Random random = new Random();
        int[] a = {34, 7, 23, 32, 5, 62, 1, 18, 18, 9};
        System.out.println("\nRANDOMISED QUICKSORT");
        System.out.println("Before: " + Arrays.toString(a));
        randomizedQuickSort(a, 0, a.length - 1, random);
        System.out.println("After:  " + Arrays.toString(a));
        System.out.println("Expected time: O(n log n); worst case remains O(n^2), but random pivots make");
        System.out.println("bad pivot sequences unlikely. This is Las Vegas: output is always correctly sorted.");
    }
 
    private static void randomizedQuickSort(int[] a, int low, int high, Random random) {
        if (low >= high) return;
        int pivotIndex = low + random.nextInt(high - low + 1);
        swap(a, pivotIndex, high);
        int p = partition(a, low, high);
        randomizedQuickSort(a, low, p - 1, random);
        randomizedQuickSort(a, p + 1, high, random);
    }
 
    private static int partition(int[] a, int low, int high) {
        int pivot = a[high], i = low;
        for (int j = low; j < high; j++) if (a[j] <= pivot) swap(a, i++, j);
        swap(a, i, high);
        return i;
    }
 
    private static void swap(int[] a, int i, int j) {
        int t = a[i]; a[i] = a[j]; a[j] = t;
    }
 
    
    // One selected Module 5 concept: Vertex Cover 2-approximation.
    public static void runVertexCoverConcept(Scanner sc) {
        System.out.println("\nMODULE 5 CONCEPT: VERTEX COVER 2-APPROXIMATION");
        vertexCoverApproximation(sc);
    }
 
    // One selected Module 6 concept: Randomised Quicksort.
    public static void runRandomizedQuicksortConcept() {
        randomizedQuickSortDemo();
    }
 
// ==================== INPUT HELPER ====================
    private static int readInt(Scanner sc, int min, int max) {
        while (true) {
            String line = sc.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) { }
            System.out.print("Enter an integer from " + min + " to " + max + ": ");
        }
    }
 
    // ==================== MAIN MENU ====================
    public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
 
    System.out.println("===============================================");
    System.out.println(" FARM ASSIST -- MAIN MENU");
    System.out.println("===============================================");
 
    while (true) {
        System.out.println("\n1. Search Crop Information");
        System.out.println("2. Analyze Irrigation Supply Network");
        System.out.println("3. Vertex Cover Approximation");
        System.out.println("4. Randomized Quicksort");
        System.out.println("5. Exit");
        System.out.print("Choose an option: ");
 
        String choice = scanner.nextLine().trim();
 
        switch (choice) {
            case "1":
                runCorpusSearch(scanner);
                break;
            case "2":
                runIrrigationFlow();
                break;
            case "3":
                runVertexCoverConcept(scanner);
                break;
            case "4":
                runRandomizedQuicksortConcept();
                break;
            case "5":
            case "exit":
            case "Exit":
                System.out.println("Thank you for using Farm Assist.");
                scanner.close();
                return;
            default:
                System.out.println("Please choose 1, 2, 3, 4, or 5.");
        }
    }
}
}
