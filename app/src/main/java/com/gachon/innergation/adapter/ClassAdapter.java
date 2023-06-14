package com.gachon.innergation.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.gachon.innergation.activity.FindActivity_five;
import com.gachon.innergation.activity.FindActivity_four;
import com.gachon.innergation.R;

import java.util.ArrayList;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassListViewHolder>{
    private ArrayList<String> mDataset;
    private final Activity activity;

    public static class ClassListViewHolder extends RecyclerView.ViewHolder{
        public CardView cardView;
        public ClassListViewHolder(CardView v){
            super(v);
            cardView = v;
        }
    }

    public ClassAdapter(Activity activity, ArrayList<String> myDataset){
        mDataset = myDataset;
        this.activity = activity;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ClassAdapter.ClassListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.class_info, parent, false);
        return new ClassListViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ClassListViewHolder holder, @SuppressLint("RecyclerView") int position){
        CardView cardView = holder.cardView;
        Button button = cardView.findViewById(R.id.btn_class);
        String className = mDataset.get(position);
        button.setText(className + "호");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("Test",mDataset.get(position));
                String first = className.substring(0,1);
                if(first.equals("4") || className.equals("artechne_4")) {
                    Intent intent = new Intent(activity, FindActivity_four.class);
                    intent.putExtra("className", mDataset.get(position));
                    activity.startActivity(intent);
                } else if(first.equals("5") || className.equals("artechne_5")) {
                    Intent intent = new Intent(activity, FindActivity_five.class);
                    intent.putExtra("className", mDataset.get(position));
                    activity.startActivity(intent);
                }
            }
        });
    }

    public void  filterList(ArrayList<String> filteredList) {
        mDataset = filteredList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount(){
        return mDataset.size();
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}