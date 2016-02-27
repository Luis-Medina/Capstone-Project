package com.luismedinaweb.whatsthat;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.luismedinaweb.whatsthat.UI.MainView.MainActivity;

/**
 * Created by lmedina on 2/22/2016.
 */
public class WTWidgetProvider extends AppWidgetProvider {

    public static final String TOAST_ACTION = "com.metrostarsystems.woozy.appwidget.TOAST_ACTION";
    public static final String EXTRA_ITEM = "com.metrostarsystems.woozy.appwidget.EXTRA_ITEM";
    private static final String REFRESH_ACTION = "com.metrostarsystems.woozy.appwidget.action.REFRESH";
    public static final String KEY_DATA = "data";

    public static Intent getRefreshBroadcastIntent(Context context) {
        return new Intent(REFRESH_ACTION)
                .setComponent(new ComponentName(context, WTWidgetProvider.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        if (intent.getAction().equals(TOAST_ACTION)) {
            int viewIndex = intent.getIntExtra(EXTRA_ITEM, 0);
            Intent newIntent = new Intent(context, MainActivity.class);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(newIntent);
        }

        super.onReceive(context, intent);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        //Log.e("WIDGETPROVIDER", "OnUpdate called");

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, MainActivity.class);
            intent.setAction(MainActivity.ACTION_TAKE_PHOTO);
            //intent.putExtra(NavigationView.ACTION_REPORT, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
            views.setOnClickPendingIntent(R.id.widget_imageView, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}


