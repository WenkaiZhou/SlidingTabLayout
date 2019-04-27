package com.kevin.slidingtablayout.sample;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kevin.delegationadapter.extras.ClickableAdapterDelegate;

/**
 * HomeAdapterDelegate
 *
 * @author zwenkai@foxmail.com, Created on 2019-04-27 17:02:36
 *         Major Function：<b></b>
 *         <p/>
 *         注:如果您修改了本类请填写以下内容作为记录，如非本人操作劳烦通知，谢谢！！！
 * @author mender，Modified Date Modify Content:
 */

public class MainAdapterDelegate extends ClickableAdapterDelegate<String, MainAdapterDelegate.ViewHolder> {

    private MainActivity mActivity;

    public MainAdapterDelegate(MainActivity activity) {
        this.mActivity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position, final String item) {
        super.onBindViewHolder(holder, position, item);
        holder.tvContent.setText(item);
    }

    @Override
    public void onItemClick(@NonNull View view, String item, int position) {
        mActivity.onItemClick(view, position, item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvContent;

        public ViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);
        }
    }
}
