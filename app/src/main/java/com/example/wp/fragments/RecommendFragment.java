package com.example.wp.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wp.R;
import com.example.wp.adapters.RecommendListAdapter;
import com.example.wp.base.BaseFragment;
import com.example.wp.utils.Constants;
import com.example.wp.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;
import com.ximalaya.ting.android.opensdk.util.IDbDataCallBack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendFragment extends BaseFragment {
    private static final String TAG = "RecommendFragment";
    private  RecyclerView mRecommendRv;
    private  View mRootView;
    private RecommendListAdapter mRecommendListAdapter;
    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        //view加载完成
        mRootView = layoutInflater.inflate(R.layout.fragment_recommend,container,false);
        //RecyclerView的使用
        //1.找到控件
       mRecommendRv = mRootView.findViewById(R.id.recommend_list);
       //2.设置布局管理器
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
       linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecommendRv.setLayoutManager(linearLayoutManager);
        //3.设置适配器
         mRecommendListAdapter=new RecommendListAdapter();
        mRecommendRv.setAdapter(mRecommendListAdapter);


       //去拿数据
        getRecommendData();

        //返回view，给界面显示
        return mRootView;
    }
    /*
    获取推荐内容 ，猜你喜欢
     */
    private void getRecommendData() {
        //封装参数
        Map<String,String> map=new HashMap<>();
        //返回多少条
        map.put(DTransferConstants.LIKE_COUNT, Constants.RECOMMEND_COUNT+"");
        CommonRequest.getGuessLikeAlbum(map, new IDataCallBack<GussLikeAlbumList>() {
            @Override
            public void onSuccess(GussLikeAlbumList gussLikeAlbumList) {
                //获取成功
                if (gussLikeAlbumList!=null){
                    List<Album> albumList=gussLikeAlbumList.getAlbumList();
                   //数据请求回来后
                    upRecommendUI(albumList);
                    }
                }




            @Override
            public void onError(int i, String s) {
                //获取出错
                LogUtil.d(TAG,"error --->"+i);
                LogUtil.d(TAG,"errorMsg --->"+s);
                
            }
        });

    }
    private void upRecommendUI(List<Album> albumList) {
        //把数据设置给适配器，更新UI
        mRecommendListAdapter.setData(albumList);
    }
}
