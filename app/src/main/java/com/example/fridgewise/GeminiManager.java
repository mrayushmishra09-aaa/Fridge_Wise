package com.example.fridgewise;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiManager {

    private final GenerativeModelFutures model;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public GeminiManager(String apiKey) {
        // We use gemini-1.5-flash because it is free and very fast
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", apiKey);
        model = GenerativeModelFutures.from(gm);
    }

    public interface InsightCallback {
        void onInsightGenerated(String greeting, String title, String description);
        void onError(Throwable t);
    }

    public void getSmartInsight(String contextData, InsightCallback callback) {
        String prompt = "You are FridgeWise, a smart assistant. Based on this data: " + contextData + 
                "\n1. Generate a short, friendly greeting for the user (max 5 words)." +
                "\n2. Generate ONE smart, data-driven insight." +
                "\nReturn the response in this EXACT format:" +
                "\nGreeting: [Your greeting]" +
                "\nTitle: [Catchy insight title]" +
                "\nDescription: [1 sentence observation]";

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new com.google.common.util.concurrent.FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String text = result.getText();
                if (text != null) {
                    parseAndSend(text, callback);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError(t);
            }
        }, executor);
    }

    private void parseAndSend(String text, InsightCallback callback) {
        try {
            String greeting = "Hello!";
            String title = "Today's Insight";
            String desc = text;

            if (text.contains("Greeting:")) {
                greeting = text.substring(text.indexOf("Greeting:") + 9, 
                        text.contains("Title:") ? text.indexOf("Title:") : text.length()).trim();
            }
            if (text.contains("Title:") && text.contains("Description:")) {
                title = text.substring(text.indexOf("Title:") + 6, text.indexOf("Description:")).trim();
                desc = text.substring(text.indexOf("Description:") + 12).trim();
            }
            
            callback.onInsightGenerated(greeting, title, desc);
        } catch (Exception e) {
            callback.onInsightGenerated("Hello!", "Smart Insight", text);
        }
    }
}
