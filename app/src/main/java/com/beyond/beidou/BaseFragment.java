package com.beyond.beidou;

import androidx.fragment.app.Fragment;

/**
 * @author: 李垚
 * @date: 2021/2/1
 */
public abstract class BaseFragment extends Fragment {

    public abstract void init();

    public abstract void initData();

    public abstract void initView();

    public abstract void initEvent();
}
