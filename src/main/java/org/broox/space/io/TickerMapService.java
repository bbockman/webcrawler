package org.broox.space.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TickerMapService {

    private final File sourceFile;
    private final ObjectMapper mapper;

    public TickerMapService(File sourceFile) {
        this.sourceFile = sourceFile;
        this.mapper = new ObjectMapper();
    }

    /**
     * Parse original JSON into Map<ticker, [title]>
     */
    public Map<String, List<String>> parseToMap() throws IOException {
        Map<String, List<String>> result = new HashMap<>();
        JsonNode root = mapper.readTree(sourceFile);

        for (Iterator<String> it = root.fieldNames(); it.hasNext(); ) {
            String key = it.next();
            JsonNode companyJson = root.get(key);
            String ticker = companyJson.get("ticker").asText();
            String title = companyJson.get("title").asText();
            result.put(ticker, new ArrayList<>(Collections.singletonList(title)));
        }
        return result;
    }

    /**
     * Serialize map to JSON file
     */
    public void writeMapToFile(Map<String, List<String>> map, File outFile) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(outFile, map);
    }

    /**
     * Read JSON file back into same map format
     */
    public Map<String, List<String>> readMapFromFile(File inFile) throws IOException {
        Map<String, List<String>> result = new HashMap<>();
        JsonNode root = mapper.readTree(inFile);

        for (Iterator<String> it = root.fieldNames(); it.hasNext(); ) {
            String ticker = it.next();
            List<String> values = new ArrayList<>();
            for (JsonNode n : root.get(ticker)) {
                values.add(n.asText());
            }
            result.put(ticker, values);
        }
        return result;
    }

    // Example usage
    public static void main(String[] args) throws Exception {
        File input = new File("src/resources/Tickers.json");
        TickerMapService service = new TickerMapService(input);

        // Step 1: Parse file
        Map<String, List<String>> tickerMap = service.parseToMap();
        System.out.println("Parsed map: " + tickerMap);

        String apiKey = System.getenv("OPENAI_API_KEY");
        String header = "Each line following these instructions represents a company's name. " +
                "Return a JSON array of integers, each integer is the index of the SINGLE word " +
                "that best distinguishes the company name. Indices start at 0, whitespace increments index, " +
                "punctuation is part of the word to which it is attached. If unsure, use 0.  " +
                "\nExample: if the line reads 'Apple Inc', the correct response would be 0 (Apple). " +
                "\nExample: if the line reads 'The Coca-Cola Company', the correct response would be 1 (Coca-Cola). " +
                "\nExample: 'iANTHUS CAPITAL HOLDINGS' -> 0 (iANTHUS). " +
                "\nExample: 'EWHERE HOLDINGS INC' -> 0 (EWHERE). " +
                "\nExample: 'LMP CAPITAL & INCOME FUND INC.' -> 0 (LMP). " +
                "\nExample: 'Cavitation Technologies, Inc.' -> 0 (Cavitation). " +
                "\nExample: 'BEWHERE HOLDINGS INC' -> 0 (BEWHERE). " +
                "\nThe result should include 36 entries, one for each line. " +
                "\nRespond ONLY with a valid JSON array of integers. The array length MUST equal the number of input " +
                "lines (36). Example: [0,1,0,...]";

        OpenAiBatchClassifierHttp classifier = new OpenAiBatchClassifierHttp(apiKey, header);

        List<String> companies = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        Map<String, List<String>> resultMap = new HashMap<>();
        int total = 0;
        int cnt = 0;
        int size = tickerMap.size();

        for (String key : tickerMap.keySet()) {
            companies.add(tickerMap.get(key).getLast());
            titles.add(key);

            if (cnt == 35 || total == size - 1) {
                List<String> results = classifier.classify(companies);
                System.out.println("Batch size: " + companies.size());
                //System.out.println(companies);

                for (int i = 0; i < results.size() && i < titles.size(); i++) {
                    String title = companies.get(i);
                    String ticker = titles.get(i);
                    int index = 0;
                    try {
                        index = Integer.parseInt(results.get(i));
                    } catch (NumberFormatException e) {
                        System.err.println("Failed to parse index for " + title + ": " + results.get(i));
                    }

                    // Split title into words
                    List<String> words = Arrays.asList(title.split("\\s+"));

                    // Clamp index
                    if (index < 0 || index >= words.size()) {
                        System.err.println("Index out of bounds for " + title + " with index " + index);
                        index = 0;
                    }

                    String keyword = words.get(index);
                    resultMap.computeIfAbsent(ticker, k -> new ArrayList<>()).add(keyword);
                    resultMap.get(ticker).add(title);
                    resultMap.get(ticker).add(ticker);
                }

                companies.clear();
                titles.clear();
                cnt = 0;
            } else {
                cnt++;
            }
            total++;
        }

        // Step 2: Write map
        File outFile = new File("src/resources/TickerMap.json");
        service.writeMapToFile(resultMap, outFile);
        System.out.println("Wrote TickerMap.json");

        // Step 3: Read back
        Map<String, List<String>> loadedMap = service.readMapFromFile(outFile);
        System.out.println("Loaded map: " + loadedMap);
    }
}
