package com.example.wp.presenters;

import com.example.wp.data.XimalayApi;
import com.example.wp.interfaces.IRecommendPresenter;
import com.example.wp.interfaces.IRecommendViewCallback;
import com.example.wp.utils.Constants;
import com.example.wp.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendPresenter implements IRecommendPresenter {

    private static final String TAG ="RecommendPresenter";
    private List<IRecommendViewCallback> mCallback =new ArrayList<>();
    private List<Album> mCurrentRecommend = null;

    private RecommendPresenter() {
    }

    private static RecommendPresenter sInstance = null;

    /*
     *获取单例对象
     */
    public static RecommendPresenter getInstance() {
        if (sInstance == null) {
            synchronized (RecommendPresenter.class) {
                if (sInstance==null){
                sInstance = new RecommendPresenter();
            }
        }
    }
      return sInstance;
}

    /**
     * 获取当前推进专辑列表
     * @return 使用时要判空
     */
  public List<Album> getCurrentRecommend(){
        return  mCurrentRecommend;
  }
    /*
      获取推荐内容 ，猜你喜欢
       */
    @Override
    public void getRecommendList() {
        //获取推荐内容
        //封装参数
        upDataLoading();

        XimalayApi ximalayApi = XimalayApi.getXimalayApi();
        ximalayApi.getRecommendList(new IDataCallBack<GussLikeAlbumList>() {


            @Override
            public void onSuccess(GussLikeAlbumList gussLikeAlbumList) {
                //获取成功
                if (gussLikeAlbumList!=null){
                    List<Album> albumList=gussLikeAlbumList.getAlbumList();
                    //数据请求回来后
                    //upRecommendUI(albumList);
                    handlerRecommendResult(albumList);
                }
            }

            @Override
            public void onError(int i, String s) {
                //获取出错
                LogUtil.d(TAG,"error --->"+i);
                LogUtil.d(TAG,"errorMsg --->"+s);
                handlerError();

            }
        });

    }
    private void handlerError() {
        if (mCallback != null) {
            for (IRecommendViewCallback callback : mCallback) {
                callback.onNetworkError();
            }
        }
    }

    private void handlerRecommendResult(List<Album> albumList) {
        //通知UI更新
        if (albumList != null) {
            //测试，清空
            //albumList.clear();
            if (albumList.size() == 0) {
                for (IRecommendViewCallback callback : mCallback) {
                    callback.onEmpty();
                }
            } else{
                    for (IRecommendViewCallback callback : mCallback) {

                        callback.onRecommendListLoad(albumList);
                    }
                    this.mCurrentRecommend = albumList;
                }
            }
        }

        private void upDataLoading(){
            for (IRecommendViewCallback callback : mCallback) {
                callback.onLoading();
            }
        }

    @Override
    public void pull2RefreshMore() {

    }

    @Override
    public void loadMore() {

    }

    @Override
    public void registerViewCallback(IRecommendViewCallback callback) {
    if (mCallback!=null&&!mCallback.contains(callback)){
        mCallback.add(callback);
    }
    }

    @Override
    public void unRegisterViewCallback(IRecommendViewCallback callback) {
    if (mCallback!=null){
        mCallback.remove(callback);
    }
    }
}
