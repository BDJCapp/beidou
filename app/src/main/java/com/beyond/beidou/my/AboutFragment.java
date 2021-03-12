package com.beyond.beidou.my;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.fragment.app.FragmentManager;

import com.beyond.beidou.BaseFragment;
import com.beyond.beidou.MainActivity;
import com.beyond.beidou.R;

public class AboutFragment extends BaseFragment implements View.OnClickListener{
    private ImageView imgBack;
    private RelativeLayout RlPlfweb;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        initView(view);
        return view;
    }

    public void initView(View view) {
        imgBack = view.findViewById(R.id.img_about_back);
        RlPlfweb = view.findViewById(R.id.platformwebsite);
        imgBack.setOnClickListener(this);
        RlPlfweb.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_about_back:
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
                MainActivity activity = (MainActivity) getActivity();
                activity.setAboutFragment(null);
                activity.setNowFragment(activity.getMyFragment());
                break;
            case R.id.platformwebsite:
                Uri uri = Uri.parse("http://39.96.80.62/bdjc/templates/login.html");
                Intent it = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(it);

        }
    }
}
