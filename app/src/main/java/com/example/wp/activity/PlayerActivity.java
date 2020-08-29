package com.example.wp.activity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.example.wp.R;
import com.example.wp.adapters.PlayTrackPagerAdapter;
import com.example.wp.interfaces.IPlayerCallback;
import com.example.wp.presenters.PlayerPresenter;
import com.example.wp.utils.LogUtil;
import com.example.wp.views.SobPopWindow;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE_LOOP;

public class PlayerActivity extends BaseActivity implements IPlayerCallback, ViewPager.OnPageChangeListener {

    private static final int BG_ANIMATOR_DURATION = 500 ;
    private static final float BG_ALPHA_HEIGHT = 1.0f ;
    private static final float BG_ALPHA_LOW = 0.7f ;
    private static final String TAG = "PlayerActivity";

    private ImageView mControlBtn;
    private PlayerPresenter mPlayerPresenter;
    private SimpleDateFormat mMinFormat = new SimpleDateFormat("mm:ss");
    private SimpleDateFormat mHourFormat = new SimpleDateFormat("hh:mm:ss");
    private TextView mTotalDuration;
    private TextView mCurrentPosition;
    private SeekBar mDurationBar;
    private int mCurrentProgress = 0;
    private  boolean mIsUserTouchProgress = false;
    private ImageView mPlayNextBtn;
    private ImageView mPlayPreBtn;
    private TextView mTrackTitleTv;
    private String mTrackTitleText;
    private ViewPager mTrackPagerView;
    private PlayTrackPagerAdapter mTrackPagerAdapter;
    private boolean mIsUserSlidePager =false;
    private ImageView mPlayModeSwitchBtn;
    private XmPlayListControl.PlayMode mCurrentMode = PLAY_MODEL_LIST;

    //
    private  static Map<XmPlayListControl.PlayMode,XmPlayListControl.PlayMode> sPlayModeRule = new HashMap<>();
    //处理播放模式切换
    //1.默认是：  PLAY_MODEL_LIST
    //2.列表循环：PLAY_MODEL_LIST_LOOP
    //3.随机播放：PLAY_MODEL_RANDOM
    //4.单曲循环：PLAY_MODEL_SINGLE_LOOP

    static {
        sPlayModeRule.put(PLAY_MODEL_LIST,PLAY_MODEL_LIST_LOOP);
        sPlayModeRule.put(PLAY_MODEL_LIST_LOOP,PLAY_MODEL_RANDOM);
        sPlayModeRule.put(PLAY_MODEL_RANDOM,PLAY_MODEL_SINGLE_LOOP);
        sPlayModeRule.put(PLAY_MODEL_SINGLE_LOOP,PLAY_MODEL_LIST);


    }

    private ImageView mPlayerListBtn;
    private SobPopWindow mSobPopWindow;
    private ValueAnimator mEnterBgAnimator;
    private ValueAnimator mOutBgAnimator;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initView();
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();
        mPlayerPresenter.registerViewCallback(this);


        initEvent();
        initBgAnimation();



    }

    private void initBgAnimation() {
        mEnterBgAnimator = ValueAnimator.ofFloat(BG_ALPHA_HEIGHT,BG_ALPHA_LOW);
        mEnterBgAnimator.setDuration(BG_ANIMATOR_DURATION);
        mEnterBgAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                //处理下背景，一点点透明
                updateBgAlpha(value);
            }
        });
        //退出动画
        mOutBgAnimator = ValueAnimator.ofFloat(BG_ALPHA_LOW, BG_ALPHA_HEIGHT);
        mOutBgAnimator.setDuration(BG_ANIMATOR_DURATION);
        mOutBgAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                updateBgAlpha(value);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放资源
        if (mPlayerPresenter != null) {
        mPlayerPresenter.unRegisterViewCallback(this);
        mPlayerPresenter = null;


        //


        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initEvent() {
        mControlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果现在状态四播放，则暂停
                if (mPlayerPresenter.isPlaying()) {
                    mPlayerPresenter.pause();
                }else {
                //如果现在状态是暂停，则播放
                    mPlayerPresenter.play();
                }
            }
        });

        mDurationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean isFromUser) {
                if (isFromUser) {
                    mCurrentProgress = progress;

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsUserTouchProgress = true;

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mIsUserTouchProgress = false;
                //手离开拖动进度条时候更新进度
                mPlayerPresenter.seekTo(mCurrentProgress);
            }
        });

        mPlayPreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //播放上一个
                mPlayerPresenter.playPre();
            }
        });


        mPlayNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //播放下一个
                mPlayerPresenter.playNext();
            }
        });


        mTrackPagerView.addOnPageChangeListener(this);

        mTrackPagerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action){
                   case  MotionEvent.ACTION_DOWN:
                        mIsUserSlidePager =true;
                        break;

                }
                return false;
            }
        });


        mPlayModeSwitchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchPlayMode();

            }
        });

        mPlayerListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSobPopWindow.showAtLocation(v, Gravity.BOTTOM,0,0);


                mEnterBgAnimator.start();
            }
        });

        mSobPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //pop窗体消失后，恢复透明度
               mOutBgAnimator.start();
            }
        });

        mSobPopWindow.setPlayListItemListener(new SobPopWindow.PlayListItemListener() {
            @Override
            public void onItemClick(int position) {
                //播放列表的item被单机了
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.playByIndex(position);
                }
            }
        });

        mSobPopWindow.setPlayListActionListener(new SobPopWindow.PlayListActionListener() {
            @Override
            public void onPlayModeClick() {
                //切换播放模式
                switchPlayMode();
            }

            @Override
            public void onOrderClick() {
                //点击切换正倒序
                if (mPlayerPresenter != null) {
                    mPlayerPresenter.reversePlayList();
                }
            }
        });

    }

    private boolean testOrder = false;

    private void switchPlayMode() {
        //根据当前的Mode获取下一个的mode
        XmPlayListControl.PlayMode playMode = sPlayModeRule.get(mCurrentMode);
        //修改播放模式
        if (mPlayerPresenter != null) {
            mPlayerPresenter.switchPlayMode(playMode);
        }
    }

    public  void updateBgAlpha(float alpha){
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.alpha =alpha;
        window.setAttributes(attributes);

    }
    /**
     *根据当前的状态，更新播放模式图表
     *  PLAY_MODEL_LIST
     * PLAY_MODEL_LIST_LOOP
     * PLAY_MODEL_RANDOM
     * PLAY_MODEL_SINGLE_LOOP
     */
    private void updatePlayModeBtnImg() {
        int resId = R.drawable.selector_play_mode_list_order;
        switch (mCurrentMode){
            case PLAY_MODEL_LIST:
                resId = R.drawable.selector_play_mode_list_order;
                break;
            case PLAY_MODEL_RANDOM:
                resId =R.drawable.selector_play_mode_random;
                break;
            case PLAY_MODEL_LIST_LOOP:
                resId =R.drawable.selector_play_mode_list_order_looper;
                break;
            case PLAY_MODEL_SINGLE_LOOP:
                resId =R.drawable.selector_play_mode_single_loop;
                break;
        }
                mPlayModeSwitchBtn.setImageResource(resId);
    }

    private void initView() {
        mControlBtn = this.findViewById(R.id.play_or_pause_btn);
        mTotalDuration = this.findViewById(R.id.track_duration);
        mCurrentPosition = this.findViewById(R.id.current_position);
        mDurationBar = this.findViewById(R.id.track_seek_bar);
        mPlayNextBtn = this.findViewById(R.id.play_next);
        mPlayPreBtn = this.findViewById(R.id.play_pre);
        mTrackTitleTv = this.findViewById(R.id.track_title);
        //播放列表
        mPlayerListBtn = this.findViewById(R.id.player_list);
        mSobPopWindow = new SobPopWindow();
        if (!TextUtils.isEmpty(mTrackTitleText)) {
        mTrackTitleTv.setText(mTrackTitleText);
        }
        mTrackPagerView = this.findViewById(R.id.track_pager_view);
        //创建适配器
        mTrackPagerAdapter = new PlayTrackPagerAdapter();
        //设置适配器
        mTrackPagerView.setAdapter(mTrackPagerAdapter);
        //切换播放模式的按钮
        mPlayModeSwitchBtn = this.findViewById(R.id.player_mode_switch_btn);
    }

    @Override
    public void onPlayStart() {
        //开始播放,修改UI成暂停
        if (mControlBtn != null) {
        mControlBtn.setImageResource(R.drawable.selector_player_pause);

        }


    }

    @Override
    public void onPlayPause() {
        if (mControlBtn != null) {
        mControlBtn.setImageResource(R.drawable.selector_player_play);

        }

    }

    @Override
    public void onPlayStop() {
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.drawable.selector_player_play);

        }
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
        //把数据设置到适配器
        if (mTrackPagerAdapter != null) {
        mTrackPagerAdapter.setData(list);
        //数据回来后，也给节目列表一份
            if (mSobPopWindow != null) {
            mSobPopWindow.setListData(list);

            }


        }

    }

    @Override
    public void onPlayModeChange(XmPlayListControl.PlayMode mode) {
        //更新播放模式更新UI
        mCurrentMode = mode;
        //更新pop里的播放模式
        mSobPopWindow.updatePlayMode(mCurrentMode);
        updatePlayModeBtnImg();

    }

    @Override
    public void onProgressChange(int currentDuration, int total) {
        //更新进度条
        mDurationBar.setMax(total);
        String totalDuration;
        String currentPosition;
        if (total>1000*60*60) {
             totalDuration = mHourFormat.format(total);
             currentPosition = mHourFormat.format(currentDuration);
        }else {
            totalDuration = mMinFormat.format(total);
            currentPosition = mMinFormat.format(currentDuration);
        }
        if (mTotalDuration != null) {
        mTotalDuration.setText(totalDuration);

        }
        //更新当前时间
        if (mCurrentPosition != null) {
            mCurrentPosition.setText(currentPosition);
        }
        if (!mIsUserTouchProgress) {
            //更新进度
            //计算当前时间
            mDurationBar.setProgress(currentDuration);
        }



    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinished() {

    }

    @Override
    public void onTrackUpdate(Track track, int playIndex) {
        if (track == null) {
            LogUtil.d(TAG, "onTrackUpdate -- > track null.");
            return;
        }
        this.mTrackTitleText = track.getTrackTitle();
        if (mTrackTitleTv != null) {
            //设置当前节目的标题
            mTrackTitleTv.setText(mTrackTitleText);
        }
        //当节目改变的时候，我们就获取到当前播放中播放位置
        //当前的节目改变以后,要修改页面的图片

        if (mTrackPagerView != null) {
            mTrackPagerView.setCurrentItem(playIndex, true);
        }

        //修改播放里里的播放位置
        if (mSobPopWindow != null) {
            mSobPopWindow.setCurrentPlayPosition(playIndex);
        }

    }

    @Override
    public void updateListOrder(boolean isReverse) {
        mSobPopWindow.updateOrderIcon(isReverse);

    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


    }

    @Override
    public void onPageSelected(int position) {
        //当页面选中的时候，就去切换播放内容
        if (mPlayerPresenter != null && mIsUserSlidePager) {
            mPlayerPresenter.playByIndex(position);

        }
        mIsUserSlidePager = false;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
