package com.gachon.innergation.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.gachon.innergation.activity.FindActivity;
import com.gachon.innergation.activity.GetDataActivity;
import com.gachon.innergation.info.GetWifiInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.gachon.innergation.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class WifiAdapter extends RecyclerView.Adapter<WifiAdapter.WifiListViewHolder>{
    private ArrayList<GetWifiInfo> mDataset;
    private final Activity activity;

    public static class WifiListViewHolder extends RecyclerView.ViewHolder{
        public RelativeLayout relativeLayout;
        public WifiListViewHolder(RelativeLayout v){
            super(v);
            relativeLayout = v;
        }
    }

    public WifiAdapter(Activity activity, ArrayList<GetWifiInfo> myDataset){
        mDataset = myDataset;
        this.activity = activity;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public WifiAdapter.WifiListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.wifi_info, parent, false);
        return new WifiListViewHolder(relativeLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull final WifiListViewHolder holder, @SuppressLint("RecyclerView") int position){
        RelativeLayout relativeLayout = holder.relativeLayout;
        TextView textSsid = relativeLayout.findViewById(R.id.text_ssid);
        TextView textBssid = relativeLayout.findViewById(R.id.text_bssid);
        TextView textRssi = relativeLayout.findViewById(R.id.text_rssi);

        textSsid.setText(mDataset.get(position).getSsid());
        textBssid.setText(mDataset.get(position).getBssid());
        textRssi.setText(mDataset.get(position).getRssi()+"");
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
