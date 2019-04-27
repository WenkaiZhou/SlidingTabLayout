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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.kevin.delegationadapter.DelegationAdapter;
import com.kevin.slidingtablayout.sample.imooc.ImoocActivity;
import com.kevin.slidingtablayout.sample.missfresh.MissFreshActivity;

import java.util.Arrays;
import java.util.List;

/**
 * MainActivity
 *
 * @author zwenkai@foxmail.com, Created on 2018-10-19 16:13:02
 * Major Function：<b>MainActivity</b>
 * <p/>
 * Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */

public class MainActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    DelegationAdapter mDelegationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRecyclerView();
        initData();
    }

    private void initRecyclerView() {
        mRecyclerView = this.findViewById(R.id.recycler_view);
        // 设置LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        // 添加分割线
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        // 设置Adapter
        mDelegationAdapter = new DelegationAdapter();
        // 向Adapter中注册委托Adapter
        mDelegationAdapter.addDelegate(new MainAdapterDelegate(this));
        mRecyclerView.setAdapter(mDelegationAdapter);
    }


    private void initData() {
        List<String> titleList = Arrays.asList(
                "慕课网首页",
                "每日优鲜首页",
                "更多使用场景等你来解锁");
        mDelegationAdapter.setDataItems(titleList);
    }

    public void onItemClick(View v, int position, String item) {
        switch (position) {
            case 0:
                startActivity(new Intent(MainActivity.this, ImoocActivity.class));
                break;
            case 1:
                startActivity(new Intent(MainActivity.this, MissFreshActivity.class));
                break;
            default:
                // Can't reach;
                break;
        }
    }

}
