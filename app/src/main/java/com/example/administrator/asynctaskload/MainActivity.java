package com.example.administrator.asynctaskload;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
/*
1.开启异步线程传入网址进行多线程解析
2.doInBackground方法里面调用getJsonData方法将网址传入进行解析
3.getJsonData方法调用readStream方法对json里的字节流一行一行转化成字符流
4.getJsonData方法返回newsBeanList,里面包含已解析的数据（图片，标题，文本）
5.异步线程的onPostExecute方法中执行适配器
6.适配器中传入两个参数：上下文，数据newBean


 */
public class MainActivity extends AppCompatActivity {

    //ListView中装布局文件
    private ListView mListView;
    private static String URL = "http://www.imooc.com/api/teacher?type=4&num=30";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.lv_main);
        new NewAsyncTask().execute(URL);

    }

    //将url中对应的json数据转化为我们所封装的NewsBean对象
    private List<NewsBean> getJsonData(String url){
        List<NewsBean> newsBeanList= new ArrayList<>();
        try {
            //根据URL直接获取网络数据，返回值为InputStream
            String jsonString = readStream(new URL(url).openStream());

            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for(int i=0 ; i<jsonArray.length();i++){
                    jsonObject = jsonArray.getJSONObject(i);
                    NewsBean newsBean = new NewsBean();
                    newsBean.newsIconUrl = jsonObject.getString("picSmall");
                    newsBean.newsTitle = jsonObject.getString("name");
                    newsBean.newsContent = jsonObject.getString("description");
                    newsBeanList.add(newsBean);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            NewsBean newsBean;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newsBeanList;
    }

    //数据读取，通过is解析网页返回的数据
    private String readStream(InputStream is){
        InputStreamReader isr;
        String result = "";
        try {
            String line = "";
            //字节流转化为字符流
            isr = new InputStreamReader(is, "utf-8");
            //保存到buffer中
            BufferedReader br = new BufferedReader(isr);
            while((line = br.readLine()) != null){
                result += line;
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    //实现了网络的异步访问
    class NewAsyncTask extends AsyncTask<String, Void, List<NewsBean>>{


        @Override
        protected List<NewsBean> doInBackground(String... params) {

            return getJsonData(params[0]);
        }

        @Override
        protected void onPostExecute(List<NewsBean> newsBean) {
            super.onPostExecute(newsBean);

            NewsAdapter adapter = new NewsAdapter(MainActivity.this,newsBean,mListView);
            mListView.setAdapter(adapter);

        }
    }
}
