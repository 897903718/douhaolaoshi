package com.bj.eduteacher.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andview.refreshview.recyclerview.BaseRecyclerAdapter;
import com.bj.eduteacher.R;
import com.bj.eduteacher.entity.ArticleInfo;
import com.bj.eduteacher.utils.StringUtils;
import com.bj.eduteacher.zzautolayout.utils.AutoUtils;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

/**
 * Created by zz379 on 2017/1/6.
 * 全部学生页面adapter
 */

public class ZhuanjiaAllAdapter extends BaseRecyclerAdapter<RecyclerView.ViewHolder> {

    private List<ArticleInfo> mDataList;

    public ZhuanjiaAllAdapter(List<ArticleInfo> mDataList) {
        this.mDataList = mDataList;
    }

    @Override
    public int getAdapterItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType, boolean isItem) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_zhuanjia, parent, false);
        RecyclerView.ViewHolder vh = new ViewHolderZhuanjia(v, true);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder fholder, int position, boolean isItem) {
        ArticleInfo itemInfo = mDataList.get(position);
        ViewHolderZhuanjia holder = (ViewHolderZhuanjia) fholder;
        holder.tvAuthorName.setText(itemInfo.getAuthor());
        holder.tvAuthorDesc.setText(itemInfo.getTitle());
        holder.tvAuthorTitle.setText(itemInfo.getAuthDesc());
        holder.tvAuthorNews.setText(itemInfo.getContent());
        if (!StringUtils.isEmpty(itemInfo.getAuthImg())) {
            holder.ivAuthorPhoto.setImageURI(itemInfo.getAuthImg());
        }
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(View view) {
        return new ViewHolderZhuanjia(view, false);
    }

    public class ViewHolderZhuanjia extends RecyclerView.ViewHolder {
        private TextView tvAuthorName, tvAuthorDesc, tvAuthorTitle, tvAuthorNews;
        private SimpleDraweeView ivAuthorPhoto;

        public ViewHolderZhuanjia(View itemView, boolean isItem) {
            super(itemView);
            if (isItem) {
                AutoUtils.auto(itemView);
                ivAuthorPhoto = (SimpleDraweeView) itemView.findViewById(R.id.iv_authorPhoto);
                tvAuthorName = (TextView) itemView.findViewById(R.id.tv_authorName);
                tvAuthorDesc = (TextView) itemView.findViewById(R.id.tv_authorDesc);
                tvAuthorTitle = (TextView) itemView.findViewById(R.id.tv_title);
                tvAuthorNews = (TextView) itemView.findViewById(R.id.tv_news_title);
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

    private OnMyItemClickListener myItemClickListener;

    public void setOnMyItemClickListener(OnMyItemClickListener listener) {
        this.myItemClickListener = listener;
    }

    public interface OnMyItemClickListener {
        void onZhuanjiaClick(View view, int position);
    }
}
