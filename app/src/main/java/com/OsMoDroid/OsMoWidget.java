package com.OsMoDroid;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
/**
 * Implementation of App Widget functionality.
 */
public class OsMoWidget extends AppWidgetProvider
    {
        @Override
        public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
            {
                // There may be multiple widgets active, so update all of them
                final int N = appWidgetIds.length;
                for (int i = 0; i < N; i++)
                    {
                        Intent is = new Intent(context, LocalService.class);
                        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.os_mo_widget);
                        remoteViews.setImageViewResource(R.id.imageButtonWidget, R.drawable.eyen);
                        is.putExtra("ACTION", "START");
                        PendingIntent stop = PendingIntent.getService(context, 0, is, PendingIntent.FLAG_UPDATE_CURRENT);
                        remoteViews.setOnClickPendingIntent(R.id.imageButtonWidget, stop);
                        appWidgetManager.updateAppWidget(N, remoteViews);
                    }
            }
        @Override
        public void onEnabled(Context context)
            {
                // Enter relevant functionality for when the first widget is created
            }
        @Override
        public void onDisabled(Context context)
            {
                // Enter relevant functionality for when the last widget is disabled
            }

    }

