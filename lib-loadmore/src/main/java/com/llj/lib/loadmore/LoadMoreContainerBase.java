package com.llj.lib.loadmore;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;

/**
 * @author
 */
public abstract class LoadMoreContainerBase extends LinearLayout implements LoadMoreContainer {
    public static final String TAG = LoadMoreContainerBase.class.getSimpleName();

    private AbsListView.OnScrollListener mOnScrollListener;
    private LoadMoreUIHandler            mLoadMoreUIHandler;// 设置加载的view(需要实现这个接口)，默认的加载view已经设置了
    private LoadMoreHandler              mLoadMoreHandler;// 外部需要实现，控制加载请求更多数据

    private boolean mIsLoading;// 是否正在加载
    private boolean mHasMore                 = true;// 是否还有更多数据
    private boolean mAutoLoadMore            = true;// 设置自动加载更多
    private boolean mShowLoadingForFirstPage = false;// 是否是加载第一页
    private boolean mListEmpty               = true;// 集合是否有数据
    private boolean mLoadError               = false;// 是否加载失误

    private View        mFooterView;// 加载的底部view
    private AbsListView mAbsListView;//

    public LoadMoreContainerBase(Context context) {
        super(context);
    }

    public LoadMoreContainerBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 获取listview
        mAbsListView = retrieveAbsListView();
        init();
    }

    /**
     * 使用默认的底部加载view
     */
//    public void useDefaultFooter() {
//        // 初始化FooterView
//        LoadMoreDefaultFooterView footerView = new LoadMoreDefaultFooterView(getContext());
//        footerView.setVisibility(VISIBLE);
//        // 设置加载更多view
//        setLoadMoreView(footerView);
//        //设置成员变量
//        setLoadMoreUIHandler(footerView);
//    }
    public View getFooterView() {
        return mFooterView;
    }

    private void init() {
        if (mFooterView != null) {
            addFooterView(mFooterView);
        }
        // 设置listview的滚动监听
        mAbsListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            private boolean mIsEnd = false;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (null != mOnScrollListener) {
                    mOnScrollListener.onScrollStateChanged(view, scrollState);
                }
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (mIsEnd) {
                        onReachBottom();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.i(TAG,"firstVisibleItem:" + firstVisibleItem + "visibleItemCount:" + visibleItemCount + "totalItemCount:" + totalItemCount);
                if (null != mOnScrollListener) {
                    mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                }
                //滑到最后一项才开始下载
                if (firstVisibleItem + visibleItemCount >= totalItemCount) {
                    mIsEnd = true;
                } else {
                    mIsEnd = false;
                }
            }
        });
    }

    private void tryToPerformLoadMore() {
        // 如果正在加载中或者没有更多了就返回
        if (!this.mIsLoading) {
            if (this.mHasMore || this.mListEmpty && this.mShowLoadingForFirstPage) {
                this.mIsLoading = true;
                //正在加载时候底部view的文字变化
                if (this.mLoadMoreUIHandler != null) {
                    this.mLoadMoreUIHandler.onLoading(this);
                }
                //然后取调用外部需要实现的接口(请求数据)
                if (null != this.mLoadMoreHandler) {
                    this.mLoadMoreHandler.onLoadMore(this);
                }
            }
        }
    }

    /**
     * 到达底部
     */
    private void onReachBottom() {
        if (!this.mLoadError) {
            if (this.mAutoLoadMore) {
                //试着去自动加载
                this.tryToPerformLoadMore();
            } else if (this.mHasMore) {
                // 非自动的情况下更新底部文字的变化
                this.mLoadMoreUIHandler.onWaitToLoadMore(this);
            }

        }
    }

    @Override
    public void setShowLoadingForFirstPage(boolean showLoading) {
        mShowLoadingForFirstPage = showLoading;
    }

    @Override
    public void setAutoLoadMore(boolean autoLoadMore) {
        mAutoLoadMore = autoLoadMore;
    }

    @Override
    public void setOnScrollListener(AbsListView.OnScrollListener l) {
        mOnScrollListener = l;
    }

    @Override
    public void setOnScrollListener(RecyclerView.OnScrollListener l) {

    }

    @Override
    public void setLoadMoreView(View view) {
        if (this.mAbsListView == null) {
            this.mFooterView = view;
        } else {
            if (this.mFooterView != null && this.mFooterView != view) {
                this.removeFooterView(mFooterView);
            }

            this.mFooterView = view;
            //设置底部可以点击效果
            this.mFooterView.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    tryToPerformLoadMore();
                }
            });
            this.addFooterView(view);
        }
    }

    /**
     * 设置底部view,需要实现LoadMoreUIHandler接口
     *
     * @param handler
     */
    @Override
    public void setLoadMoreUIHandler(LoadMoreUIHandler handler) {
        mLoadMoreUIHandler = handler;
    }

    /**
     * 外面需要实现的接口
     *
     * @param handler
     */
    @Override
    public void setLoadMoreHandler(LoadMoreHandler handler) {
        mLoadMoreHandler = handler;
    }

    @Override
    public boolean isReachBottom() {
        return false;
    }

    @Override
    public boolean isReachTop() {
        return false;
    }

    @Override
    public boolean isBottomHasMore() {
        return false;
    }

    @Override
    public boolean isTopHasMore() {
        return false;
    }

    @Override
    public void topLoadMoreFinish(boolean hasMore) {

    }

    @Override
    public void bottomLoadMoreFinish(boolean hasMore) {
        this.mLoadError = false;
        this.mIsLoading = false;
        this.mHasMore = hasMore;
        if (this.mLoadMoreUIHandler != null) {
            this.mLoadMoreUIHandler.onLoadFinish(this, hasMore);
        }
    }

    /**
     * 加载失败的时候，手动调用
     *
     * @param errorCode
     * @param errorMessage
     */
    @Override
    public void loadMoreError(int errorCode, String errorMessage) {
        this.mIsLoading = false;
        this.mLoadError = true;
        if (this.mLoadMoreUIHandler != null) {
            this.mLoadMoreUIHandler.onLoadError(this, errorCode, errorMessage);
        }

    }

    // 添加底部view
    protected abstract void addFooterView(View view);

    // 移除底部view
    protected abstract void removeFooterView(View view);

    // 获取ListView
    protected abstract AbsListView retrieveAbsListView();
}