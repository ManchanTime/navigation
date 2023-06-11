package com.gachon.innergation.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.gachon.innergation.activity.GetDataActivity;
import com.gachon.innergation.R;

import java.util.ArrayList;

public class ChooseAdapter extends RecyclerView.Adapter<ChooseAdapter.ChooseListViewHolder>{
    private ArrayList<String> mDataset;
    private final Activity activity;

    public static class ChooseListViewHolder extends RecyclerView.ViewHolder{
        public CardView cardView;
        public ChooseListViewHolder(CardView v){
            super(v);
            cardView = v;
        }
    }

    public ChooseAdapter(Activity activity, ArrayList<String> myDataset){
        mDataset = myDataset;
        this.activity = activity;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ChooseAdapter.ChooseListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_info, parent, false);
        return new ChooseListViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChooseListViewHolder holder, @SuppressLint("RecyclerView") int position){
        CardView cardView = holder.cardView;
        Button button = cardView.findViewById(R.id.btn_class);
        String name = mDataset.get(position);
        if(name.equals("artechne_4") || name.equals("artechne_5")){
            button.setText("artechne");
        }else
            button.setText(mDataset.get(position) + "호");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, GetDataActivity.class);
                intent.putExtra("className", mDataset.get(position));
                activity.startActivity(intent);
            }
        });
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
