package com.example.herewego;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class BOIListAdapter extends ArrayAdapter<bus> {
    //bois = buses of interest
    public BOIListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<bus> bois) {
        super(context, R.layout.boi_item, R.id.boibusrouteid, bois);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View boiItemView = convertView;
        if (boiItemView == null) {
            boiItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.boi_item, parent, false);
        }
        bus currentboi = getItem(position);

        TextView busRouteView = (TextView) boiItemView.findViewById(R.id.boibusrouteid);
        busRouteView.setText(currentboi.getBus_route());

        return boiItemView;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View boiItemView = convertView;
        if (boiItemView == null) {
            boiItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.boi_item, parent, false);
        }
        bus currentboi = getItem(position);

        TextView busRouteView = (TextView) boiItemView.findViewById(R.id.boibusrouteid);
        busRouteView.setText(currentboi.getBus_route());

        return boiItemView;
    }
}
