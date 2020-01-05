package com.xxun.xungallery;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.CountDownTimer;
import android.os.SystemProperties;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ImageView;

import com.xxun.xungallery.adapter.ViewPagerAdapter;
import com.xxun.xungallery.entity.AlbumInfo;
import com.xxun.xungallery.entity.PhotoInfo;
import com.xxun.xungallery.fragments.AlbumFragment;
import com.xxun.xungallery.fragments.AlbumFragment.OnAlbumClickListener;
import com.xxun.xungallery.fragments.PhotoFragment;
import com.xxun.xungallery.fragments.PhotoFragment.OnGridClickListener;
import com.xxun.xungallery.fragments.ViewPagerFragment;
import com.xxun.xungallery.stickview710.ChooseStickerActivity;
import com.xxun.xungallery.stickview710.DeleteActivity;
import com.xxun.xungallery.util.CheckImageLoaderConfiguration;
import com.xxun.xungallery.util.UniversalImageLoader;
import com.xxun.xungallery.util.ShareIntentService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import com.xiaoxun.sdk.utils.Constant;
import com.xiaoxun.statistics.XiaoXunStatisticsManager;
import com.xiaoxun.sdk.XiaoXunNetworkManager;

/**
 * 选择图片的界面，继承自ActionBarActivity，为了与主工程的主题保持一致。
 * 实现了两个自定义接口，接收列表中条目被点击的响应事件
 *
 * @author ghc
 */
public class MainPhotoActivity extends AppCompatActivity
        implements OnAlbumClickListener, OnGridClickListener, ViewPagerAdapter.OnStickerClickListener,
        Constants, ShareIntentService.OnUpdateShareView {

    private AlbumFragment mAlbumFragment;
    private PhotoFragment mPhotoFragment;
    private ViewPagerFragment mPagerFragment;
    private android.support.v4.app.FragmentManager mFragmentManager;
    private int mEditTag = 0;
    private int mSelectTag = 0;

    private static final String TAG = "MainPhotoActivity";
    private Context mContext;
    private static final int REQUEST_PERMISSION_CAMERA_CODE = 1;

    private String[] permissions = {
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private TextView tv_up;
    private TextView tv_noimg;
    // private ImageView img_up;
    
    private final boolean isSW730 = "SW730".equals(Constant.PROJECT_NAME);

    ////////
    private XiaoXunStatisticsManager statisticsManager;
    private long mStatusStartTime;
    ////////

    // add by guohongcheng_20180514
    // 省电测策略，分享前由2G切4G
    private int mFileType = -1;
    private String mFilePath;
    private NetSwitchReceiver mNetSwitchReceiver = null;
    private XiaoXunNetworkManager mXiaoXunNetworkManager = null;

    private boolean isCanBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "SPA onCreate() >> ");

        mContext = this;
        savedInstanceState = null;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_photo_main);
        tv_up = (TextView) findViewById(R.id.tv_up);
        tv_noimg = (TextView) findViewById(R.id.tv_noimg);
        if (isSW730) {
            tv_noimg.setTextSize(50);
        }
        //img_up = (ImageView) findViewById(R.id.//img_up);
        //img_up.setVisibility(View.GONE);

        if (savedInstanceState == null && mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }
        if (checkPermisson()) {
            initData();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart ..");
        try {
            CheckImageLoaderConfiguration.checkImageLoaderConfiguration(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume ..");
        if (checkPermisson()) {
            initView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause ..");
        releaseLTEMode();
        // 如果是朋友圈选择照片模式，那么在选择照片过程中被打断（如视频通话），那么直接退出
        // if (mSelectTag == 1) { 
        //     finish();
        // }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop ..");
    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy ..");

        UniversalImageLoader.clearMemoryCache();

        if (statisticsManager != null) {
            int mDurationTime = (int) (System.currentTimeMillis() - mStatusStartTime) / 1000;
            Log.d(TAG, "mDurationTime: " + mDurationTime);
            statisticsManager.stats(XiaoXunStatisticsManager.STATS_PHOTO_TIME, mDurationTime);
        }
        unRegisterReceiver();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return false;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void initData() {
        if (getIntent().hasExtra("edit_photo")) {
            mEditTag = getIntent().getIntExtra("edit_photo", 0);
        }
        if (getIntent().hasExtra("select_photo")) {
            mSelectTag = getIntent().getIntExtra("select_photo", 0);
            Message msg = new Message();
            msg.what = MSG_TRUE_BACK;
            mUIHandler.sendMessageDelayed(msg, 1500);  
        }
    }

    private void initView() {
        Log.d(TAG, "[initView] >> begin");
        if (mFragmentManager != null) {
            android.support.v4.app.FragmentTransaction transaction = mFragmentManager.beginTransaction();

            // modify by guohongcheng_20190425 start
            // 在视频、图片预览界面，发起找手表、视频通话，结束后返回相册界面，会出现黑屏不显示内容
            // 
            // mAlbumFragment = (AlbumFragment) mFragmentManager.findFragmentByTag(TAG_FRAGMENT_ALBUM);
            // if (mAlbumFragment == null) {
            //     Log.d(TAG, "[mAlbumFragment] >> begin");
            //     mAlbumFragment = new AlbumFragment();
            //     transaction.add(R.id.selectphoto_content, mAlbumFragment, TAG_FRAGMENT_ALBUM);
            // } else {
            //     Log.d(TAG, "[initView] >> mAlbumFragment == null 33");
            //     transaction.add(R.id.selectphoto_content, mAlbumFragment, TAG_FRAGMENT_ALBUM);
            // }
            
            mAlbumFragment = new AlbumFragment();
            transaction.add(R.id.selectphoto_content, mAlbumFragment, TAG_FRAGMENT_ALBUM);
            // modify by guohongcheng_20190425 end

            transaction.commitAllowingStateLoss();
        }

        statisticsManager = (XiaoXunStatisticsManager) getSystemService("xun.statistics.service");
        mStatusStartTime = System.currentTimeMillis();
        // 发送使用次数
        statisticsManager.stats(XiaoXunStatisticsManager.STATS_PHOTO);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "[onBackPressed] >> mSelectTag: " + mSelectTag + " isCanBack:" + isCanBack);
        if (mSelectTag == 1) {
            if (isCanBack) {
                super.onBackPressed();
            } else {
                return;
            }
            
        }

        super.onBackPressed();
        if (mPhotoFragment != null) {
            mPhotoFragment.invalidate();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        setFullScreen(false);
        return super.onKeyDown(keyCode, event);
    }

    /**
     * GridView的Item点击的事件响应--图片列表的点击事件
     */
    @Override
    public void onGridItemClick(AlbumInfo albumInfo, final int position) {
        if (mFragmentManager != null) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.hide(mPhotoFragment);

            if (mEditTag == 0) {
                mPagerFragment = (ViewPagerFragment) mFragmentManager.findFragmentByTag(TAG_FRAGMENT_PAGER);
                if (mPagerFragment == null) {
                    mPagerFragment = new ViewPagerFragment();
                    mPagerFragment.setInfo(albumInfo, position);
                    // 0:MODE_NORMAL 正常模式
                    // 1:MODE_SELECT 图片选择模式，给聊天应用使用
                    mPagerFragment.setCurrentMode(mSelectTag);

                    transaction.add(R.id.selectphoto_content, mPagerFragment, TAG_FRAGMENT_PAGER);
                    transaction.addToBackStack(null);
                } else {
                    mPagerFragment.setInfo(albumInfo, position);
                    transaction.show(mPagerFragment);
                }
            }

            transaction.commitAllowingStateLoss();
        }
    }

    /**
     * ListView的Item点击的事件响应--相册列表的点击事件
     */
    @Override
    public void onListClick(AlbumInfo albumInfo) {
        if (albumInfo != null) {
            if (mFragmentManager != null && resetDataStatus(albumInfo)) {
                tv_noimg.setVisibility(View.GONE);
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                transaction.hide(mAlbumFragment);

                mPhotoFragment = (PhotoFragment) mFragmentManager.findFragmentByTag(TAG_FRAGMENT_PHOTO);
                if (mPhotoFragment == null) {
                    mPhotoFragment = new PhotoFragment();
                    mPhotoFragment.setInfo(albumInfo);

                    transaction.add(R.id.selectphoto_content, mPhotoFragment, TAG_FRAGMENT_PHOTO);
                } else {
                    //添加贴纸后，通知更新
                    mPhotoFragment.invalidate();
                    mPhotoFragment.setInfo(albumInfo);
                    transaction.show(mPhotoFragment);
                }
                transaction.commitAllowingStateLoss();
            }
        } else {
            Log.d(TAG, "albumInfo == null");
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.hide(mAlbumFragment);
            tv_noimg.setVisibility(View.VISIBLE);
        }
    }

    public boolean removeFragment(Fragment fragment) {
        if (null == fragment) return false;
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.remove(fragment);
        ft.commitAllowingStateLoss();
        getSupportFragmentManager().popBackStack();
        if (fragment instanceof ViewPagerFragment) {
            mPhotoFragment.invalidate();
        }
        return true;
    }

    public void setFullScreen(boolean noTitle) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        if (noTitle) {
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(lp);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private boolean resetDataStatus(AlbumInfo aInfo) {
        List<PhotoInfo> pInfos = aInfo.getPhotoList();
        for (int i = 0; i < pInfos.size(); i++) {
            pInfos.get(i).isSelected = false;
            pInfos.get(i).isOriginal = false;
        }
        return true;
    }

    @Override
    public void onStickerClickListener(String path) {
        Intent intent = new Intent(this, ChooseStickerActivity.class);
        intent.putExtra(EDIT_PIC_PATH, path);
        startActivity(intent);
    }

    @Override
    public void onDeleteTouchListener(String path) {
		Log.d(TAG, "onDeleteTouchListener >> path: " + path);
        Intent intent = new Intent(this, DeleteActivity.class);
        intent.putExtra(EDIT_PIC_PATH, path);
        startActivity(intent);
    }

    @Override
    public void onShareTouchListener(int fileType, String path) {
        Log.d(TAG, "[onShareTouchListener] >> " + path);
        Boolean isBinded = SystemProperties.getBoolean("persist.sys.isbinded", false);

        setTouchUnable(); //底部按钮不可点击
        setTextView();

        if (!isBinded) {
            sendMsgUnbindFail();
        } else if (!isNetworkAvailable()) {
            Log.d(TAG, "[onUpdateShareView] >> wifi not available. ");
            sendMsgWifiFail();
            return;
        } else {
            // add by guohongcheng_20180514
            // 省电测策略，分享前由2G切4G
            countDownTimer.start();
            sendMsgOngoing();

            if (isWifiContected()) { // WIFI模式下 ，直接分享，不需要判断是否2G切4G
                Log.d(TAG, "isWifiContected = true ");
                startShareService(fileType, path);
            } else if (isNeedChangeTo4GMode()) { // 4G模式下 ，直接分享，不需要2G切4G
                Log.d(TAG, "isNeedChangeTo4GMode = true ");
                startShareService(fileType, path);
            } else { // 正在做2G切换4G操作，等待切换完毕后分享。
                doRegister();
                mFileType = fileType;
                mFilePath = path;
            }
        }
    }

    @Override
    public void onChatSelectListener(String path) {
        Log.d(TAG, "onChatSelectListener >> path: " + path);
        Intent intent = new Intent();
        intent.putExtra("select_photo", path);
        setResult(1, intent);
        finish();
    }

    private void setTextView() {
        // tv_up.setText(R.string.share_ongoing);
        tv_up.setVisibility(View.VISIBLE);
        // tv_up.setBackgroundColor(Color.parseColor("#86222222"));
        //img_up.setVisibility(View.VISIBLE);
    }

    private Handler mUIHandler = new Handler() {
        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHARE_ONGOING:
                    tv_up.setText(R.string.share_ongoing);
                    break;

                case MSG_SHARE_SUCCESS:
                    //img_up.setVisibility(View.GONE);
                    tv_up.setText(R.string.share_success);
                    releaseLTEMode();
                    break;

                case MSG_SHARE_FAIL:
                    //img_up.setVisibility(View.GONE);
                    tv_up.setText(R.string.share_fail);
                    releaseLTEMode();
                    break;

                case MSG_SHARE_DISMISS:
                    //img_up.setVisibility(View.GONE);
                    tv_up.setVisibility(View.GONE);
                    setTouchEnable();
                    // 分享结束一定会调用到此处，要释放2G切4G
                    releaseLTEMode();
                    break;

                case MSG_SHARE_WIFI_FAIL:
                    //img_up.setVisibility(View.GONE);
                    tv_up.setText(R.string.share_fail_wifi);
                    break;

                case MSG_SHARE_FILE_TOOBIG:
                    //img_up.setVisibility(View.GONE);
                    tv_up.setText(R.string.share_file_toobig);
                    break;

                case MSG_SHARE_UNBIND_FAIL:
                    tv_up.setText(R.string.share_file_unbind);        
                    break;

                case MSG_TRUE_BACK:
                    isCanBack = true;
                    break;

                default:
                    break;
            }

        }
    };

   @Override
   public void onUpdateShareView(Message message) {
       mUIHandler.sendMessageDelayed(message, 0);
       Message messageDismiss = new Message();
       messageDismiss.what = MSG_SHARE_DISMISS;
       mUIHandler.sendMessageDelayed(messageDismiss, 3 * 1000); // 3000ms
   }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Log.i(TAG, "[isNetworkAvailable] >> connectivity == null");
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        Log.i(TAG, "[isNetworkAvailable] >> state = connected. ");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void sendMsgOngoing() {
        Message msg = new Message();
        msg.what = MSG_SHARE_ONGOING;
        mUIHandler.sendMessageDelayed(msg, 0);        
    }

    private void sendMsgWifiFail() {
        Message msg = new Message();
        msg.what = MSG_SHARE_WIFI_FAIL;
        mUIHandler.sendMessageDelayed(msg, 0);
        Message msg_dismiss = new Message();
        msg_dismiss.what = MSG_SHARE_DISMISS;
        mUIHandler.sendMessageDelayed(msg_dismiss, 2 * 1000); // 2000ms
    }

    private void sendMsgUnbindFail() {
        Message msg = new Message();
        msg.what = MSG_SHARE_UNBIND_FAIL;
        mUIHandler.sendMessageDelayed(msg, 0);
        Message msg_dismiss = new Message();
        msg_dismiss.what = MSG_SHARE_DISMISS;
        mUIHandler.sendMessageDelayed(msg_dismiss, 2 * 1000); // 2000ms
    }

    private void sendMsgOuttimeFail() {
        Log.d(TAG, "sendMsgOuttimeFail ..");
        Message msg = new Message();
        msg.what = MSG_SHARE_FAIL;
        mUIHandler.sendMessageDelayed(msg, 0);
        Message msg_dismiss = new Message();
        msg_dismiss.what = MSG_SHARE_DISMISS;
        mUIHandler.sendMessageDelayed(msg_dismiss, 2 * 1000); // 2000ms
    }

    private void sendMsgFileTooBig() {
        Message msg = new Message();
        msg.what = MSG_SHARE_FILE_TOOBIG;
        mUIHandler.sendMessageDelayed(msg, 0);
        Message msg_dismiss = new Message();
        msg_dismiss.what = MSG_SHARE_DISMISS;
        mUIHandler.sendMessageDelayed(msg_dismiss, 2 * 1000); // 2000ms
    }

   private void startShareService(int fileType, String path) {
       switch (fileType) {
           case SHARE_TYPE_IMAGE:
               Log.d(TAG, "share img. ");

               try {
                   if (getFileSize(path) > MAXFILEZISE) {
                       Log.d(TAG, "[shareVideo] >> size>10M, cannot upload. ");
                       sendMsgFileTooBig();
                       return;
                   }
               } catch (Exception e) {
                   e.printStackTrace();
               }

               Intent intent = new Intent(this, ShareIntentService.class);
               intent.putExtra(SHARE_TYPE, fileType);
               intent.putExtra(SHARE_PIC_PATH, path);
               startService(intent);
               ShareIntentService.setOnUpdateShareView(this);
               break;

           case SHARE_TYPE_VIDEO:
               Log.d(TAG, "share video. ");
               Intent intent_video = new Intent(this, ShareIntentService.class);
               String[] pathArray = path.split("##");
               if (pathArray.length < 2) {
                   Log.d(TAG, "video path error. ");
                   return;
               }
               Log.d(TAG, "videoPath : " + pathArray[0] + "  " + pathArray[1]);

               try {
                   if (getFileSize(pathArray[0]) + getFileSize(pathArray[1]) > MAXFILEZISE) {
                       Log.d(TAG, "[shareVideo] >> size>10M, cannot upload. ");
                       sendMsgFileTooBig();
                       return;
                   }
               } catch (Exception e) {
                   e.printStackTrace();
               }

               intent_video.putExtra(SHARE_TYPE, fileType);
               intent_video.putExtra(SHARE_VIDEO_PATH, pathArray[0]);
               intent_video.putExtra(SHARE_VIDEOTHUMB_PATH, pathArray[1]);
               startService(intent_video);
               ShareIntentService.setOnUpdateShareView(this);
       }
   }

    private void setTouchUnable() {
        TextView tv_touch = (TextView) findViewById(R.id.tv_touch);
        tv_touch.setVisibility(View.VISIBLE);
        tv_touch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    private void setTouchEnable() {
        TextView tv_touch = (TextView) findViewById(R.id.tv_touch);
        tv_touch.setVisibility(View.GONE);
    }

    private static long getFileSize(String path) throws Exception {
        long size = 0;
        File file = new File(path);
        if (file.exists()) {
            FileInputStream fileInputStream = null;
            fileInputStream = new FileInputStream(file);
            size = fileInputStream.available();
        } else {
            Log.e(TAG, "[getFileSize] >> file not exit!");
        }
        Log.d(TAG, "[getFileSize] >> the file size is : " + size / 1000 + " KB == " + path);
        return size;
    }

    private boolean checkPermisson() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(permissions,
                        REQUEST_PERMISSION_CAMERA_CODE);
                return false;
            } else
                return true;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CAMERA_CODE && grantResults.length > 0) {
            int grantResult = grantResults[0];
            boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                initData();
                initView();
            }
        }
    }

    /**
     * 倒计时40秒，每1秒执行一次onTick
     */
    private CountDownTimer countDownTimer = new CountDownTimer(40 * 1000, 1 * 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            Log.d(TAG, "[CountDownTimer] >> onTick ..");
        }

        @Override
        public void onFinish() {
            Log.d(TAG, "[CountDownTimer] >> onFinish ..");
            sendMsgOuttimeFail();
        }
    };
	
	@Override
    public void startActivity(Intent intent) {		
        super.startActivity(intent);		
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_left);
		Log.d(TAG, "startActivity ");
    }

	/**
	 * 程序退出动画，左进右出
	*/
    @Override
    public void finish() {		
        super.finish();
        overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_right);
		Log.d(TAG, "finish ");
    }

    // add by guohongcheng_20180514
    // 省电测策略，分享前由2G切4G
    private void doRegister() {
        Log.d(TAG, "doRegister ");
        mNetSwitchReceiver = new NetSwitchReceiver();
        IntentFilter loginFilter = new IntentFilter();
        loginFilter.addAction(Constant.ACTION_NET_SWITCH_SUCC);
        registerReceiver(mNetSwitchReceiver, loginFilter);
    }

    // 收到2G切换到4G的广播后，再进行分享
    class NetSwitchReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "NetSwitchReceiver: " + action);
            if (Constant.ACTION_NET_SWITCH_SUCC.equals(action)) {
                if (mFileType != -1 && mFilePath != null) {
                    startShareService(mFileType, mFilePath);
                    mFileType = -1;
                    mFilePath = null;
                }
            }
        }
    }

    /**
     * 判断WIFI是否连接成功
     * @return
     */
    private boolean isWifiContected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (info != null && info.isConnected()) {
            Log.d(TAG, "Wifi网络连接成功");
            return true;
        }
        Log.d(TAG, "Wifi网络连接失败");
        return false;
    }

    private boolean isNeedChangeTo4GMode() {
        if (mXiaoXunNetworkManager == null) {
            mXiaoXunNetworkManager =
                    (XiaoXunNetworkManager) getSystemService("xun.network.Service");
        }
        // requireLTEMode 如果当前已经处于4G，则不需要切换网络，requireLTEMode返回false
        // 如果是网络状况不好导致切换到2G，此时requireLTEMode返回false，并且保持2G，不切换到4G
        return !mXiaoXunNetworkManager.requireLTEMode("com.xxun.xungallery");
    }

    private void releaseLTEMode() {
        if (mXiaoXunNetworkManager != null) {
            mXiaoXunNetworkManager.releaseLTEMode("com.xxun.xungallery");
        }
    }

    private void unRegisterReceiver() {
        if (mNetSwitchReceiver != null) {
            unregisterReceiver(mNetSwitchReceiver);
        }
    }
}
