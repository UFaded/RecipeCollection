package com.example.sonata.recipecollection;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

public class classification extends AppCompatActivity {


    private int SEARCH_RECIPE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classification);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_view, menu);
        //找到searchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
//        searchView.setIconified(false); //这是一开始就处于搜索状态。
        searchView.setQueryHint("按菜谱名称、ID搜索");
//        searchView.onActionViewExpanded();// 当展开无输入内容的时候，没有关闭的图标
        searchView.setIconifiedByDefault(false);//默认为true在框内，设置false则在框外
//
        searchView.setSubmitButtonEnabled(true);//显示提交按钮
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String s) {

                new Thread(){
                    public void run()
                    {
                        RecipeCollectionAPI recipeCollectionAPI = new RecipeCollectionAPI();
                        RecipeInfo recipeInfo = recipeCollectionAPI.getRequest1(s);
                        Message msg = new Message();
                        msg.obj = recipeInfo;
                        msg.what = 1;
                        handler.sendMessage(msg);

                    }
                }.start();

                return true;
            }

            //搜索框每次变化，都会回调函数
            @Override
            public boolean onQueryTextChange(String s) {
                Log.i("test","内容:" + s);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    protected Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (msg.what ==1)
            {
                RecipeInfo recipeInfo = (RecipeInfo) msg.obj;
                Intent intent = new Intent(classification.this,RecipeDetails.class);
                intent.putExtra("RecipeDetails",recipeInfo);
                startActivityForResult(intent,SEARCH_RECIPE);
            }
        }
    };

    protected void jumpToDetails(RecipeInfo recipeInfo)
    {
        Intent intent = new Intent(classification.this,RecipeDetails.class);
        intent.putExtra("RecipeDetails",recipeInfo);
        startActivityForResult(intent,SEARCH_RECIPE);
    }

}
