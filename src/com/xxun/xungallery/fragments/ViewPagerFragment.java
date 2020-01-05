package com.xxun.xungallery.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import com.xxun.xungallery.MainPhotoActivity;
import com.xxun.xungallery.R;
import com.xxun.xungallery.adapter.ViewPagerAdapter;
import com.xxun.xungallery.entity.AlbumInfo;
import com.xxun.xungallery.widget.ViewPagerFixed;
import com.xxun.xungallery.Constants;

/**
 * 展示图片大图的Fragment
 *
 * @author ghc
 */
public class ViewPagerFragment extends BaseFragment implements OnPageChangeListener {
    private static final String TAG = "ViewPagerFragment";
    //    @BindView(R.id.view_pager)
    ViewPagerFixed mViewPager;

    private AlbumInfo mAlbumInfo;
    private ViewPagerAdapter mAdapter;

    private MainPhotoActivity mActivity;

    private int mCurPosition = 0;

    private int MODE_NORMAL_SELECT = Constants.MODE_NORMAL;

    public void setInfo(AlbumInfo info, int position) {
        this.mAlbumInfo = info;
        this.mCurPosition = position;
    }

    public void setCurrentMode(int mode) {
        this.MODE_NORMAL_SELECT = mode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragment = inflater.inflate(R.layout.fragment_viewpager, container, false);
        mActivity = (MainPhotoActivity) getActivity();
//        ButterKnife.bind(this, mFragment);
        mViewPager = (ViewPagerFixed) mFragment.findViewById(R.id.view_pager);
        initView();
        return mFragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mActivity != null) {
            mAdapter = new ViewPagerAdapter(mActivity, mAlbumInfo, MODE_NORMAL_SELECT);
            mViewPager.setAdapter(mAdapter);
        }
        initEvent();
    }

    @Override
    public void onResume() {
        super.onResume();
        onPageSelected(mCurPosition);
    }

    @Override
    public void initView() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPause() {
        super.onPause();
        // 焦点被强占时，退出预览界面
        // 为解决bug，播放视频时，来语音消息，导致视频黑屏
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ViewPagerFragment fragment = (ViewPagerFragment) fm.findFragmentByTag(getTag());
        Log.d(TAG, "getTag " + getTag());
        if (fragment != null) {
            ((MainPhotoActivity) getActivity()).removeFragment(fragment);
        } else {
            Log.d(TAG, "fragment = null ");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fm = getActivity().getSupportFragmentManager();
                ViewPagerFragment fragment = (ViewPagerFragment) fm.findFragmentByTag(getTag());
                if (fragment == null) return false;
                ((MainPhotoActivity) getActivity()).removeFragment(fragment);
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void initEvent() {
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setCurrentItem(mCurPosition);
    }

    @Override
    public void invalidate() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mAdapter.checkVideoViewList();
        mCurPosition = position;
    }
}
