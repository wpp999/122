package com.example.wp.views;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wp.R;
import com.example.wp.adapters.PlayListAdapter;
import com.example.wp.base.BaseApplication;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.util.List;

public class SobPopWindow extends PopupWindow {

    private final View mPopView;
    private View mCloseBtn;
    private RecyclerView mTracksList;
    private PlayListAdapter mPlayListAdapter;
    private TextView mPlayModeTv;
    private ImageView mPlayModeIv;
    private View mPlayModeContainer;
    private PlayListActionListener mPlayModeClickListener =null;
    private View mOrderBtnContainer;
    private TextView mOrderText;
    private ImageView mOrderIcon;

    public  SobPopWindow(){
        //设置宽高
        super (ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        //先设置setBackgroundDrawable，再设置setOutsideTouchable ,否则无法点击外部关闭pop
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setOutsideTouchable(true);
        //载入View
        mPopView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.pop_play_list, null);
        //设置内容
        setContentView(mPopView);
        //设置进入和退出动画
        setAnimationStyle(R.style.pop_animation);
        initView();
        initEvent();

    }

    private void initEvent() {
        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SobPopWindow.this.dismiss();
            }
        });
        mPlayModeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换播放模式
                if (mPlayModeClickListener != null) {
                    mPlayModeClickListener.onPlayModeClick();
                }
            }
        });
        mOrderBtnContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换正倒序
                if (mPlayModeClickListener != null) {
                    mPlayModeClickListener.onOrderClick();
                }



            }
        });
    }

    private void initView() {
        mCloseBtn = mPopView.findViewById(R.id.play_list_close_btn);
        //RecycleView,先找到控件
        mTracksList = mPopView.findViewById(R.id.play_list_rv);
        //1.设置布局管理器
        mTracksList.setLayoutManager(new LinearLayoutManager(BaseApplication.getAppContext()));
        //2.设置适配器
        mPlayListAdapter = new PlayListAdapter();
        mTracksList.setAdapter(mPlayListAdapter);
        //播放相关模式
        mPlayModeTv = mPopView.findViewById(R.id.play_list_play_mode_tv);
        mPlayModeIv = mPopView.findViewById(R.id.play_list_play_mode_iv);
        mPlayModeContainer = mPopView.findViewById(R.id.play_list_play_mode_container);
        //正倒序变化
        mOrderIcon = mPopView.findViewById(R.id.play_list_order_iv);
        mOrderText = mPopView.findViewById(R.id.play_list_order_tv);
        mOrderBtnContainer = mPopView.findViewById(R.id.play_list_order_container);



    }

    /**
     * 给适配器设置数据
     * @param data
     */
    public void setListData(List<Track> data){
        if (mPlayListAdapter != null) {
            mPlayListAdapter.setData(data);
        }
    }
    //设置播放列表里当前播放的位置
    public void setCurrentPlayPosition(int position){
        if (mPlayListAdapter != null) {
            mPlayListAdapter.setCurrentPlayPosition(position);
            mTracksList.scrollToPosition(position);
        }

    }
    public void setPlayListItemListener(PlayListItemListener listener){
        mPlayListAdapter.setOnItemClickListener(listener);
    }

    /**
     * 更新播放列表的图表和文字
     * @param currentMode
     */
    public void updatePlayMode(XmPlayListControl.PlayMode currentMode) {
    updatePlayModeBtnImg(currentMode);
    }


    /**
     * 切换正倒序图表和文字
     * @param isReverse
     */
    public void updateOrderIcon(boolean isReverse){
        mOrderIcon.setImageResource(isReverse ?   R.drawable.selector_play_mode_list_revers :R.drawable.selector_play_mode_list_order);
        mOrderText.setText(BaseApplication.getAppContext().getResources().getString (isReverse ?  R.string.revers_text :R.string.order_text));
    }

    /**
     *根据当前的状态，更新播放模式图表
     *  PLAY_MODEL_LIST
     * PLAY_MODEL_LIST_LOOP
     * PLAY_MODEL_RANDOM
     * PLAY_MODEL_SINGLE_LOOP
     */
    private void updatePlayModeBtnImg(XmPlayListControl.PlayMode playMode) {
        int resId = R.drawable.selector_play_mode_list_order;
        int textId =R.string.play_mode_order_text;
        switch (playMode){
            case PLAY_MODEL_LIST:
                resId = R.drawable.selector_play_mode_list_order;
                textId =R.string.play_mode_order_text;
                break;
            case PLAY_MODEL_RANDOM:
                resId =R.drawable.selector_play_mode_random;
                textId  = R.string.play_mode_random_text;
                break;
            case PLAY_MODEL_LIST_LOOP:
                resId =R.drawable.selector_play_mode_list_order_looper;
                textId  = R.string.play_mode_list_play_text;
                break;
            case PLAY_MODEL_SINGLE_LOOP:
                resId =R.drawable.selector_play_mode_single_loop;
                textId  = R.string.play_mode_single_play_text;
                break;
        }
        mPlayModeIv.setImageResource(resId);
        mPlayModeTv.setText(textId);
    }

    public interface PlayListItemListener{
        void onItemClick(int position);
    }

    public void setPlayListActionListener(PlayListActionListener playModeListener){
        mPlayModeClickListener = playModeListener;
    }


    public interface PlayListActionListener {
        //播放模式被点击了
        void onPlayModeClick();
//        播放正倒序被点击了
        void onOrderClick();
    }
}
