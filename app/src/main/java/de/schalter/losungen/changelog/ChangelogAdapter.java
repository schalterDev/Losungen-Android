package de.schalter.losungen.changelog;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import de.schalter.losungen.R;

/**
 * Created by Smarti on 14.01.2016.
 */
public class ChangelogAdapter extends ArrayAdapter<ChangelogElement> {

    private Context context;
    private List<ChangelogElement> items;

    public ChangelogAdapter(Context context, List<ChangelogElement> items) {
        super(context, R.layout.changelog_card, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.changelog_card, parent, false);

        // 3. Get the two text view from the rowView
        TextView titleView = rowView.findViewById(R.id.changelog_title);
        TextView contentView = rowView.findViewById(R.id.changelog_content);

        // 4. Set the text for textView
        List<String> changes = items.get(position).changes;
        List<Boolean> important = items.get(position).important;

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < changes.size(); i++) {
            if(i != 0) {
                stringBuilder.append("<br/>").append("<br/>");
            }

            if(important.get(i)) {
                //Text rot hervorheben
                stringBuilder.append("<font color=red>").append(changes.get(i)).append("</font>");
            } else {
                stringBuilder.append(changes.get(i));
            }
        }

        String styledText = stringBuilder.toString();

        titleView.setText(items.get(position).appVesionNice);
        contentView.setText(Html.fromHtml(styledText));

        // 5. retrn rowView
        return rowView;
    }

}
