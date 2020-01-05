package com.xxun.xungallery.stickview710;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.view.MotionEvent;
import android.support.v4.view.MotionEventCompat;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.content.res.Resources;

import com.xxun.xungallery.MainPhotoActivity;
import com.xxun.xungallery.R;
import com.xxun.xungallery.stickview710.stickerview.StickerView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class StickerViewActivity extends Activity {
    //    RecyclerView recyclerView;
	private final static String TAG = "StickerViewActivity";
    TextView cleanTv, nextTv;
    FrameLayout bottomPanel;
    RelativeLayout upLayout;
    StickerView stickerView;
    int icons[] = new int[]{
            R.drawable.ic_sticker01, R.drawable.ic_sticker02,
            R.drawable.ic_sticker03, R.drawable.ic_sticker04};

    public static final String STICKER_UP = "StickerViewActivity.position";

    private Context context;

    private boolean isShowBtn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sticker_view_main);
        context = getApplicationContext();
        cleanTv = (TextView) findViewById(R.id.cleanTv);
        nextTv = (TextView) findViewById(R.id.nextTv);
        stickerView = (StickerView) findViewById(R.id.sticker_View_layout);
        stickerView.setMinStickerSizeScale(0.9f);

        upLayout = (RelativeLayout) findViewById(R.id.upLayout);
        bottomPanel = (FrameLayout) findViewById(R.id.bottomPanel);

        String urlPic = getIntent().getStringExtra(MainPhotoActivity.EDIT_PIC_PATH).substring(7);

        Log.d("StickerViewActivity", "pathPic: " + urlPic);
        stickerView.setImageBitmap(getLoacalBitmap(urlPic));

        System.gc();

        int stickerFromUp = getIntent().getIntExtra(STICKER_UP, 0);
        stickerView.addSticker(icons[stickerFromUp]);

        // 上方的 CLEAN 点击事件，清除所有的 贴纸
        cleanTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stickerView.clearSticker();
                finish();
            }
        });

        // 保存事件
        nextTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String savepath = stickerView.saveSticker();
                Log.d("StickerViewActivity", "savepath: " + savepath);
				nextTv.setClickable(false);
                // Toast.makeText(StickerViewActivity.this, R.string.pic_onsaving, Toast.LENGTH_SHORT).show();
                showShortTimeDialog();
                // 启动SelectPhoto时，加800毫秒的延时，避免出现图片未扫描的情况
                new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        startActivitySelect(v);
                        return false;
                    }
                }).sendEmptyMessageDelayed(0, 800); //800 毫秒


            }
        });

        stickerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("StickerViewActivity", "event: " + event);

            int action = MotionEventCompat.getActionMasked(event);
            switch (action) {
                case MotionEvent.ACTION_POINTER_DOWN:                                   
                    break;

                case MotionEvent.ACTION_DOWN:                
                    break;

                case MotionEvent.ACTION_MOVE:
                    bottomPanel.setVisibility(View.GONE);
                    isShowBtn = false; 
                    break;

                case MotionEvent.ACTION_UP:
                    if(isShowBtn) {
                        bottomPanel.setVisibility(View.GONE);            
                    }else {
                        bottomPanel.setVisibility(View.VISIBLE);
                    }
                    isShowBtn = !isShowBtn;
                    break;

                case MotionEvent.ACTION_POINTER_UP:              
                    break;
            }
                return false;  
        }
        });
    }

    /**
     * 加载本地图片
     *
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        Bitmap bitmap = null;
        try {
            FileInputStream fis = new FileInputStream(url);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
            // 先判断是否已经回收
            if (bitmap != null && !bitmap.isRecycled()) {
                // 回收并且置为null
                bitmap.recycle();
                bitmap = null;
            }
            System.gc();
        }
        if (bitmap == null) {
            // 如果实例化失败 返回默认的Bitmap对象
            return null;
        }
        return bitmap;
    }

    public void startActivitySelect(View v) {
        //添加activity的过场动画
//        ActivityOptionsCompat compat = ActivityOptionsCompat.makeScaleUpAnimation(v, v.getWidth() / 2,
//                v.getHeight() / 2, 0, 0);
        Intent intent = new Intent(this, MainPhotoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(/*this, */intent/*, compat.toBundle()*/);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        return;
    }
	
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

    private void showShortTimeDialog() {
        Toast toast = Toast.makeText(this, "正在保存", Toast.LENGTH_SHORT);
        LinearLayout view = (LinearLayout) toast.getView();
        view.setBackgroundColor(getResources().getColor(R.color.transparent));
        TextView textView = (TextView) view.getChildAt(0);
        Resources r  = getResources();
        int textSize = (int) r.getDimension(R.dimen.sticker_textsize);
        textView.setTextSize(textSize);
        toast.setGravity(Gravity.CENTER, 0, 60);
        toast.setView(view);
        toast.show();
    }
}
