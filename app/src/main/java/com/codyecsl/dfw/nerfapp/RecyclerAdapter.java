package com.codyecsl.dfw.nerfapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

// This adapter initially didn't have the <RecyclerAdapter.MyViewHolder> object at the end
// but since we changed the arguments for the implemented Adapter abstracted class to use
// <RecyclerAdapter.MyViewHolder>, it was required to add the explicit reference on the extension
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>{
    private List<String> list;

    public RecyclerAdapter(List<String> list) {
        this.list = list;
    }


    @NonNull
    @Override
    // Originally returned a normal RecyclerView.ViewHolder, this function was changed
    // to return our custom class of RecyclerView.ViewHolder named MyViewHolder
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        TextView textView =  (TextView) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.text_view_layout, viewGroup, false);
        MyViewHolder myViewHolder = new MyViewHolder(textView);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {
        viewHolder.VersionName.setText(list.get(i));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // This class was created which extends ViewHolder.  onCreateViewHolder requires a
    // ViewHolder object return so we are using this custom one for that purpose.
    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView VersionName;

        public MyViewHolder(@NonNull TextView itemView) {
            super(itemView);
            VersionName = itemView;
        }
    }
}
