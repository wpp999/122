package com.example.wp.utils;

import com.example.wp.base.BaseFragment;
import com.example.wp.fragments.AccountFragment;
import com.example.wp.fragments.HistoryFragment;
import com.example.wp.fragments.MusicFragment;
import com.example.wp.fragments.RecommendFragment;
import com.example.wp.fragments.SubscriptionFragment;
import com.example.wp.fragments.XiaoshuoFragment;

import java.util.HashMap;
import java.util.Map;

public class FragmentCreator {

    public final static int INDEX_RECOMMEND = 0;
    public final static int INDEX_SUBSCRIPTION = 1;
    public final static int INDEX_HISTORY = 2;
//    public final static int INDEX_MUSIC = 3;
//    public final static int INDEX_XIAOSHUO = 4;
//    public final static int INDEX_ACCOUNT= 5;


    public final static int PAGE_COUNT = 3;

    private static Map<Integer, BaseFragment> sCache = new HashMap<>();


    public static BaseFragment getFragment(int index) {
        BaseFragment baseFragment = sCache.get(index);
        if (baseFragment != null) {
            return baseFragment;
        }

        switch (index) {
            case INDEX_RECOMMEND:
                baseFragment = new RecommendFragment();
                break;
            case INDEX_SUBSCRIPTION:
                baseFragment = new SubscriptionFragment();
                break;
            case INDEX_HISTORY:
                baseFragment = new HistoryFragment();
                break;
//            case INDEX_MUSIC:
//                baseFragment = new MusicFragment();
//                break;
//            case INDEX_XIAOSHUO:
//                baseFragment = new XiaoshuoFragment();
//                break;
//            case INDEX_ACCOUNT:
//                baseFragment = new AccountFragment();
//                break;
        }

        sCache.put(index, baseFragment);
        return baseFragment;
    }

}