package com.xxun.xungallery.stickview710;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.xxun.xungallery.R;
import com.xxun.xungallery.adapter.GridStickerAdapter;

import static com.xxun.xungallery.MainPhotoActivity.EDIT_PIC_PATH;

/**
 * Created by guohongcheng on 2017/8/30.
 */

public class ChooseStickerActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
	private final static String TAG = "ChooseStickerActivity";
    private GridView gridView;

    private Integer[] images = {
            R.drawable.ic_sticker01, R.drawable.ic_sticker02,
            R.drawable.ic_sticker03, R.drawable.ic_sticker04
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_choose_sticker);

        gridView = (GridView) findViewById(R.id.stickerGridView);

        GridStickerAdapter pictureAdapter = new GridStickerAdapter(images, this);


        gridView.setAdapter(pictureAdapter);
        gridView.setOnItemClickListener(this);

    }

    //每个Grid Item的点击事件，需要传递给Activity做处理
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("ChooseStickerActivity", "position click " + position);

        String editPathPic = getIntent().getStringExtra(EDIT_PIC_PATH);


        Intent intent = new Intent(this, StickerViewActivity.class);
        intent.putExtra(StickerViewActivity.STICKER_UP, position);
        intent.putExtra(EDIT_PIC_PATH, editPathPic);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
}

