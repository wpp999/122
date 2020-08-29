package com.example.wp.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.List;

public interface IAlbumDetailViewCallback {
    /**
     * 专辑详情内容加载出来了
     * @param tracks
     */
    void onDetailListLoaded(List<Track> tracks);
    //网络错误时显示的

    void onNetworkError(int errorCode, String errorMsg);

    /**
     * 把album传给UI
     * @param album
     */
    void onAlbumLoaded(Album album);

    /**
     * 加载更多的结果
     * @param
     */
    void onLoaderMoreFinished(int size);
    /**
     * 下拉刷新更多的结果
     * @param
     */
    void onRefreshFinished(int size);
}
