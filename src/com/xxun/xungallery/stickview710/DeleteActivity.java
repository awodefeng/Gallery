package com.xxun.xungallery.stickview710;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.xxun.xungallery.MainPhotoActivity;
import com.xxun.xungallery.R;
import com.xxun.xungallery.util.BitmapUtil;
import com.xxun.xungallery.util.UniversalImageLoader;
import com.xxun.xungallery.RoundAngleImageView;

import java.io.File;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by guohongcheng on 2017/8/31.
 */

public class DeleteActivity extends Activity {
	private final static String TAG = "DeleteActivity";

    private String intentpath, deletePath, showPath;
    private RoundAngleImageView imgNeedDel;
	private TextView tv_delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.delete_layout);
		
		intentpath = getIntent().getStringExtra(MainPhotoActivity.EDIT_PIC_PATH);
        Log.d("DeleteActivity", "pathPic: " + intentpath);
		initView(intentpath);
    }
	
	private void initView(String intentpath) {
		imgNeedDel = (RoundAngleImageView) findViewById(R.id.img_delete);
		tv_delete = (TextView) findViewById(R.id.tv);
		
		/**
		 * 设置中间显示的图片，需要先判断视频文件或者图片文件
		*/
		if (isVideoFile(intentpath)) { // 传过来的是视频文件
			String[] str = intentpath.split("#");
			deletePath = str[0]; // 视频文件路径
			showPath = str[1]; // 视频缩略图
			imgNeedDel.setImageBitmap(BitmapUtil.getLoacalBitmap(showPath));
			tv_delete.setText(R.string.confirm_delete_video);
		} else { // 传过来的是图片文件
			deletePath = intentpath;
			showPath = intentpath;
			imgNeedDel.setImageBitmap(BitmapUtil.getLoacalBitmap(showPath));
			tv_delete.setText(R.string.confirm_delete_photo);
		}       

        //窗口对齐屏幕宽度
        Window win = this.getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = 550;
        lp.height = 550;
        lp.gravity = Gravity.TOP | Gravity.LEFT;//设置对话框置顶显示
        win.setAttributes(lp);
	}

    public void confirm_delete(View view) {
//        String pathDelete = getIntent().getStringExtra(MainPhotoActivity.EDIT_PIC_PATH);
        DeleteImage(deletePath);
        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Intent intent = new Intent(DeleteActivity.this, MainPhotoActivity.class);
				// 通过addFlags添加两个标志，创建一个新的任务栈，并把之前的任务栈清空
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return false;
            }
        }).sendEmptyMessageDelayed(0, 500);
    }

    public void cancel_delete(View view) {
        this.finish();
    }

    private void DeleteImage(String imgPath) {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = MediaStore.Images.Media.query(resolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=?",
                new String[]{imgPath}, null);
        boolean result = false;
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri uri = ContentUris.withAppendedId(contentUri, id);
            int count = getContentResolver().delete(uri, null, null);
            result = count == 1;
        } else {
            File file = new File(imgPath);
            result = file.delete();
        }

        if (result) {
            UniversalImageLoader.clearMemoryCache();
            Log.d("ViewPagerAdapter", "delete success ");
        }
    }
	
	private boolean isVideoFile(String path) {
		return path.contains("#");
	}		

    @Override
    public void onBackPressed() {
        this.finish();
        super.onBackPressed();
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

}
