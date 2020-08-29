package com.example.wp.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import com.example.wp.base.BaseApplication;
import com.example.wp.data.XimalayApi;
import com.example.wp.interfaces.IPlayerCallback;
import com.example.wp.interfaces.IPlayerPresenter;
import com.example.wp.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.advertis.Advertis;
import com.ximalaya.ting.android.opensdk.model.advertis.AdvertisList;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.advertis.IXmAdsStatusListener;
import com.ximalaya.ting.android.opensdk.player.constants.PlayerConstants;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE_LOOP;

public class PlayerPresenter implements IPlayerPresenter, IXmAdsStatusListener, IXmPlayerStatusListener {

    private static final String TAG = "PlayerManger";
    private static final int DEFAULT_PLAY_INDEX = 0 ;
    private final XmPlayerManager mPlayerManager;
    private List<IPlayerCallback> mIPlayerCallbacks = new ArrayList<>();
    private Track mCurrentTrack;
    private int mCurrentIndex = DEFAULT_PLAY_INDEX;
    private final SharedPreferences mPlayModeSp;
    private XmPlayListControl.PlayMode mCurrentMode = XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
    private boolean mIsReverse =false;


//    PLAY_MODEL_LIST
//    PLAY_MODEL_LIST_LOOP
//    PLAY_MODEL_RANDOM
//    PLAY_MODEL_SINGLE_LOOP
    private static final int PLAY_MODEL_LIST_INT = 0;
    private static final int PLAY_MODEL_LIST_LOOP_INT = 1;
    private static final int PLAY_MODEL_RANDOM_INT = 2;
    private static final int PLAY_MODEL_SINGLE_LOOP_INT = 3;

    /**
     * modeSp 的key and name
     */
    private  static  final String PLAY_MODE_SP_NAME = "PlayMod";
    private  static  final String PLAY_MODE_SP_KEY = "currentPlayMode";
    private int mCurrentProgressPosition = 0;
    private int mProgressDuration = 0;
    private String mCoverUrlMiddle;


    private PlayerPresenter(){
        mPlayerManager = XmPlayerManager.getInstance(BaseApplication.getAppContext());
        //广告相关的回调接口
        mPlayerManager.addAdsStatusListener(this);
        //注册播放器的回调接口
        mPlayerManager.addPlayerStatusListener(this);
        //需要记录当前的播放模式
        mPlayModeSp = BaseApplication.getAppContext().getSharedPreferences(PLAY_MODE_SP_NAME, Context.MODE_PRIVATE);


    }
    private  static PlayerPresenter sPlayerPresenter;
    public  static  PlayerPresenter getPlayerPresenter(){
        if (sPlayerPresenter==null) {
            synchronized (PlayerPresenter.class) {
                if (sPlayerPresenter == null) {
                    sPlayerPresenter = new PlayerPresenter();
                }
            }
        }
        return sPlayerPresenter;
    }
    private boolean isPlayListSet = false;
    public void setPlayList(List<Track> list,int playIndex){
        if (mPlayerManager != null) {
        mPlayerManager.setPlayList(list,playIndex);
            isPlayListSet = true;
            mCurrentTrack = list.get(playIndex);
            mCurrentIndex = playIndex;
        }else {
            LogUtil.d(TAG,"mPlayManger is null");
        }

    }

    @Override
    public void play() {
        if (isPlayListSet) {

            mPlayerManager.play();
        }

    }

    @Override
    public void stop() {

    }

    @Override
    public void pause() {
        if (mPlayerManager !=null) {
            mPlayerManager.pause();
        }

    }

    @Override
    public void playPre() {
        //播放上一个
        if (mPlayerManager != null) {
            mPlayerManager.playPre();
        }

    }

    @Override
    public void playNext() {
        //播放下一个
        if (mPlayerManager != null) {
            mPlayerManager.playNext();
        }
    }

    @Override
    public void switchPlayMode(XmPlayListControl.PlayMode mode) {
        if (mPlayerManager != null) {
            mCurrentMode = mode;
            mPlayerManager.setPlayMode(mode);
            //通知UI更新播放模式
            for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
                iPlayerCallback.onPlayModeChange(mode);
            }
            //保存到SP中
            SharedPreferences.Editor editor = mPlayModeSp.edit();
            editor.putInt(PLAY_MODE_SP_KEY,getIntByPlayMode(mode));
            editor.commit();

        }

    }
    private  int getIntByPlayMode(XmPlayListControl.PlayMode mode){
        switch (mode){
            case PLAY_MODEL_LIST:
                return PLAY_MODEL_LIST_INT;
            case PLAY_MODEL_SINGLE_LOOP:
                return PLAY_MODEL_SINGLE_LOOP_INT;
            case PLAY_MODEL_LIST_LOOP:
                return PLAY_MODEL_LIST_LOOP_INT;
            case PLAY_MODEL_RANDOM:
                return PLAY_MODEL_RANDOM_INT;
        }
        return PLAY_MODEL_LIST_INT;
    }


    private XmPlayListControl.PlayMode getModeByInt(int index){
        switch (index){
            case PLAY_MODEL_LIST_INT:
                return PLAY_MODEL_LIST;
            case PLAY_MODEL_SINGLE_LOOP_INT:
                return PLAY_MODEL_SINGLE_LOOP;
            case PLAY_MODEL_LIST_LOOP_INT:
                return PLAY_MODEL_LIST_LOOP;
            case PLAY_MODEL_RANDOM_INT:
                return PLAY_MODEL_RANDOM;
        }
        return PLAY_MODEL_LIST;
    }

    @Override
    public void getPlayList() {
        if (mPlayerManager != null) {
        List<Track> playList = mPlayerManager.getPlayList();
            for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
                iPlayerCallback.onListLoaded(playList);
            }

        }
    }

    @Override
    public void playByIndex(int index) {
        //切换播放第index的位置播放
        if (mPlayerManager != null) {
        mPlayerManager.play(index);
        }

    }

    @Override
    public void seekTo(int progress) {
        //更新播放器进度的
        mPlayerManager.seekTo(progress);

    }

    @Override
    public boolean isPlaying() {
        //返回是否正在播放
        return   mPlayerManager.isPlaying();

    }

    @Override
    public void reversePlayList() {
        //把播放列表反转
        List<Track> playList = mPlayerManager.getPlayList();
        Collections.reverse(playList);
        mIsReverse = !mIsReverse;
        //第一个参数是播放列表的，第二个参数是开始播放d的下标
        mCurrentIndex =playList.size() -1 - mCurrentIndex;
        mPlayerManager.setPlayList(playList,mCurrentIndex);
        //更新UI
        mCurrentTrack = (Track) mPlayerManager.getCurrSound();
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onListLoaded(playList);
            iPlayerCallback.onTrackUpdate(mCurrentTrack,mCurrentIndex);
            iPlayerCallback.updateListOrder(mIsReverse);
        }
    }

    @Override
    public void playByAlbumId(long id) {
        //要获取专辑的内容
        XimalayApi ximalayApi = XimalayApi.getXimalayApi();
        ximalayApi.getAlbumDetail(new IDataCallBack<TrackList>() {
            @Override
            public void onSuccess(TrackList trackList) {
                //吧专辑设置给播放器
                List<Track> tracks = trackList.getTracks();
                if (trackList != null && tracks.size()>0) {
                    mPlayerManager.setPlayList(tracks,DEFAULT_PLAY_INDEX);
                    isPlayListSet = true;

                    mCurrentTrack = tracks.get(DEFAULT_PLAY_INDEX);
                    mCurrentIndex = DEFAULT_PLAY_INDEX;

                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogUtil.d(TAG,"errorCode"+errorCode);
                LogUtil.d(TAG,"errorMsg"+errorMsg);
                Toast.makeText(BaseApplication.getAppContext(), "请求出错", Toast.LENGTH_SHORT).show();
            }
        },id,1);
        //播放。。。
    }

    @Override
    public void registerViewCallback(IPlayerCallback iPlayerCallback) {
        if (!mIPlayerCallbacks.contains(iPlayerCallback)) {
            mIPlayerCallbacks.add(iPlayerCallback);
        }
        //更新钱，让UI的pager有数据
        getPlayList();
        //通知当前的节目
        iPlayerCallback.onTrackUpdate(mCurrentTrack,mCurrentIndex);

        //更新状态
        handlePlayState(iPlayerCallback);
        //更新进度条
        iPlayerCallback.onProgressChange(mCurrentProgressPosition,mProgressDuration);

        //
        //从Sp里面拿mode
        int modeIndex = mPlayModeSp.getInt(PLAY_MODE_SP_KEY, PLAY_MODEL_LIST_INT);
        //
        mCurrentMode= getModeByInt(modeIndex);
        iPlayerCallback.onPlayModeChange(mCurrentMode);


    }




    private void handlePlayState(IPlayerCallback iPlayerCallback) {
        int playerStatus = mPlayerManager.getPlayerStatus();
        //根据状态调用接口方法
        if (PlayerConstants.STATE_STARTED == playerStatus) {
            iPlayerCallback.onPlayStart();
        } else {
            iPlayerCallback.onPlayPause();
        }
    }

    @Override
    public void unRegisterViewCallback(IPlayerCallback iPlayerCallback) {
        mIPlayerCallbacks.remove(iPlayerCallback);

    }

    //====================广告相关的回调方法start==================//
    @Override
    public void onStartGetAdsInfo() {
        LogUtil.d(TAG,"onStartGetAdsInfo-->");
    }

    @Override
    public void onGetAdsInfo(AdvertisList advertisList) {
        LogUtil.d(TAG,"onGetAdsInfo-->");

    }

    @Override
    public void onAdsStartBuffering() {
        LogUtil.d(TAG,"onAdsStartBuffering-->");

    }

    @Override
    public void onAdsStopBuffering() {
        LogUtil.d(TAG,"onAdsStopBuffering-->");

    }

    @Override
    public void onStartPlayAds(Advertis advertis, int i) {
        LogUtil.d(TAG,"onStartPlayAds-->");

    }

    @Override
    public void onCompletePlayAds() {
        LogUtil.d(TAG,"onCompletePlayAds-->");

    }

    @Override
    public void onError(int i, int i1) {
        LogUtil.d(TAG,"onError-->");

    }
    //====================广告相关的回调发放end==================//



    //==================== 播放器相关回调接口 Start==================//

    @Override
    public void onPlayStart() {
    LogUtil.d(TAG,"onPlayStart-->");
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onPlayStart();
        }

    }

    @Override
    public void onPlayPause() {
    LogUtil.d(TAG,"onPlayPause-->");
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onPlayPause();
        }

    }

    @Override
    public void onPlayStop() {
    LogUtil.d(TAG,"onPlayStop-->");
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onPlayStop();
        }

    }

    @Override
    public void onSoundPlayComplete() {
    LogUtil.d(TAG,"onSoundPlayComplete-->");


    }

    @Override
    public void onSoundPrepared() {
    LogUtil.d(TAG,"onSoundPrepared-->");
        mPlayerManager.setPlayMode(mCurrentMode);
        if (mPlayerManager.getPlayerStatus()== PlayerConstants.STATE_PREPARED) {
            //播放器准备好了，可以播放了
            mPlayerManager.play();
        }

    }

    @Override
    public void onSoundSwitch(PlayableModel lastModel, PlayableModel curModel) {
    LogUtil.d(TAG,"onSoundSwitch-->");
        /**
         * curMode代表的是当前播放的内容
         * 通过getKind（）方法取得它类型--->track类型
         */
        mCurrentIndex = mPlayerManager.getCurrentIndex();
        if (curModel instanceof Track) {
            Track currentTrack = (Track) curModel;
            mCurrentTrack = currentTrack;
            //保存播放记录
            HistoryPresenter historyPresenter = HistoryPresenter.getHistoryPresenter();
            historyPresenter.addHistory(currentTrack);
            //更新UI
            for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
                iPlayerCallback.onTrackUpdate(mCurrentTrack,mCurrentIndex);
            }
        }


    }

    @Override
    public void onBufferingStart() {
    LogUtil.d(TAG,"onBufferingStart-->");

    }

    @Override
    public void onBufferingStop() {
    LogUtil.d(TAG,"onBufferingStop-->");
    }

    @Override
    public void onBufferProgress(int progress) {
        LogUtil.d(TAG,"onBufferProgress-->" + progress);

    }

    @Override
    public void onPlayProgress(int currPos, int duration) {
        this.mCurrentProgressPosition = currPos;
        this.mProgressDuration = duration;
        //LogUtil.d(TAG,"onPlayProgress-->" );
        for (IPlayerCallback iPlayerCallback : mIPlayerCallbacks) {
            iPlayerCallback.onProgressChange(currPos,duration);
        }

    }

    @Override
    public boolean onError(XmPlayerException e) {
        LogUtil.d(TAG,"onError-->" + e);
        return false;
    }

    //判断是否有播放列表
    public boolean hasPlayList() {
        List<Track> playList = mPlayerManager.getPlayList();
        return isPlayListSet;
    }
    //====================播放器相关回调接口end==================//
}
