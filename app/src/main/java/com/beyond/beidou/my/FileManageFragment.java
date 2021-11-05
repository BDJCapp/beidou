package com.beyond.beidou.my;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.Selection;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.adapter.FileManagerAdapter;
import com.beyond.beidou.adapter.FileSelectAdapter;
import com.beyond.beidou.entites.FileItem;
import com.beyond.beidou.entites.FileSelectItem;
import com.beyond.beidou.util.FileUtil;
import com.beyond.beidou.util.ListUtil;
import com.beyond.beidou.util.LogUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class FileManageFragment extends BaseFragment implements View.OnClickListener {

    private MainActivity mMainActivity = null;
    private ImageView mIvBack;
    private List<FileItem> mFiles = new ArrayList<>();
    private List<FileSelectItem> mFilesSelect = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private FileManagerAdapter mFileAdapter;
    private FileSelectAdapter mFileSelectAdapter;
    private LinearLayoutManager mLayoutManager;
    private boolean sIsSettingsSelected = false;
    private Toolbar mToolbar;
    public PopupWindow mPopupWindow;
    private boolean isCheckAll = false;
    private Menu mMenu;

    public FileManageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainActivity = (MainActivity) getActivity();
        View view = inflater.inflate(initLayout(), container, false);
        initView(view);
        ((MainActivity) getActivity()).setSupportActionBar(mToolbar);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            ((MainActivity) getActivity()).setSupportActionBar(mToolbar);
            setHasOptionsMenu(true);
            getFileNames();
            mFileAdapter.setData(mFiles);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mLayoutManager = new LinearLayoutManager(mMainActivity);
        mRecyclerView.setLayoutManager(mLayoutManager);
        getFileNames();
        mFileAdapter = new FileManagerAdapter(mFiles);
        mFileAdapter.setOnItemClickListener(new FileManagerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                FileUtil.openExcelFile(mMainActivity, mMainActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + mFiles.get(position).getFileName());
            }
        });
        mFileAdapter.setOnItemLongClickListener(new FileManagerAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClickListener(final View view, final int position) {
                final View dialogView = LayoutInflater.from(mMainActivity).inflate(R.layout.dialog_file, (ViewGroup) view, false);
                final EditText mEtFileName = dialogView.findViewById(R.id.et_fileName);
                String oldName = mFiles.get(position).getFileName();
                mEtFileName.setText(oldName.substring(0, oldName.length() - 4));
                final AlertDialog dialog = new AlertDialog.Builder(mMainActivity).setTitle("提示")
                        .setView(dialogView)
                        .setMessage("请输入新名字：").setPositiveButton("确定", null).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setCancelable(false).create();
                dialog.show();

                mEtFileName.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mEtFileName.requestFocus();
                        mEtFileName.selectAll();
                        InputMethodManager manager = ((InputMethodManager)mMainActivity.getSystemService(Context.INPUT_METHOD_SERVICE));
                        if (manager != null) {
                            manager.showSoftInput(mEtFileName, 0);
                        }
                    }
                }, 300);

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText mEtFileName = dialogView.findViewById(R.id.et_fileName);
                        String oldName = mFiles.get(position).getFileName();
                        String newName = mEtFileName.getText().toString();
                        if (TextUtils.isEmpty(newName)) {
                            showToast("名字不能为空");
                            return;
                        }
                        if (newName.length() > 204) {
                            showToast("文件名过长");
                            return;
                        }
                        fileRename(mMainActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + oldName, mMainActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + newName + ".xls");
                        dialog.dismiss();
                        onHiddenChanged(false);
                    }

                });
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#0075E3"));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#0075E3"));

            }
        });
        mRecyclerView.setAdapter(mFileAdapter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public int initLayout() {
        return R.layout.fragment_filemanage;
    }

    public void initView(View view) {
        mIvBack = view.findViewById(R.id.img_file_back);
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    popWindowDismiss();
                    return;
                }
                if (mMainActivity.getMyFragment() == null) {
                    getFragmentManager().beginTransaction().remove(mMainActivity.getFileManageFragment()).commit();
                    mMainActivity.setFileManageFragment(null);
                    mMainActivity.getNavigationView().setSelectedItemId(mMainActivity.getNavigationView().getMenu().getItem(3).getItemId());
                } else {
                    getFragmentManager().beginTransaction().hide(mMainActivity.getFileManageFragment()).show(mMainActivity.getMyFragment()).remove(mMainActivity.getFileManageFragment()).commit();
                    mMainActivity.setFileManageFragment(null);
                }
                mMainActivity.setNowFragment(mMainActivity.getMyFragment());
            }
        });
        mRecyclerView = view.findViewById(R.id.rv_file);
        mToolbar = view.findViewById(R.id.toolbar);
    }

    private void getFileNames() {
        mFiles.clear();
        String path = mMainActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath();
        File file = new File(path);
        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.exists()) {
                    if (pathname.isFile() && pathname.canRead() && pathname.canWrite()) {
                        if (pathname.getName().toLowerCase().endsWith(".xls")) {
                            return true;
                        }
                    }
                }
                return false;
            }
        });
        LogUtil.e("FileManage", files.length + "");
        for (File f : files) {
            mFiles.add(new FileItem(f.getName()));
        }
        ListUtil.sort(mFiles, true, "fileName");
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        if (sIsSettingsSelected) {
            menu.findItem(R.id.mu_setting).setVisible(false);
            menu.findItem(R.id.mu_complete).setVisible(true);
            menu.findItem(R.id.mu_checkAll).setVisible(true);
        } else {
            menu.findItem(R.id.mu_setting).setVisible(true);
            menu.findItem(R.id.mu_complete).setVisible(false);
            menu.findItem(R.id.mu_checkAll).setVisible(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        this.mMenu = menu;
        inflater.inflate(R.menu.mu_file, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mu_setting:
                sIsSettingsSelected = true;
                mMainActivity.invalidateOptionsMenu();
                showPopupWindow();
                return true;
            case R.id.mu_complete:
                popWindowDismiss();
                return true;
            case R.id.mu_checkAll:
                if (!isCheckAll) {
                    for (FileSelectItem selectItem : mFilesSelect) {
                        selectItem.setSelect(true);
                    }
                    mFileSelectAdapter.setData(mFilesSelect);
                    item.setTitle("全反选");
                    isCheckAll = !isCheckAll;
                } else {
                    for (FileSelectItem selectItem : mFilesSelect) {
                        selectItem.setSelect(false);
                    }
                    mFileSelectAdapter.setData(mFilesSelect);
                    item.setTitle("全选");
                    isCheckAll = !isCheckAll;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showPopupWindow() {
        mMainActivity.getNavigationView().setVisibility(View.INVISIBLE);
        View contentView = LayoutInflater.from(mMainActivity).inflate(R.layout.popupwindow_file, null);
        mPopupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
        Button mDeleteButton = contentView.findViewById(R.id.pop_btn_delete);
        mDeleteButton.setOnClickListener(this);
        Button mShareButton = contentView.findViewById(R.id.pop_btn_share);
        mShareButton.setOnClickListener(this);
        View rootView = LayoutInflater.from(mMainActivity).inflate(R.layout.activity_main, null);
//        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x66000000));
        mPopupWindow.setAnimationStyle(R.style.pop_anim_style);
        mPopupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);

        mFilesSelect.clear();
        for (FileItem mFile : mFiles) {
            mFilesSelect.add(new FileSelectItem(mFile.getFileName()));
        }

        ListUtil.sort(mFilesSelect, true, "fileName");
        mFileSelectAdapter = new FileSelectAdapter(mFilesSelect);
        mFileSelectAdapter.setOnItemClickListener(new FileSelectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mFilesSelect.get(position).setSelect(!mFilesSelect.get(position).isSelect());
                mFileSelectAdapter.setData(mFilesSelect);
            }
        });
        mRecyclerView.setAdapter(mFileSelectAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pop_btn_delete:
                List<String> deleteList = mFileSelectAdapter.getDeleteList();
                if (deleteList.size() == 0) {
                    showToast("未选中删除的文件");
                    break;
                }
                deleteFiles(deleteList);
                break;
            case R.id.pop_btn_share:
                List<String> shareFilePaths = new ArrayList<>();
                for (FileSelectItem file : mFilesSelect) {
                    if (file.isSelect()) {
                        shareFilePaths.add(mMainActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + file.getFileName());
//                        FileUtil.shareFile(mMainActivity, mMainActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + file.getFileName());
//                        List<String> filePaths = new ArrayList<>();
//                        filePaths.add(mMainActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + file.getFileName());
//                        FileUtil.yeZip(filePaths);
                    }
                }
                if (shareFilePaths.size() > 1) {
                    showToast("只支持单文件分享，请重选");
                } else if (shareFilePaths.size() < 1) {
                    FileUtil.shareFile(mMainActivity, "");
                } else {
                    FileUtil.shareFile(mMainActivity, shareFilePaths.get(0));
                }
                break;
        }
    }

    public void popWindowDismiss() {
        mMainActivity.getNavigationView().setVisibility(View.VISIBLE);
        sIsSettingsSelected = false;
        mMainActivity.invalidateOptionsMenu();
        mRecyclerView.setAdapter(mFileAdapter);
        mPopupWindow.dismiss();
    }

    private void deleteFiles(final List<String> fileList) {
        if (fileList.isEmpty()) {
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(mMainActivity).setTitle("提示")
                .setMessage("确定要删除" + fileList.size() + "个文件吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        popWindowDismiss();

                        String prefix = mMainActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath() + "/";
                        File file = null;
                        for (String fileName : fileList) {
                            file = new File(prefix + fileName);
                            file.delete();
                        }
                        mFiles.clear();
                        for (int i = 0; i < mFilesSelect.size(); i++) {
                            if (mFilesSelect.get(i).isSelect()) {
                                mFilesSelect.remove(i--);
                            } else {
                                mFiles.add(new FileItem(mFilesSelect.get(i).getFileName()));
                            }
                        }
                        popWindowDismiss();

                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(false).create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#0075E3"));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#0075E3"));
    }

    private void fileRename(String oldPath, String newPath) {
        if (TextUtils.isEmpty(oldPath) || TextUtils.isEmpty(newPath)) {
            return;
        }
        new File(oldPath).renameTo(new File(newPath));
    }
}