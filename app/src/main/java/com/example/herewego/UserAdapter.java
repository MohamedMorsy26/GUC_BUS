package com.example.herewego;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> implements Filterable {
    private ArrayList<user> mUserList;
    private ArrayList<user> mUserListFiltered;
    private customButtonClickListener mListener;

    public interface customButtonClickListener{
        void onEnableButtonClickListener(int position);
        void onDisableButtonClickListener(int position);

    }
    public void setCustomButtonClickListener(customButtonClickListener listener){
        mListener = listener;
    }

    public void addItems(ArrayList<user> newUserList){
        this.mUserList.addAll(newUserList);
        notifyDataSetChanged();
    }

    public void clearItems(){
        mUserList.clear();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{
        public TextView usernametext, useremailtext;
        public Button enablebutton, disablebutton;

        public UserViewHolder(@NonNull View itemView, final customButtonClickListener listener) {
            super(itemView);
            usernametext = itemView.findViewById(R.id.usernameid);
            useremailtext = itemView.findViewById(R.id.useremailid);
            enablebutton = itemView.findViewById(R.id.enablebuttonid);
            disablebutton = itemView.findViewById(R.id.disablebuttonid);

            enablebutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onEnableButtonClickListener(position);
                        }
                    }
                }
            });
            disablebutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onDisableButtonClickListener(position);
                        }
                    }
                }
            });
        }
    }

    public UserAdapter(ArrayList<user> userlist){
        this.mUserListFiltered = userlist;
        mUserList = new ArrayList<>(userlist);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adminviewuserslistitem, parent, false);
        UserViewHolder uvh = new UserViewHolder(v, mListener);
        return uvh;
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        user u = mUserListFiltered.get(position);
        holder.usernametext.setText(u.getFirstname() + " " + u.getLastname());
        holder.useremailtext.setText(u.getEmail());
    }

    @Override
    public int getItemCount() {
        return mUserListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<user> filteredList = new ArrayList<>();

            //this line is to display the whole list when deleting whatever was in the search bar
            filteredList.addAll(mUserList);

            if(constraint == null || constraint.length()==0){
            }
            else if(constraint.length()!=0){
                filteredList.clear();

                String filterPattern  = constraint.toString().toLowerCase().trim();
                for(user userItem: mUserList){
                    String nameFilter = userItem.getFirstname() + " " + userItem.getLastname();
                    if(nameFilter.toLowerCase().contains(filterPattern)){
                        filteredList.add(userItem);

                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults results) {
            mUserListFiltered.clear();
            mUserListFiltered.addAll((List) results.values);

            notifyDataSetChanged();
        }
    };

}
