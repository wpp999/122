package com.example.wp.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wp.R;
import com.example.wp.activity.PlayerActivity;
import com.example.wp.adapters.TrackListAdapter;
import com.example.wp.base.BaseApplication;
import com.example.wp.base.BaseFragment;
import com.example.wp.interfaces.IHistoryCallback;
import com.example.wp.presenters.HistoryPresenter;
import com.example.wp.presenters.PlayerPresenter;
import com.example.wp.views.ConfirmCheckBoxDialog;
import com.example.wp.views.UILoader;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;


/**
 * Created by TrillGates on 2018/12/16.
 * God bless my code!
 */
public class HistoryFragment extends BaseFragment implements IHistoryCallback, TrackListAdapter.ItemClickListener, TrackListAdapter.ItemLongClickListener, ConfirmCheckBoxDialog.OnDialogActionClickListener {

    private UILoader mUiLoader;
    private TrackListAdapter mTrackListAdapter;
    private HistoryPresenter mHistoryPresenter;
    private Track mCurrentClickHistoryItem = null;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater,ViewGroup container) {
        FrameLayout rootView = (FrameLayout) layoutInflater.inflate(R.layout.fragment_history,container,false);
        if(mUiLoader == null) {
            mUiLoader = new UILoader(BaseApplication.getAppContext()) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView(container);
                }

                @Override
                protected View getEmptyView() {
                    View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_empty_view,this,false);
                    TextView tips = emptyView.findViewById(R.id.empty_view_tips_tv);
                    tips.setText("没有历史记录呢");
                    return emptyView;
                }
            };
        } else {
            if(mUiLoader.getParent() instanceof ViewGroup) {
                ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
            }
        }
        //HistoryPresenter
        mHistoryPresenter = HistoryPresenter.getHistoryPresenter();
        mHistoryPresenter.registerViewCallback(this);
        mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
        mHistoryPresenter.listHistories();
        rootView.addView(mUiLoader);
        return rootView;
    }

    private View createSuccessView(ViewGroup container) {
        View successView = LayoutInflater.from(container.getContext()).inflate(R.layout.item_history,container,false);
        TwinklingRefreshLayout refreshLayout = successView.findViewById(R.id.over_scroll_view);
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadmore(false);
        refreshLayout.setEnableOverScroll(true);
        //recyclerView.
        RecyclerView historyList = successView.findViewById(R.id.history_list);
        historyList.setLayoutManager(new LinearLayoutManager(container.getContext()));
        //设置item的上下间距
        historyList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(),2);
                outRect.bottom = UIUtil.dip2px(view.getContext(),2);
                outRect.left = UIUtil.dip2px(view.getContext(),2);
                outRect.right = UIUtil.dip2px(view.getContext(),2);
            }
        });
        //设置适配器
        mTrackListAdapter = new TrackListAdapter();
        mTrackListAdapter.setItemClickListener(this);
        mTrackListAdapter.setItemLongClickListener(this);
        historyList.setAdapter(mTrackListAdapter);
        return successView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mHistoryPresenter != null) {
            mHistoryPresenter.unRegisterViewCallback(this);
        }
    }

    @Override
    public void onHistoriesLoaded(List<Track> tracks) {
        if(tracks == null || tracks.size() == 0) {
            mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
        } else {
            //更新数据
            mTrackListAdapter.setData(tracks);
            mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }
    }


    @Override
    public void onItemClick(List<Track> detailData, int position) {
        List<Track> mCurrentTrack=detailData;
        /**
         * 这里设置播放器的数据
         */
        PlayerPresenter playerPresenter = PlayerPresenter.getPlayerPresenter();
        playerPresenter.setPlayList(detailData,position);
        //跳转到播放器见面
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(Track track) {
        this.mCurrentClickHistoryItem = track;
        //去删除历史
        ConfirmCheckBoxDialog dialog =new ConfirmCheckBoxDialog(getActivity());
        dialog.setOnDialogActionClickListener(this);
        dialog.show();
    }

    @Override
    public void onCancelClick() {
        //不用做
    }

    @Override
    public void onConfirmClick(boolean isCheck) {

    //去删除历史
        if (mHistoryPresenter!=null &&mCurrentClickHistoryItem != null) {
            if (!isCheck) {
                mHistoryPresenter.delHistory(mCurrentClickHistoryItem);
            } else {
            mHistoryPresenter.cleanHistories();

            }
        }
    }
}