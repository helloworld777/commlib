package com.lu.library.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.lu.library.util.EventBusHelper;
import com.lu.library.util.NetUtil;
import com.lu.library.util.ObjectUtil;
import com.lu.library.util.PermissionUtil;
import com.lu.library.util.ScreenUtil;
import com.lu.library.util.ToastUtil;
import com.lu.library.widget.CommonTitleBarHelper;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;

/**
 * Created by lyw.
 *
 * @author: lyw
 * @package: com.ido.veryfitpro.base
 * @description: ${TODO}{ 所有Activity的超类}
 * @date: 2018/7/16 0016
 * 子类如果有多个泛型，则第一个泛型必须是BasePresenter的子类
 */

public abstract class BaseActivity<P extends BasePresenter> extends AppCompatActivity implements PermissionUtil.RequsetResult {

    protected P mPersenter;
    /**
     * 通用头部辅助类
     */
    protected CommonTitleBarHelper commonTitleBarHelper;
    private PermissionUtil permissionUtil;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(getLayoutResID());
        ButterKnife.bind(this);
        EventBusHelper.register(this);
        initPresenter();
        permissionUtil=new PermissionUtil();
        permissionUtil.setRequsetResult(this);
        commonTitleBarHelper=new CommonTitleBarHelper();
        commonTitleBarHelper.init(this);
        setNavigationBar();
        initViews();
        initData();


    }
    /**
     * 获取编辑框的内容（去除两边空格）
     *
     * @param editText
     * @return
     */
    public String getEditTextStr(EditText editText) {
        return editText.getText().toString().trim();
    }
    @Override
    public void setTitle(int titleId) {
       commonTitleBarHelper.setTitle(titleId);
    }
    public void initLayout(int style){
        commonTitleBarHelper.initLayout(style);
    }
    public void setRightOnClick(View.OnClickListener rightOnClick){
        commonTitleBarHelper.setRightOnClick(rightOnClick);
    }
    @Override
    public void setTitle(CharSequence titleId) {
       commonTitleBarHelper.setTitle((String) titleId);
    }

    public void initViews(){

    }
    /**
     * 设置沉浸式状态栏
     * 这个方法只适用于使用公共标题的情况下，个人情况下需要重新该方法
     */
    protected void setNavigationBar() {
        ScreenUtil.setNavigationBar(this);
    }
    @Subscribe(threadMode=ThreadMode.MAIN)
    public void handleMessageInner(BaseMessage message){
        handleMessage(message);
    }

    /**
     * 子类如需处理EventBus发送的消息，重写此方法
     * @param message 消息内容
     */
    protected void handleMessage(BaseMessage message) {
    }

    /**
     * 初始化P类型的对象，并且绑定该IBaseView子类
     */
    private void initPresenter() {
        mPersenter=autoCreatePresenter();
        if (mPersenter!=null){
            try {
                mPersenter.attachView((IBaseView) this);
            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }
    public boolean checkSelfPermission(String... permissions){
        return PermissionUtil.checkSelfPermission(permissions);
    }
    public void requestPermissions(int requestCode,String... permissions){
        PermissionUtil.requestPermissions(this,requestCode,permissions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionUtil.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }
    public void requestPermissionsSuccess(int requestCode){

    };
    public void requestPermissionsFail(int requestCode){};
    /**
     * 初始化数据
     */
    public abstract void initData();

    /**
     * 布局资源文件
     * @return 布局资源文件
     */
    public abstract int getLayoutResID();
    /**
     * 生成P类型的一个实例
     * @return P类型的一个实例
     */
    public  P autoCreatePresenter() {
        return ObjectUtil.getParameterizedType(getClass());
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        EventBusHelper.unregister(this);
        if (mPersenter!=null) {
            mPersenter.detachView();
        }
    }

    /**
     * 跳转到Activity
     * @param clazz Activity类
     */
    protected void startActivity(Class clazz) {
        startActivityForResult(clazz,-1,null);
    }

    /**
     * 跳转到Activity，携带参数
     * @param clazz Activity类
     * @param bundle 数据
     */
    protected void startActivity(Class clazz, Bundle bundle) {
        startActivityForResult(clazz,-1,bundle);
    }
    /**
     * 开启一个activity并且自己销毁
     *
     * @param clazz
     */
    protected void startActivityAndSelfFinish(Class clazz) {
        startActivity(clazz);
        finish();
    }
    /**
     * startActivityForResult方式跳转到Activity
     * @param clazz Activity类
     */
    protected void startActivityForResult(Class clazz, int requestCode) {
        startActivityForResult(clazz, requestCode,null);
    }
    /**
     * startActivityForResult方式跳转到Activity
     * @param clazz Activity类
     * @param bundle 数据
     */
    protected void startActivityForResult(Class clazz, int requestCode, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        if (bundle!=null){
            intent.putExtras(bundle);
        }
        if (requestCode>0){
            startActivityForResult(intent, requestCode);
        }else{
            startActivity(intent);
        }

    }

    protected void showToast(String msg) {
        ToastUtil.showToast(this, msg);
    }

    protected void showToast(int msg) {
//        ToastUtil.showToast(IdoApp.getAppContext(), msg);
        ToastUtil.showToast(this, getString(msg));
    }


    protected boolean isNetWorkAndToast() {
        if (!NetUtil.isConnected(this)) {
//            showToast((R.string.no_network));
            return false;
        }
        return true;

    }
    /**
     * 百度统计埋点
     *
     * @param eventId 事件id
     * @param label   事件标签
     */
    protected void setBaiduStat(String eventId, String label) {
//        StatService.onEvent(this, eventId, label);
    }
}