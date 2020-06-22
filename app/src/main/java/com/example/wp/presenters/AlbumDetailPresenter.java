package com.example.wp.presenters;

import com.example.wp.interfaces.IAlbumDetailPresenter;
import com.example.wp.interfaces.IAlbumDetailViewCallback;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.ArrayList;
import java.util.List;

public class AlbumDetailPresenter implements IAlbumDetailPresenter {

    private List<IAlbumDetailViewCallback> mCallback=new ArrayList<>();
    private Album mTargetAlbum=null;

    private AlbumDetailPresenter(){

    }

    private  static AlbumDetailPresenter sInstance  =null;

    public  static  AlbumDetailPresenter getInstance(){
        if (sInstance==null) {
            synchronized (AlbumDetailPresenter.class){
                sInstance=new AlbumDetailPresenter();
            }
        }
        return  sInstance;
    }





    @Override
    public void pull2RefreshMore() {

    }

    @Override
    public void loadMore() {

    }

    @Override
    public void getAlbumDetail(int albumId, int page) {

    }

    @Override
    public void registerViewCallback(IAlbumDetailViewCallback detailViewCallback) {
        if (!mCallback.contains(detailViewCallback)) {
            mCallback.add(detailViewCallback);
            if (mTargetAlbum!=null) {
                detailViewCallback.onAlbumLoaded(mTargetAlbum);
            }
        }
    }

    @Override
    public void unregisterViewCallback(IAlbumDetailViewCallback detailViewCallback) {
        mCallback.remove(detailViewCallback);
    }

    public void setTargetAlbum(Album targetAlbum){
        this.mTargetAlbum   =targetAlbum;
    }
}
