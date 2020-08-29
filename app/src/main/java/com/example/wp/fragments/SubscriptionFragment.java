package com.example.wp.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wp.R;
import com.example.wp.activity.DetailActivity;
import com.example.wp.adapters.AlbumListAdapter;
import com.example.wp.base.BaseApplication;
import com.example.wp.base.BaseFragment;
import com.example.wp.interfaces.ISubscriptionCallback;
import com.example.wp.interfaces.ISubscriptionPresenter;
import com.example.wp.presenters.AlbumDetailPresenter;
import com.example.wp.presenters.SubscriptionPresenter;
import com.example.wp.utils.Constants;
import com.example.wp.views.ConfirmDialog;
import com.example.wp.views.UILoader;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.Collections;
import java.util.List;

public class SubscriptionFragment extends BaseFragment implements ISubscriptionCallback, AlbumListAdapter.OnAlbumItemClickListener, AlbumListAdapter.OnAlbumItemLongClickListener, ConfirmDialog.OnDialogActionClickListener {

    private ISubscriptionPresenter mSubscriptionPresenter;
    private RecyclerView mSubListView;
    private AlbumListAdapter mAlbumListAdapter;
    private Album mCurrentClickAlbum =null;
    private UILoader mUiLoader;


    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        FrameLayout rootView = (FrameLayout) layoutInflater.inflate(R.layout.fragment_subscription, container, false);
        if (mUiLoader == null) {
        mUiLoader = new UILoader(container.getContext()) {
            @Override
            protected View getSuccessView(ViewGroup container) {
                return createSuccessView();
            }

            @Override
            protected View getEmptyView() {

                View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_empty_view, this, false);
                TextView tipsview = emptyView.findViewById(R.id.empty_view_tips_tv);
                tipsview.setText(R.string.no_sub_content_tips_text);
                return emptyView;
            }
        };
            if (mUiLoader.getParent() instanceof ViewGroup) {
                ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
            }
            rootView.addView(mUiLoader);
        }

        return rootView;
    }

    private View createSuccessView() {
        View itemView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.item_subscription,null);
        TwinklingRefreshLayout refreshLayout = itemView.findViewById(R.id.over_scroll_view);
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadmore(false);
        mSubListView = itemView.findViewById(R.id.sub_list);
        mSubListView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        mSubListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top= UIUtil.dip2px(view.getContext(),3);
                outRect.left=UIUtil.dip2px(view.getContext(),5);
                outRect.bottom=UIUtil.dip2px(view.getContext(),3);
                outRect.right=UIUtil.dip2px(view.getContext(),5);

            }
        });
        //
        mAlbumListAdapter = new AlbumListAdapter();
        mAlbumListAdapter.setAlbumItemClickListener(this);
        mAlbumListAdapter.setOnAlbumItemLongClickListener(this);
        mSubListView.setAdapter(mAlbumListAdapter);
        mSubscriptionPresenter = SubscriptionPresenter.getInstance();
        mSubscriptionPresenter.registerViewCallback(this);
        mSubscriptionPresenter.getSubscriptionList();
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
        }
        return itemView;
    }

    @Override
    public void onAddResult(boolean isSuccess) {

    }

    @Override
    public void onDeleteResult(boolean isSuccess) {
        Toast.makeText(BaseApplication.getAppContext(), isSuccess?R.string.cancel_sub_success:R.string.cancel_sub_failed , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSubscriptionsLoaded(List<Album> albums) {
        if (albums.size() == 0) {
            mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
        } else {
            if (mUiLoader != null) {
                mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
            }
        }
    //更新ui
        if (mAlbumListAdapter != null) {
            mAlbumListAdapter.setData(albums);
        }
    }

    @Override
    public void onSubFull() {
        Toast.makeText(getActivity(), "订阅数量不得超过" + Constants.MAX_SUB_COUNT, Toast.LENGTH_SHORT).show();

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //取消接口的注册
        if (mSubscriptionPresenter != null) {
            mSubscriptionPresenter.unRegisterViewCallback(this);
        }
        mAlbumListAdapter.setAlbumItemClickListener(null);
    }

    @Override
    public void OnItemClick(int position, Album album) {

        //根据位置获取数据
        AlbumDetailPresenter.getInstance().setTargetAlbum(album);
        //item被点击了，跳转页面
        Intent intent=new Intent(getContext(), DetailActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(Album album) {
        this.mCurrentClickAlbum =album;
        //这是订阅的item被长按了
//        Toast.makeText(BaseApplication.getAppContext(), "订阅被长按了", Toast.LENGTH_SHORT).show();
        ConfirmDialog confirmDialog = new ConfirmDialog(getActivity());
        confirmDialog.setOnDialogActionClickListener(this);
        confirmDialog.show();
    }

    @Override
    public void onCancelSubClick() {
        //取消订阅内容
        if (mCurrentClickAlbum != null && mSubscriptionPresenter != null) {
            mSubscriptionPresenter.deleteSubscription(mCurrentClickAlbum);
        }

    }

    @Override
    public void onGiveUpClick() {
        //放弃取消订阅

    }
}
