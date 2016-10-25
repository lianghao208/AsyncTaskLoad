package com.example.administrator.asynctaskload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2016/10/23.
 */

public class NewsAdapter extends BaseAdapter implements AbsListView.OnScrollListener{

    private List<NewsBean> mList;
    private LayoutInflater mInflater;
    private ImageLoader  mImageLoader;
    private boolean mFirstIn;

    private int mStart,mEnd;
    public static String[] URLS;

    //适配器中创建含参构造器，传入上下文activity，数据数组List，布局ListView
    public NewsAdapter(Context context, List<NewsBean> data, ListView listView){
        //数据传输
        mList = data;
        //转换布局文件
        mInflater = LayoutInflater.from(context);
        //实例化ImageLoader类，传入布局ListView参数
        mImageLoader = new ImageLoader(listView);
        //创建一个大小为data.size()的字符串数组装传入的数据(返回的图片，标题，文章url)
        URLS = new String[data.size()];
        //将url中IconUrl传入数组中
        for(int i =  0; i < data.size();i++){
            URLS[i] = data.get(i).newsIconUrl;
        }
        //第一次启动时为true
        mFirstIn = true;
        //实现了接口功能要记得注册接口事件
        listView.setOnScrollListener(this);
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(viewHolder == null){
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.itemlayout, null);
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_content);
            convertView.setTag(viewHolder);

        }else {
            viewHolder = (ViewHolder) convertView.getTag();

        }

        viewHolder.ivIcon.setImageResource(R.mipmap.ic_launcher);
        //将图片url设为标签进行绑定，使图片和它的url相对应
        viewHolder.ivIcon.setTag( mList.get(position).newsIconUrl);


        //new ImageLoader().showImageByThread(viewHolder.ivIcon, mList.get(position).newsIconUrl);
        mImageLoader.showImageByAsyncTask(viewHolder.ivIcon, mList.get(position).newsIconUrl);

        viewHolder.tvTitle.setText(mList.get(position).newsTitle);
        viewHolder.tvContent.setText(mList.get(position).newsContent);
        return convertView;
    }

    //在ListView滑动状态切换时才调用
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //在ListView滚动时停止加载项，ListView滚动停止后开始加载
        //处于停滞状态
        if(scrollState == SCROLL_STATE_IDLE){
            //加载可见项
            mImageLoader.loadImages(mStart,mEnd);
        }
        else{
            //停止任务
            mImageLoader.cancelAllTasks();

        }
    }

    //整个滑动过程都会调用，第二个参数为第一个可见元素，第三个参数为当前可见元素的长度
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        //获得起始项的URL
        mStart = firstVisibleItem;
        mEnd = firstVisibleItem + visibleItemCount;

        //第一次显示时调用
        if(mFirstIn == true && visibleItemCount > 0){
            mFirstIn = false;
            mImageLoader.loadImages(mStart,mEnd);
        }

    }


    class ViewHolder{
        public TextView tvTitle,tvContent;
        public ImageView ivIcon;

    }


}
