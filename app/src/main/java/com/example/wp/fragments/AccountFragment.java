package com.example.wp.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wp.R;
import com.example.wp.adapters.AlbumListAdapter;
import com.example.wp.base.BaseFragment;
import com.example.wp.interfaces.ISubscriptionCallback;
import com.example.wp.interfaces.ISubscriptionPresenter;
import com.example.wp.presenters.SubscriptionPresenter;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.List;

public class AccountFragment extends BaseFragment {

    private ISubscriptionPresenter mISubscriptionPresenter;
    private RecyclerView mSubListView;
    private AlbumListAdapter mAlbumListAdapter;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        View rootView = layoutInflater.inflate(R.layout.fragment_account,container,false);

        return rootView;
    }


}
