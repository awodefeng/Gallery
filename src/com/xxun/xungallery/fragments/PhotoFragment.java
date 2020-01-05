package com.xxun.xungallery.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.xxun.xungallery.R;
import com.xxun.xungallery.adapter.PhotoAdapter;
import com.xxun.xungallery.entity.AlbumInfo;
import com.xxun.xungallery.entity.PhotoInfo;

import java.util.List;

/**
 * 展示某一个相册中图片表格的Fragment
 *
 * @author ghc
 */
public class PhotoFragment extends BaseFragment {

    //    @BindView(R.id.photo_gridview)
    GridView mGridView;

    //    @BindView(R.id.textview_date)
    TextView textViewDate;

    private AlbumInfo mAlbumInfo;
    private PhotoAdapter mAdapter;
    private OnGridClickListener mOnGridClickListener;
    private List<PhotoInfo> mPhotoList;
    PhotoInfo photoInfo = null;


    public interface OnGridClickListener {
        void onGridItemClick(AlbumInfo albumInfo, int position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() instanceof OnGridClickListener) {
            mOnGridClickListener = (OnGridClickListener) getActivity();
        } else if (getParentFragment() instanceof OnGridClickListener) {
            mOnGridClickListener = (OnGridClickListener) getParentFragment();
        } else {

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFragment = inflater.inflate(R.layout.fragment_gridview, container, false);
//        ButterKnife.bind(this, mFragment);
        mGridView = (GridView) mFragment.findViewById(R.id.photo_gridview);
        textViewDate = (TextView) mFragment.findViewById(R.id.textview_date);
        initView();
        return mFragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            mAdapter = new PhotoAdapter(getActivity().getApplicationContext(), mAlbumInfo);
            mGridView.setAdapter(mAdapter);
        }
        initEvent();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    public void setInfo(AlbumInfo info) {
        this.mAlbumInfo = info;

        mPhotoList = mAlbumInfo.getPhotoList();
        photoInfo = mPhotoList.get(0);
    }

    @Override
    public void initView() {
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void initEvent() {
        mGridView.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                ShowDateOnTop(firstVisibleItem);
            }
        });

        mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAdapter != null) {
                    if (mOnGridClickListener != null) {
                        mOnGridClickListener.onGridItemClick(mAlbumInfo, position);
                    }
                }
            }
        });
    }

    @Override
    public void invalidate() {
        mAdapter.notifyDataSetChanged();
    }

    private String convertDate(String date) {
        String str[] = date.split("-");
        return str[0] + "/" + str[1] + "/" + str[2];
    }

    private boolean isNeedChange(String datePre, String dateNow) {
        return !datePre.equals(dateNow);
    }

    private void ShowDateOnTop(int firstVisibleItem) {
        photoInfo = mPhotoList.get(firstVisibleItem);
        String date = photoInfo.getDateModify();
        if (isNeedChange((String) textViewDate.getText(), convertDate(date))) {
            textViewDate.setText(convertDate(date));
            Log.d("PhotoFragment", "convertDate: " + convertDate(date));
        }
        Log.d("PhotoFragment", "firstVisibleItem: date " + firstVisibleItem + "; " + date);
    }


}
