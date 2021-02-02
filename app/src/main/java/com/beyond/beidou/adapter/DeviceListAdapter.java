package com.beyond.beidou.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beyond.beidou.R;

import java.util.List;

/**
 * @author: 李垚
 * @date: 2021/2/1
 */
public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

    private List<String> deviceNames;
    private List<String> deviceTypes;
    private List<String> lastTimes;
    private List<String> deviceStatus;
    private onItemLookdataClockListener lookDataListener;

    public DeviceListAdapter(List<String> deviceNames, List<String> deviceTypes, List<String> lastTimes, List<String> deviceStatus) {
        this.deviceNames = deviceNames;
        this.deviceTypes = deviceTypes;
        this.lastTimes = lastTimes;
        this.deviceStatus = deviceStatus;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data_devicelist,parent,false);
        DeviceListAdapter.ViewHolder holder = new DeviceListAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        String tName = deviceNames.get(position);
        holder.name.setText(tName);
        String tType = deviceTypes.get(position);
        holder.type.setText(tType);
        String tTime = lastTimes.get(position);
        holder.lastTime.setText(tTime);
        String tStatus = deviceStatus.get(position);
        switch (tStatus){
            case "在线":
                holder.status.setImageResource(R.drawable.ic_svg_online_point);
                break;
            case "警告":
                holder.status.setImageResource(R.drawable.ic_svg_warning_point);
                break;
            case "故障":
                holder.status.setImageResource(R.drawable.ic_svg_error_point);
                break;
            case "离线":
                holder.status.setImageResource(R.drawable.ic_svg_offline_point);
                break;
        }
        holder.lookData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lookDataListener != null)
                {
                    lookDataListener.onItemClick(v,position);
                }
            }
        });
        holder.lookDataImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lookDataListener != null)
                {
                    lookDataListener.onItemClick(v,position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceNames.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView status,lookDataImg;
        TextView type,lastTime,name,lookData;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            status = itemView.findViewById(R.id.img_deviceStatus);
            type = itemView.findViewById(R.id.tv_deviceTypeValue);
            lastTime = itemView.findViewById(R.id.tv_lastTimeValue);
            name = itemView.findViewById(R.id.tv_deviceName);
            lookData = itemView.findViewById(R.id.tv_lookData);
            lookDataImg = itemView.findViewById(R.id.img_lookData);
        }
    }

    public interface onItemLookdataClockListener{
        void onItemClick(View view, int position);
    }

    public void setLookDataListener(onItemLookdataClockListener lookDataListener) {
        this.lookDataListener = lookDataListener;
    }
}
