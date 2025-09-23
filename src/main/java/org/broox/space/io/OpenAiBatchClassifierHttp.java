package org.broox.space.io;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;

public class OpenAiBatchClassifierHttp {

    private final String apiKey;
    private final String header;
    private final HttpClient client;
    private final ObjectMapper mapper;

    public OpenAiBatchClassifierHttp(String apiKey, String header) {
        this.apiKey = apiKey;
        this.header = header;
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public List<String> classify(List<String> inputs) throws Exception {
        // Build JSON-output-enforcing prompt
        StringBuilder prompt = new StringBuilder(header).append("\n\n");
        for (String s : inputs) {
            prompt.append(s).append("\n");
        }
        prompt.deleteCharAt(prompt.length()-1); // remove last newline
        System.out.println(prompt);

        // Build request body
        ObjectNode body = mapper.createObjectNode();
        body.put("model", "gpt-5-nano");
        ArrayNode messages = body.putArray("messages");
        ObjectNode message = mapper.createObjectNode();
        message.put("role", "user");
        message.put("content", prompt.toString());
        messages.add(message);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode json = mapper.readTree(response.body());
        String content = json.at("/choices/0/message/content").asText();

        // Parse JSON array
        List<String> results = new ArrayList<>();
        try {
            JsonNode arr = mapper.readTree(content);
            if (arr.isArray()) {
                for (JsonNode n : arr) {
                    try {
                        results.add(n.asText().trim());
                    } catch (Exception e) {
                        results.add("0");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse model output as JSON: " + content);
            results = new ArrayList<>(Collections.nCopies(inputs.size(), "0"));
        }

        // Ensure correct size
        if (results.size() != inputs.size()) {
            System.err.printf("Mismatch between input and output sizes (%d vs %d)%n",
                    inputs.size(), results.size());
            while (results.size() < inputs.size()) results.add("0");
            if (results.size() > inputs.size()) results = results.subList(0, inputs.size());
        }

        return results;
    }
}
