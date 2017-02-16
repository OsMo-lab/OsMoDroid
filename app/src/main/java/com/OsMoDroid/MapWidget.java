package com.OsMoDroid;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import org.osmdroid.views.MapView;
/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link MapWidgetConfigureActivity MapWidgetConfigureActivity}
 */
public class MapWidget extends AppWidgetProvider
    {
        @Override
        public void onReceive(Context context, Intent intent)
            {
                Log.d(getClass().getSimpleName(), "on recieve"+context.getPackageName());
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
                for (int appWidgetId : appWidgetIds)
                    {
                        MapWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
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

