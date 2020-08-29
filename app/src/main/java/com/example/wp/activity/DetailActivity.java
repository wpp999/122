package com.example.wp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wp.R;
import com.example.wp.adapters.TrackListAdapter;
import com.example.wp.base.BaseApplication;
import com.example.wp.interfaces.IAlbumDetailViewCallback;
import com.example.wp.interfaces.IPlayerCallback;
import com.example.wp.interfaces.ISubscriptionCallback;
import com.example.wp.interfaces.ISubscriptionPresenter;
import com.example.wp.presenters.AlbumDetailPresenter;
import com.example.wp.presenters.PlayerPresenter;
import com.example.wp.presenters.SubscriptionPresenter;
import com.example.wp.utils.Constants;
import com.example.wp.utils.ImageBlur;
import com.example.wp.utils.LogUtil;
import com.example.wp.views.RoundRectImageView;
import com.example.wp.views.UILoader;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.header.bezierlayout.BezierLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class DetailActivity extends BaseActivity implements IAlbumDetailViewCallback, UILoader.OnRetryClickListener, TrackListAdapter.ItemClickListener, IPlayerCallback, ISubscriptionCallback {

    private static final String TAG ="DetailActivity" ;
    private  ImageView mLargerCover;
    private RoundRectImageView mSmallCover;
    private TextView mAlbumTitle;
    private TextView mAlbumAuthor;
    private AlbumDetailPresenter mAlbumDetailPresenter;
    private int mCurrentPage = 1;
    private UILoader mUiLoader;
    private long mCurrentId = -1;
    private Album  mCurrentAlbum;
    private RecyclerView mDetailList;
    private TrackListAdapter mTrackListAdapter;
    private FrameLayout mDetailListContainer;
    private ImageView mPlayControlBtn;
    private TextView mPlayControlTips;
    private PlayerPresenter mPlayerPresenter;
    private View mPlayControlContainer;
    private List<Track> mCurrentTracks = null;
    private final int DEFAULT_PLAY_INDEX=0;
    private TwinklingRefreshLayout mRefreshLayout;
    private String mCurrentTrackTitle =null;
    private TextView mSubBtn;
    private ISubscriptionPresenter mSubscriptionPresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        getWindow().setStatusBarColor(Color.TRANSPARENT);


        initView();
        initPresent();
        //设置订阅按钮的状态
        updateSubState(); 
        updatePlayState(mPlayerPresenter.isPlaying());
        initListener();
    }

    private void updateSubState() {
        if (mSubscriptionPresenter != null) {
            boolean isSub = mSubscriptionPresenter.isSub(mCurrentAlbum);
            mSubBtn.setText(isSub ? R.string.cancel_sub_tips_text :R.string.sub_tips_text);
        }
    }

    private void initPresent() {
        //这个是专辑详情的presenter.
        mAlbumDetailPresenter = AlbumDetailPresenter.getInstance();
        mAlbumDetailPresenter.registerViewCallback(this);
        //播放器的Presenter.
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();
        mPlayerPresenter.registerViewCallback(this);
        //订阅相关的presenter.
        mSubscriptionPresenter = SubscriptionPresenter.getInstance();
        mSubscriptionPresenter.getSubscriptionList();
        mSubscriptionPresenter.registerViewCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAlbumDetailPresenter != null) {
            mAlbumDetailPresenter.unRegisterViewCallback(this);
        }
        if (mPlayerPresenter != null) {
            mPlayerPresenter.unRegisterViewCallback(this);
        }
        if (mSubscriptionPresenter != null) {
            mSubscriptionPresenter.unRegisterViewCallback(this);
        }
    }

    private void initListener() {

        mPlayControlContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    if (mPlayerPresenter != null) {
                        /**
                         * 判断播放器是否有播放列表
                         */
                        boolean has = mPlayerPresenter.hasPlayList();
                        if (has) {
                            //控制播放器状态
                            handlePlayControl();
                        } else {
                            handleNoPlayList();

                        }
                    }
                }
            });
        mSubBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSubscriptionPresenter != null) {
                    boolean isSub = mSubscriptionPresenter.isSub(mCurrentAlbum);
                    if (isSub) {
                        mSubscriptionPresenter.deleteSubscription(mCurrentAlbum);
                    } else {
                        mSubscriptionPresenter.addSubscription(mCurrentAlbum);
                    }

                }
            }
        });

    }

    /**
     * 当播放器里面没有播放列表时
     */
    private void handleNoPlayList() {
        mPlayerPresenter.setPlayList(mCurrentTracks,DEFAULT_PLAY_INDEX);

    }

    private void handlePlayControl() {
        //控制播放器状态
        if (mPlayerPresenter.isPlaying()) {
            //正在播放，则暂停
            mPlayerPresenter.pause();
        } else {
            mPlayerPresenter.play();
        }
    }

    private void initView() {
        mDetailListContainer = this.findViewById(R.id.detail_list_container);
        if (mUiLoader == null) {
            mUiLoader = new UILoader(this) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView(container);
                }
            };
            mDetailListContainer.removeAllViews();
            mDetailListContainer.addView(mUiLoader);
            mUiLoader.setOnRetryClickListener(DetailActivity.this);
        }


        mLargerCover = this.findViewById(R.id.iv_large_cover);
        mSmallCover= this.findViewById(R.id.viv_small_cover);
        mAlbumAuthor=this.findViewById(R.id.tv_album_author);
        mAlbumTitle=this.findViewById(R.id.tv_album_title);
        mPlayControlBtn = this.findViewById(R.id.detail_play_control);
        mPlayControlTips = this.findViewById(R.id.play_control_tv);
        mPlayControlTips.setSelected(true);
        mPlayControlContainer= this.findViewById(R.id.player_control_container);
        mSubBtn = this.findViewById(R.id.detail_sub_btn);


    }
        private boolean mIsLoaderMore = false;
    private View createSuccessView(ViewGroup container) {
        View detailListView = LayoutInflater.from(this).inflate(R.layout.item_detail_list, container, false);
        mDetailList = detailListView.findViewById(R.id.album_detail_list);
        mRefreshLayout = detailListView.findViewById(R.id.refresh_layout);
        //RecyclerView使用步骤
        //1.设置布局管理器
        mDetailList.setLayoutManager(new LinearLayoutManager(this));
        //2.设置适配器
        mTrackListAdapter = new TrackListAdapter();
        mDetailList.setAdapter(mTrackListAdapter);
        //设置item的间距
        mDetailList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top= UIUtil.dip2px(view.getContext(),3);
                outRect.left=UIUtil.dip2px(view.getContext(),5);
                outRect.bottom=UIUtil.dip2px(view.getContext(),3);
                outRect.right=UIUtil.dip2px(view.getContext(),5);

            }
        });
        mTrackListAdapter.setItemClickListener(this);
        BezierLayout headerView=new BezierLayout(this);
        mRefreshLayout.setHeaderView(headerView);
        mRefreshLayout.setMaxHeadHeight(100);
        mRefreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                    super.onRefresh(refreshLayout);
                BaseApplication.getsHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DetailActivity.this, "刷新成功...", Toast.LENGTH_SHORT).show();
                        mRefreshLayout.finishRefreshing();
                    }
                },2000);

            }

            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                super.onLoadMore(refreshLayout);
                if (mAlbumDetailPresenter != null) {
                    mAlbumDetailPresenter.loadMore();
                }
                refreshLayout.finishLoadmore();
//                BaseApplication.getsHandler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(DetailActivity.this, "加载更多完成", Toast.LENGTH_SHORT).show();
//                        mRefreshLayout.finishLoadmore();
//
//                    }
//                },2000);

            }
        });
        return detailListView;
    }

    @Override
    public void onDetailListLoaded(List<Track> tracks) {
        if (mIsLoaderMore && mRefreshLayout!=null) {
            mRefreshLayout.finishLoadmore();
            mIsLoaderMore =false;

        }
        this.mCurrentTracks =tracks;
        //判断数据结果 ，根据数据结果控制UI
        if (tracks == null || tracks.size() == 0) {
            if (mUiLoader != null) {
                mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
            }
        }
            if (mUiLoader != null) {
                mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
            }


        //更新,设置UI
        mTrackListAdapter.setData(tracks);




    }

    @Override
    public void onNetworkError(int errorCode, String errorMsg) {
        //请求发生错误，显示网络异常状态
        mUiLoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
    }

    @Override
    public void onAlbumLoaded(Album album) {
        this.mCurrentAlbum = album;
        long id = album.getId();

//        LogUtil.d(TAG, "album -- > " + id);
        mCurrentId = id;

        //获取专辑的详情内容
        if (mAlbumDetailPresenter != null) {
            mAlbumDetailPresenter.getAlbumDetail((int) id,mCurrentPage);
        }
        //拿数据，显示Loading状态
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
        }
        if (mAlbumTitle!=null) {
            mAlbumTitle.setText(album.getAlbumTitle());
        }
        if (mAlbumAuthor!=null) {
            mAlbumAuthor.setText(album.getAnnouncer().getNickname());
        }

        //做毛玻璃效果
        if (mLargerCover != null && null != mLargerCover) {
            Picasso.with(this).load(album.getCoverUrlLarge()).into(mLargerCover, new Callback() {
                @Override
                public void onSuccess() {
                    Drawable drawable = mLargerCover.getDrawable();
                    if (drawable != null) {
                        //到这里才说明是有图片的
                        ImageBlur.makeBlur(mLargerCover, DetailActivity.this);
                    }
                }

                @Override
                public void onError() {
                    LogUtil.d(TAG, "onError");
                }
            });

        }

        if (mSmallCover!=null) {

            Picasso.with(this).load(album.getCoverUrlLarge()).into(mSmallCover);
        }
    }

    @Override
    public void onLoaderMoreFinished(int size) {
        if (size > 0) {
            Toast.makeText(this, "成功加载", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "没有更多节目", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRefreshFinished(int size) {

    }

    @Override
    public void onRetryClick() {
        //这里面表示用户网络不红重新加载

        //获取专辑的详情内容
        if (mAlbumDetailPresenter != null) {
            mAlbumDetailPresenter.getAlbumDetail((int) mCurrentId,mCurrentPage);
        }
    }

    @Override
    public void onItemClick(List<Track> detailData, int position) {
        /**
         * 这里设置播放器的数据
         */
        PlayerPresenter playerPresenter = PlayerPresenter.getPlayerPresenter();
        playerPresenter.setPlayList(detailData,position);
        //跳转到播放器见面
        Intent intent = new Intent(this, PlayerActivity.class);
        startActivity(intent);
    }


    /**
     * 根据播放状态修改图标和文字
     * @param playing
     */
    private void updatePlayState(boolean playing) {
        if (mPlayControlTips != null && mPlayControlBtn != null) {
            mPlayControlBtn.setImageResource(playing ? R.drawable.selector_play_control_pause :R.drawable.selector_play_control_play );
            if (!playing) {
            mPlayControlTips.setText(R.string.click_play_tips_text);
            } else {
                if (!TextUtils.isEmpty(mCurrentTrackTitle)) {
                    mPlayControlTips.setText(mCurrentTrackTitle);
                }
            }


        }
    }
    @Override
    public void onPlayStart() {
        //修改图标微暂停，文字微正在播放
        updatePlayState(true);

    }

    @Override
    public void onPlayPause() {
        //修改图标微播放，文字微已暂停
        updatePlayState(false);
    }

    @Override
    public void onPlayStop() {
        //修改图标微播放，文字微已暂停
        updatePlayState(false);
    }

    @Override
    public void onPlayError() {

    }

    @Override
    public void nextPlay(Track track) {

    }

    @Override
    public void onPrePlay(Track track) {

    }

    @Override
    public void onListLoaded(List<Track> list) {

    }

    @Override
    public void onPlayModeChange(XmPlayListControl.PlayMode mode) {

    }

    @Override
    public void onProgressChange(int currentProgress, int total) {

    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinished() {

    }

    @Override
    public void onTrackUpdate(Track track, int playIndex) {
        if (track != null) {
            mCurrentTrackTitle = track.getTrackTitle();
            if (!TextUtils.isEmpty(mCurrentTrackTitle) && mPlayControlTips !=null) {
                mPlayControlTips.setText(mCurrentTrackTitle);

            }
        }
    }

    @Override
    public void updateListOrder(boolean isReverse) {

    }

    @Override
    public void onAddResult(boolean isSuccess) {
        if (isSuccess) {
            //如果成功了，那就修改UI成取消订阅
            mSubBtn.setText(R.string.cancel_sub_tips_text);
        }
        //给个toast
        String tipsText = isSuccess ? "订阅成功" : "订阅失败";
        Toast.makeText(this, tipsText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteResult(boolean isSuccess) {
        if (isSuccess) {
            //如果成功了，那就修改UI成取消订阅
            mSubBtn.setText(R.string.sub_tips_text);
        }
        //给个toast
        String tipsText = isSuccess ? "删除成功" : "删除失败";
        Toast.makeText(this, tipsText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSubscriptionsLoaded(List<Album> albums) {
        //在这个界面 不需要处理
        for (Album album : albums) {
            LogUtil.d(TAG,"album-->"+album.getAlbumTitle());
        }
    }

    @Override
    public void onSubFull() {
        //处理一个即可，toast
        Toast.makeText(this, "订阅数量不得超过" + Constants.MAX_SUB_COUNT, Toast.LENGTH_SHORT).show();
    }
}
