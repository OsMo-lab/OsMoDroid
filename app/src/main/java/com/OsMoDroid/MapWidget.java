package com.OsMoDroid;

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
 * App Widget Configuration implemented in {@link MapWidgetConfigureActivity MapWidgetConfigureActivity}
 */
public class MapWidget extends AppWidgetProvider
    {
        @Override
        public void onReceive(Context context, Intent intent)
            {
                if(intent!=null)
                {
                    Log.d(getClass().getSimpleName(), "on recieve" + context.getPackageName() + " intent=" + intent);
                    Bundle extras = intent.getExtras();
                    if(extras!=null)
                        {
                            for (String key : extras.keySet())
                                {
                                    Object value = extras.get(key);
                                    if (value != null)
                                        {
                                            Log.d(getClass().getSimpleName(), "on recieve" + String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
                                        }
                                }
                        }
                }
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName thisAppWidget = new ComponentName(context.getPackageName(), MapWidget.class.getName());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
                onUpdate(context, appWidgetManager, appWidgetIds);

                super.onReceive(context, intent);
            }

        @Override
        public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
            {
                // There may be multiple widgets active, so update all of them
                final int N = appWidgetIds.length;
                for (int i = 0; i < N; i++)
                    {
                        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.map_widget);

                       // mMapView = new MapView(context);
                    }
            }
        @Override
        public void onDeleted(Context context, int[] appWidgetIds)
            {
                // When the user deletes the widget, delete the preference associated with it.

            }
        @Override
        public void onEnabled(Context context)
            {
                Log.d(getClass().getSimpleName(), "on enable"+context.getPackageName());
                // Enter relevant functionality for when the first widget is created
            }
        @Override
        public void onDisabled(Context context)
            {
                Log.d(getClass().getSimpleName(), "on disable"+context.getPackageName());
            }
    }

