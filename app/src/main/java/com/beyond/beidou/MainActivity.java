package com.beyond.beidou;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.beyond.beidou.data.DataHomeFragment;
import com.beyond.beidou.data.DownloadService;
import com.beyond.beidou.my.MyFragment;
import com.beyond.beidou.project.ProjectFragment;
import com.beyond.beidou.util.FileUtil;
import com.beyond.beidou.util.LogUtil;
import com.beyond.beidou.warning.WarningFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;


public class MainActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private Fragment projectFragment;
    private Fragment dataFragment;
    private Fragment warningFragment;
    private Fragment myFragment;
    private Fragment nowFragment = null;
    private Fragment chartFragment = null;
    private Fragment helpFragment = null;
    private Fragment aboutFragment = null;
    private Fragment feedbackFragment = null;
    private Fragment versionInfoFragment = null;
    private Fragment settingsFragment = null;
    private Fragment securityFragment = null;
    private Fragment updatePwdFragment = null;
    private BottomNavigationView navigationView;
    private String presentProject = null;
    private Intent downloadIntent;
    private CoordinatorLayout coordinatorLayout;

    private DownloadService.DownloadBinder downloadBinder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            downloadBinder = (DownloadService.DownloadBinder) iBinder;

            final DownloadService service = downloadBinder.getService();
            service.setDownLoadExcelSuccess(new DownloadService.DownLoadExcelSuccess() {
                @Override
                public void showSnackBar() {
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, "导出报表成功，是否打开报表", Snackbar.LENGTH_LONG)
                            .setAction("是", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    LogUtil.e("获取的FilePath", service.getFilePath());
                                    FileUtil.openExcelFile(getApplicationContext(), service.getFilePath());
                                }
                            })
                            .setActionTextColor(getResources().getColor(R.color.main_blue));
                    snackbar.show();
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    public DownloadService.DownloadBinder getDownloadBinder() {
        return downloadBinder;
    }

    public String getPresentProject() {
        return presentProject;
    }

    public void setPresentProject(String presentProject) {
        this.presentProject = presentProject;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(downloadIntent);
        unbindService(connection);  //解绑服务
    }

    @Override
    protected void onStop() {
//        saveStringToSP("sessionUUID", ApiConfig.getSessionUUID());
        saveStringToSP("lastProjectName", getPresentProject());
        super.onStop();
    }

    @Override
    public void init() {
        navigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public void initData() {
        projectFragment = new ProjectFragment();
    }

    @Override
    public void initView() {
        coordinatorLayout = findViewById(R.id.layout_snack);
        navigationView = findViewById(R.id.layout_bottomNavigation);
    }

    @Override
    public void initEvent() {
        //首次启动时，应显示工程的Fragment
        switchFragment(nowFragment, projectFragment);

        downloadIntent = new Intent(this, DownloadService.class);
        //绑定服务，绑定服务时会自动调用实参connection对象中的onServiceConnected方法
        bindService(downloadIntent, connection, BIND_AUTO_CREATE);

        startService(downloadIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initEvent();
        init();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_warning)
        {
            return false;
        }
        changePageFragment(item.getItemId());
        return true;
    }

    /**
     * 当点击导航栏时改变fragment
     *
     * @param id 选择的ItemId
     */
    public void changePageFragment(int id) {
        switch (id) {
            case R.id.nav_my:
                LogUtil.e("111nowFragement", nowFragment.toString());
                if (nowFragment == settingsFragment || nowFragment == securityFragment || nowFragment == updatePwdFragment) {
                    break;
                }
                if (updatePwdFragment != null) {
                    switchFragment(nowFragment, updatePwdFragment);
                    break;
                } else if (securityFragment != null) {
                    switchFragment(nowFragment, securityFragment);
                    break;
                } else if (settingsFragment != null) {
                    switchFragment(nowFragment, settingsFragment);
                    break;
                } else if (myFragment == null) { //减少new fragment,避免不必要的内存消耗
                    myFragment = new MyFragment();
                }
                switchFragment(nowFragment, myFragment);
                break;
            case R.id.nav_project:
                if (projectFragment == null) {
                    projectFragment = new ProjectFragment();
                }
                switchFragment(nowFragment, projectFragment);
                break;
            case R.id.nav_data:
                if (nowFragment == chartFragment)
                    break;
                if (chartFragment != null) {
                    switchFragment(nowFragment, chartFragment);
                    break;
                } else if (dataFragment == null) {
                    dataFragment = new DataHomeFragment();
                }
                switchFragment(nowFragment, dataFragment);
                break;
            case R.id.nav_warning:
                if (warningFragment == null) {
                    warningFragment = new WarningFragment();
                }
                switchFragment(nowFragment, warningFragment);
                break;
        }
    }

    /**
     * 隐藏显示fragment
     *
     * @param from 需要隐藏的fragment
     * @param to   需要显示的fragment
     */
    public void switchFragment(Fragment from, Fragment to) {
        if (to == null)
            return;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (!to.isAdded()) {
            if (from == null) {
                transaction.add(R.id.layout_home, to).show(to).commit();
            } else {
                // 隐藏当前的fragment，add下一个fragment到Activity中
                transaction.hide(from).add(R.id.layout_home, to).commitAllowingStateLoss();
            }
        } else {
            // 隐藏当前的fragment，显示下一个
            Log.e("from", from.toString());
            transaction.hide(from).show(to).commit();
        }
        nowFragment = to;
    }


    /**
     * 监听物理返回键，判断当前是否是chartfragment，解决返回重影问题
     */
    @Override
    public void onBackPressed() {

        if (nowFragment == chartFragment) {
            FragmentManager fm = getProjectFragment().getFragmentManager();
//            fm.popBackStack();
            fm.beginTransaction().remove(nowFragment);
            this.setChartFragment(null);
            LogUtil.e("nowFragment", nowFragment.toString());

//            Log.e("BackStack11:", "" + fm.getBackStackEntryCount());
//            Log.e("BackStack11:", fm.getBackStackEntryAt(fm.getBackStackEntryCount()-1).getName());
            if (fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName().equals("projectFragment")) {
                this.setNowFragment(this.getProjectFragment());
                this.getNavigationView().setSelectedItemId(this.getNavigationView().getMenu().getItem(0).getItemId());
            } else {
                this.setNowFragment(this.getDataFragment());
                Log.e("fragment now1111", nowFragment.toString());
                this.getNavigationView().setSelectedItemId(this.getNavigationView().getMenu().getItem(1).getItemId());
            }

        }

        if (nowFragment == settingsFragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(nowFragment);
            nowFragment = myFragment;
            settingsFragment = null;
        }

        if (nowFragment == securityFragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(nowFragment);
            nowFragment = settingsFragment;
            securityFragment = null;
        }

        if (nowFragment == updatePwdFragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(updatePwdFragment);
            nowFragment = securityFragment;
            updatePwdFragment = null;
        }
        super.onBackPressed();
    }

    public Fragment getChartFragment() {
        return chartFragment;
    }

    public void setChartFragment(Fragment chartFragment) {
        this.chartFragment = chartFragment;
    }

    public void setHelpFragment(Fragment helpFragment) {
        this.helpFragment = helpFragment;
    }

    public void setAboutFragment(Fragment aboutFragment) {
        this.aboutFragment = aboutFragment;
    }

    public void setFeedBackFragment(Fragment feedbackFragment) {
        this.feedbackFragment = feedbackFragment;
    }

    public void setVersionInfoFragment(Fragment versioninfoFragment) {
        this.versionInfoFragment = versioninfoFragment;
    }


    public Fragment getNowFragment() {
        return nowFragment;
    }

    public void setNowFragment(Fragment nowFragment) {
        this.nowFragment = nowFragment;
    }

    public Fragment getDataFragment() {
        return dataFragment;
    }

    public Fragment getMyFragment() {
        return myFragment;
    }

    public Fragment getAboutFragment() {
        return aboutFragment;
    }

    public Fragment getHelpFragment() {
        return helpFragment;
    }

    public Fragment getFeedbackFragment() {
        return feedbackFragment;
    }

    public void setDataFragment(Fragment dataFragment) {
        this.dataFragment = dataFragment;
    }

    public BottomNavigationView getNavigationView() {
        return navigationView;
    }

    public Fragment getProjectFragment() {
        return projectFragment;
    }

    public Fragment getSecurityFragment() {
        return securityFragment;
    }

    public void setSecurityFragment(Fragment securityFragment) {
        this.securityFragment = securityFragment;
    }

    public Fragment getUpdatePwdFragment() {
        return updatePwdFragment;
    }

    public void setUpdatePwdFragment(Fragment updatePwdFragment) {
        this.updatePwdFragment = updatePwdFragment;
    }

    public Fragment getSettingsFragment() {
        return settingsFragment;
    }

    public void setSettingsFragment(Fragment settingsFragment) {
        this.settingsFragment = settingsFragment;
    }
}