//package org.broox.space.io;
//
//import org.apache.hc.client5.http.classic.methods.HttpPost;
//import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
//import org.apache.hc.client5.http.impl.classic.HttpClients;
//import org.apache.hc.core5.http.io.entity.EntityUtils;
//import org.apache.hc.core5.http.io.entity.StringEntity;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import java.io.IOException;
//import java.util.*;
//
//public class GeminiBatchClassifier {
//
//    private final String apiKey;
//    private final String model = "gemini-1.5-flash"; // Gemini model
//
//    public GeminiBatchClassifier(String apiKey) {
//        this.apiKey = apiKey;
//    }
//
//    public List<Integer> classify(List<String> companyNames) throws IOException {
//        List<Integer> indices = new ArrayList<>();
//        for (String companyName : companyNames) {
//            String prompt = "For the company name '" + companyName + "',return the index of the word that best summarizes the company. " +
//                "Respond with only one integer per line, in the same order as the inputs.";
//            String response = sendRequest(prompt);
//            int index = parseResponse(response);
//            indices.add(index);
//        }
//        return indices;
//    }
//
//    private String sendRequest(String prompt) throws IOException {
//        try (CloseableHttpClient client = HttpClients.createDefault()) {
//            HttpPost post = new HttpPost("https://generativelanguage.googleapis.com/v1beta2/models/" + model + ":generateText");
//            post.setHeader("Authorization", "Bearer " + apiKey);
//            post.setHeader("Content-Type", "application/json");
//
//            String json = "{ \"prompt\": { \"text\": \"" + prompt + "\" } }";
//            post.setEntity(new StringEntity(json));
//
//            return client.execute(post, response -> EntityUtils.toString(response.getEntity()));
//        }
//    }
//
//    private int parseResponse(String response) throws IOException {
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode rootNode = mapper.readTree(response);
//        JsonNode textNode = rootNode.path("candidates").get(0).path("output").path("text");
//        String text = textNode.asText().trim();
//        return Integer.parseInt(text);
//    }
//
//    public static void main(String[] args) {
//        try {
//            String apiKey = System.getenv("GEMINI_API_KEY");
//            GeminiBatchClassifier classifier = new GeminiBatchClassifier(apiKey);
//
//            List<String> companies = List.of(
//                "NVIDIA Corporation",
//                "Microsoft Corp",
//                "Tesla Inc",
//                "Apple Incorporated"
//            );
//
//            List<Integer> indices = classifier.classify(companies);
//
//            for (int i = 0; i < companies.size(); i++) {
//                System.out.println(companies.get(i) + " → " + indices.get(i));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
