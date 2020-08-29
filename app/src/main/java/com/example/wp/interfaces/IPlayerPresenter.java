package com.example.wp.interfaces;

import com.example.wp.base.IBasePresenter;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

public interface IPlayerPresenter extends IBasePresenter<IPlayerCallback> {
    /**
     * 开始播放
     */
    void play();
    /**
     * 停止播放
     */
    void stop();
    /**
     * 暂停播放
     */
    void pause();
    /**
     * 播放上一首
     */
    void playPre();
    /**
     * 播放下一首
     */
    void playNext();
    /**
     * 播放模式
     */
    void switchPlayMode(XmPlayListControl.PlayMode mode);
    /**
     * 获取播放列表
     */
    void getPlayList();
    /**
     * 根据节目的位置 进行播放
     */
    void playByIndex(int index);
    /**
     *切换播放进度
     */
    void seekTo(int progress);
    /**
     * 判断播放器是否正在播放
     */
    boolean isPlaying();

    /**
     * 把播放器列表反转
     */
    void reversePlayList();

    /**
     * 播放通过推荐里的第一首歌
     * @param id
     */
    void playByAlbumId(long id);
}
