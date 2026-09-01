package com.example.fridgewise;

import java.util.List;

/**
 * A data class representing the UI state of the HomeFragment.
 */
public class HomeUiState {
    public final List<AttentionItem> attentionItems;
    public final String insightTitle;
    public final String insightDescription;
    public final String greeting;
    public final String userName;
    public final List<ActivityRecord> recentActivities;
    public final List<RecipeItem> suggestedRecipes;
    public final String smartTip;
    public final boolean hasActionableItems;
    public final boolean isLoading;

    public HomeUiState(List<AttentionItem> attentionItems, 
                      String insightTitle, 
                      String insightDescription, 
                      String greeting,
                      String userName,
                      List<ActivityRecord> recentActivities,
                      List<RecipeItem> suggestedRecipes,
                      String smartTip,
                      boolean hasActionableItems,
                      boolean isLoading) {
        this.attentionItems = attentionItems;
        this.insightTitle = insightTitle;
        this.insightDescription = insightDescription;
        this.greeting = greeting;
        this.userName = userName;
        this.recentActivities = recentActivities;
        this.suggestedRecipes = suggestedRecipes;
        this.smartTip = smartTip;
        this.hasActionableItems = hasActionableItems;
        this.isLoading = isLoading;
    }
}
