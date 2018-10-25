package de.schalter.losungen.lists;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import de.schalter.losungen.AppWidgetActivity;
import de.schalter.losungen.Losung;
import de.schalter.losungen.R;
import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.settings.Tags;

/**
 * Created by martin on 01.04.16.
 */
public class GridAdapterWidget extends RecyclerView.Adapter<GridAdapterWidget.ViewHolder> {

    private List<Integer> items;
    private DBHandler dbHandler;
    private Context context;

    private SharedPreferences settings;

    public GridAdapterWidget(List<Integer> appWidgetIds, Context context) {
        super();
        items = appWidgetIds;
        this.context = context;
        dbHandler = DBHandler.newInstance(context);
        settings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public GridAdapterWidget.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.appwidget, parent, false);

        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final GridAdapterWidget.ViewHolder holder, final int position) {
        final int appWidgetId = items.get(position);

        //Set text of Losung
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        String text = "";
        int content = settings.getInt("widgetcontent" + appWidgetId, Tags.LOSUNG_UND_LEHRTEXT_NOTIFICATION);
        final int schriftgroesse = settings.getInt("widgetfontsize" + appWidgetId, -2);
        final int color = settings.getInt("widgetcolor" + appWidgetId, -2);
        final int background = settings.getInt("widgetbackground" + appWidgetId, -2);

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

        holder.text.setText(text);

        //Set textColor
        if(color != -2)
            holder.text.setTextColor(color);

        //Set backgroundColor
        if(background != -2) {
            holder.relativeLayout.setBackgroundColor(background);
        }

        //Set font-size
        if(schriftgroesse != -2)
            holder.text.setTextSize(schriftgroesse);

        //Set onClickListener
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle extras = new Bundle();
                extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                extras.putInt(AppWidgetActivity.COLOR, color);
                extras.putInt(AppWidgetActivity.BACKGROUND, background);
                extras.putInt(AppWidgetActivity.FONTSIZE, schriftgroesse);

                Intent intentWidget = new Intent(context, AppWidgetActivity.class);
                intentWidget.putExtras(extras);

                context.startActivity(intentWidget);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView text;
        private RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            text = itemView.findViewById(R.id.textView_main);
            relativeLayout = itemView.findViewById(R.id.relLayout_widget);
        }
    }
}
