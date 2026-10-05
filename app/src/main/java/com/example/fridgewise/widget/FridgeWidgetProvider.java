package com.example.fridgewise.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.example.fridgewise.R;
import com.example.fridgewise.data.PreferenceManager;

public class FridgeWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        PreferenceManager prefManager = new PreferenceManager(context);
        int streak = prefManager.getStreakCount();

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_fridge);
        views.setTextViewText(R.id.tvWidgetTitle, "🔥 FridgeWise Streak");
        views.setTextViewText(R.id.tvWidgetStatus, "Streak: " + streak + " days active");

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
