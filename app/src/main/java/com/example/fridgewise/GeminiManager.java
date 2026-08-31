package com.example.fridgewise;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiManager {

    private final GenerativeModelFutures model;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public GeminiManager(String apiKey) {
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", apiKey);
        model = GenerativeModelFutures.from(gm);
    }

    public interface InsightCallback {
        void onInsightGenerated(String greeting, String title, String description);
        void onError(Throwable t);
    }

    public interface RecipeCallback {
        void onRecipesGenerated(List<RecipeItem> recipes);
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

    public void getRecipeSuggestions(String ingredients, RecipeCallback callback) {
        String prompt = "You are a chef. Based on these ingredients that are about to expire: " + ingredients + 
                "\nSuggest 3 quick recipes. For each recipe, provide:" +
                "\n1. Name" +
                "\n2. Preparation time (e.g., 20 min)" +
                "\n3. Match percentage based on provided ingredients (e.g., 90)" +
                "\n4. Short list of ingredients needed" +
                "\n5. 3 simple steps" +
                "\nReturn the response in this EXACT format for each recipe:" +
                "\nRECIPE_START" +
                "\nName: [Recipe Name]" +
                "\nTime: [Time]" +
                "\nMatch: [Percentage]" +
                "\nIngredients: [List]" +
                "\nSteps: [Steps]" +
                "\nRECIPE_END";

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new com.google.common.util.concurrent.FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String text = result.getText();
                if (text != null) {
                    parseRecipes(text, callback);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError(t);
            }
        }, executor);
    }

    private void parseRecipes(String text, RecipeCallback callback) {
        List<RecipeItem> recipes = new ArrayList<>();
        String[] blocks = text.split("RECIPE_START");
        
        for (String block : blocks) {
            if (!block.contains("RECIPE_END")) continue;
            
            try {
                String name = extract(block, "Name:", "Time:");
                String time = extract(block, "Time:", "Match:");
                int match = Integer.parseInt(extract(block, "Match:", "Ingredients:").replaceAll("[^0-9]", ""));
                String ing = extract(block, "Ingredients:", "Steps:");
                String steps = extract(block, "Steps:", "RECIPE_END");
                
                int imageResId = R.drawable.vegi_img01;
                String lowerName = name.toLowerCase();
                if (lowerName.contains("chicken") || lowerName.contains("meat")) imageResId = R.drawable.non_veg_img01;
                else if (lowerName.contains("salad")) imageResId = R.drawable.vegi_img01;
                else if (lowerName.contains("pasta") || lowerName.contains("noodle")) imageResId = R.drawable.bakery_img01;
                else if (lowerName.contains("drink") || lowerName.contains("smoothie")) imageResId = R.drawable.drinks_img01;

                RecipeItem item = new RecipeItem(name, time, match, imageResId);
                item.setIngredients(ing);
                item.setInstructions(steps);
                
                // Count how many ingredients are in the "Ingredients" list
                try {
                    String[] items = ing.split(",");
                    if (items.length > 1) {
                        item.setExtraItemsCount(items.length - 1);
                    }
                } catch (Exception e) {}

                recipes.add(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        callback.onRecipesGenerated(recipes);
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

    private String extract(String source, String start, String end) {
        int s = source.indexOf(start) + start.length();
        int e = source.indexOf(end, s);
        return source.substring(s, e).trim();
    }
}
