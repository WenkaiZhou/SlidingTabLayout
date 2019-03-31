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

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jaeger.library.StatusBarUtil;
import com.kevin.slidingtab.SlidingTabLayout;

import java.util.ArrayList;

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

    private ViewPager mViewPager;
    private SlidingTabLayout mTabLayout0;
    private SlidingTabLayout mTabLayout1;
    private SlidingTabLayout mTabLayout2;
    private SlidingTabLayout mTabLayout3;
    private SlidingTabLayout mTabLayout4;
    private SlidingTabLayout mTabLayout5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.view_pager);
        mTabLayout0 = findViewById(R.id.tab_layout0);
        mTabLayout1 = findViewById(R.id.tab_layout1);
        mTabLayout2 = findViewById(R.id.tab_layout2);
        mTabLayout3 = findViewById(R.id.tab_layout3);
        mTabLayout4 = findViewById(R.id.tab_layout4);
        mTabLayout5 = findViewById(R.id.tab_layout5);

        Adapter adapter = new Adapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(4);

        mTabLayout0.setViewPager(mViewPager);
        mTabLayout1.setViewPager(mViewPager);
        mTabLayout2.setViewPager(mViewPager);
        mTabLayout3.setViewPager(mViewPager);
        mTabLayout4.setViewPager(mViewPager);
        mTabLayout5.setViewPager(mViewPager);

        mTabLayout0.setDividerColors(Color.BLUE, Color.RED, Color.GREEN);
        mTabLayout0.setIndicatorColors(Color.BLUE, Color.RED, Color.GREEN);
        mTabLayout4.setOnColorChangedListener(new SlidingTabLayout.OnColorChangeListener() {
            @Override
            public void onColorChanged(int color) {
                findViewById(R.id.toolbar).setBackgroundColor(color);
                StatusBarUtil.setColor(MainActivity.this, color);
            }
        });

        mTabLayout4.setIndicatorColors(
                Color.parseColor("#EC0000"),
                Color.parseColor("#EC0000"),
                Color.parseColor("#8119EA"),
                Color.parseColor("#CA7D00")
        );
    }

    static class Adapter extends SlidingTabLayout.SlidingTabPageAdapter {

        private final Context mContext;
        ArrayList<String> titles = new ArrayList<>();
        private ArrayList<Fragment> fragments = new ArrayList<>();

        private int[] icons = {R.mipmap.ic_recommend, R.mipmap.ic_free, R.mipmap.ic_path, R.mipmap.ic_actual};

        public Adapter(FragmentManager fm, Context context) {
            super(fm);
            this.mContext = context;

            titles.add("推荐");
            titles.add("课程");
            titles.add("路径");
            titles.add("实战");
            titles.add("推荐");
            titles.add("关注");
            titles.add("新时代");
            titles.add("呼号和特");
            titles.add("科技");
            titles.add("健康");
            titles.add("关注");
            titles.add("新时代");
            titles.add("呼号和特");

            for (int i = 0; i < 4; i++) {
                MainFragment fragment = new MainFragment();
                fragment.setTitle(titles.get(i));
                fragments.add(fragment);
            }

        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
//            return "标题" + position;
            return titles.get(position);
        }

        @Override
        public Drawable getDrawable(int position) {
            return mContext.getResources().getDrawable(icons[position]);
        }
    }
}
