package com.example.wp.fragments;

import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wp.R;
import com.example.wp.adapters.RecommendListAdapter;
import com.example.wp.base.BaseFragment;
import com.example.wp.interfaces.IRecommendViewCallback;
import com.example.wp.presenters.RecommendPresenter;
import com.example.wp.utils.Constants;
import com.example.wp.utils.LogUtil;
import com.example.wp.views.UILoader;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;
import com.ximalaya.ting.android.opensdk.util.IDbDataCallBack;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendFragment extends BaseFragment implements IRecommendViewCallback,UILoader.OnRetryClickListener {


    private static final String TAG = "RecommendFragment";
    private  RecyclerView mRecommendRv;
    private  View mRootView;
    private RecommendListAdapter mRecommendListAdapter;
    private RecommendPresenter mRecommendPresenter;
    private UILoader mUiLoader;
    @Override
    protected View onSubViewLoaded(final LayoutInflater layoutInflater, ViewGroup container) {

       mUiLoader=new UILoader(getContext()) {
            @Override
            protected View getSuccessView(ViewGroup container) {
                return createSuccessView(layoutInflater,container);
            }
        };


       //获取逻辑的对象
       mRecommendPresenter= RecommendPresenter.getInstance();
       //先要设置通知接口的注册
        mRecommendPresenter.registerViewCallback(this);
        //获取推荐列表
        mRecommendPresenter.getRecommendList();
        if (mUiLoader.getParent() instanceof ViewGroup) {
            ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
        }

        mUiLoader.setOnRetryClickListener(this);
        //返回view，给界面显示
        return mUiLoader;
    }

    private View createSuccessView(LayoutInflater layoutInflater, ViewGroup container) {
        //view加载完成
        mRootView = layoutInflater.inflate(R.layout.fragment_recommend,container,false);
        //1.找到控件
        mRecommendRv = mRootView.findViewById(R.id.recommend_list);
        //2.设置布局管理器
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecommendRv.setLayoutManager(linearLayoutManager);
        mRecommendRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top= UIUtil.dip2px(view.getContext(),5);
                outRect.left=UIUtil.dip2px(view.getContext(),5);
                outRect.bottom=UIUtil.dip2px(view.getContext(),5);
                outRect.right=UIUtil.dip2px(view.getContext(),5);

            }
        });
        //3.设置适配器
        mRecommendListAdapter=new RecommendListAdapter();
        mRecommendRv.setAdapter(mRecommendListAdapter);
        return mRootView;
    }


    @Override
  public void onRecommendListLoad(List<Album> result){
        //当我们获取到推荐内容时，这个方法被调用（成功    ）
        //数据回来，就更新UI
        //把数据设置给适配器，更新UI
        mRecommendListAdapter.setData(result);
        mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
  }

    @Override
    public void onNetworkError() {
        mUiLoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);

    }

    @Override
    public void onEmpty() {
        mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);

    }

    @Override
    public void onLoading() {
        mUiLoader.updateStatus(UILoader.UIStatus.LOADING);

    }

    /*@Override
    public  void onLoadMore(List<Album> result ){

  }
    @Override
    public  void onRefreshMore(List<Album> result ){

    }

     */
    @Override
    public void onDestroyView(){
        super.onDestroyView();
        if (mRecommendPresenter!=null){
            mRecommendPresenter.unRegisterViewCallback(this);
        }
    }

    @Override
    public void onRetryClick() {
        //表示网络不佳的时候，点击重试
        //重新获取数据即可
        if (mRecommendPresenter!=null) {
            mRecommendPresenter.getRecommendList();
        }
    }
}
