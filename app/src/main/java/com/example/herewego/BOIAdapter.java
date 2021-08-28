package com.example.herewego;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class BOIAdapter extends BaseAdapter {
    private customButtonClickListener mListener;
    private ImageButton starButton;

    Context mContext;
    private List<MenuBus> boiList;

    //Create a listener interface to have a custom method for the remove button
    //(or any button in general)
    public interface customButtonClickListener{
        void onStarButtonClickListener(int position);
        void onCustomItemClickListener(final AdapterView<?> parent, View view, final int position);
    }
    //set the listener
    public void setCustomButtonClickListener(BOIAdapter.customButtonClickListener listener){
        mListener = listener;
    }

    //bois = buses of interest
    public BOIAdapter(@NonNull Context context, @NonNull List<MenuBus> bois, customButtonClickListener mListener) {
        mContext = context;
        this.boiList = bois;
        this.mListener = mListener;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View boiItemView = convertView;
        if (boiItemView == null) {
            boiItemView = LayoutInflater.from(mContext).inflate(R.layout.boi_item, parent, false);
        }
        String currentboi = (String)getItem(position);

        TextView busRouteView = (TextView) boiItemView.findViewById(R.id.boibusrouteid);
        busRouteView.setText(currentboi);



        return boiItemView;
    }

    @Override
    public int getCount() {
        return boiList.size();
    }

    @Override
    public Object getItem(int position) {
        return boiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        View boiItemView = convertView;
        if (boiItemView == null) {
            boiItemView = LayoutInflater.from(mContext).inflate(
                    R.layout.boi_item, parent, false);
        }
        MenuBus currentboi = (MenuBus) getItem(position);

        TextView busRouteView = (TextView) boiItemView.findViewById(R.id.boibusrouteid);
        busRouteView.setText(currentboi.getRoute());

        TextView busNumberView = (TextView) boiItemView.findViewById(R.id.boibusnumberid);
        busNumberView.setText(currentboi.getBusNumber());

        //setting the onClickListener method because the onItemClickListener doesn't work on custom adapters
        //function with parameters same as the original onItemClickListener
        //the position is sent as a tag which is set here and retrieved on the other side using getTag()
        final View finalBoiItemView = boiItemView;
        finalBoiItemView.setTag(position);
        boiItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener != null){
                    mListener.onCustomItemClickListener((AdapterView<?>) parent, finalBoiItemView, (Integer) view.getTag());
                }
            }
        });

        starButton = (ImageButton) boiItemView.findViewById(R.id.boistarbutton);
        starButton.setTag(position);
        starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener != null){
                    mListener.onStarButtonClickListener((Integer) view.getTag());
                }
            }
        });


        //changing the color of the circle based on the bus status:
        //getting the textView containing the status circle
        TextView statusCircleView = (TextView) boiItemView.findViewById(R.id.userbusstatusid);
        //getting the status circle itself
        GradientDrawable statusCircleViewBackground = (GradientDrawable) statusCircleView.getBackground();

        //getting the color based on the status of the bus
        int statusCircleColor = getStatusCircleColor(currentboi.isOnline(), currentboi.isShared(), currentboi.isRequested());
        //setting the new color
        statusCircleViewBackground.setColor(statusCircleColor);
        //getting the Star image based on whther the bus is a favourite or not
        Drawable backgroundStar = getBackgroundStar(currentboi.isFavourite());
        //setting the new Star background
        starButton.setBackground(backgroundStar);


        return boiItemView;
    }

    private Drawable getBackgroundStar(boolean favourite) {
        if(favourite){
            return ContextCompat.getDrawable(mContext, R.drawable.ic_round_star_48);
        }
        else {
            return ContextCompat.getDrawable(mContext, R.drawable.ic_round_star_empty_48);
        }
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
        return ContextCompat.getColor(mContext, statusCircleColorResourceId);
    }
}
