package de.schalter.losungen.services;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import java.util.Calendar;

import de.schalter.losungen.Losung;
import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.settings.Tags;

/**
 * Created by marti on 04.11.2015.
 */
public class WidgetBroadcast extends AppWidgetProvider {

    /*
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context,
                                          AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        Log.d("Losungen", "Changed dimensions");

        // See the dimensions and
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        // Get min width and height.
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = options
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        // Obtain appropriate widget and update it.
        appWidgetManager.updateAppWidget(appWidgetId,
                getRemoteViews(context, minWidth, minHeight));

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
                newOptions);
    }

    private RemoteViews getRemoteViews(Context context, int minWidth,
                                       int minHeight) {
        // First find out rows and columns based on width provided.
        int rows = getCellsForSize(minHeight);
        int columns = getCellsForSize(minWidth);

        if (columns == 4) {
            // Get 4 column widget remote view and return
            return null;
        } else {
            // Get appropriate remote view.
            return new RemoteViews(context.getPackageName(),
                    R.layout.appwidget);
        }
    }*/

    /**
     * Returns number of cells needed for given size of the widget.
     *
     * @param size Widget size in dp.
     * @return Size in number of cells.
     */
    private static int getCellsForSize(int size) {
        int n = 2;
        while (70 * n - 30 < size) {
            ++n;
        }
        return n - 1;
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();

        for (int appWidgetId : appWidgetIds) {
            editor.remove("widgetcontent" + appWidgetId);
            editor.remove("widgetfontsize" + appWidgetId);
            editor.remove("widgetcolor" + appWidgetId);
            editor.remove("widgetbackground" + appWidgetId);
        }

        editor.apply();

        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            String text = "";
            int appWidgetId = appWidgetIds[i];
            int content = settings.getInt("widgetcontent" + appWidgetId, Tags.LOSUNG_UND_LEHRTEXT_NOTIFICATION);
            int schriftgroesse = settings.getInt("widgetfontsize" + appWidgetId, -2);
            int color = settings.getInt("widgetcolor" + appWidgetId, -2);
            int background = settings.getInt("widgetbackground" + appWidgetId, -2);

            // Create an Intent to launch MainActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
            views.setOnClickPendingIntent(R.id.relLayout_widget, pendingIntent);

            Calendar calendar = Calendar.getInstance();
            DBHandler dbHandler = DBHandler.newInstance(context);
            Losung losung = dbHandler.getLosung(calendar.getTimeInMillis());

            switch(content) {
                case Tags.LOSUNG_NOTIFICATION:
                    text = losung.getLosungstext() + System.getProperty("line.separator") +
                            losung.getLosungsvers();
                    break;
                case Tags.LEHRTEXT_NOTIFICATION:
                    text = losung.getLehrtext() + System.getProperty("line.separator") +
                            losung.getLehrtextVers();
                    break;
                case Tags.LOSUNG_UND_LEHRTEXT_NOTIFICATION:
                    text = losung.getLosungstext() + " " +
                            losung.getLosungsvers() + System.getProperty("line.separator") + System.getProperty("line.separator") +
                            losung.getLehrtext() + " " +
                            losung.getLehrtextVers();
                    break;
            }

            views.setTextViewText(R.id.textView_main, text);

            if(color != -2)
                views.setTextColor(R.id.textView_main, color);

            if(background != -2) {
                views.setInt(R.id.relLayout_widget, "setBackgroundColor", background);
            }

            if(schriftgroesse != -2)
                views.setFloat(R.id.textView_main, "setTextSize", schriftgroesse);


            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }


}
