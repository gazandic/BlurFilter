package com.example.gazandic.blurfilter.Adapter;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gazandic.blurfilter.NewColor;
import com.example.gazandic.blurfilter.R;
import com.example.gazandic.blurfilter.databinding.ItemCardColorBinding;

import java.util.List;

/**
 * Representing Find Me List Adapter.
 *
 * @author Gazandi Cahyadarma <gazandi@urbanindo.com>
 * @since 2016.07.25
 */
public class ColorListAdapter extends AbstractListAdapter<NewColor> {

    public ColorListAdapter(Context context, List<NewColor> newColors) {
        super(context, newColors);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        if (viewType == VIEW_ITEM) {
            ItemCardColorBinding itemCardColorBinding  = DataBindingUtil.inflate(
                    layoutInflater,
                    R.layout.item_card_color,
                    viewGroup,
                    false
            );
            return new ColorViewHolder(itemCardColorBinding);
        }
        return super.onCreateViewHolder(viewGroup, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof ColorViewHolder)) {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
            return;
        }

        NewColor newColor = getItemAt(position);
        ColorViewHolder colorHolder = (ColorViewHolder) holder;
        colorHolder.newColor = newColor;
        colorHolder.clickListener = (ColorListClickListener) getListClickListener();
        colorHolder.listItemBinding.executePendingBindings();
        ((ItemCardColorBinding) colorHolder.listItemBinding).setNewColor(newColor);
        ((ItemCardColorBinding) colorHolder.listItemBinding).coloring.setBackgroundColor(newColor.getRGB());
    }

    /**
     * Used to communicating interaction between adapter and other fragment or activity.
     */
    public interface ColorListClickListener extends ListClickListener {

        /**
         * Called when agent item on list has been clicked.
         *
         * @param newColor selected findme object
         */
        void onItemClick(NewColor newColor);

    }

    public static class ColorViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private ViewDataBinding listItemBinding;
        private ColorListClickListener clickListener;
        private NewColor newColor;

        public ColorViewHolder(ViewDataBinding listItemBinding) {
            super(listItemBinding.getRoot());
            this.listItemBinding = listItemBinding;
            if (listItemBinding instanceof ItemCardColorBinding) {
                ((ItemCardColorBinding)listItemBinding).findmeCardAgent.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            if (null != clickListener) {
                clickListener.onItemClick(newColor);
            }
        }
    }

}
