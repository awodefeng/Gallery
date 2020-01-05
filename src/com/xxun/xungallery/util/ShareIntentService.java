package com.xxun.xungallery.util;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.util.Log;

import com.xxun.xungallery.Constants;

import com.xiaoxun.sdk.XiaoXunNetworkManager;
import com.xiaoxun.sdk.ResponseData;
import com.xiaoxun.sdk.IResponseDataCallBack;


import com.xiaoxun.smart.uploadfile.OnUploadResult;
import com.xiaoxun.smart.uploadfile.ProgressListener;
import com.xiaoxun.smart.uploadfile.UploadFile;



public class ShareIntentService extends IntentService implements Constants {

    private static final String name = "359076060015655";  // BtAddr
    private static final String machineSerialNo = "60015655"; //IMEI
    private static final String TAG = "ShareIntentService";

    String mEID = null;
    String mGID = null;
    String mToken = null;
    String mAES_KEY = null;
    String mImgOriginalPath = "/storage/emulated/0/DCIM/Camera/thumbnails/1504180395200.jpg";
    String mVideoThumbnailPath = "/storage/emulated/0/DCIM/Camera/thumbnails/1504180395200.jpg";
    String mVideoPath = "/storage/emulated/0/DCIM/Camera/IMG_20171107_070248.jpg";

    String VIDEO_TYPE = "video";
    String IMAGE_TYPE = "photo";

    private Context mContext;
    public static OnUpdateShareView mOnUpdateShareView;

    // @step 1

    // @step2

    private static final long MAXFILEZISE = 10 * 1000000;  // 10M

    private int fileType = -1;

    private XiaoXunNetworkManager mXunNetworkManager;


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mXunNetworkManager = (XiaoXunNetworkManager)getSystemService("xun.network.Service");
        mGID = mXunNetworkManager.getWatchGid();
        mEID = mXunNetworkManager.getWatchEid();
        mToken = mXunNetworkManager.getSID();
        mAES_KEY = mXunNetworkManager.getAESKey();
        Log.d(TAG, "[onCreate] >> isLoginOK " + mXunNetworkManager.isLoginOK());
        Log.d(TAG, "[onCreate] >> mGID " + mGID);
        Log.d(TAG, "[onCreate] >> mEID " + mEID);
        Log.d(TAG, "[onCreate] >> mToken " + mToken);
        Log.d(TAG, "[onCreate] >> mAES_KEY " + mAES_KEY);
    }



    public ShareIntentService() {
        super(TAG);
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        fileType = intent.getIntExtra(SHARE_TYPE, 0);
        switch (fileType) {
            case SHARE_TYPE_IMAGE:
                setImagePath(intent.getStringExtra(SHARE_PIC_PATH));
                getAndShareImage();
                break;

            case SHARE_TYPE_VIDEO:
                setVideoPath(intent.getStringExtra(SHARE_VIDEO_PATH), intent.getStringExtra(SHARE_VIDEOTHUMB_PATH));
                getAndUploadVideo();
                break;
        }


    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "[onDestroy] >> perform. ");
        super.onDestroy();
    }

    private void setImagePath(String path) {
        this.mImgOriginalPath = path;
    }

    private void setVideoPath(String videoPath, String thumbnailPath) {
        this.mVideoPath = videoPath;
        this.mVideoThumbnailPath = thumbnailPath;
    }

    /**
     * @step1 视频分享
     * 首先跟服务器握手，获取权限
     * 
     */
    private void getAndUploadVideo() {
        ShakeAndUploadVideoCallback callback = new ShakeAndUploadVideoCallback();
        mXunNetworkManager.setMapMSetValue(mGID, new String[]{"test"}, new String[]{"test"}, callback);
    }

    private void getAndShareImage() {
        ShakeAndUploadImageCallback callback = new ShakeAndUploadImageCallback();
        mXunNetworkManager.setMapMSetValue(mGID, new String[]{"test"}, new String[]{"test"}, callback);
    }




    public interface OnUpdateShareView {
        void onUpdateShareView(Message message);
    }

    public static void setOnUpdateShareView(OnUpdateShareView onUpdateShareView) {
        mOnUpdateShareView = onUpdateShareView;
    }

    private void notifyUISuccess() {
        if (mOnUpdateShareView != null) {
            Message message = new Message();
            message.what = MSG_SHARE_SUCCESS;
            mOnUpdateShareView.onUpdateShareView(message);
            Log.d(TAG, "[uploadImage] >> send ui handler success.");
        } else {
            Log.d(TAG, "[uploadImage] >> mOnUpdateShareView is null, send ui handler fail.");
        }
    }

    private void notifyUIFail() {
        if (mOnUpdateShareView != null) {
            Message message = new Message();
            message.what = MSG_SHARE_FAIL;
            mOnUpdateShareView.onUpdateShareView(message);
            Log.d(TAG, "mOnUpdateShareView is null, update ui fail.");
        } else {
            Log.d(TAG, "mOnUpdateShareView is null, send ui handler fail.");
        }
    }
	
    /**
     * [uploadFilesLocal 上传文件]
     * @param token           [标志位]
     * @param type            [文件类型]
     * @param eid             [EID]
     * @param gid             [GID]
     * @param filePath        [原文件路径]
     * @param previewFilePath [预览文件路径]
     *
     *  上传结果通过OnUploadResult返回
     *  文件上传成功只是表示服务器收到了文件，最终需要服务器发送给家长APP端才是真正的分享成功
     *  服务器发送文件给家长APP端通过uploadNotice完成，结果在UploadFileCallback回调
     */
	private void uploadFilesLocal(final String token, String type, final String eid, final String gid, final String filePath, final String previewFilePath) {
        UploadFile uploadFile = new UploadFile(mContext, mToken, mAES_KEY);
        Log.d(TAG, "uploadFilesLocal >> filePath " + filePath + " " + previewFilePath);
        uploadFile.uploadFile(token, type, eid, gid, filePath, previewFilePath, 
            new ProgressListener() {
                @Override
                public void transferred(long l) {
                    Log.d(TAG, "[transferred] >> l : " + l);
                }
            }, 
            new OnUploadResult() {
                @Override
                public void onResult(String s) {
                    Log.d(TAG, "[OnUploadResult] >> onResult : " + s);
                    if (s.contains("GP")) {
                        // upload success
                        UploadFileCallback callback = new UploadFileCallback();                        
                        mXunNetworkManager.uploadNotice(eid, gid, type, s, callback);
                    } else {
                        notifyUIFail();
                    }
                }
            });            
        
    }

     private class UploadFileCallback extends IResponseDataCallBack.Stub{
           @Override
           public void onSuccess(ResponseData responseData) {         
               Log.d(TAG,"[UploadFileCallback] onSuccess >> responseData :" + responseData);
               notifyUISuccess();
           }
           @Override
           public void onError(int i, String s) {
               Log.d(TAG,"[UploadFileCallback] onError >> i :" + i + " ; s : " + s);
               notifyUIFail();            
          }    
    }

    private class ShakeAndUploadImageCallback extends IResponseDataCallBack.Stub{
           @Override
           public void onSuccess(ResponseData responseData) {        
               uploadFilesLocal(mToken, IMAGE_TYPE, mEID, mGID, mImgOriginalPath, mImgOriginalPath); 
               Log.d(TAG,"[ShakeAndUploadImageCallback] onSuccess >> responseData :" + responseData);
           }
           @Override
           public void onError(int i, String s) {
               Log.d(TAG,"[ShakeAndUploadImageCallback] onError >> i :" + i + " ; s : " + s);
          }    
    }

    /**
     * setMapValue的回调接口，会将服务器的握手结果返回
    */
    private class ShakeAndUploadVideoCallback extends IResponseDataCallBack.Stub{
           @Override
           public void onSuccess(ResponseData responseData) {
                // 上传文件接口    
                uploadFilesLocal(mToken, VIDEO_TYPE, mEID, mGID, mVideoPath, mVideoThumbnailPath); 
                Log.d(TAG,"[ShakeAndUploadVideoCallback] onSuccess >> responseData :" + responseData);
           }
           @Override
           public void onError(int i, String s) {
               Log.d(TAG,"[ShakeAndUploadVideoCallback] onError >> i :" + i + " ; s : " + s);
          }    
    }

    

}