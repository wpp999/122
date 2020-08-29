package com.example.wp.presenters;

import com.example.wp.data.XimalayApi;
import com.example.wp.interfaces.ISearchCallback;
import com.example.wp.interfaces.ISearchPresenter;
import com.example.wp.utils.Constants;
import com.example.wp.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.SearchAlbumList;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.HotWordList;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;
import com.ximalaya.ting.android.opensdk.model.word.SuggestWords;

import java.util.ArrayList;
import java.util.List;

public class SearchPresenter  implements ISearchPresenter {
    private List<Album> mSearchResult = new ArrayList<>();

    private static final String TAG = "SearchPresenter" ;
    private final XimalayApi mXimalayApi;
    private String mCurrentKeyword = null;
    private static int mCurrentPage = 1;


    private SearchPresenter(){
        mXimalayApi = XimalayApi.getXimalayApi();
    }
    private static SearchPresenter sSearchPresenter = null;

    public static SearchPresenter getSearchPresenter(){
        if (sSearchPresenter == null) {
            synchronized (SearchPresenter.class){
                if (sSearchPresenter == null) {
                    sSearchPresenter = new SearchPresenter();
                }
            }
        }



        return sSearchPresenter;
    }


   private List<ISearchCallback> mCallback = new ArrayList<>();

    @Override
    public void doSearch(String keyword) {
        mCurrentPage = 1;
        mSearchResult.clear();

        this.mCurrentKeyword = keyword;
        search(keyword);
    }

    private void search(String keyword) {
        mXimalayApi.searchByKeyword(keyword, mCurrentPage, new IDataCallBack<SearchAlbumList>() {
            @Override
            public void onSuccess(SearchAlbumList searchAlbumList) {
                List<Album> albums = searchAlbumList.getAlbums();
                mSearchResult.addAll(albums);
                if (albums != null) {
                    LogUtil.d(TAG,"albums size-->"+albums.size());
                    if (mIsLoadMore) {
                        for (ISearchCallback iSearchCallback : mCallback) {
                            iSearchCallback.onLoadMoreResult(mSearchResult,albums.size() != 0);
                            iSearchCallback.onLoadMoreResult(mSearchResult,true);
                            }

                        mIsLoadMore = false;
                    } else {
                        for (ISearchCallback iSearchCallback : mCallback) {
                            iSearchCallback.onSearchResultLoaded(mSearchResult);
                        }
                    }
                } else {
                    LogUtil.d(TAG,"albums is null-->");
                }

            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogUtil.d(TAG,"errorCode--->"+errorCode);
                LogUtil.d(TAG,"errorMsg--->"+errorMsg);

                    for (ISearchCallback iSearchCallback : mCallback) {
                        if (mIsLoadMore) {
                            iSearchCallback.onLoadMoreResult(mSearchResult,false);
                            mCurrentPage --;
                            mIsLoadMore = false;
                        } else {

                        iSearchCallback.onError(errorCode,errorMsg);
                    }
                }

            }
        });
    }

    @Override
    public void reSearch() {
        search(mCurrentKeyword);
    }

    private  boolean mIsLoadMore = false;
    @Override
    public void loadMore() {
        //判断有没有要加载更多
        if (mSearchResult.size()< Constants.COUNT_DEFAULT) {
            for (ISearchCallback iSearchCallback : mCallback) {
                iSearchCallback.onLoadMoreResult(mSearchResult,false);
            }
        } else {
            mIsLoadMore = true;
            mCurrentPage++;
            search(mCurrentKeyword);
        }

    }

    @Override
    public void getHotWord() {
        mXimalayApi.getHotWords(new IDataCallBack<HotWordList>() {
            @Override
            public void onSuccess(HotWordList hotWordList) {
                if (hotWordList != null) {
                    List<HotWord> hotWords = hotWordList.getHotWordList();
                    LogUtil.d(TAG,"hotWord size-->" + hotWords.size());
                    for (ISearchCallback iSearchCallback : mCallback) {
                        iSearchCallback.onHotWordLoaded(hotWords);
                    }
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogUtil.d(TAG,"errorCode--->"+errorCode);
                LogUtil.d(TAG,"errorMsg--->"+errorMsg);
                for (ISearchCallback iSearchCallback : mCallback) {
                    iSearchCallback.onError(errorCode,errorMsg);
                }
            }
        });
    }

    @Override
    public void getRecommendWord(final String keyword) {
        mXimalayApi.getSuggestWords(keyword, new IDataCallBack<SuggestWords>() {
            @Override
            public void onSuccess(SuggestWords suggestWords) {
                if (suggestWords != null) {
                    List<QueryResult> keyWordList = suggestWords.getKeyWordList();
                    LogUtil.d(TAG, "keywordsList size-->" + keyWordList.size());
                    for (ISearchCallback iSearchCallback : mCallback) {
                        iSearchCallback.onRecommendWordLoaded(keyWordList);
                    }
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogUtil.d(TAG,"errorCode--->"+errorCode);
                LogUtil.d(TAG,"errorMsg--->"+errorMsg);

            }
        });

    }


    @Override
    public void registerViewCallback(ISearchCallback iSearchCallback) {
        if (!mCallback.contains(iSearchCallback)) {
            mCallback.add(iSearchCallback);
        }
    }

    @Override
    public void unRegisterViewCallback(ISearchCallback iSearchCallback) {
        mCallback.remove(iSearchCallback);
    }
}
