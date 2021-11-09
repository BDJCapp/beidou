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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beyond.beidou.data.DataHomeFragment;
import com.beyond.beidou.data.DownloadService;
import com.beyond.beidou.my.FileManageFragment;
import com.beyond.beidou.my.MyFragment;
import com.beyond.beidou.project.ProjectFragment;
import com.beyond.beidou.util.FileUtil;
import com.beyond.beidou.util.LogUtil;
import com.beyond.beidou.warning.WarningFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.lang.reflect.Method;


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
    private Fragment userInfoFragment = null;
    private Fragment fileManageFragment = null;
    private BottomNavigationView navigationView;
    private String presentProject = null;
    private Intent downloadIntent;
    private CoordinatorLayout coordinatorLayout;
    private boolean isExit = false;
    private DownloadService.DownloadBinder downloadBinder;
    private Toast mToast;
    public boolean isCacheUpdated = false;

    public void displayToast(String msg){
        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void cancelToast() {
        if (mToast != null) {
            LogUtil.e("MainActivity cancelToast","toast不为空");
            mToast.cancel();
            mToast = null;
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            downloadBinder = (DownloadService.DownloadBinder) iBinder;

            final DownloadService service = downloadBinder.getService();
            service.setDownLoadExcelSuccess(new DownloadService.DownLoadExcelSuccess() {
                @Override
                public void showSnackBar() {
                    cancelToast();
                    //LENGTH_INDEFINITE：不取消显示
                    final Snackbar snackbar = Snackbar.make(coordinatorLayout, "导出路径为：" + service.getFilePath().substring(20), Snackbar.LENGTH_INDEFINITE)
                            .setAction("打开报表", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    LogUtil.e("获取的FilePath", service.getFilePath());
                                    FileUtil.openExcelFile(getApplicationContext(), service.getFilePath());
//                                    FileUtil.openDownload(getApplicationContext(),service.getFilePath());
                                }
                            })
                            .setActionTextColor(getResources().getColor(R.color.main_blue));
                    //默认显示7s
                    snackbar.setDuration(7000);
                    //设置4行显示，避免文字截断
                    View snackBarView = snackbar.getView();
                    TextView messageView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
                    messageView.setMaxLines(4);
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
        if (item.getItemId() == R.id.nav_warning) {
            return false;
        }
        FileManageFragment f = (FileManageFragment) fileManageFragment;
        if(f != null && f.mPopupWindow != null && f.mPopupWindow.isShowing()){
            f.popWindowDismiss();
        }
        isExit = false;
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
                if (nowFragment == settingsFragment || nowFragment == securityFragment || nowFragment == updatePwdFragment || nowFragment == userInfoFragment || nowFragment == fileManageFragment) {
                    break;
                }
                if(fileManageFragment != null){
                    switchFragment(nowFragment, fileManageFragment);
                    break;
                }
                if(userInfoFragment != null){
                    switchFragment(nowFragment, userInfoFragment);
                    break;
                } else if (updatePwdFragment != null) {
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
            transaction.hide(from).show(to).commit();
        }
        nowFragment = to;
    }


    /**
     * 监听物理返回键，判断当前是否是chartfragment，解决返回重影问题
     */
    @Override
    public void onBackPressed() {
        FileManageFragment f = (FileManageFragment) fileManageFragment;
        if(f != null && f.mPopupWindow != null && f.mPopupWindow.isShowing()){
            f.popWindowDismiss();
            return;
        }
        if (isExit)
        {
            finish();
            return;
        }
        if (nowFragment == projectFragment || nowFragment == dataFragment || nowFragment == warningFragment || nowFragment == myFragment) {
            if (!isExit) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                isExit = true;
                return;
            }
        }
        isExit = false;

        if (nowFragment == chartFragment) {
            FragmentManager fm = getSupportFragmentManager();
            if (dataFragment == null){
                fm.beginTransaction().remove(chartFragment).commit();
                this.setChartFragment(null);
                dataFragment = new DataHomeFragment();
                this.getNavigationView().setSelectedItemId(this.getNavigationView().getMenu().getItem(1).getItemId());
            }
            else {
                fm.beginTransaction().hide(chartFragment).show(dataFragment).remove(chartFragment).commit();
                this.setChartFragment(null);
            }
            nowFragment = dataFragment;
            return;
        }

        if (nowFragment == settingsFragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(settingsFragment).remove(settingsFragment).show(myFragment).commit();
            nowFragment = myFragment;
            settingsFragment = null;
            return;
        }

        if (nowFragment == securityFragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(securityFragment).remove(securityFragment).show(settingsFragment).commit();
            nowFragment = settingsFragment;
            securityFragment = null;
            return;
        }

        if (nowFragment == updatePwdFragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(updatePwdFragment).remove(updatePwdFragment).show(securityFragment).commit();
            nowFragment = securityFragment;
            updatePwdFragment = null;
            return;
        }

        if(nowFragment == userInfoFragment){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(userInfoFragment).remove(userInfoFragment).show(myFragment).commit();
            nowFragment = myFragment;
            userInfoFragment = null;
            return;
        }

        if(nowFragment == fileManageFragment){
            if (myFragment == null){
                getSupportFragmentManager().beginTransaction().remove(fileManageFragment).commit();
                this.setFileManageFragment(null);
                this.getNavigationView().setSelectedItemId(this.getNavigationView().getMenu().getItem(3).getItemId());
            }else {
                getSupportFragmentManager().beginTransaction().hide(fileManageFragment).show(this.getMyFragment()).remove(fileManageFragment).commit();
                this.setFileManageFragment(null);
            }
            nowFragment = myFragment;
            return;
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

    public Fragment getUserInfoFragment(){ return userInfoFragment;}

    public void setUserInfoFragment(Fragment userInfoFragment){
        this.userInfoFragment = userInfoFragment;
    }

    public Fragment getFileManageFragment() {
        return fileManageFragment;
    }

    public void setFileManageFragment(Fragment fileManageFragment) {
        this.fileManageFragment = fileManageFragment;
    }

    public void setExit(boolean exit) {
        isExit = exit;
    }

}