package com.example.android.arrival.Model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.android.arrival.R;

import java.util.ArrayList;

public class CustomSuggestionList extends ArrayAdapter<Place> {

    private ArrayList<Place> loc;
    private Context context;

    public CustomSuggestionList(Context context, ArrayList<Place> loc){
        super(context,0, loc);
        this.loc = loc;
        this.context = context;
    }



    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        return super.getView(position, convertView, parent);
        View view = convertView;

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.simple_list, parent,false);
        }

        Place place = loc.get(position);

        TextView name = view.findViewById(R.id.location_name);
        TextView address = view.findViewById(R.id.location_address);

        name.setText(place.getName());
        address.setText(place.getAddress());

        return view;
    }

}