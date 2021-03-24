package com.beyond.beidou.my;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.FragmentManager;

import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;

public class VersionInfoFragment extends BaseFragment implements View.OnClickListener{

    private ImageView imgBack;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_versioninfo, container, false);
        initView(view);
        return view;
    }

    public void initView(View view) {
        imgBack = view.findViewById(R.id.img_versionInfo_back);
        imgBack.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_versionInfo_back:
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
                MainActivity activity = (MainActivity) getActivity();
                activity.setVersionInfoFragment(null);
                activity.setNowFragment(activity.getHelpFragment());
                break;
        }

    }
}
