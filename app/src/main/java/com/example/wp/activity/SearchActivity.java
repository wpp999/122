package com.example.wp.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wp.R;
import com.example.wp.adapters.AlbumListAdapter;
import com.example.wp.adapters.SearchRecommendAdapter;
import com.example.wp.interfaces.ISearchCallback;
import com.example.wp.presenters.AlbumDetailPresenter;
import com.example.wp.presenters.SearchPresenter;
import com.example.wp.utils.LogUtil;
import com.example.wp.views.FlowTextLayout;
import com.example.wp.views.UILoader;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchActivity extends BaseActivity implements ISearchCallback, AlbumListAdapter.OnAlbumItemClickListener {

    private static final String TAG = "SearchActivity";
    private ImageView mBackBtn;
    private EditText mInputBox;
    private TextView mSearchBtn;
    private FrameLayout mResultContainer;
    private SearchPresenter mSearchPresenter;
    private UILoader mUILoader;
    private RecyclerView mResultListView;
    private AlbumListAdapter mAlbumListAdapter;
    private FlowTextLayout mFlowTextLayout;
    private InputMethodManager mImm;
    private View mDelBtn;
    private RecyclerView mSearchRecommendList;
    private SearchRecommendAdapter mRecommendAdapter;
    private TwinklingRefreshLayout mRefreshLayout;
    private boolean mNeedSuggestWords =  true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initView();
        initEvent();
        initPresenter();

    }

    private void initPresenter() {
        mImm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        mSearchPresenter = SearchPresenter.getSearchPresenter();
        //注册Ui更新的接口
        mSearchPresenter.registerViewCallback(this);
        //去拿热词
        mSearchPresenter.getHotWord();

    }

    private void initEvent() {
        mAlbumListAdapter.setAlbumItemClickListener(this);
        mRefreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                //加载更多内容


                    if (mSearchPresenter != null) {
                        mSearchPresenter.loadMore();
                    }
                }


        });
        if (mRecommendAdapter != null) {
            mRecommendAdapter.setItemClickListener(new SearchRecommendAdapter.ItemClickListener() {
                @Override
                public void onItemClick(String keyword) {
                    LogUtil.d(TAG,"mRecommendAdapter keyword-->"+keyword);
                    //不需要相关的联想词
                    mNeedSuggestWords = false;
                    //推荐热词的点击
                    switch2Search(keyword);


                }
            });
        }
        mDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputBox.setText("");
            }
        });
        //点击重试
        mUILoader.setOnRetryClickListener(new UILoader.OnRetryClickListener() {
            @Override
            public void onRetryClick() {
                if (mSearchPresenter != null) {
                    mSearchPresenter.reSearch();
                    mUILoader.updateStatus(UILoader.UIStatus.LOADING);
                }
            }
        });
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    finish();
            }
        });
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //去调用搜索逻辑
                String keyWord = mInputBox.getText().toString().trim();
                if (TextUtils.isEmpty(keyWord)) {
                    Toast.makeText(SearchActivity.this, "搜索不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mSearchPresenter != null) {
                    mSearchPresenter.doSearch(keyWord);
                    mUILoader.updateStatus(UILoader.UIStatus.LOADING);
                }
            }
        });
        mInputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    mSearchPresenter.getHotWord();
                    mDelBtn.setVisibility(View.GONE);

                    } else {
                    mDelBtn.setVisibility(View.VISIBLE);
                    if (mNeedSuggestWords) {
                    //触发联想查询
                    getSuggestWord(s.toString());

                    } else {
                        mNeedSuggestWords= true;
                    }

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mFlowTextLayout.setClickListener(new FlowTextLayout.ItemClickListener() {
            @Override
            public void onItemClick(String text) {
                //不需要相关的联想词
                mNeedSuggestWords = false;
                switch2Search(text);
            }
        });
    }

    private void switch2Search(String text) {
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "搜索不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        //把热词放在搜索里
        mInputBox.setText(text);
        mInputBox.setSelection(text.length());
        //发起搜索
        if (mSearchPresenter != null) {
            mSearchPresenter.doSearch(text);
        }
        //改变UI状态
        if (mUILoader != null) {
            mUILoader.updateStatus(UILoader.UIStatus.LOADING);
        }
    }

    /**
     * 获取联想关键词
     * @param keyword
     */
    private void getSuggestWord(String keyword) {
        if (mSearchPresenter != null) {
            mSearchPresenter.getRecommendWord(keyword);
        }
    }

    private void initView() {
        mBackBtn = this.findViewById(R.id.search_back);
        mInputBox = this.findViewById(R.id.search_input);
        mDelBtn = this.findViewById(R.id.search_input_delete);
        mDelBtn.setVisibility(View.GONE);
        mInputBox.postDelayed(new Runnable() {
            @Override
            public void run() {
                mInputBox.requestFocus();
                mImm.showSoftInput(mInputBox, InputMethodManager.SHOW_IMPLICIT);
            }
        },300);
        mSearchBtn = this.findViewById(R.id.search_btn);
        mResultContainer = this.findViewById(R.id.search_container);

        if (mUILoader == null) {
            mUILoader = new UILoader(this) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView();
                }
                @Override
                protected View getEmptyView() {

                    View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_empty_view, this, false);
                    TextView tipsview = emptyView.findViewById(R.id.empty_view_tips_tv);
                    tipsview.setText(R.string.search_no_content_tips_text);
                    return emptyView;
                }
            };
            if (mUILoader.getParent() instanceof ViewGroup) {
                ((ViewGroup) mUILoader.getParent()).removeView(mUILoader);
            }
            mResultContainer.addView(mUILoader);
        }
    }

    /**
     * 创建数据请求成功d的View
     * @return
     */
    private View createSuccessView() {
        View resultView = LayoutInflater.from(this).inflate(R.layout.search_result_layout, null);
        //刷新控件
        mRefreshLayout = resultView.findViewById(R.id.search_result_refresh_layout);
        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadmore(false);
        //显示热词
        mFlowTextLayout = resultView.findViewById(R.id.recommend_hot_word_view);
        mResultListView = resultView.findViewById(R.id.result_list_view);
        //设置布局管理器
        mResultListView.setLayoutManager(new LinearLayoutManager(this));
        //设置适配器
        mAlbumListAdapter = new AlbumListAdapter();
        mResultListView.setAdapter(mAlbumListAdapter);
        mResultListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top= UIUtil.dip2px(view.getContext(),3);
                outRect.left=UIUtil.dip2px(view.getContext(),5);
                outRect.bottom=UIUtil.dip2px(view.getContext(),3);
                outRect.right=UIUtil.dip2px(view.getContext(),5);

            }
        });
        //搜索推荐
        mSearchRecommendList = resultView.findViewById(R.id.search_recommend_list);
        //设置布局管理器
        mSearchRecommendList.setLayoutManager(new LinearLayoutManager(this));
        mSearchRecommendList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top= UIUtil.dip2px(view.getContext(),3);
                outRect.left=UIUtil.dip2px(view.getContext(),5);
                outRect.bottom=UIUtil.dip2px(view.getContext(),3);
                outRect.right=UIUtil.dip2px(view.getContext(),5);

            }
        });
        //设置适配器
        mRecommendAdapter = new SearchRecommendAdapter();
        mSearchRecommendList.setAdapter(mRecommendAdapter);

        return resultView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSearchPresenter != null) {
        mSearchPresenter.unRegisterViewCallback(this);
        }
        mSearchPresenter = null;

    }

    @Override
    public void onSearchResultLoaded(List<Album> result) {
        handleSearchResult(result);
        //隐藏键盘
        mImm.hideSoftInputFromWindow(mInputBox.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void handleSearchResult(List<Album> result) {
        hideSuccessView();
        mResultListView.setVisibility(View.VISIBLE);
        if (result != null ) {
            if (result.size() == 0) {
                if (mUILoader != null) {
                    mUILoader.updateStatus(UILoader.UIStatus.EMPTY);

                }
            } else {
                mAlbumListAdapter.setData(result);
                mUILoader.updateStatus(UILoader.UIStatus.SUCCESS);
            }

        }
    }

    @Override
    public void onHotWordLoaded(List<HotWord> hotWordList) {
        //显示热词
        hideSuccessView();
        mFlowTextLayout.setVisibility(View.VISIBLE);

        if (mUILoader != null) {
            mUILoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }
        LogUtil.d(TAG,"hotWordList-->" + hotWordList.size());
        List<String> hotWords = new ArrayList<>();
        hotWords.clear();
        for (HotWord hotWord : hotWordList) {
            String searchword = hotWord.getSearchword();
            hotWords.add(searchword);
        }
        Collections.sort(hotWords);
        //更新UI
        mFlowTextLayout.setTextContents(hotWords);
    }

    @Override
    public void onLoadMoreResult(List<Album> result, boolean isOkay) {
        //处理加载更多的结果
        if (mRefreshLayout != null) {
            mRefreshLayout.finishLoadmore();
        }
        if (isOkay){
          handleSearchResult(result);
        } else {
            Toast.makeText(this, "没有更多内容", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onRecommendWordLoaded(List<QueryResult> keyWordList) {
        //联想的关键子
        LogUtil.d(TAG,"keyWordList-->"+ keyWordList.size());
        if (mRecommendAdapter != null) {
            mRecommendAdapter.setData(keyWordList);
        }
        //控制UI状态和隐藏显示
        if (mUILoader != null) {
            mUILoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }
        //控制显示和隐藏
        hideSuccessView();
        mSearchRecommendList.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        if (mUILoader != null) {
        mUILoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
        }
    }

    private void hideSuccessView(){
        mSearchRecommendList.setVisibility(View.GONE);
        mFlowTextLayout.setVisibility(View.GONE);
        mResultListView.setVisibility(View.GONE);
    }

    @Override
    public void OnItemClick(int position, Album album) {
        //根据位置获取数据
        AlbumDetailPresenter.getInstance().setTargetAlbum(album);
        //item被点击了，跳转页面
        Intent intent=new Intent(this, DetailActivity.class);
        startActivity(intent);
    }
}
