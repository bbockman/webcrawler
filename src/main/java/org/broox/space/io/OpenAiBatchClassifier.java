/*
package org.broox.space.io;

import com.openai.OpenAI;
import com.openai.api.resources.chat.Chat;
import com.openai.api.resources.chat.types.ChatCompletionCreateResponse;
import com.openai.api.resources.chat.types.ChatCompletionMessageParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenAiBatchClassifier {

    private final OpenAI client;
    private final String header;

    public OpenAiBatchClassifier(String apiKey, String header) {
        this.client = OpenAI.builder().apiKey(apiKey).build();
        this.header = header;
    }

    */
/**
     * Classify each string in the input list.
     * Returns a list of integers, one per string (same order).
     *//*

    public List<Integer> classify(List<String> inputs) {
        // Build the prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append(header).append("\n\n");
        for (int i = 0; i < inputs.size(); i++) {
            prompt.append(i).append(": ").append(inputs.get(i)).append("\n");
        }

        // Call OpenAI API
        Chat chat = client.chat();
        ChatCompletionCreateResponse response = chat.completions()
                .create(r -> r
                        .model("gpt-4o-mini") // fast + cheap, adjust if needed
                        .messages(ChatCompletionMessageParam.ofUser(prompt.toString()))
                        .temperature(0.0)
                );

        // Get the model's reply (as plain text)
        String content = response.choices().get(0).message().content().get(0).text();

        // Parse numbers line by line
        List<Integer> results = new ArrayList<>();
        for (String line : content.split("\\r?\\n")) {
            line = line.trim();
            if (!line.isEmpty()) {
                try {
                    results.add(Integer.parseInt(line));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Unexpected output from model: " + line);
                }
            }
        }

        // Sanity check: one result per input
        if (results.size() != inputs.size()) {
            throw new RuntimeException("Mismatch: expected " + inputs.size() +
                    " results but got " + results.size());
        }

        return results;
    }

    // Example usage
    public static void main(String[] args) {
        String apiKey = System.getenv("OPENAI_API_KEY"); // safer than hardcoding
        String header = "For each company name below, return the index of the word that best summarizes the company. " +
                "Respond with only one integer per line, in the same order as the inputs.";

        OpenAiBatchClassifier classifier = new OpenAiBatchClassifier(apiKey, header);

        List<String> companies = Arrays.asList(
                "NVIDIA Corporation",
                "Microsoft Corp",
                "Tesla Inc",
                "Apple Incorporated"
        );

        List<Integer> results = classifier.classify(companies);

        for (int i = 0; i < companies.size(); i++) {
            System.out.println(companies.get(i) + " → " + results.get(i));
        }
    }
}
*/
