package com.beyond.beidou.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.beyond.beidou.R;
import com.beyond.beidou.entites.MonitoringPoint;

import java.util.List;

public class MonitoringPointsAdapter extends RecyclerView.Adapter<MonitoringPointsAdapter.ViewHolder>{
    private List<MonitoringPoint> mPointsList;

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView mIv_status;
        TextView mTv_name;
        TextView mTv_type;
        TextView mTv_activeTime;
        TextView mTv_data;

        public ViewHolder(View view){
            super(view);
            mIv_status = view.findViewById(R.id.iv_status);
            mTv_name = view.findViewById(R.id.tv_name);
            mTv_type = view.findViewById(R.id.tv_type);
            mTv_activeTime = view.findViewById(R.id.tv_activeTime);
            mTv_data = view.findViewById(R.id.tv_data);
        }

        @Override
        public void onClick(View v) {

        }
    }

    public MonitoringPointsAdapter(List<MonitoringPoint> mPointsList){
        this.mPointsList = mPointsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_point, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MonitoringPoint point = mPointsList.get(position);
        holder.mTv_name.setText(point.getName());
        holder.mIv_status.setImageResource(R.drawable.ic_status);
        holder.mTv_type.setText(point.getType());
        holder.mTv_activeTime.setText(point.getActiveTime());
    }

    @Override
    public int getItemCount() {
        return mPointsList.size();
    }


}
