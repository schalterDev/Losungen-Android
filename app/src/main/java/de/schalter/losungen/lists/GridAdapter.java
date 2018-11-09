package de.schalter.losungen.lists;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import de.schalter.losungen.Losung;
import de.schalter.losungen.MainActivity;
import de.schalter.losungen.R;
import de.schalter.losungen.dialogs.BibleDialog;
import de.schalter.losungen.dialogs.ShareDialog;
import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.settings.Tags;

/**
 * Created by marti on 30.10.2015.
 */
public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    private List<Losung> items;
    private DBHandler dbHandler;
    private Context context;

    private boolean useSmallLayout = false;

    private boolean fav;

    private SharedPreferences settings;

    public GridAdapter(List<Losung> losungen, Context context, boolean fav, boolean small) {
        this(losungen, context, fav);
        useSmallLayout = small;
    }

    public GridAdapter(List<Losung> losungen, Context context, boolean fav) {
        super();
        items = losungen;
        this.fav = fav;
        this.context = context;
        dbHandler = DBHandler.newInstance(context);
        settings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public GridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        if(useSmallLayout) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.losung_klein_card, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.losung_lehrtext_card, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final GridAdapter.ViewHolder holder, int position) {
        final Losung losung = items.get(holder.getAdapterPosition());
        String datum = Losung.getDatumLongFromTime(losung.getDate());

        if(losung.getTitleLosung().trim().equals("")) {
            holder.titleLosung.setText(String.format("%s %s", context.getResources().getString(R.string.losung_from), Losung.getFullDatumFromTime(losung.getDate())));
        } else {
            holder.titleLosung.setText(losung.getTitleLosung());
        }

        if(losung.getTitleLehrtext().trim().equals("")) {
            holder.titleLehrtext.setText(context.getResources().getString(R.string.lehrtext));
        } else {
            holder.titleLehrtext.setText(losung.getTitleLehrtext());
        }

        holder.losungstext.setText(losung.getLosungstext());
        holder.losungsvers.setText(losung.getLosungsvers());
        holder.lehrtext.setText(losung.getLehrtext());
        holder.lehrtextVers.setText(losung.getLehrtextVers());
        holder.setNotes(losung.getNotesLosung());
        //holder.notizen.setText(losung.getNotesLosung());
        holder.datum = losung.getDate();
        holder.markiert = losung.isMarked();

        if(!losung.isMarked()) {
            holder.imageView.setImageResource(R.drawable.ic_star_notfilled);
        }

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(fav) {
                    items.remove(holder.getAdapterPosition());
                    dbHandler.removeMarkiert(losung.getDate());
                    GridAdapter.this.notifyDataSetChanged();

                    MainActivity activity = MainActivity.getInstance();
                    if(activity != null)
                        activity.snackbar(activity.getResources().getString(R.string.remove_fav), Snackbar.LENGTH_SHORT, true);
                } else {
                    if(holder.markiert) {
                        dbHandler.removeMarkiert(losung.getDate());
                        holder.imageView.setImageResource(R.drawable.ic_star_notfilled);

                        MainActivity activity = MainActivity.getInstance();
                        if(activity != null)
                            activity.snackbar(activity.getResources().getString(R.string.remove_fav), Snackbar.LENGTH_SHORT, true);
                    } else {
                        dbHandler.setMarkiert(losung.getDate());
                        holder.imageView.setImageResource(R.drawable.ic_star_filled);

                        MainActivity activity = MainActivity.getInstance();
                        if(activity != null)
                            activity.snackbar(activity.getResources().getString(R.string.add_fav), Snackbar.LENGTH_SHORT, true);
                    }
                }

                holder.markiert = !holder.markiert;
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView losungstext;
        private TextView losungsvers;
        private TextView lehrtext;
        private TextView lehrtextVers;
        private TextView titleLosung;
        private TextView titleLehrtext;
        private EditText notizen;
        private CardView cardView;
        private ImageView imageView;
        private long datum;
        private boolean markiert;
        private RelativeLayout relativeLayout;
        //private String notizenLosung;

        public ViewHolder(View itemView) {
            super(itemView);

            losungstext = itemView.findViewById(R.id.textView_losungText);
            losungsvers = itemView.findViewById(R.id.textView_losungVers);
            lehrtext = itemView.findViewById(R.id.textView_lehrtext);
            lehrtextVers = itemView.findViewById(R.id.textView_lehrtextVers);
            titleLehrtext = itemView.findViewById(R.id.textView_lehrtextTitle);
            titleLosung = itemView.findViewById(R.id.textView_losungTitle);
            notizen = itemView.findViewById(R.id.editText_notizen_losung_lehrtext);
            cardView = itemView.findViewById(R.id.card_view_losung_lehrtext);
            imageView = itemView.findViewById(R.id.imageView_mark);

            cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String[] items = {context.getResources().getString(R.string.losung),
                            context.getResources().getString(R.string.lehrtext),
                            context.getResources().getString(R.string.losung_and_lehrtext),
                            context.getResources().getString(R.string.losung_and_lehrtext)};

                    String title = context.getResources().getString(R.string.losung_from) + " " + Losung.getDatumFromTime(datum);

                    String[] titles = {title, title, title};
                    String[] content = new String[3];

                    content[0] = String.valueOf(losungstext.getText() + System.getProperty("line.separator") + losungsvers.getText());
                    content[1] = String.valueOf(lehrtext.getText() + System.getProperty("line.separator") + lehrtextVers.getText());
                    content[2] = content[0] + System.getProperty("line.separator") + System.getProperty("line.separator") + content[1];

                    ShareDialog dialog = new ShareDialog(context, items, titles, content);
                    dialog.show();
                    return true;
                }
            });

            //Open in Quick Bible
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (settings.getBoolean(Tags.OPEN_WITH_APP, false)) {
                        try {
                            BibleDialog bibleDialog = new BibleDialog(context);
                            bibleDialog.loadVers(String.valueOf(losungsvers.getText()));
                            bibleDialog.openApp();
                        } catch (IOException | ActivityNotFoundException e) {
                            MainActivity.toast(context, context.getResources().getString(R.string.open_in_app_failed),
                                    Toast.LENGTH_SHORT);
                        }
                    }
                }
            });

            if(settings.getBoolean(Tags.PREF_SHOWNOTES, true)) {
                notizen.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if(ViewHolder.this.notesChanged()) {
                            if (dbHandler != null)
                                dbHandler.editLosungNotiz(datum, s.toString());
                        } else {
                            ViewHolder.this.notesAreChanged();
                        }
                    }
                });
            } else {
                relativeLayout = itemView.findViewById(R.id.relative_layout_losung_card);
                relativeLayout.removeView(notizen);
            }


        }

        private boolean notesChanged = true;

        public void setNotes(String note) {
            notesChanged = false;
            notizen.setText(note);
        }

        public boolean notesChanged() {
            return notesChanged;
        }

        private void notesAreChanged() {
            notesChanged = true;
        }

    }
}
