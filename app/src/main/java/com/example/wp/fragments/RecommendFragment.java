package com.example.wp.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wp.R;
import com.example.wp.base.BaseFragment;

public class RecommendFragment extends BaseFragment {
    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        //view加载完成
        View rootView = layoutInflater.inflate(R.layout.fragment_recommend,container,false);



        return rootView;
    }
}
