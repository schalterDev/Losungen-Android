package schalter.dev.customizelibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by marti on 30.10.2015.
 */
public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    private List<Integer> items;
    private Context context;
    private int checkedItem;

    private GridAdapter.ViewHolder holder;

    private SharedPreferences settings;

    public GridAdapter(List<Integer> colors, Context context, int checkedItem) {
        super();
        items = colors;
        this.checkedItem = checkedItem;
        this.context = context;
        settings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getColorPosition() {
        return checkedItem;
    }

    @Override
    public GridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.color, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final GridAdapter.ViewHolder holder, final int position) {
        this.holder = holder;
        final int color = items.get(position);

        if(checkedItem == position)
            holder.colorImage.setImageResource(R.drawable.circle_checked);
        else
            holder.colorImage.setImageResource(R.drawable.circle);

        holder.colorImage.setColorFilter(color);

        holder.colorImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkedItem = position;
                notifyDataSetChanged();
            }
        });

    }

    public void setCheckedItem(int checkedItem) {
        this.checkedItem = checkedItem;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView colorImage;
        //private String notizenLosung;

        public ViewHolder(View itemView) {
            super(itemView);

            colorImage = (ImageView) itemView.findViewById(R.id.imageView_color);
        }
    }
}
