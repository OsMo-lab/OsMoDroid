package com.OsMoDroid;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class SOSWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {


        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.soswidget);
        Intent is = new Intent(context.getApplicationContext(), LocalService.class);


        is.putExtra("GCM", "WIDGETSOS");
        is.setClass(context,LocalService.class);
        PendingIntent sospintent = PendingIntent.getService(context.getApplicationContext(), 101, is, PendingIntent.FLAG_MUTABLE);
        if(LocalService.sos)
        {
            views.setInt(R.id.soswidgetbutton, "setBackgroundColor", Color.RED);
        }
        else
        {
            views.setInt(R.id.soswidgetbutton, "setBackgroundColor", Color.parseColor("#ffff8800"));
        }

        views.setOnClickPendingIntent(R.id.soswidgetbutton, sospintent);
        appWidgetManager.updateAppWidget(new ComponentName(context, OsMoWidget.class), views);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(getClass().getSimpleName(), "on recieve"+context.getPackageName());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), SOSWidget.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        onUpdate(context, appWidgetManager, appWidgetIds);

        super.onReceive(context, intent);
    }
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

