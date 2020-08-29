package com.example.wp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.example.wp.activity.PlayerActivity;
import com.example.wp.activity.SearchActivity;
import com.example.wp.adapters.IndicatorAdapter;
import com.example.wp.adapters.MainContentAdapter;
import com.example.wp.data.XimalayaDBHelper;
import com.example.wp.interfaces.IPlayerCallback;
import com.example.wp.presenters.HistoryPresenter;
import com.example.wp.presenters.PlayerPresenter;
import com.example.wp.presenters.RecommendPresenter;
import com.example.wp.utils.LogUtil;
import com.squareup.picasso.Picasso;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements IPlayerCallback {

    private static final String TAG="MainActivity";
    private MagicIndicator mMagicIndicator;
    private ViewPager mContentPager;
    private IndicatorAdapter mIndicatorAdapter;
    private TextView mHeaderTitle;
    private ImageView mRoundRectImageView;
    private TextView mSubTitle;
    private PlayerPresenter mPlayerPresenter;
    private ImageView mPlayControl;
    private View mPlayControlItem;
    private View mSearchBtn;
    private String mTrackTitle;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        initPresenter();
//        XimalayaDBHelper ximalayaDBHelper =new XimalayaDBHelper(this);
//        ximalayaDBHelper.getWritableDatabase();

    }

    private void initPresenter() {
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();

        mPlayerPresenter.registerViewCallback(this);


    }

    private void initEvent() {
        mIndicatorAdapter.setOnIndicatorTapListener(new IndicatorAdapter.OnIndicatorTapListener() {
            @Override
            public void OnTabClick(int index) {
                LogUtil.d(TAG,"click index is-->"+index);
                if(mContentPager!=null){
                    mContentPager.setCurrentItem(index,false);
                }
            }
        });

        mPlayControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerPresenter != null) {
                    boolean hasPlayList = mPlayerPresenter.hasPlayList();
                    if (!hasPlayList) {
                        //没有设置过播放列表，播放推荐中的第一个
                        playFirstRecommend();

                    } else {
                        if (mPlayerPresenter.isPlaying()) {
                            mPlayerPresenter.pause();
                        } else {
                            mPlayerPresenter.play();
                        }
                    }

                }

            }
        });

        mPlayControlItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerPresenter != null) {
                boolean hasPlayList = mPlayerPresenter.hasPlayList();
                    if(!hasPlayList) {
                        playFirstRecommend();
                }
                //跳转到播放器界面
                Intent intent = new Intent(MainActivity.this,PlayerActivity.class);
                startActivity(intent);

            }







            }

        });


        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

    }

    /**
     * 播放第一个推荐内容
     */
    private void playFirstRecommend() {
        List<Album> currentRecommend = RecommendPresenter.getInstance().getCurrentRecommend();
        if (currentRecommend != null && currentRecommend.size() > 0  ) {
            Album album = currentRecommend.get(0);
            long albumId = album.getId();
            mPlayerPresenter.playByAlbumId(albumId);









        }
    }

    private  void initView(){
        mMagicIndicator=this.findViewById(R.id.main_indicator);
        mMagicIndicator.setBackgroundColor(this.getResources().getColor(R.color.main_color));
        // 创建indicator适配器
        mIndicatorAdapter=new IndicatorAdapter(this);
        CommonNavigator commonNavigator=new CommonNavigator(this);
        //自动调节，平分
        commonNavigator.setAdjustMode(true);
        commonNavigator.setAdapter(mIndicatorAdapter);

        //ViewPager
       mContentPager = this.findViewById(R.id.content_pager);

        //创建内容适配器
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        MainContentAdapter mainContentAdapter=new MainContentAdapter(supportFragmentManager);
        mContentPager.setAdapter(mainContentAdapter);

        //把Viewpager和indicator绑定到一起
        mMagicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(mMagicIndicator, mContentPager);

        //播放器控制相关的
        mRoundRectImageView = this.findViewById(R.id.main_track_cover);
        mHeaderTitle = this.findViewById(R.id.main_head_title);
        mHeaderTitle.setSelected(true);
        mSubTitle = this.findViewById(R.id.main_sub_title);
        mPlayControl = this.findViewById(R.id.main_play_control);
        mPlayControlItem = this.findViewById(R.id.main_play_control_item);
        //搜索按钮
        mSearchBtn = this.findViewById(R.id.search_btn);



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayerPresenter != null) {
        mPlayerPresenter.unRegisterViewCallback(this);
        }
    }
//=======================================回调更新UI的方法======================//
    @Override
    public void onPlayStart() {
    updatePlayControl(true);
    }
    private void updatePlayControl(boolean isPlaying){
        if (mPlayControl != null) {
            mPlayControl.setImageResource(isPlaying ? R.drawable.selector_player_pause : R.drawable.selector_player_play);
        }
    }

    @Override
    public void onPlayPause() {
        updatePlayControl(false);
    }

    @Override
    public void onPlayStop() {
        updatePlayControl(false);
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
        //

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
        if (track != null ) {
            mTrackTitle = track.getTrackTitle();
            String nickname = track.getAnnouncer().getNickname();
            String coverUrlMiddle = track.getCoverUrlMiddle();
            LogUtil.d(TAG,"标题"+ mTrackTitle);
            if (mHeaderTitle!= null){
                mHeaderTitle.setText(mTrackTitle);
            }
            LogUtil.d(TAG,"作者"+nickname);
            if (mSubTitle != null) {
                mSubTitle.setText(nickname);
            }
            LogUtil.d(TAG,"图片"+coverUrlMiddle);
            Picasso.with(this).load(coverUrlMiddle).into(mRoundRectImageView);
        }

    }

    @Override
    public void updateListOrder(boolean isReverse) {

    }
}

