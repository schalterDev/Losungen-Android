package schalter.dev.customizelibrary;

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
    private int checkedItem;

    GridAdapter(List<Integer> colors, int checkedItem) {
        super();
        items = colors;
        this.checkedItem = checkedItem;
    }

    int getColorPosition() {
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

    void setCheckedItem(int checkedItem) {
        this.checkedItem = checkedItem;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView colorImage;
        //private String notizenLosung;

        ViewHolder(View itemView) {
            super(itemView);

            colorImage = (ImageView) itemView.findViewById(R.id.imageView_color);
        }
    }
}
