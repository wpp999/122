package com.example.wp.interfaces;

import com.example.wp.base.IBasePresenter;

public interface IAlbumDetailPresenter extends IBasePresenter<IAlbumDetailViewCallback> {

    /*
     *下拉刷新内容
     */
    void pull2RefreshMore();
    /*
     *上拉加载更多
     */
    void loadMore();

    /**
     * 获取专辑详情
     * @param albumId
     * @param page
     */

    void getAlbumDetail(int albumId, int page);





}
