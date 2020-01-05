package com.xxun.xungallery.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.xxun.xungallery.R;
import com.xxun.xungallery.entity.AlbumInfo;
import com.xxun.xungallery.entity.PhotoInfo;
import com.xxun.xungallery.util.ThumbnailsUtil;
import com.xxun.xungallery.util.UniversalImageLoader;
import com.xxun.xungallery.widget.RotateImageViewAware;

import java.util.List;

/**
 * 相册列表适配器
 *
 * @author ghc
 */
public class AlbumAdapter extends BaseAdapter {

    private List<AlbumInfo> mList;
    private ViewHolder mHolder;
    private Context mContext;

    public AlbumAdapter(Context context) {
        super();
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return (mList == null ? 0 : mList.size());
    }

    @Override
    public Object getItem(int position) {
        Log.d("AlbumAdapter", "position " + position);
        if (mList == null || mList.size() <= 0) {
            Log.d("AlbumAdapter", "mList == null");
            return null;
        } else {
            Log.d("AlbumAdapter", "mList != null");
            return (mList == null ? null : mList.get(position));
        }
//        return (mList.size() > 0 ? null : mList.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_image_list, null);
            mHolder = new ViewHolder(convertView);
            convertView.setTag(mHolder);
        } else
            mHolder = (ViewHolder) convertView.getTag();

        AlbumInfo aInfo = mList.get(position);
        PhotoInfo pInfo = aInfo.getPhotoList().get(0);
        UniversalImageLoader.displayLocalImage(ThumbnailsUtil.MapgetHashValue(pInfo.getImageURI(), pInfo.getImageURI()),
                new RotateImageViewAware(mHolder.iv_album, pInfo.getImagePath()));
        return convertView;
    }

    public List<AlbumInfo> getList() {
        return mList;
    }

    public void setList(List<AlbumInfo> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    class ViewHolder {
        //        @BindView(R.id.album_iv)
        ImageView iv_album;

        ViewHolder(View view) {
//            ButterKnife.bind(this, view);
            iv_album = (ImageView) view.findViewById(R.id.album_iv);
        }
    }
}
