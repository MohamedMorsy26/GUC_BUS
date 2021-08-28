package com.example.herewego;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.List;

public class MenuBusAdapter extends ArrayAdapter<MenuBus> {
    public MenuBusAdapter( Context context, int resource, int textViewResourceId,  List<MenuBus> busses) {
        super(context, R.layout.all_buses_item, R.id.boibusnumberid, busses);
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent){
        View dropDownItemView = convertView;
        if (dropDownItemView == null) {
            dropDownItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.all_buses_item, parent, false);
        }
        MenuBus currentMenuBus = getItem(position);

        TextView busNumberView = (TextView) dropDownItemView.findViewById(R.id.boibusnumberid);
        busNumberView.setText(currentMenuBus.getBusNumber());

        TextView busRouteView = (TextView) dropDownItemView.findViewById(R.id.boibusrouteid);
        busRouteView.setText(currentMenuBus.getRoute());

        return dropDownItemView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.all_buses_item, parent, false);
        }
        MenuBus currentMenuBus = getItem(position);

        TextView busNumberView = (TextView) itemView.findViewById(R.id.boibusnumberid);
        busNumberView.setText(currentMenuBus.getBusNumber());

        TextView busRouteView = (TextView) itemView.findViewById(R.id.boibusrouteid);
        busRouteView.setText(currentMenuBus.getRoute());

        //changing the color of the circle based on the bus status:
        //getting the textView containing the status circle
        TextView statusCircleView = (TextView) itemView.findViewById(R.id.userbusstatusid);
        //getting the status circle itself
        GradientDrawable statusCircleViewBackground = (GradientDrawable) statusCircleView.getBackground();
        //getting the color based on the status of the bus
        int statusCircleColor = getStatusCircleColor(currentMenuBus.isOnline(), currentMenuBus.isShared(), currentMenuBus.isRequested());
        //setting the new color
        statusCircleViewBackground.setColor(statusCircleColor);
        
        return itemView;
    }

    private int getStatusCircleColor(boolean status, boolean shared, boolean requested){
        int statusCircleColorResourceId;
        if(status || shared){
            statusCircleColorResourceId = R.color.statusonline;
        }
        else if(requested){
            statusCircleColorResourceId = R.color.requested;
        }
        else{
            statusCircleColorResourceId = R.color.statusoffline;
        }
        //I honestly don't know why I can't just return the color...
        //But I used this in a previous project and it worked so...yeah
        return ContextCompat.getColor(getContext(), statusCircleColorResourceId);
    }
}
