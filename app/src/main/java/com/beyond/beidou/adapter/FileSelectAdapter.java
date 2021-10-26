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
import com.beyond.beidou.entites.FileItem;
import com.beyond.beidou.entites.FileSelectItem;

import java.util.ArrayList;
import java.util.List;

public class FileSelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<FileSelectItem> files;
    private static FileSelectAdapter.OnItemClickListener mOnItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout mLl_item;
        TextView mTv_fileName;
        ImageView mIv_Check;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTv_fileName = itemView.findViewById(R.id.tv_fileName);
            mIv_Check = itemView.findViewById(R.id.iv_isChecked);
            mLl_item = itemView.findViewById(R.id.ll_item);
        }
    }

    public FileSelectAdapter(List<FileSelectItem> files){
        this.files = files;
    }

    public void setData(List<FileSelectItem> files){
        this.files = files;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileSelectAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_check_file, parent, false));
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
        if(files.get(position).isSelect()){
            itemHolder.mIv_Check.setImageResource(R.drawable.ic_file_checked);
        }else{
            itemHolder.mIv_Check.setImageResource(R.drawable.ic_file_unchecked);
        }
    }

    @Override
    public int getItemCount() {
        return (files == null ? 0 : files.size());
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(FileSelectAdapter.OnItemClickListener mOnItemClickListener) {
        if (mOnItemClickListener == null) {
            Log.e("setOnItemClickListener", "mOnItemClickListener is null");
        }
        FileSelectAdapter.mOnItemClickListener = mOnItemClickListener;
    }

    public List<String> getDeleteList(){
        List<String> fileList = new ArrayList<>();
        for (FileSelectItem file : files) {
            if(file.isSelect()){
                fileList.add(file.getFileName());
            }
        }
        return fileList;
    }
}
