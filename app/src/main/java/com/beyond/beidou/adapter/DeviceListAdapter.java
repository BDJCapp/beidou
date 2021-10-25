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


public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

    private List<String> deviceNames;
    private List<String> deviceTypes;
    private List<String> lastTimes;
    private List<String> deviceStatus;
    private onItemLookdataClockListener lookDataListener;
    private View VIEW_FOOTER;

    //Type
    private int TYPE_NORMAL = 1000;
    private int TYPE_FOOTER = 1002;

    public DeviceListAdapter(List<String> deviceNames, List<String> deviceTypes, List<String> lastTimes, List<String> deviceStatus) {
        this.deviceNames = deviceNames;
        this.deviceTypes = deviceTypes;
        this.lastTimes = lastTimes;
        this.deviceStatus = deviceStatus;
    }

    @NonNull
    @Override
    public DeviceListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            return new MyHolder(VIEW_FOOTER);
        }else {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data_devicelist,parent,false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        if (isFooterView(position)) {
            //nothing to bind
        }else {
            String tName = deviceNames.get(position);
            holder.name.setText(tName);
            String tType = deviceTypes.get(position);
            holder.type.setText(tType);
            String tTime = lastTimes.get(position);
            holder.lastTime.setText(tTime);
            String tStatus = deviceStatus.get(position);
            Integer stationStatus = Integer.valueOf(tStatus);
            if (stationStatus == 0)
            {
                // "未知";
                holder.status.setImageResource(R.drawable.ic_svg_offline_point);
            }
            else if (stationStatus == 4)
            {
                // "移除";
                holder.status.setImageResource(R.drawable.ic_svg_offline_point);
            }
            else if (stationStatus >= 10 && stationStatus <= 19) {
                // "在线";
                holder.status.setImageResource(R.drawable.ic_svg_online_point);
            } else if (stationStatus >= 20 && stationStatus <= 29) {
                //"离线";
                holder.status.setImageResource(R.drawable.ic_svg_offline_point);
            } else if (stationStatus >= 30 && stationStatus <= 39) {
                // "警告";
                holder.status.setImageResource(R.drawable.ic_svg_warning_point);
            } else if (stationStatus >= 40 && stationStatus <= 49) {
                // "故障";
                holder.status.setImageResource(R.drawable.ic_svg_error_point);
            } else {
                // "错误";
                holder.status.setImageResource(R.drawable.ic_svg_offline_point);
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
    }

    @Override
    public int getItemViewType(int position) {
        if(isFooterView(position)){
            return TYPE_FOOTER;
        }else{
            return TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        int count = (deviceNames == null ? 0 : deviceNames.size());
        if (VIEW_FOOTER != null) {
            count++;
        }
        return count;
    }

    private boolean isFooterView(int position) {
        return haveFooterView() && position == getItemCount() - 1;
    }

    private boolean haveFooterView() {
        return VIEW_FOOTER != null;
    }

    public void addFooterView(View footerView) {
        if (haveFooterView()) {
            return;
        } else {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            footerView.setLayoutParams(params);
            VIEW_FOOTER = footerView;
            notifyItemInserted(getItemCount() - 1);
        }
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

    static class MyHolder extends DeviceListAdapter.ViewHolder {

        public MyHolder(View itemView) {
            super(itemView);
        }
    }

    public interface onItemLookdataClockListener{
        void onItemClick(View view, int position);
    }

    public void setLookDataListener(onItemLookdataClockListener lookDataListener) {
        this.lookDataListener = lookDataListener;
    }
}
