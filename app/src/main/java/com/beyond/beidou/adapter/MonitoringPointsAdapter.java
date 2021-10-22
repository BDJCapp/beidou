package com.beyond.beidou.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beyond.beidou.R;
import com.beyond.beidou.entites.MonitoringPoint;

import java.util.List;

public class MonitoringPointsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<MonitoringPoint> mPointsList;
    private static OnItemClickListener mOnItemClickListener;
    private static OnAreaClickListener mOnAreaClickListener;

    private View VIEW_HEADER;
    private View VIEW_FOOTER;

    //Type
    private int TYPE_NORMAL = 1000;
    private int TYPE_HEADER = 1001;
    private int TYPE_FOOTER = 1002;

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout mLl_item;
        ImageView mIv_status;
        TextView mTv_name;
        TextView mTv_type;
        TextView mTv_activeTime;
        TextView mTv_data;
        ImageView mIv_arrow;

        public ViewHolder(View view) {
            super(view);
            mLl_item = view.findViewById(R.id.ll_item);
            mIv_status = view.findViewById(R.id.iv_status);
            mTv_name = view.findViewById(R.id.tv_name);
            mTv_type = view.findViewById(R.id.tv_type);
            mTv_activeTime = view.findViewById(R.id.tv_activeTime);
            mTv_data = view.findViewById(R.id.tv_data);
            mIv_arrow = view.findViewById(R.id.iv_arrow);
        }
    }

    static class MyHolder extends RecyclerView.ViewHolder {

        public MyHolder(View itemView) {
            super(itemView);
        }
    }


    public MonitoringPointsAdapter(List<MonitoringPoint> mPointsList) {
        this.mPointsList = mPointsList;
    }

    public MonitoringPointsAdapter() {
    }

    public void setData(List<MonitoringPoint> mPointsList) {
        this.mPointsList = mPointsList;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_point, parent, false);
//        ViewHolder holder = new ViewHolder(view);
//        return holder;
        if (viewType == TYPE_FOOTER) {
            return new MyHolder(VIEW_FOOTER);
        } else if (viewType == TYPE_HEADER) {
            return new MyHolder(VIEW_HEADER);
        } else {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_point, parent, false));
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (isFooterView(position)) {
            //nothing to bind
        } else {
            ViewHolder bodyHolder = (ViewHolder) holder;
            MonitoringPoint point = mPointsList.get(position);
            bodyHolder.mTv_name.setText(point.getName());
            bodyHolder.mTv_name.setSelected(true);
            int statusCode = Integer.parseInt(point.getStatus());
            if (statusCode >= 10 && statusCode <= 19) {
                bodyHolder.mIv_status.setImageResource(R.drawable.ic_svg_online_point);
            } else if (statusCode >= 20 && statusCode <= 29) {
                bodyHolder.mIv_status.setImageResource(R.drawable.ic_svg_offline_point);
            } else if (statusCode >= 30 && statusCode <= 39) {
                bodyHolder.mIv_status.setImageResource(R.drawable.ic_svg_warning_point);
            } else if (statusCode >= 40 && statusCode <= 49) {
                bodyHolder.mIv_status.setImageResource(R.drawable.ic_svg_error_point);
            }
            switch (point.getType()) {
                case "0":
                    bodyHolder.mTv_type.setText("未知");
                    break;
                case "1":
                    bodyHolder.mTv_type.setText("基准站");
                    break;
                case "2":
                    bodyHolder.mTv_type.setText("移动站");
                    break;
            }
            bodyHolder.mTv_activeTime.setText(point.getActiveTime());

            bodyHolder.mLl_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnAreaClickListener.onAreaClick(v, position);
                }
            });

            bodyHolder.mTv_data.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            });

            bodyHolder.mIv_arrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(isHeaderView(position)){
            return TYPE_HEADER;
        }else if(isFooterView(position)){
            return TYPE_FOOTER;
        }else{
            return TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        int count = (mPointsList == null ? 0 : mPointsList.size());
        if (VIEW_HEADER != null) {
            count++;
        }
        if (VIEW_FOOTER != null) {
            count++;
        }
        return count;
    }

    private boolean haveHeaderView() {
        return VIEW_HEADER != null;
    }

    private boolean haveFooterView() {
        return VIEW_FOOTER != null;
    }

    private boolean isHeaderView(int position) {
        return haveHeaderView() && position == 0;
    }

    private boolean isFooterView(int position) {
        return haveFooterView() && position == getItemCount() - 1;
    }

    public void addHeaderView(View headerView) {
        if (haveHeaderView()) {
            throw new IllegalStateException("hearview has already exists!");
        } else {
            //避免出现宽度自适应
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            headerView.setLayoutParams(params);
            VIEW_HEADER = headerView;
//            ifGridLayoutManager();
            notifyItemInserted(0);
        }

    }

    public void addFooterView(View footerView) {
        if (haveFooterView()) {
//            throw new IllegalStateException("footerView has already exists!");
//            removeFooterView();
//            addFooterView(footerView);
            return;
        } else {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            footerView.setLayoutParams(params);
            VIEW_FOOTER = footerView;
//            ifGridLayoutManager();
            notifyItemInserted(getItemCount() - 1);
        }
    }

    private void removeFooterView(){
        VIEW_FOOTER = null;
        notifyItemRemoved(getItemCount() - 1);
    }


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnAreaClickListener {
        void onAreaClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        if (mOnItemClickListener == null) {
            Log.e("setOnItemClickListener", "mOnItemClickListener is null");
        }
        MonitoringPointsAdapter.mOnItemClickListener = mOnItemClickListener;
    }

    public void setOnAreaClickListener(OnAreaClickListener mOnAreaClickListener) {
        if (mOnAreaClickListener == null) {
            Log.e("setOnAreaClickListener", "mOnAreaClickListener is null");
        }

        MonitoringPointsAdapter.mOnAreaClickListener = mOnAreaClickListener;
    }
}
