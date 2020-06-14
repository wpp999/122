package com.example.wp.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wp.R;
import com.example.wp.base.BaseFragment;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;

import java.util.HashMap;
import java.util.Map;

public class HistoryFragment extends BaseFragment {
    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {

       View rootView = layoutInflater.inflate(R.layout.fragment_history,container,false);



       return rootView;
    }



}
