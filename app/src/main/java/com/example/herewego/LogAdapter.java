package com.example.herewego;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> implements Filterable {

    ArrayList<logItem> allLogItems;
    ArrayList<logItem> filteredLogItems;

    public LogAdapter(ArrayList<logItem> logItems) {
        this.filteredLogItems = logItems;
        allLogItems = new ArrayList<>();
    }

    public void addAllItems(ArrayList<logItem> fullLogList){
        this.allLogItems.addAll(fullLogList);
        notifyDataSetChanged();
    }

    public void clearItems(){
        allLogItems.clear();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<logItem> filteredList = new ArrayList<>();

            //this line is to display the whole list when deleting whatever was in the search bar
            filteredList.addAll(allLogItems);

            if(constraint == null || constraint.length()==0){}
            else if(constraint.length()!=0){
                filteredList.clear();

                String filterPattern  = constraint.toString().toLowerCase().trim();
                for(logItem item: allLogItems){
                    String text = item.getText();
                    if(text.toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults results) {
            filteredLogItems.clear();
            filteredLogItems.addAll((List) results.values);

            notifyDataSetChanged();
        }
    };

    //create a class for the custom LogViewHolder
    public static class LogViewHolder extends RecyclerView.ViewHolder{
        public TextView logItemText;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            logItemText = itemView.findViewById(R.id.logTextViewid);
        }
    }

    //kinda constructor for the custom LogViewHolder >> create a view from the parent's context, and
    //inflate it with log_items (xml file) then create a LogViewHolder and pass the view to it
    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.log_item, parent, false);
        LogViewHolder logViewHolder = new LogViewHolder(v);
        return logViewHolder;
    }

    //method to set the data of the item in a ViewHolder >>
    //get the item from the array and set the holder's text to its data
    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        logItem item = filteredLogItems.get(position);
        holder.logItemText.setText(item.getText());
    }

    //method to return the number of items in the used array
    @Override
    public int getItemCount() {
        return filteredLogItems.size();
    }
}
