package com.llj.lib.loadmore;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;

public interface LoadMoreContainer {
    /**
     * @param showLoading
     */
    void setShowLoadingForFirstPage(boolean showLoading);

    /**
     * 设置自动加载更多
     *
     * @param autoLoadMore
     */
    void setAutoLoadMore(boolean autoLoadMore);

    /**
     * 设置滚动监听
     *
     * @param l
     */
    void setOnScrollListener(AbsListView.OnScrollListener l);

    /**
     * 设置滚动监听
     *
     * @param l
     */
    void setOnScrollListener(RecyclerView.OnScrollListener l);

    /**
     * 设置加载更多 view
     *
     * @param view
     */
    void setLoadMoreView(View view);

    /**
     * 加载完成后控制ui
     *
     * @param handler
     */
    void setLoadMoreUIHandler(LoadMoreUIHandler handler);

    /**
     * 加载更多回调
     *
     * @param handler
     */
    void setLoadMoreHandler(LoadMoreHandler handler);


    /**
     * 一页加载完成后自己手动调用
     *
     * @param hasMore
     */
    void bottomLoadMoreFinish(boolean hasMore);

    void topLoadMoreFinish(boolean hasMore);

    boolean isReachBottom();

    boolean isReachTop();

    boolean isBottomHasMore();

    boolean isTopHasMore();

    void loadMoreError(int var1, String var2);

}
