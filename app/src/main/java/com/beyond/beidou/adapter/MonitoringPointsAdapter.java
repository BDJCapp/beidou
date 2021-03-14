package com.beyond.beidou.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.beyond.beidou.R;
import com.beyond.beidou.entites.MonitoringPoint;

import java.util.List;

public class MonitoringPointsAdapter extends RecyclerView.Adapter<MonitoringPointsAdapter.ViewHolder>{

    private List<MonitoringPoint> mPointsList;
    private static OnItemClickListener mOnItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder{
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

    }

    public MonitoringPointsAdapter(List<MonitoringPoint> mPointsList){
        this.mPointsList = mPointsList;
    }

    public void setData(List<MonitoringPoint> mPointsList) {
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
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        MonitoringPoint point = mPointsList.get(position);
        holder.mTv_name.setText(point.getName());
        int statusCode = Integer.parseInt(point.getStatus());
        if(statusCode >= 10 && statusCode <= 19){
            holder.mIv_status.setImageResource(R.drawable.ic_svg_online_point);
        }else if(statusCode >= 20 && statusCode <= 29){
            holder.mIv_status.setImageResource(R.drawable.ic_svg_offline_point);
        }else if(statusCode >= 30 && statusCode <= 39){
            holder.mIv_status.setImageResource(R.drawable.ic_svg_warning_point);
        }else if(statusCode >= 40 && statusCode <= 49){
            holder.mIv_status.setImageResource(R.drawable.ic_svg_error_point);
        }
        switch (point.getType()){
            case "0":
                holder.mTv_type.setText("未知");
                break;
            case "1":
                holder.mTv_type.setText("基准站");
                break;
            case "2":
                holder.mTv_type.setText("移动站");
                break;
        }
        holder.mTv_activeTime.setText(point.getActiveTime());
        if(mOnItemClickListener == null){
            Log.e("123", "mOnItemClickListener is null");
        }
        holder.mTv_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener == null) {
                    Log.e("onBindViewHolder", "mOnItemClickListener is null");
                } else {
                    mOnItemClickListener.onItemClick(v, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPointsList.size();
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        if(mOnItemClickListener == null){
            Log.e("setOnItemClickListener", "mOnItemClickListener is null");
        }
        this.mOnItemClickListener = mOnItemClickListener;
    }
}
