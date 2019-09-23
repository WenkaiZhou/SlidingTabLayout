package com.kevin.slidingtablayout.sample.missfresh;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.kevin.slidingtab.SlidingTabLayout;
import com.kevin.slidingtablayout.sample.MainFragment;
import com.kevin.slidingtablayout.sample.R;

import java.util.ArrayList;

/**
 * MissFreshActivity
 *
 * @author zhouwenkai@baidu.com, Created on 2019-04-27 17:51:30
 * Major Function：<b></b>
 * <p/>
 * Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
public class MissFreshActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private SlidingTabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miss_fresh);

        mViewPager = findViewById(R.id.view_pager);
        mTabLayout = findViewById(R.id.tab_layout);

        initTabListener();

        Adapter adapter = new Adapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);

        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void initTabListener() {
        mTabLayout.setOnTabClickListener(new SlidingTabLayout.OnTabClickListener() {
            @Override
            public void onClick(int position) {
                Toast.makeText(MissFreshActivity.this, "点击了条目：" + position, Toast.LENGTH_SHORT).show();
            }
        });

        mTabLayout.setOnSelectedTabClickListener(new SlidingTabLayout.OnSelectedTabClickListener() {
            @Override
            public void onClick(int position) {
                Toast.makeText(MissFreshActivity.this, "点击了选择的条目：" + position, Toast.LENGTH_SHORT).show();
            }
        });

        mTabLayout.setOnTabSelectedListener(new SlidingTabLayout.OnTabSelectedListener() {
            @Override
            public void onSelected(int position) {
                Toast.makeText(MissFreshActivity.this, "选中了条目：" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class Adapter extends FragmentPagerAdapter {

        ArrayList<String> titles = new ArrayList<>();
        private ArrayList<Fragment> fragments = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);

            titles.add("热卖");
            titles.add("半价菜场");
            titles.add("下午茶");
            titles.add("日百满减");
            titles.add("会员特价");
            titles.add("优鲜冰铺");

            for (int i = 0; i < titles.size(); i++) {
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
            return titles.get(position);
        }
    }
}
