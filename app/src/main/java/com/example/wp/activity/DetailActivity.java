package com.example.wp.activity;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wp.R;
import com.example.wp.interfaces.IAlbumDetailViewCallback;
import com.example.wp.presenters.AlbumDetailPresenter;
import com.example.wp.utils.ImageBlur;
import com.example.wp.utils.LogUtil;
import com.example.wp.views.RoundRectImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.List;

public class DetailActivity extends BaseActivity implements IAlbumDetailViewCallback {


    private static final String TAG ="DetailActivity" ;
    private  ImageView mLargerCover;
    private RoundRectImageView mSmallCover;
    private TextView mAlbumTitle;
    private TextView mAlbumAuthor;
    private AlbumDetailPresenter mAlbumDetailPresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        getWindow().setStatusBarColor(Color.TRANSPARENT);


        initView();
       mAlbumDetailPresenter= AlbumDetailPresenter.getInstance();
       mAlbumDetailPresenter.registerViewCallback(this);

    }

    private void initView() {
        mLargerCover = this.findViewById(R.id.cover_bg);
        mSmallCover= this.findViewById(R.id.small_cover);
        mAlbumAuthor=findViewById(R.id.album_author_tv);
        mAlbumTitle=findViewById(R.id.album_title_tv);
    }

    @Override
    public void onDetailListLoad(List<Track> tracks) {

    }

    @Override
    public void onAlbumLoaded(Album album) {
        if (mAlbumTitle!=null) {
            mAlbumTitle.setText(album.getAlbumTitle());
        }
        if (mAlbumAuthor!=null) {
            mAlbumAuthor.setText(album.getAnnouncer().getNickname());
        }

        //做毛玻璃效果
        if (mLargerCover != null && null != mLargerCover) {
            Picasso.with(this).load(album.getCoverUrlLarge()).into(mLargerCover, new Callback() {
                @Override
                public void onSuccess() {
                    Drawable drawable = mLargerCover.getDrawable();
                    if (drawable != null) {
                        //到这里才说明是有图片的
                        ImageBlur.makeBlur(mLargerCover, DetailActivity.this);
                    }
                }

                @Override
                public void onError() {
                    LogUtil.d(TAG, "onError");
                }
            });

        }

        if (mSmallCover!=null) {
            Picasso.with(this).load(album.getCoverUrlLarge()).into(mSmallCover);
        }
    }
}