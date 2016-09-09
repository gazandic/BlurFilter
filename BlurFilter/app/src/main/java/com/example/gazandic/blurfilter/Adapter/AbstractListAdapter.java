package com.example.gazandic.blurfilter.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;


import com.example.gazandic.blurfilter.R;

import java.util.List;

/**
 * Representing abstract class of RecyclerView Adapter.
 *
 * @author Raffi Ditya <raffi@urbanindo.com>
 * @since 2016.04.03
 * @param <T> the item type generic object
 */
public abstract class AbstractListAdapter<T> extends RecyclerView.Adapter {

    protected static final int VIEW_ITEM = 1;
    protected static final int VIEW_PROGRESS = 0;

    private ListClickListener listClickListener;
    private List<T> itemList;
    private Context mContext;

    /**
     * Constructing Base List Adapter.
     *
     * @param context   Android {@link Context}
     * @param itemList  list of item
     */
    public AbstractListAdapter(Context context, List<T> itemList) {
        this.itemList = itemList;
        this.mContext = context;
    }

    /**
     * Get current context.
     *
     * @return Android {@link Context}
     */
    protected Context getContext() {
        return mContext;
    }

    /**
     * Get current item list.
     *
     * @return current item list
     */
    public List<T> getItemList() {
        return itemList;
    }

    /**
     * Set the item list.
     *
     * @param itemList the new item list
     */
    public void setItemList(List<T> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    /**
     * Return item object at specified index location.
     *
     * @param index index location
     * @return item object at specified index location
     */
    protected T getItemAt(int index) {
        return itemList.get(index);
    }

    /**
     * Get current list click listener.
     *
     * @return current list click listener
     */
    public ListClickListener getListClickListener() {
        return listClickListener;
    }

    /**
     * Set item list click listener.
     *
     * @param listClickListener listener to handle click event
     */
    public void setOnItemClickListener(ListClickListener listClickListener) {
        this.listClickListener = listClickListener;
    }

    /**
     * Show progress bar.
     *
     * @param show showing status
     */
    public void showProgressBar(boolean show) {
        if (show) {
            addProgressBar();
        }
        else {
            removeProgressBar();
        }
    }

    /**
     * Add progress bar to the item list.
     */
    public void addProgressBar() {
        if (itemList.size() == 0 || null != itemList.get(itemList.size() - 1)) {
            this.itemList.add(null);
            notifyItemInserted(itemList.size() - 1);
        }
    }

    /**
     * Remove progress bar from item list.
     */
    public void removeProgressBar() {
        if (itemList.size() > 0 && null == itemList.get(itemList.size() - 1)) {
            itemList.remove(itemList.size() - 1);
            notifyItemRemoved(itemList.size());
        }
    }

    /**
     * Insert new item list to the current item list.
     *
     * @param itemList the new item list to be inserted
     */
    public void addList(List<T> itemList) {
        for (T t : itemList) {
            this.itemList.add(t);
            notifyItemInserted(this.itemList.size() - 1);
        }
    }

    /**
     * Clear current item list.
     */
    public void clearList() {
        itemList.clear();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View progressView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.item_progressbar, viewGroup, false);

        return new ProgressViewHolder(progressView);
    }

    @Override
    public abstract void onBindViewHolder(RecyclerView.ViewHolder holder, int position);

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.size() > 0 && itemList.get(position) != null ? VIEW_ITEM : VIEW_PROGRESS;
    }

    /**
     * Interface to be used on interaction with item click event.
     */
    public interface ListClickListener {
    }

    /**
     * View Holder that show the progress bar.
     */
    protected static class ProgressViewHolder extends RecyclerView.ViewHolder {

        /**
         * Progress Bar View.
         */
        public ProgressBar progressBar;

        /**
         * Progress view holder constructor.
         *
         * @param v the View layout
         */
        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        }
    }
}
