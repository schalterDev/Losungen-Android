package de.schalter.losungen.customViews;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import de.schalter.losungen.R;

/**
 * Created by martin on 28.10.2015.
 */
public class CardLosung extends CardView {

    private TextView title;
    private TextView text;
    private TextView vers;

    public CardLosung(Context context) {
        super(context);
        initComponent(context);
    }

    public CardLosung(Context context, AttributeSet attrs) {
        super(context, attrs);
        initComponent(context);
    }

    private void initComponent (Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.losung_card, null, false);
        this.addView(v);

        text = (TextView) v.findViewById(R.id.textView_losungText);
        title = (TextView) v.findViewById(R.id.textView_losungTitle);
        vers = (TextView) v.findViewById(R.id.textView_losungVers);
    }

    public void setLosungsText(String text) {
        this.text.setText(text);
    }

    public void setLosungsVers(String vers) {
        this.vers.setText(vers);
    }

    public void setLosungsTitle(String title) {
        this.title.setText(title);
    }
}
