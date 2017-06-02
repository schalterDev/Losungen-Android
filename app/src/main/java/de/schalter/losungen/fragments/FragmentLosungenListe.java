package de.schalter.losungen.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.schalter.losungen.Losung;
import de.schalter.losungen.R;
import de.schalter.losungen.files.DBHandler;
import de.schalter.losungen.lists.GridAdapter;

/**
 * Created by marti on 30.10.2015.
 */
public class FragmentLosungenListe extends Fragment {

    //Contains all daily words
    private List<Losung> losungen;

    //Is this fragment showing the favorite daily words
    //or search results
    private boolean fav = false;

    public static FragmentLosungenListe newInstance(List<Losung> losungen) {
        FragmentLosungenListe fragment = new FragmentLosungenListe();
        fragment.setLosungen(losungen);

        return fragment;
    }

    public static FragmentLosungenListe newInstance(Context context, boolean favoriten) {
        if(favoriten) {
            FragmentLosungenListe fragment = FragmentLosungenListe.newInstance(getFavoriten(context));
            fragment.setFav(favoriten);
            return fragment;
        } else {
            Log.e("Losungen", "Error: FragmentLosungenListe newInstance(context, false) sollte niemals aufgerufen werden");
            return null;
        }
    }

    public FragmentLosungenListe() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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

        RecyclerView.Adapter adapter = new GridAdapter(losungen, getContext(), fav);
        recyclerView.setAdapter(adapter);
    }

    /**
     * @return a list of all marked daily words
     */
    private static List<Losung> getFavoriten(Context context) {
        DBHandler dbHandler = DBHandler.newInstance(context);
        return dbHandler.getFavoriten();
    }

    public void setLosungen(List<Losung> losungen) {
        this.losungen = losungen;
    }

    public void setFav(boolean fav){
        this.fav = fav;
    }
}
