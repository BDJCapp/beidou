package com.beyond.beidou;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import static android.content.Context.MODE_PRIVATE;

/**
 * @author: 李垚
 * @date: 2021/2/1
 */
public abstract class BaseFragment extends Fragment {

    private Activity mActivity;

    public Context getContext(){
        if(mActivity == null){
            return MyApplication.getContext();
        }
        return mActivity;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }


    public void showToast(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    public void showToastSync(String msg){
        Looper.prepare();
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        Looper.loop();
    }

    public void navigateTo(Class cls){
        Intent intent = new Intent(getActivity(), cls);
        startActivity(intent);
    }

    public void navigateToWithBundle(Class cls, Bundle bundle){
        Intent intent = new Intent(getActivity(), cls);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void navigateToWithFlag(Class cls, int flags){
        Intent intent = new Intent(getActivity(), cls);
        intent.setFlags(flags);
        startActivity(intent);
    }

    protected void saveStringToSP(String key, String value){
        SharedPreferences sp = getActivity().getSharedPreferences("sp_user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    protected String getStringFromSP(String key){
        SharedPreferences sp = getActivity().getSharedPreferences("sp_user", MODE_PRIVATE);
        return  sp.getString(key, "");
    }

    protected void removeByKey(String key){
        SharedPreferences sp = getActivity().getSharedPreferences("sp_user", MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.remove(key);
        edit.commit();
    }
}
