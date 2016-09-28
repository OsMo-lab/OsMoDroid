package com.OsMoDroid;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
/**
 * Implementation of App Widget functionality.
 */
public class DoubleOsmoWidget extends AppWidgetProvider
    {
        @Override
        public void onReceive(Context context, Intent intent)
            {
                Log.d(getClass().getSimpleName(), "on recieve"+context.getPackageName());
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName thisAppWidget = new ComponentName(context.getPackageName(), DoubleOsmoWidget.class.getName());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
                onUpdate(context, appWidgetManager, appWidgetIds);

                super.onReceive(context, intent);
            }
        @Override
        public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
            {
                Log.d(getClass().getSimpleName(), "on update"+context.getPackageName());
                final int N = appWidgetIds.length;
                for (int i = 0; i < N; i++)
                    {
                        Log.d(getClass().getSimpleName(), "on update for");
                        Intent is = new Intent(context.getApplicationContext(), LocalService.class);
                        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.double_osmo_widget);
                        remoteViews.setImageViewResource(R.id.imageButtonWidget, R.drawable.off);
                        is.putExtra("ACTION", "START");
                        is.setClass(context,LocalService.class);
                        PendingIntent stop = PendingIntent.getService(context.getApplicationContext(), 0, is, 0);
                        Log.d(getClass().getSimpleName(), "on update for pinetn="+stop.toString());
                        remoteViews.setOnClickPendingIntent(R.id.imageButtonWidget, stop);
                        appWidgetManager.updateAppWidget(new ComponentName(context, DoubleOsmoWidget.class), remoteViews);
                    }
            }
        @Override
        public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)
            {
                Log.d(getClass().getSimpleName(), "on optyionschanged");
                super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
            }
        @Override
        public void onDeleted(Context context, int[] appWidgetIds)
            {
                Log.d(getClass().getSimpleName(), "on deleted");
                super.onDeleted(context, appWidgetIds);
            }
        @Override
        public void onEnabled(Context context)
            {
                Log.d(getClass().getSimpleName(), "on enabled");
                // Enter relevant functionality for when the first widget is created
            }
        @Override
        public void onDisabled(Context context)
            {
                Log.d(getClass().getSimpleName(), "on disabled");
            }
    }

