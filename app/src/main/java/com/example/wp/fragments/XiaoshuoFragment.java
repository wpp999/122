package com.example.wp.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.wp.R;
import com.example.wp.base.BaseFragment;

public class XiaoshuoFragment extends BaseFragment {
    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        View rootView = layoutInflater.inflate(R.layout.fragment_xiaoshuo,container,false);
        return rootView;
    }
}
