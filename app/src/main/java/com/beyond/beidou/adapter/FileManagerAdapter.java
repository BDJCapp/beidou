package com.beyond.beidou.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beyond.beidou.R;
import com.beyond.beidou.entites.FileItem;

import java.util.List;

public class FileManagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<FileItem> files;
    private FileManagerAdapter.OnItemClickListener mOnItemClickListener;
    private FileManagerAdapter.OnItemLongClickListener mOnItemLongClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout mLl_item;
        TextView mTv_fileName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTv_fileName = itemView.findViewById(R.id.tv_fileName);
            mLl_item = itemView.findViewById(R.id.ll_item);
        }
    }

    public FileManagerAdapter(List<FileItem> files){
        this.files = files;
    }

    public void setData(List<FileItem> files){
        this.files = files;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileManagerAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        ViewHolder itemHolder = (ViewHolder) holder;
        String fileName = files.get(position).getFileName();
        itemHolder.mTv_fileName.setText(fileName);
        itemHolder.mLl_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(v, position);
            }
        });
        itemHolder.mLl_item.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mOnItemLongClickListener.onItemLongClickListener(v, position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return (files == null ? 0 : files.size());
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener{
        void onItemLongClickListener(View view, int position);
    }

    public void setOnItemClickListener(FileManagerAdapter.OnItemClickListener mOnItemClickListener) {
        if (mOnItemClickListener == null) {
            Log.e("setOnItemClickListener", "mOnItemClickListener is null");
        }
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setOnItemLongClickListener(FileManagerAdapter.OnItemLongClickListener mOnItemLongClickListener){
        this.mOnItemLongClickListener = mOnItemLongClickListener;
    }

}
