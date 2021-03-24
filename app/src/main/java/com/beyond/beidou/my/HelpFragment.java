package com.beyond.beidou.my;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;
import com.beyond.beidou.api.ApiConfig;

public class HelpFragment extends BaseFragment implements View.OnClickListener {

    private ImageView imgBack;
    private ImageView btFeedBack;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);
        initView(view);
        return view;
    }

    public void initView(View view) {
        imgBack = view.findViewById(R.id.img_help_back);
        btFeedBack = view.findViewById(R.id.img_feedback);
        imgBack.setOnClickListener(this);
        btFeedBack.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_help_back:
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
                MainActivity activity = (MainActivity) getActivity();
                activity.setHelpFragment(null);
                activity.setNowFragment(activity.getMyFragment());
                break;
            case R.id.img_feedback:
                Fragment feedbackFragment = new FeedBackFragment();
                MainActivity activity2 = (MainActivity) getActivity();
                activity2.setFeedBackFragment(feedbackFragment);
                activity2.setNowFragment(feedbackFragment);
                FragmentManager fragmentManager2 = getFragmentManager();
                FragmentTransaction ft2 = fragmentManager2.beginTransaction();
                ft2.add(R.id.layout_home, feedbackFragment).hide(this);
                ft2.addToBackStack(null);   //加入到返回栈中
                ft2.commit();
                break;

        }
    }
}