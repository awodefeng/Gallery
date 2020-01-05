package com.xxun.xungallery.util;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.xxun.xungallery.R;
import com.xxun.xungallery.universalimageloader.core.DisplayImageOptions;
import com.xxun.xungallery.universalimageloader.core.ImageLoader;
import com.xxun.xungallery.universalimageloader.core.assist.ImageScaleType;
import com.xxun.xungallery.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.xxun.xungallery.universalimageloader.core.imageaware.ImageAware;
import com.xxun.xungallery.universalimageloader.core.listener.ImageLoadingListener;
import com.xxun.xungallery.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * 通用图片加载程序（UIL）的显示方法工具类
 */
public class UniversalImageLoader {

    private static final String TAG = "UniversalImageLoader";
    private static final String videoThumbUri = "file:///storage/emulated/0/Pictures/";

    private static ImageLoader imageLoader = ImageLoader.getInstance();

    public static boolean checkImageLoader() {
        return imageLoader.isInited();
    }

    /**
     * 加载显示listview gridview本地图库里图片
     *
     * @param uri
     * @param imageAware
     */
    public static void displayLocalImage(String uri, ImageAware imageAware) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.icon_display_profile_def)
                .showImageForEmptyUri(R.drawable.ic_error)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new SimpleBitmapDisplayer())
                .build();

        //System.out.println("cccc----display local image 111");
        imageLoader.displayImage(uri, imageAware, options);
    }

    /**
     * 加载显示viewpager大图片
     *  @param uri
     * @param imageView
     * @param listener
     */
    public static void displayImage(String uri, ImageView imageView, SimpleImageLoadingListener listener) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_error)
                .showImageOnFail(R.drawable.ic_error)
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)
                .displayer(new SimpleBitmapDisplayer())
                .build();

        //System.out.println("cccc----display big image 222");
        Log.d(TAG, "[displayImage] >>> uri : " + uri);
        if (isVideoFile(uri)) {
            String videoThumb = createVideoThumbnail(uri);
            Log.d(TAG, "[displayImage] >>> thumbPath " + videoThumb);
            imageLoader.displayImage(videoThumb, imageView, options, listener);
        } else {
            imageLoader.displayImage(uri, imageView, options, listener);

        }
    }

    private static String createVideoThumbnail(String videoPath) {

        String thumbPath = videoThumbUri + videoPath.substring(videoPath.lastIndexOf("/"), videoPath.length() - 4) + ".jpg";

        return thumbPath;
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

    /**
     * 清除内存缓存
     */
    public static void clearMemoryCache() {
        imageLoader.clearMemoryCache();
    }

}
