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

```
compile 'com.kevin:slidingtablayout:1.1.2'
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