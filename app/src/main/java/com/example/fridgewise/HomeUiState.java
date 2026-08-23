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
    public final boolean isLoading;

    public HomeUiState(List<AttentionItem> attentionItems, 
                      String insightTitle, 
                      String insightDescription, 
                      String greeting,
                      String userName,
                      List<ActivityRecord> recentActivities,
                      boolean isLoading) {
        this.attentionItems = attentionItems;
        this.insightTitle = insightTitle;
        this.insightDescription = insightDescription;
        this.greeting = greeting;
        this.userName = userName;
        this.recentActivities = recentActivities;
        this.isLoading = isLoading;
    }
}
