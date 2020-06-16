package com.example.wp.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.List;

public interface IRecommendViewCallback  {
    /*
    *获取推荐内容的结果
    * @param result
     */
    void onRecommendListLoad(List<Album> result);
    /**
     * 网络错误
     */
    void onNetworkError();
    /**
     * 数据为空
     */
    void onEmpty();
    /**
     * 网络加载中
     */
    void onLoading();
    /*加载更多
    * @param result
     */
   // void onLoadMore(List<Album> result);
    /*
    *下拉刷新
     */
    //void onRefreshMore(List<Album> result);
}
