package com.example.wp.interfaces;

import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.util.List;

public interface IPlayerCallback {
    //播放开始
    void onPlayStart();
    //播放暂停
    void onPlayPause();
    //播放停止
    void onPlayStop();
    //b播放错误
    void onPlayError();
    //播放下一首
    void nextPlay(Track track);
    //播放上一首
    void onPrePlay(Track track);

    /**
     * 播放列表加载完成
     * @param list
     */
    void onListLoaded(List<Track> list);
    /**
     * 播放模式改变了
     */
    void onPlayModeChange(XmPlayListControl.PlayMode mode);
    /**
     * 进度条的改变
     */
    void onProgressChange(int currentProgress,int total);
    /**
     * 广告正在加载
     */
    void onAdLoading();
    /**
     * 广告结束
     */
    void onAdFinished();
    /**
     * 更新节目
     */
    void onTrackUpdate(Track track,int playIndex);
    /**
     * 通知Ui更新播放列表的顺序和文字
     *
     */
    void updateListOrder(boolean isReverse);
}
