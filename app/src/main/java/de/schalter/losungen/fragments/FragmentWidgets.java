package de.schalter.losungen.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.schalter.losungen.R;
import de.schalter.losungen.lists.GridAdapterWidget;

/**
 * Created by martin on 01.04.16.
 */
public class FragmentWidgets extends Fragment {

    private List<Integer> widgetIds;

    public static FragmentWidgets newInstance(List<Integer> widgetIds) {
        FragmentWidgets fragment = new FragmentWidgets();
        fragment.setWidgetIds(widgetIds);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_losung_list, container,
                false);

        initialise(view);

        return view;
    }

    private void initialise(View view) {

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_losung_list);
        recyclerView.setHasFixedSize(false);

        //Number of columns, Tablet has 2 - phone 1
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        RecyclerView.LayoutManager layoutManager;
        if (tabletSize) {
            layoutManager = new GridLayoutManager(view.getContext(), 2);
        } else {
            layoutManager = new GridLayoutManager(view.getContext(), 1);
        }

        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter adapter = new GridAdapterWidget(widgetIds, getContext());
        recyclerView.setAdapter(adapter);
    }

    public void setWidgetIds (List<Integer> widgetIds) {
        this.widgetIds = widgetIds;
    }
}
