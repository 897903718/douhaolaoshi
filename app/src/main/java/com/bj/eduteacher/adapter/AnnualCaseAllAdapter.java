package com.bj.eduteacher.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andview.refreshview.recyclerview.BaseRecyclerAdapter;
import com.bj.eduteacher.R;
import com.bj.eduteacher.entity.ArticleInfo;
import com.bj.eduteacher.zzautolayout.utils.AutoUtils;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

/**
 * Created by zz379 on 2017/1/6.
 * 年会案例Adapter
 */

public class AnnualCaseAllAdapter extends BaseRecyclerAdapter<RecyclerView.ViewHolder> {

    private List<ArticleInfo> mDataList;

    public AnnualCaseAllAdapter(List<ArticleInfo> mDataList) {
        this.mDataList = mDataList;
    }

    public enum ShowType {ITEM_TYPE_DECORATION, ITEM_TYPE_DOUKE}

    @Override
    public int getAdapterItemViewType(int position) {
        if (mDataList.get(position).getShowType() == ArticleInfo.SHOW_TYPE_DECORATION) {
            return ShowType.ITEM_TYPE_DECORATION.ordinal();
        } else {
            return ShowType.ITEM_TYPE_DOUKE.ordinal();
        }
    }

    @Override
    public int getAdapterItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType, boolean isItem) {
        RecyclerView.ViewHolder vh;
        if (viewType == ShowType.ITEM_TYPE_DECORATION.ordinal()) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_line_textview_line_douke, parent, false);
            vh = new ViewHolderDecoration(v, true);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_annual_case, parent, false);
            vh = new ViewHolderDouke(v, true);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder fholder, int position, boolean isItem) {
        ArticleInfo itemInfo = mDataList.get(position);
        if (fholder instanceof ViewHolderDecoration) {
            ViewHolderDecoration holder = (ViewHolderDecoration) fholder;
            holder.tvTitle.setText(itemInfo.getTitle());
        } else {
            ViewHolderDouke holder = (ViewHolderDouke) fholder;
            holder.ivPicture.setImageURI(itemInfo.getAuthImg());
            holder.tvName.setText(itemInfo.getTitle());
            holder.tvDesc.setText(itemInfo.getAuthor() + "，" + itemInfo.getContent());
        }
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(View view) {
        return new ViewHolderDouke(view, false);
    }

    public class ViewHolderDecoration extends RecyclerView.ViewHolder {

        TextView tvTitle;

        public ViewHolderDecoration(View itemView, boolean isItem) {
            super(itemView);
            if (isItem) {
                AutoUtils.auto(itemView);
                tvTitle = (TextView) itemView.findViewById(R.id.tv_latest_news);
            }
        }
    }

    public class ViewHolderDouke extends RecyclerView.ViewHolder {
        private SimpleDraweeView ivPicture;
        private TextView tvName;
        private TextView tvDesc;

        public ViewHolderDouke(View itemView, boolean isItem) {
            super(itemView);
            if (isItem) {
                AutoUtils.auto(itemView);
                ivPicture = (SimpleDraweeView) itemView.findViewById(R.id.iv_authorPhoto);
                tvName = (TextView) itemView.findViewById(R.id.tv_authorName);
                tvDesc = (TextView) itemView.findViewById(R.id.tv_authorDesc);
            }
        }
    }

    public void setData(List<ArticleInfo> list) {
        this.mDataList = list;
        notifyDataSetChanged();
    }

    public void insert(ArticleInfo person, int position) {
        insert(mDataList, person, position);
    }

    public void remove(int position) {
        remove(mDataList, position);
    }

    public void clear() {
        clear(mDataList);
    }

    public ArticleInfo getItem(int position) {
        if (position < mDataList.size())
            return mDataList.get(position);
        else
            return null;
    }
}
