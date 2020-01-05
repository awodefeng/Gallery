package com.xxun.xungallery.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.xxun.xungallery.R;
import com.xxun.xungallery.entity.AlbumInfo;
import com.xxun.xungallery.entity.PhotoInfo;
import com.xxun.xungallery.util.BitmapCache;
import com.xxun.xungallery.util.ThumbnailsUtil;
import com.xxun.xungallery.util.UniversalImageLoader;
import com.xxun.xungallery.widget.RotateImageViewAware;
import com.xxun.xungallery.RoundAngleImageView;

import java.io.File;
import java.util.List;

import com.xiaoxun.sdk.utils.Constant;

/**
 * 图片列表适配器
 * <p>
 * 首页展示
 *
 * @author ghc
 */
public class PhotoAdapter extends BaseAdapter {

    private Context mContext;
    private List<PhotoInfo> mPhotoList;
    private ViewHolder mHolder;
    private BitmapCache mBitmapCache;

    private final static int HEIGHT_GRID_ITEM = 113;
    private final static int HEIGHT_GRID_ITEM_730 = 168;
    private final static int WIDTH_GRID_ITEM_730 = 150;

    private final boolean isSW730 = "SW730".equals(Constant.PROJECT_NAME);

    private static final String TAG = "PhotoAdapter";

    BitmapCache.ImageCallback mCallback = new BitmapCache.ImageCallback() {

        @Override
        public void imageLoad(ImageView imageView, Bitmap bitmap, Object... params) {
            if (imageView != null && bitmap != null) {
                String url = (String) params[0];
                if (url != null && url.equals((String) imageView.getTag())) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    Log.e("TAG", "------callback, bmp not match");
                }
            } else {
                Log.e("TAG", "------callback, bmp null");
            }
        }
    };

    public PhotoAdapter(Context context, AlbumInfo mInfo) {
        super();
        this.mContext = context;
        this.mPhotoList = mInfo.getPhotoList();
        mBitmapCache = new BitmapCache();
    }

    public int getPhotoBoundSize() {
        return HEIGHT_GRID_ITEM;
    }

    @Override
    public int getCount() {
        return (mPhotoList == null ? 0 : mPhotoList.size());
    }

    @Override
    public Object getItem(int position) {
        return (mPhotoList == null ? null : mPhotoList.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_image_grid, null);
            mHolder = new ViewHolder(convertView);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }
        LayoutParams layoutParams = (LayoutParams) mHolder.iv_photo.getLayoutParams();
        Log.d(TAG, "getView >> isSW730 " + isSW730);
        if (isSW730) {
            layoutParams.width = WIDTH_GRID_ITEM_730;
            layoutParams.height = HEIGHT_GRID_ITEM_730;
        } else {
            layoutParams.width = getPhotoBoundSize();
            layoutParams.height = getPhotoBoundSize();
        }
        
        Log.d(TAG, "position: " + position);
        Log.d(TAG, "getPhotoBoundSize() >> " + getPhotoBoundSize());
        mHolder.iv_photo.setLayoutParams(layoutParams);
        mHolder.iv_photo.setScaleType(ScaleType.CENTER_CROP);

        final PhotoInfo photoInfo = mPhotoList.get(position);
        String date = photoInfo.getDateModify();
        Log.d(TAG, "date: " + date);
//        mHolder.textViewDate.setText(date);

        if (photoInfo != null) {
            String thumbnailPath = photoInfo.getThumbnailPath();
            String thumbnailURI = "file://" + thumbnailPath;
            String sourcePath = photoInfo.getImagePath();
            mHolder.iv_photo.setTag(sourcePath);

            // 判断是否是Video文件，是的话添加播放按钮
            if (isVideoFile(sourcePath)) {
                mHolder.iv_btn_play.setVisibility(View.VISIBLE);
            } else {
                mHolder.iv_btn_play.setVisibility(View.GONE);
            }

            Log.d(TAG, "thumbnailPath = " + thumbnailPath + ";  sourcePath = " + sourcePath);

            if (!TextUtils.isEmpty(thumbnailPath)) {
                File file = new File(thumbnailPath);
                if (file.exists()) {
                    UniversalImageLoader.displayLocalImage(
                            ThumbnailsUtil.MapgetHashValue(thumbnailURI, thumbnailURI),
                            new RotateImageViewAware(mHolder.iv_photo, sourcePath));
                } else {
                    mBitmapCache.displayBitmap(
                            mHolder.iv_photo,
                            sourcePath,
                            mCallback);
                }
            } else {
                mBitmapCache.displayBitmap(
                        mHolder.iv_photo,
                        sourcePath,
                        mCallback);
            }
        }

        return convertView;
    }

    class ViewHolder {
        //        @BindView(R.id.item_grid_image)
        RoundAngleImageView iv_photo;

        //        @BindView(R.id.item_grid_play)
        ImageView iv_btn_play;

        /*@BindView(R.id.textview_date)
        TextView textViewDate;*/

        ViewHolder(View view) {
//            ButterKnife.bind(this, view);
            iv_photo = (RoundAngleImageView) view.findViewById(R.id.item_grid_image);
            iv_btn_play = (ImageView) view.findViewById(R.id.item_grid_play);
        }
    }

    private boolean isVideoFile(String sourcePath) {
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
}
