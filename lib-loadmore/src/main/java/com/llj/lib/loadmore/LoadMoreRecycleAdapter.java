package com.llj.lib.loadmore;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by liulj on 16/3/18.
 */
public class LoadMoreRecycleAdapter<Holder extends RecyclerView.ViewHolder> extends RecyclerView.Adapter {

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }
}
