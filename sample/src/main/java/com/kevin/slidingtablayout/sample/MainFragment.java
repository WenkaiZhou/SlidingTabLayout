/*
 * Copyright (c) 2018 Kevin zhou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kevin.slidingtablayout.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kevin.delegationadapter.AdapterDelegate;
import com.kevin.delegationadapter.DelegationAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment
 *
 * @author zwenkai@foxmail.com, Created on 2018-09-27 14:53:37
 *          Major Function：<b></b>
 *          <p/>
 *          Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
public class MainFragment extends Fragment {

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view);

        // ① 设置 LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        // ② 创建 DelegationAdapter 对象
        DelegationAdapter delegationAdapter = new DelegationAdapter();
        // ③ 向Adapter中注册委托Adapter
        delegationAdapter.addDelegate(new CompanyAdapterDelegate());
        // ④ 设置Adapter
        recyclerView.setAdapter(delegationAdapter);

        List<String> companies = new ArrayList<>();
        companies.add("🇨🇳 Baidu");
        companies.add("🇨🇳 Alibaba");
        companies.add("🇨🇳 Tencent");
        companies.add("🇺🇸 Google");
        companies.add("🇺🇸 Facebook");
        companies.add("🇺🇸 Microsoft");
        companies.add("🇨🇳 Baidu");
        companies.add("🇨🇳 Alibaba");
        companies.add("🇨🇳 Tencent");
        companies.add("🇺🇸 Google");
        companies.add("🇺🇸 Facebook");
        companies.add("🇺🇸 Microsoft");
        companies.add("🇨🇳 Baidu");
        companies.add("🇨🇳 Alibaba");
        companies.add("🇨🇳 Tencent");
        companies.add("🇺🇸 Google");
        companies.add("🇺🇸 Facebook");
        companies.add("🇺🇸 Microsoft");
        companies.add("🇨🇳 Baidu");
        companies.add("🇨🇳 Alibaba");
        companies.add("🇨🇳 Tencent");
        companies.add("🇺🇸 Google");
        companies.add("🇺🇸 Facebook");
        companies.add("🇺🇸 Microsoft");
        companies.add("🇨🇳 Baidu");
        companies.add("🇨🇳 Alibaba");
        companies.add("🇨🇳 Tencent");
        companies.add("🇺🇸 Google");
        companies.add("🇺🇸 Facebook");
        companies.add("🇺🇸 Microsoft");
        // ⑤ 设置数据
        delegationAdapter.setDataItems(companies);
    }

    public static class CompanyAdapterDelegate extends AdapterDelegate<String, CompanyAdapterDelegate.ViewHolder> {

        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        public void onBindViewHolder(final ViewHolder holder, final int position, final String item) {
            holder.tvName.setText(item);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView tvName;

            public ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
