package com.llj.lib.loadmore;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;

/**
 * Created by liulj on 16/3/11.
 */
public abstract class LoadMoreContainerRecyclerViewBase extends LinearLayout implements LoadMoreContainer {

    private RecyclerView.OnScrollListener mOnScrollListener;
    private LoadMoreUIHandler             mLoadMoreUIHandler;// 设置加载的view(需要实现这个接口)，默认的加载view已经设置了
    private LoadMoreHandler               mLoadMoreHandler;// 外部需要实现，控制加载请求更多数据

    private boolean mIsLoading;// 是否正在加载
    private boolean mAutoLoadMore            = true;// 设置自动加载更多
    private boolean mShowLoadingForFirstPage = false;// 是否是加载第一页
    private boolean mListEmpty               = true;// 集合是否有数据
    private boolean mLoadError               = false;// 是否加载失误

    private boolean mOnReachBottomHasMore = true;// 上拉是否还有更多数据
    private boolean mOnReachTopHasMore    = false;// 下拉是否还有更多数据,一般情况下用不到,都是上拉加载更多

    private boolean mIsReachBottom = false;//是否到达底部
    private boolean mIsReachTop    = false;//是否到达顶部

    private View         mFooterView;// 加载的底部view
    private RecyclerView mRecyclerView;//

    public LoadMoreContainerRecyclerViewBase(Context context) {
        super(context);
    }

    public LoadMoreContainerRecyclerViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 获取listview
        mRecyclerView = retrieveRecyclerView();
        init();
    }

    /**
     * 使用默认的底部加载view
     */
//    public void useDefaultFooter() {
//        // 初始化FooterView
//        LoadMoreDefaultFooterView footerView = new LoadMoreDefaultFooterView(getContext());
//        footerView.setVisibility(GONE);
//        // 设置加载更多view
//        setLoadMoreView(footerView);
//        //设置成员变量
//        setLoadMoreUIHandler(footerView);
//    }

    private void init() {
        if (mFooterView != null) {
            addFooterView(mFooterView);
        }
        // 设置mRecyclerView的滚动监听
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (null != mOnScrollListener) {
                    mOnScrollListener.onScrollStateChanged(recyclerView, newState);
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //上拉加载更多,到达底部
                    if (mIsReachBottom) {
                        if (mSupportType == MULTIPLE_LOAD_MORE || mSupportType == REACH_BOTTOM_LOAD_MORE)
                            onReachBottom();
                    } else if (mIsReachTop) {
                        //下拉加载更多,到达顶部
                        if (mSupportType == MULTIPLE_LOAD_MORE || mSupportType == REACH_TOP_LOAD_MORE)
                            onReachBegin();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (null != mOnScrollListener)
                    mOnScrollListener.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    mIsReachTop = false;
                    //上拉
                    int lastVisiblePosition = getLastVisiblePosition();
                    //距离底部第五项的时候就开始加载
                    if (lastVisiblePosition + 1 == recyclerView.getAdapter().getItemCount()) {
                        mIsReachBottom = true;
                    } else {
                        mIsReachBottom = false;
                    }
                } else {
                    mIsReachBottom = false;
                    //下拉
                    int firstVisiblePosition = getFirstVisiblePosition();
                    //到达第一项的时候
//                    if (firstVisiblePosition <= 3) {
                    if (firstVisiblePosition <= 1) {
                        mIsReachTop = true;
                    } else {
                        mIsReachTop = false;
                    }
                }
            }
        });

    }

    /**
     * 获取最后一条展示的位置
     *
     * @return
     */
    public int getLastVisiblePosition() {
        int position;
        if (mRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            position = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
        } else if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
            position = ((GridLayoutManager) mRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
        } else if (mRecyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) mRecyclerView.getLayoutManager();
            int[] lastPositions = layoutManager.findLastVisibleItemPositions(new int[layoutManager.getSpanCount()]);
            position = getMaxPosition(lastPositions);
        } else {
            position = mRecyclerView.getLayoutManager().getItemCount() - 1;
        }
        return position;
    }

    /**
     * 获取最后一条展示的位置
     *
     * @return
     */
    public int getFirstVisiblePosition() {
        int position;
        if (mRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            position = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        } else if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
            position = ((GridLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        } else if (mRecyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) mRecyclerView.getLayoutManager();
            int[] lastPositions = layoutManager.findFirstVisibleItemPositions(new int[layoutManager.getSpanCount()]);
            position = lastPositions[0];
        } else {
            position = 0;
        }
        return position;
    }

    /**
     * 获得最大的位置
     *
     * @param positions
     * @return
     */
    private int getMaxPosition(int[] positions) {
        int size = positions.length;
        int maxPosition = Integer.MIN_VALUE;
        for (int i = 0; i < size; i++) {
            maxPosition = Math.max(maxPosition, positions[i]);
        }
        return maxPosition;
    }

    private void tryToPerformLoadMore() {
        // 如果正在加载中或者没有更多了就返回
        if (!this.mIsLoading) {
            if (mOnReachBottomHasMore || mOnReachTopHasMore || this.mListEmpty && this.mShowLoadingForFirstPage) {
                //表明正在加载,需要外面手动设置为false
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
            } else if (mOnReachBottomHasMore) {
                // 非自动的情况下更新底部文字的变化
                this.mLoadMoreUIHandler.onWaitToLoadMore(this);
            }

        }
    }

    /**
     * 到达顶部
     */
    private void onReachBegin() {
        if (!this.mLoadError) {
            if (this.mAutoLoadMore) {
                //试着去自动加载(默认为自动加载)
                this.tryToPerformLoadMore();
            } else if (mOnReachTopHasMore) {
                // 非自动的情况下更新底部文字的变化
                this.mLoadMoreUIHandler.onWaitToLoadMore(this);
            }

        }
    }

    @Override
    public boolean isReachBottom() {
        return mIsReachBottom;
    }

    @Override
    public boolean isReachTop() {
        return mIsReachTop;
    }

    @Override
    public boolean isBottomHasMore() {
        return mOnReachBottomHasMore;
    }

    @Override
    public boolean isTopHasMore() {
        return mOnReachTopHasMore;
    }

    @Override
    public void setShowLoadingForFirstPage(boolean showLoading) {
        mShowLoadingForFirstPage = showLoading;
    }

    /**
     * 设置是否自动加载,默认为true
     *
     * @param autoLoadMore
     */
    @Override
    public void setAutoLoadMore(boolean autoLoadMore) {
        mAutoLoadMore = autoLoadMore;
    }

    @Override
    public void setOnScrollListener(RecyclerView.OnScrollListener l) {
        mOnScrollListener = l;
    }

    /**
     * 无用
     *
     * @param l
     */
    @Override
    public void setOnScrollListener(AbsListView.OnScrollListener l) {

    }

    @Override
    public void setLoadMoreView(View view) {
        if (this.mRecyclerView == null) {
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

    public static int REACH_BOTTOM_LOAD_MORE = 0;
    public static int REACH_TOP_LOAD_MORE    = 1;
    public static int MULTIPLE_LOAD_MORE     = 2;
    private       int mSupportType           = REACH_BOTTOM_LOAD_MORE;

    /**
     * 外面需要实现的接口
     *
     * @param handler
     */
    @Override
    public void setLoadMoreHandler(LoadMoreHandler handler) {
        mLoadMoreHandler = handler;
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


    @Override
    public void bottomLoadMoreFinish(boolean hasMore) {
        if ((mSupportType == MULTIPLE_LOAD_MORE || mSupportType == REACH_BOTTOM_LOAD_MORE)) {
            reachBottomLoadMoreFinish(hasMore);
        }
    }

    @Override
    public void topLoadMoreFinish(boolean hasMore) {
        if ((mSupportType == MULTIPLE_LOAD_MORE || mSupportType == REACH_TOP_LOAD_MORE)) {
            reachTopLoadMoreFinish(hasMore);
        }
    }

    /**
     * @param supportType
     */
    public void setSupportType(int supportType) {
        mSupportType = supportType;
        if (mSupportType == REACH_BOTTOM_LOAD_MORE) {
            mOnReachBottomHasMore = true;
            mOnReachTopHasMore = false;
        } else if (mSupportType == REACH_TOP_LOAD_MORE) {
            mOnReachBottomHasMore = false;
            mOnReachTopHasMore = true;
        } else {
            mOnReachBottomHasMore = true;
            mOnReachTopHasMore = true;
        }
    }


    /**
     * 重置加载更多
     */
    public void resetLoadMore() {
        mOnReachBottomHasMore = true;
        mOnReachTopHasMore = true;
    }


    /**
     * 下拉加载更多到达顶部,加载完成
     *
     * @param hasMore
     */
    public void reachTopLoadMoreFinish(boolean hasMore) {
        this.mLoadError = false;
        this.mIsLoading = false;
        this.mOnReachTopHasMore = hasMore;
        if (this.mLoadMoreUIHandler != null) {
            this.mLoadMoreUIHandler.onLoadFinish(this, hasMore);
        }
    }


    /**
     * 上拉加载更多到达底部,加载完成
     *
     * @param hasMore
     */
    public void reachBottomLoadMoreFinish(boolean hasMore) {
        this.mLoadError = false;
        this.mIsLoading = false;
        this.mOnReachBottomHasMore = hasMore;
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
    public abstract RecyclerView retrieveRecyclerView();
}
