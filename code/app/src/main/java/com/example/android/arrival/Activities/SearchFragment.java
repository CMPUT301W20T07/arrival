package com.example.android.arrival.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.android.arrival.Model.CustomSuggestionList;
import com.example.android.arrival.Model.Place;
import com.example.android.arrival.R;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends DialogFragment {
    private ListView list;
    private ArrayAdapter<Place> adapter;
    private SearchView searchBar;
    private ArrayList<Place> arrayList = new ArrayList<>();

    static SearchFragment newInstance(){
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.search_fragment, null);


        SearchView search = view.findViewById(R.id.searchButton);
        list = view.findViewById(R.id.suggestionsListView);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * When a user submits the text from the searchView this function is called
             * @param query : final text in the searchBar
             * @return : returns false when finished executing
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            /**
             * When a user updates the text in the searchView this function is called
             * @param newText : updated text in the searchBar
             * @return : returns false when finished executing
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                getSearch(newText, view);
                return false;
            }
        });


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Place selected = (Place) parent.getItemAtPosition(position);

                //TODO have to send the place back to the rider activity
                //TODO maybe highlight the selected and have place be a global
//                LatLng latLng = new LatLng(selected.getLat(), selected.getLon());
//                MarkerOptions markerOptions= new MarkerOptions();
//                markerOptions.position(latLng);
//                markerOptions.draggable(true);
//                markerOptions.title("Destination");
//                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
//                Marker destinationMarker = mMap.addMarker(markerOptions);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Search")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO pass back start action, and place
                    }}).create();
    }

    /**
     * Gets a list of possible addresses based on the users updated search
     * @param newText : new string in the searchBar
     */
    public void getSearch(String newText, View view) {
        //Update suggestions when query text changes
        Geocoder geocoder = new Geocoder(getContext());
        searchBar = view.findViewById(R.id.searchButton);

        adapter = new CustomSuggestionList(getContext(), arrayList);
        list.setAdapter(adapter);
        list.setVisibility(View.VISIBLE);

        List<Address> addresses;
        try {
            //Gets addresses from within Edmonton
            addresses = geocoder.getFromLocationName(newText, 5,
                    53.431898,  -113.662692,
                    53.646464,  -113.343030);

            //If the geocoder found possible addresses we should add them to the
            //arrayList if they aren't already there
            if (addresses.size() > 0) {
                updateArraylist(addresses);
                adapter.notifyDataSetChanged();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * For each address that the geocoder found we check create a new place object and check
     * if it can be added to the arrayList
     * @param addresses : list of potential addresses determined by the geocoder
     */
    public void updateArraylist(List<Address> addresses){
        for (int i = 0; i < addresses.size(); i++) {
            String name = addresses.get(i).getFeatureName();
            String address = addresses.get(i).getAddressLine(0);
            Double lat = addresses.get(i).getLatitude();
            Double lon = addresses.get(i).getLongitude();
            Place place = new Place(name, address, lat, lon);

            //By default we add the first address that the geocoder found
            if (arrayList.size() == 0) {
                arrayList.add(0, place);
            }
            else {
                checkArrayList(place);
            }
        }

        //Keeps the suggestions to a max of 6 and filters out the older suggestions
        while(arrayList.size() > 6) {
            arrayList.remove(6);
        }
    }


    /**
     * For each Place we want to ensure that we aren't having duplicates in our arrayList so we
     * check that with this function
     * @param place : Place object
     */
    public void checkArrayList(Place place) {
        int a = arrayList.size() - 1;
        int count = 0;
        while(a >= 0) {
            count = count + place.compareTo(arrayList.get(a));
            a--;
        }
        if (count == arrayList.size()) {
            arrayList.add(0, place);
        }
    }

}
