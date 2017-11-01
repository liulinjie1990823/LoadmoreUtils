package com.llj.lib.loadmore;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by liulj on 16/3/11.
 */
public class LoadMoreRecyclerContainer extends LoadMoreContainerRecyclerViewBase {

    private RecyclerView mRecyclerView;

    public LoadMoreRecyclerContainer(Context context) {
        super(context);
    }

    public LoadMoreRecyclerContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void addFooterView(View view) {

    }

    @Override
    protected void removeFooterView(View view) {

    }

    @Override
    public RecyclerView retrieveRecyclerView() {
        mRecyclerView = (RecyclerView) getChildAt(0);
        return mRecyclerView;
    }
}
