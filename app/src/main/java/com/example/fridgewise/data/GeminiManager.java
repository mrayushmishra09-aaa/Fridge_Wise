package com.example.fridgewise.data;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiManager {

    private final GenerativeModelFutures model;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    public GeminiManager(String apiKey) {
        GenerationConfig.Builder builder = new GenerationConfig.Builder();
        builder.responseMimeType = "application/json";
        GenerationConfig config = builder.build();
        
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", apiKey, config);
        model = GenerativeModelFutures.from(gm);
    }

    public interface InsightCallback {
        void onInsightGenerated(String greeting, String title, String description);
        void onError(Throwable t);
    }

    private static class InsightResponse {
        String greeting;
        String title;
        String description;
    }

    public void getSmartInsight(String contextData, InsightCallback callback) {
        String prompt = "You are FridgeWise, a highly intelligent and empathetic life assistant. Based on this user data: " + contextData +
                "\n\nTASKS:" +
                "\n1. GREETING: Generate a very personal, friendly greeting (e.g., 'Good morning, Ayush! Ready to conquer?'). Max 5 words." +
                "\n2. INSIGHT TITLE: A short, catchy heading for today's main focus." +
                "\n3. DESCRIPTION: A concise summary of the day. Connect the dots: if meds are remaining, remind them; if food is expiring, suggest using it; if shopping is long, encourage a trip. Make it sound like a proactive friend, not a computer." +
                "\n\nReturn the response in this JSON format:" +
                "\n{" +
                "\n  \"greeting\": \"...\", " +
                "\n  \"title\": \"...\", " +
                "\n  \"description\": \"...\"" +
                "\n}";

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String text = result.getText();
                if (text != null) {
                    try {
                        InsightResponse insight = gson.fromJson(text, InsightResponse.class);
                        if (insight != null) {
                            callback.onInsightGenerated(insight.greeting, insight.title, insight.description);
                        } else {
                            callback.onError(new Exception("Parsed insight is null"));
                        }
                    } catch (Exception e) {
                        callback.onError(e);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError(t);
            }
        }, executor);
    }
}
