package com.example.wp.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wp.R;
import com.example.wp.base.BaseFragment;

public class MusicFragment extends BaseFragment {
    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        View rootView = layoutInflater.inflate(R.layout.fragment_music,container,false);
        return rootView;
    }
}
