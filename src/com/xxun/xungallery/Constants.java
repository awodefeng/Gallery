package com.xxun.xungallery;

/**
 * 定义了一下静态常量
 *
 * @author ghc
 */
public interface Constants {

    // fragment tag
    public static final String TAG_FRAGMENT_ALBUM = "ALBUM_LIST";
    public static final String TAG_FRAGMENT_PHOTO = "PHOTO_GRID";
    public static final String TAG_FRAGMENT_PAGER = "VIEW_PAGER";

    // 编辑照片，添加贴纸
    public final static String EDIT_PIC_PATH = "com.select.pic.edit";

    // 分享结果相关tag
    public static final int MSG_SHARE_SUCCESS = 0; //分享成功
    public static final int MSG_SHARE_FAIL = 1; // 分享失败
    public static final int MSG_SHARE_DISMISS = 3; // 取消分享框
    public static final int MSG_SHARE_WIFI_FAIL = 4;  // 分享失败：当前网络不可用
    public static final int MSG_SHARE_FILE_TOOBIG = 5; // 分享失败：文件过大
    public static final int MSG_SHARE_ONGOING = 6; // 正在分享
    public static final int MSG_SHARE_UNBIND_FAIL = 7;  // 分享失败：当前未绑定

    public static final int MSG_TRUE_BACK = 8;  // 分享失败：当前未绑定

    // 分享过程的tag，用于intent传递
    public static final String SHARE_PIC_PATH = "share_pic_path"; // 分享照片的地址
    public static final String SHARE_VIDEO_PATH = "share_video_path"; // 分享视频的地址
    public static final String SHARE_VIDEOTHUMB_PATH = "share_thumb_path"; // 分享文件的缩略图

    // 分享文件的类型
    public static final String SHARE_TYPE = "share_file_type";
    public static final int SHARE_TYPE_IMAGE = 0;
    public static final int SHARE_TYPE_VIDEO = 1;

    // 可分享的最大文件 >> 15M
    public static final long MAXFILEZISE = 15 * 1024 * 1024;  // 15M

    public static final int MODE_NORMAL = 0;
    public static final int MODE_SELECT = 1;

}
