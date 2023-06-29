package com.kevin.slidingtablayout.sample.imooc;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.kevin.slidingtab.SlidingTabLayout;
import com.kevin.slidingtablayout.sample.MainFragment;
import com.kevin.slidingtablayout.sample.R;
import com.kevin.slidingtablayout.sample.util.StatusBarUtil;

import java.util.ArrayList;

/**
 * ImoocActivity
 *
 * @author zwenkai@foxmail.com, Created on 2019-04-27 17:07:25
 * Major Function：<b></b>
 * <p/>
 * Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
public class ImoocActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private SlidingTabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imooc);

        mViewPager = findViewById(R.id.view_pager);
        mTabLayout = findViewById(R.id.tab_layout);

        initTabListener();

        Adapter adapter = new Adapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(4);

        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.setOnColorChangedListener(new SlidingTabLayout.OnColorChangeListener() {
            @Override
            public void onColorChanged(int color) {
                findViewById(R.id.toolbar).setBackgroundColor(color);
                StatusBarUtil.setColor(ImoocActivity.this, color);
            }
        });

        mTabLayout.setSelectedTextColors(
                Color.parseColor("#EC0000"),
                Color.parseColor("#EC0000"),
                Color.parseColor("#8119EA"),
                Color.parseColor("#CA7D00")
        );
    }

    private void initTabListener() {
        mTabLayout.setOnTabClickListener(new SlidingTabLayout.OnTabClickListener() {
            @Override
            public void onClick(int position) {
                Toast.makeText(ImoocActivity.this, "点击了条目：" + position, Toast.LENGTH_SHORT).show();
            }
        });

        mTabLayout.setOnSelectedTabClickListener(new SlidingTabLayout.OnSelectedTabClickListener() {
            @Override
            public void onClick(int position) {
                Toast.makeText(ImoocActivity.this, "点击了选择的条目：" + position, Toast.LENGTH_SHORT).show();
            }
        });

        mTabLayout.setOnTabSelectedListener(new SlidingTabLayout.OnTabSelectedListener() {
            @Override
            public void onSelected(int position) {
                Toast.makeText(ImoocActivity.this, "选中了条目：" + position, Toast.LENGTH_SHORT).show();
            }
        });
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
            return titles.get(position);
        }

        @Override
        public Drawable getDrawable(int position) {
            return mContext.getResources().getDrawable(icons[position]);
        }
    }
}
