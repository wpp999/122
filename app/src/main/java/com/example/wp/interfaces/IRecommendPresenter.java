package com.example.wp.interfaces;

public interface IRecommendPresenter  {
    /*
    *获取推荐内容
     */
    void getRecommendList();
    /*
    *下拉刷新内容
     */
    void pull2RefreshMore();
    /*
    *上拉加载更多
     */
    void loadMore();
    /*
    *用于注册UI的回调
     */
    void registerViewCallback(IRecommendViewCallback callback);
    /*
    *取消UI的注册回调
     */
    void unRegisterViewCallback(IRecommendViewCallback callback);
}
