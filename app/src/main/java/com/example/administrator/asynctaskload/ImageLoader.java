package com.example.administrator.asynctaskload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;

/**
 * Created by Administrator on 2016/10/24.
 */

public class ImageLoader {


    private ImageView mImageView;
    private String mURL;
    //用LruCaches类进行图片缓存，键值对泛型为url和图片
    private LruCache<String,Bitmap> mCaches;

    private ListView mListView;
    private Set<NewsAsyncTask> mTask;


    public ImageLoader(ListView listView){

        //传入布局ListView
        mListView = listView;
        mTask = new HashSet<>();
        /*
        *指定一部分内存为LruCache的缓存空间
        */
        //获取最大的缓存空间
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        //指定缓存所用的大小
        int cachesSize = maxMemory/4;
        //用匿名类重写sizeOf方法(默认返回元素个数)
        mCaches = new LruCache<String, Bitmap>(cachesSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //每次存入缓存时调用，返回Bitmap的实际大小
                return value.getByteCount();
            }
        };


    }

    //将图片添加入缓存中
    public void addBitmapToCache(String url, Bitmap bitmap){
        //判断缓存中是否存在当前url
        if(getBitmapFromCache(url) == null){
            //mCaches为map类型，有key和value
            mCaches.put(url,bitmap);
        }
    }

    //将图片从缓存中读取
    public Bitmap getBitmapFromCache(String url){
        //mCaches底层是通过map实现的，可以使用map的get方法返回其
        //键值对所对应的bitmap
        return mCaches.get(url);
    }
    private android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //当获取的url为正确对应的url时才显示缓存图片
            if(mImageView.getTag().equals(mURL)) {

                mImageView.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };


    public void showImageByThread(ImageView imageView, final String url){
        mImageView = imageView;
        mURL = url;

        //多线程用Thread类
        new Thread(){
            //重写run方法
            @Override
            public void run() {
                super.run();
                Bitmap bitmap = getBitmapFromURL(url);
                Message message = Message.obtain();
                message.obj = bitmap;
                mHandler.sendMessage(message);
            }
        }.start();
    }

    //将url里的图片解析出来返回Bitmap
    public Bitmap getBitmapFromURL(String urlString){
        Bitmap bitmap;
        InputStream is = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(connection.getInputStream());
            bitmap = BitmapFactory.decodeStream(is);
            connection.disconnect();
            return bitmap;

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }finally {
            try{
            is.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
        return null;
    }


    public void showImageByAsyncTask(ImageView imageView, String url ){
        //从缓存中取出图片
        Bitmap bitmap = getBitmapFromCache(url);
        //判断bitmap是否为空
        if(bitmap == null){
            //内存中没有这张图片，应下载
            imageView.setImageResource(R.mipmap.ic_launcher);
        }
        else {
            //内存中有图片，直接使用
            imageView.setImageBitmap(bitmap);
        }
        new NewsAsyncTask(url).execute(url);
    }

    //用loadImages方法加载从start到end的所有图片
    public void loadImages(int start, int end){
        for(int i = start; i<end;i++){
            //取url数组中的对应url加载图片
            String url = NewsAdapter.URLS[i];
            //从缓存中取出图片
            Bitmap bitmap = getBitmapFromCache(url);
            //判断bitmap是否为空
            if(bitmap == null){
                //内存中没有这张图片，应下载
                NewsAsyncTask task = new NewsAsyncTask(url);
                task.execute(url);
                mTask.add(task);
            }
            else {
                //内存中有图片，直接使用
                ImageView imageView = (ImageView) mListView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public void cancelAllTasks(){
        if(mTask != null){
            for(NewsAsyncTask task: mTask){
                task.cancel(false);
            }
        }
    }
    private class NewsAsyncTask extends AsyncTask<String, Void, Bitmap>{

     //   private ImageView mImageView;
        private String mURL;
        public NewsAsyncTask(String url){
    //        mImageView = imageView;
            mURL = url;
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = getBitmapFromURL(params[0]);
            //从网络获取图片
            if(bitmap != null){
                //bitmap存在，则将其保存入缓存中
                addBitmapToCache(params[0],bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView imageView = (ImageView) mListView.findViewWithTag(mURL);
            if(imageView !=null && bitmap != null){
                imageView.setImageBitmap(bitmap);
            }
            mTask.remove(this);
        }
    }
}
