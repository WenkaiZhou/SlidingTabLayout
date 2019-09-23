# SlidingTabLayout

该项目是博客[《ViewPager导航的一些想法》](https://blog.csdn.net/xuehuayous/article/details/83178601)的示例。

## 示例截图

<div>
  <table>
    <tr>
      <td>
        <img src="https://raw.githubusercontent.com/xuehuayous/SlidingTabLayout/master/sample/art/sample1.gif" />
      </td>
      <td>
        <img src="https://raw.githubusercontent.com/xuehuayous/SlidingTabLayout/master/sample/art/sample2.gif" />
      </td>
    </tr>
  </table>
</div>

## 在项目中使用

如果您的项目使用 Gradle 构建, 只需要在您的`build.gradle`文件添加下面一行到 `dependencies` :

**AndroidX**

```
compile 'com.kevin:slidingtablayout:2.0.0'
```

**Support**

```
compile 'com.kevin:slidingtablayout:1.1.10'
```

## 简单使用

### 在layout.xml 中配置

在Layout文件添加`<com.kevin.slidingtab.SlidingTabLayout`

```XML
<com.kevin.slidingtab.SlidingTabLayout
    android:id="@+id/sliding_tab"
    android:layout_width="match_parent"
    android:layout_height="44dp">
</com.kevin.slidingtab.SlidingTabLayout>
```
### 在代码中配置

```
SlidingTabLayout tabLayout = findViewById(R.id.sliding_tab);
ViewPager viewPager = findViewById(R.id.view_pager);
tabLayout.setViewPager(viewPager);
```

## 更多配置

### Attributes

在XML中使用SlidingTabLayout，可以有如下配置：

名称 | 格式 |  说明
-|-|-
stl_tabMode | enum | fixed:水平平分整体宽度, scrollable:可滚动 |
stl_leftPadding | dimension | 第一个Tab距离左边的距离 |
stl_rightPadding | dimension | 最后一个Tab距离右边的距离 |
stl_smoothScroll | boolean | Tab在点击时ViewPager是否平滑切换 |
stl_tabLayout | reference | 自定义Tab布局 |
stl_tabPadding | dimension | Tab边距 |
stl_tabPaddingStart | dimension | Tab左边距 |
stl_tabPaddingTop | dimension | Tab上边距 |
stl_tabPaddingRight | dimension | Tab右边距 |
stl_tabPaddingBottom | dimension | Tab下边距 |
stl_tabGravity | enum | Tab垂直方向位置，为上、中、下 |
stl_tabTextSize | dimension | Tab未选中文本大小 |
stl_tabSelectedTextSize | dimension | Tab选中文本大小 |
stl_tabTextColor | color | Tab未选中文本颜色 |
stl_tabSelectedTextColor | color | Tab选中文本颜色 |
stl_tabTextBold | boolean | Tab是否粗体 |
stl_tabTextSelectedBold | boolean | Tab是否选中粗体 |
stl_tabTextShowScaleAnim | boolean | Tab选中是否字体大小动画渐变 |
stl_tabIndicatorCreep | boolean | Tab指示器是否蠕动前行 |
stl_tabIndicatorColor | color | Tab指示器颜色 |
stl_tabIndicator | reference | Tab指示器背景 |
stl_tabIndicatorHeight | dimension | Tab指示器高度 |
stl_tabIndicatorWidth | dimension | Tab指示器宽度 |
stl_tabIndicatorWidthRatio | float | Tab指示器占Tab宽度比重 |
stl_tabIndicatorCornerRadius | dimension | Tab指示器圆角半径 |
stl_tabIndicatorMarginTop | dimension | Tab指示器距离顶部高度 |
stl_tabIndicatorMarginBottom | dimension | Tab指示器距离底部高度 |
stl_tabIndicatorGravity | dimension | Tab指示器垂直方向位置，为上、中、下 |
stl_tabDividerColor | color | Tab间分割线颜色 |
stl_tabDividerWidth | dimension | Tab间分割线宽度 |
stl_tabDividerPadding | dimension | Tab间分割线上下边距 |

### 在代码中配置

```java
// 设置选中颜色，可以单独配置每个Tab对应颜色
tabLayout.setSelectedTextColors(Color.parseColor("#EC0000"),Color.parseColor("#EC0000"));

// 设置Tab选中的监听
tabLayout.setOnTabSelectedListener(new SlidingTabLayout.OnTabSelectedListener() {
    @Override
    public void onSelected(int position) {
    }
});
// 设置Tab点击的监听
tabLayout.setOnTabClickListener(new SlidingTabLayout.OnTabClickListener() {
    @Override
    public void onClick(int position) {
    }
});
// 设置已选中的Tab点击的监听
tabLayout.setOnSelectedTabClickListener(new SlidingTabLayout.OnSelectedTabClickListener() {
    @Override
    public void onClick(int position) {
    }
});
```