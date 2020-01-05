package com.xxun.xungallery.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.xxun.xungallery.Constants;
import com.xxun.xungallery.R;
import com.xxun.xungallery.entity.AlbumInfo;
import com.xxun.xungallery.entity.PhotoInfo;
import com.xxun.xungallery.photoview.PhotoView;
import com.xxun.xungallery.universalimageloader.core.assist.FailReason;
import com.xxun.xungallery.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.xxun.xungallery.util.UniversalImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewPager适配器，继承自PagerAdapter
 *
 * @author ghc
 */
public class ViewPagerAdapter extends PagerAdapter implements Constants {

    private Context mContext;
    private Activity mActivity;
    private List<PhotoInfo> mPhotoList;
    private boolean showToolbar = false;

    boolean isPlay = false;

    private boolean isShowPlayBtn = false;

    private OnStickerClickListener mOnStickerClickListener;

    private static final String videoThumbUri = "/storage/emulated/0/Pictures/";

    private final static String TAG = "ViewPagerAdapter";

    private int mode_normal_or_select = Constants.MODE_NORMAL;

    // 收集启动的所有VideView对象，在滑动向下一页的时候做判断，如果有后台播放的VideoView吗，则pause掉
    private List<VideoViewUtil> mVideoview;

    public interface OnStickerClickListener {
        void onStickerClickListener(String path);

        void onDeleteTouchListener(String path);

        void onShareTouchListener(int fileType, String path);

        void onChatSelectListener(String path);
    }


    public ViewPagerAdapter(Context context, AlbumInfo info, int mode_normal_select) {
        this.mContext = context;
        this.mPhotoList = info.getPhotoList();
        this.mActivity = (Activity) context;

        this.mode_normal_or_select = mode_normal_select;

        initItemView();
    }

    @Override
    public int getCount() {
        return mPhotoList == null ? 0 : mPhotoList.size();
    }

    private void initItemView() {
        mOnStickerClickListener = (OnStickerClickListener) mActivity;

        mVideoview = new ArrayList<VideoViewUtil>();

    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final View imageLayout;
        if (mode_normal_or_select == Constants.MODE_SELECT) {
            imageLayout = LayoutInflater.from(mContext).inflate(R.layout.item_image_pager4chat, null);
            assert imageLayout != null;
            initView4Chat(imageLayout, position, container);
            return imageLayout;
        } else {
            imageLayout = LayoutInflater.from(mContext).inflate(R.layout.item_image_pager, null);
            assert imageLayout != null;
            initViewNormal(imageLayout, position, container);
            return imageLayout;
        }
        
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.d(TAG, "[destroyItem] >> ");
        container.removeView((View) object);
    }

    public void checkVideoViewList() {
        for (int i = 0; i < mVideoview.size(); i++) {
            Log.d(TAG, "[checkVideoViewList] >> videoView: " /*+ mVideoview.get(i)*/
                    + "; " + mVideoview.get(i).mVideoView_util.isPlaying()
                    + "; " + mVideoview.get(i).isPlaying
                    + " mVideoview.size() " + mVideoview.size());
            if (mVideoview.get(i).mVideoView_util.isPlaying()) {
                mVideoview.get(i).mVideoView_util.pause();
                Log.d(TAG, "[checkVideoViewList] >> videoview pause.");
                break;
            }
        }
        //用于清除List中的缓存，避免造成OOM
        for (int ii = mVideoview.size() - 3; ii > 0; ii--) {
            Log.d(TAG, "[checkVideoViewList] >> videoView: " + mVideoview.get(ii)
                    + " " + mVideoview.get(ii).mVideoView_util.isPlaying() + "  remove");
            mVideoview.remove(ii);
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        Log.d(TAG, "[isViewFromObject] >> ");
        return view.equals(object);
    }


    private static boolean isVideoFile(String sourcePath) {
        String type = sourcePath.substring(sourcePath.length() - 4, sourcePath.length());
        if (type.equals(".mp4")
                || type.equals(".avi")
                || type.equals(".3gp")
            /*|| type.equals(".mkv")*/) {
            return true;
        } else {
            return false;
        }
    }

    private String createVideoThumbnail(String thumbFromCP, String videoPath) {
        // for example: /storage/emulated/0/DCIM/Camera/VID_20180424_093559.mp4 
        String thumbPath = videoThumbUri + videoPath.substring(videoPath.lastIndexOf("/"), videoPath.length() - 4) + ".jpg";
        return thumbFromCP == null ? thumbPath : thumbFromCP;
    }

    class VideoViewUtil {
        VideoView mVideoView_util;
        boolean isPlaying;
    }

    private void initViewNormal(View imageLayout, int position, ViewGroup container) {
        final ProgressBar progressBar = (ProgressBar) imageLayout.findViewById(R.id.loading);
        PhotoView photoView = (PhotoView) imageLayout.findViewById(R.id.photoview);
        final ImageView imgPlay = (ImageView) imageLayout.findViewById(R.id.img_play);
        ImageView imgShare = (ImageView) imageLayout.findViewById(R.id.img_share);

        LinearLayout bottom_btn = (LinearLayout) imageLayout.findViewById(R.id.bottom_btn);
        bottom_btn.setVisibility(View.GONE);
        showToolbar = false;

        final VideoView videoView = (VideoView) imageLayout.findViewById(R.id.video_view);
        final ImageView imgSticker = (ImageView) imageLayout.findViewById(R.id.img_sticker);
        final ImageView imgDelete = (ImageView) imageLayout.findViewById(R.id.img_delete);
//        final ImageView img

        PhotoInfo pInfo = mPhotoList.get(position);
        final String uri = pInfo.getImageURI();
        final String thumbFromCP = pInfo.getThumbnailPath();

        final String path = pInfo.getImagePath();
        Log.d(TAG, "thumbFromCP >> " + thumbFromCP + " ; path " + path);


        // 当前的ViewPager加载的是视频文件还是图片文件
        if (isVideoFile(uri)) {

            final String videoThumb = createVideoThumbnail(thumbFromCP, uri);

            imgSticker.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);

            imgPlay.setImageResource(R.drawable.ic_play_normal);
            imgPlay.setVisibility(View.VISIBLE);
            photoView.setVisibility(View.GONE);
            videoView.setVideoPath(uri);
            isPlay = false;
            Log.d(TAG, "thumbFromCP >> " + videoThumb);
            // 设置视频的封面
            videoView.setBackgroundDrawable(Drawable.createFromPath(videoThumb));

            // 设置播放按钮的点击事件，包括状态切换
            imgPlay.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            return true;

                        case MotionEvent.ACTION_UP:
                            if (/*!isPlay*/!videoView.isPlaying()) {
                                videoView.setBackgroundColor(Color.parseColor("#00000000"));
                                videoView.start();
                                // 添加视频播放结束监听，更改图标状态
                                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        //播放结束后的动作
                                        imgPlay.setImageResource(R.drawable.ic_play_click);
                                        isPlay = false;
                                    }
                                });
                                imgPlay.setImageResource(R.drawable.ic_play_stop);
//                                imgPlay.setVisibility(View.GONE);
                                isShowPlayBtn = false;
                                isPlay = true;
                            } else {
                                imgPlay.setImageResource(R.drawable.ic_play_click);
                                videoView.pause();
                                isPlay = false;
                            }
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            return false;
                    }


                    return false;
                }
            });
            VideoViewUtil videoViewUtil = new VideoViewUtil();
            videoViewUtil.mVideoView_util = videoView;
            videoViewUtil.isPlaying = false;
            mVideoview.add(videoViewUtil);

            // 设置整个video画面的点击事件
            // 包括播放按钮的显示与消失
            // 根据最新的交互设计，不需要点击画面隐藏播放按钮
            videoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    switch (motionEvent.getAction()) {

                        case MotionEvent.ACTION_DOWN:
                            return true;

                        case MotionEvent.ACTION_UP:
                            if (showToolbar) {
                                bottom_btn.setVisibility(View.GONE);
                            } else {
                                bottom_btn.setVisibility(View.VISIBLE);
                            }
                            showToolbar = !showToolbar;
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            return false;
                    }
                    return false;
                }
            });

            videoView.requestFocus();


            imgShare.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            return true;

                        case MotionEvent.ACTION_UP:
//                        if(isVideoFile(uri)) {
//                        mOnStickerClickListener.onDeleteTouchListener(path);
//                        }
                            String videoPath = path + "##" + videoThumb;
                            mOnStickerClickListener.onShareTouchListener(SHARE_TYPE_VIDEO, videoPath);
                            Log.d("ViewPagerAdapter", "video share button touch . ");
                            return true;
                    }

                    return false;
                }
            });

            /**
             * 删除按钮事件监听
             */
            imgDelete.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            return true;

                        case MotionEvent.ACTION_UP:
//                         if(isVideoFile(uri)) {
                            mOnStickerClickListener.onDeleteTouchListener(path + "#" + videoThumb);
//                         }
                            Log.d("ViewPagerAdapter", "delete button touch . ");
                            notifyDataSetChanged();
                            return true;
                    }
                    return false;
                }
            });

            // 图片文件的处理逻辑
        } else {
            imgSticker.setVisibility(View.VISIBLE);
            imgPlay.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            photoView.setVisibility(View.VISIBLE);


            UniversalImageLoader.displayImage(
                    uri,
                    photoView,
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            progressBar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });


            photoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            return true;

                        case MotionEvent.ACTION_UP:
                            if (showToolbar) {
                                Log.d("ViewPagerAdapter", "show bottom_btn");
                                // imgSticker.setVisibility(View.VISIBLE);
                                bottom_btn.setVisibility(View.GONE);

                            } else {
                                Log.d("ViewPagerAdapter", "hide bottom_btn");
                                // imgSticker.setVisibility(View.GONE);
                                bottom_btn.setVisibility(View.VISIBLE);
                            }
                            showToolbar = !showToolbar;
                            return true;
                    }
                    return false;
                }
            });

            imgSticker.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            return true;

                        case MotionEvent.ACTION_UP:
                            mOnStickerClickListener.onStickerClickListener(uri);
                    }

                    return false;
                }
            });

            imgShare.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            return true;

                        case MotionEvent.ACTION_UP:
//                        if(isVideoFile(uri)) {
//                        mOnStickerClickListener.onDeleteTouchListener(path);
//                        }
                            mOnStickerClickListener.onShareTouchListener(SHARE_TYPE_IMAGE, path);
                            Log.d("ViewPagerAdapter", "img share button touch . ");
                            return true;
                    }

                    return false;
                }
            });

            imgDelete.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            return true;

                        case MotionEvent.ACTION_UP:
//                         if(isVideoFile(uri)) {
                            mOnStickerClickListener.onDeleteTouchListener(path);
//                         }
                            Log.d("ViewPagerAdapter", "delete button touch . ");
                            notifyDataSetChanged();
                            return true;
                    }
                    return false;
                }
            });


        }
        Log.d(TAG, "instante>>");
        container.addView(imageLayout);
    }

    private void initView4Chat(View imageLayout, int position, ViewGroup container) {
        final ProgressBar progressBar = (ProgressBar) imageLayout.findViewById(R.id.loading);
        PhotoView photoView = (PhotoView) imageLayout.findViewById(R.id.photoview);
        final ImageView imgPlay;

        LinearLayout bottom_btn = (LinearLayout) imageLayout.findViewById(R.id.bottom_select_chat);
        bottom_btn.setVisibility(View.VISIBLE);
        showToolbar = false;

        final VideoView videoView = (VideoView) imageLayout.findViewById(R.id.video_view);
        final ImageView btnOk = (ImageView) imageLayout.findViewById(R.id.btn_ok);

        PhotoInfo pInfo = mPhotoList.get(position);
        final String uri = pInfo.getImageURI();
        final String thumbFromCP = pInfo.getThumbnailPath();

        final String path = pInfo.getImagePath();
        Log.d(TAG, "thumbFromCP >> " + thumbFromCP + " ; path " + path);


        // 当前的ViewPager加载的是视频文件还是图片文件
        if (isVideoFile(uri)) {

            final String videoThumb = createVideoThumbnail(thumbFromCP, uri);
            videoView.setVisibility(View.VISIBLE);

            // imgPlay.setImageResource(R.drawable.ic_play_normal);
            // imgPlay.setVisibility(View.VISIBLE);
            photoView.setVisibility(View.GONE);
            videoView.setVideoPath(uri);
            // isPlay = false;
            Log.d(TAG, "thumbFromCP >> " + videoThumb);
            // 设置视频的封面
            videoView.setBackgroundDrawable(Drawable.createFromPath(videoThumb));
            // videoView.setBackgroundColor(Color.parseColor("#00000000"));
            // videoView.start();

            VideoViewUtil videoViewUtil = new VideoViewUtil();
            videoViewUtil.mVideoView_util = videoView;
            videoViewUtil.isPlaying = false;
            mVideoview.add(videoViewUtil);

            btnOk.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            return true;

                        case MotionEvent.ACTION_UP:
                            mOnStickerClickListener.onChatSelectListener(uri);
                            Log.d("ViewPagerAdapter", "btnOk button touch . ");
                            return true;
                    }
                    return false;
                }
            });

            // 设置整个video画面的点击事件
            // 包括播放按钮的显示与消失
            // 根据最新的交互设计，不需要点击画面隐藏播放按钮
            videoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            return true;

                        case MotionEvent.ACTION_UP:
                            if (/*!isPlay*/!videoView.isPlaying()) {
                                videoView.setBackgroundColor(Color.parseColor("#00000000"));
                                videoView.start();
                            } else {
                                videoView.pause();
                            }
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            return false;
                    }
                    return false;
                }
            });

            // 设置播放按钮的点击事件，包括状态切换
//             imgPlay.setOnTouchListener(new View.OnTouchListener() {
//                 @Override
//                 public boolean onTouch(View view, MotionEvent motionEvent) {

//                     switch (motionEvent.getAction()) {
//                         case MotionEvent.ACTION_DOWN:
//                             return true;

//                         case MotionEvent.ACTION_UP:
//                             if (/*!isPlay*/!videoView.isPlaying()) {
//                                 videoView.setBackgroundColor(Color.parseColor("#00000000"));
//                                 videoView.start();
//                                 // 添加视频播放结束监听，更改图标状态
//                                 videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                                 @Override
//                                 public void onCompletion(MediaPlayer mp) {
//                                     //播放结束后的动作
//                                     imgPlay.setImageResource(R.drawable.ic_play_click);
//                                     isPlay = false;
//                                 }
//                                 });
//                                 imgPlay.setImageResource(R.drawable.ic_play_stop);
// //                                imgPlay.setVisibility(View.GONE);
//                                 isShowPlayBtn = false;
//                                 isPlay = true;
//                             } else {
//                                 imgPlay.setImageResource(R.drawable.ic_play_click);
//                                 videoView.pause();
//                                 isPlay = false;
//                             }
//                             return true;
//                         case MotionEvent.ACTION_MOVE:
//                             return false;
//                     }

//                    return false;
//                 }
//             });

            videoView.requestFocus();

        } else { // 图片文件的处理逻辑
            // imgSticker.setVisibility(View.VISIBLE);
            // imgPlay.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            photoView.setVisibility(View.VISIBLE);


            UniversalImageLoader.displayImage(
                    uri,
                    photoView,
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            progressBar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });

            btnOk.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            return true;

                        case MotionEvent.ACTION_UP:
                            mOnStickerClickListener.onChatSelectListener(path);
                            Log.d("ViewPagerAdapter", "btnOk button touch . ");
                            return true;
                    }
                    return false;
                }
            });

            /*photoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            return true;

                        case MotionEvent.ACTION_UP:
                            if (showToolbar) {
                                Log.d("ViewPagerAdapter", "show bottom_btn");
                                // imgSticker.setVisibility(View.VISIBLE);
                                bottom_btn.setVisibility(View.GONE);

                            } else {
                                Log.d("ViewPagerAdapter", "hide bottom_btn");
                                // imgSticker.setVisibility(View.GONE);
                                bottom_btn.setVisibility(View.VISIBLE);
                            }
                            showToolbar = !showToolbar;
                            return true;
                    }
                    return false;
                }
            });*/
        }
        Log.d(TAG, "instante>>");
        container.addView(imageLayout);
    }
}
