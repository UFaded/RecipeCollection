package com.example.sonata.recipecollection;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main2Activity extends AppCompatActivity {

    private ListView listView;
    private int SEARCH_RECIPE = 1;
    protected static final int SUCCESS = 0;
    protected static final int ERROR = 1;
    protected static final int NETWORK_ERROR = 2;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent2 = new Intent(Main2Activity.this,MainActivity.class);
                    startActivity(intent2);
                    return true;
                case R.id.navigation_dashboard:
                    Intent intent = new Intent(Main2Activity.this,classification.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_notifications:
                    listView = (ListView) findViewById(R.id.listView);
                    query();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        listView = (ListView) findViewById(R.id.listView);
        query();
    }

    protected void query()
    {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("data/data/com.example.sonata.recipecollection/databases/RecipeCollection.db",null);
        List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
        String sql = "select * from PersonalCollection";
        Cursor cursor = db.rawQuery(sql,null);

        while (cursor.moveToNext())
        {
            Map<String,Object> item = new HashMap<String,Object>();

            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String ingregients = cursor.getString(cursor.getColumnIndex("ingredients"));
            String albums = cursor.getString(cursor.getColumnIndex("albums"));

            Bitmap bitmap = stringToBitmap(albums);
            item.put("id",id);
            item.put("title",title);
            item.put("ingredients",ingregients);
            item.put("albums",bitmap);
            items.add(item);
        }
        cursor.close();
        db.close();

        SimpleAdapter adapter = new SimpleAdapter(this,items,R.layout.activity_cardview,new String[]{"albums","title","ingredients"},new int[]{R.id.imageView,R.id.title,R.id.ingredients});
        //我真的很想问 这段代码啥意思
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object o, String s) {

                if (view instanceof ImageView && o instanceof Bitmap)
                {
                    ImageView imageView = (ImageView) view;
                    imageView.setImageBitmap((Bitmap)o);
                    return true;
                }else
                    return false;
            }
        });

        listView.setAdapter(adapter);

        AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ListView list_View = (ListView)adapterView;
                HashMap<String,String> map = (HashMap<String,String>)list_View.getItemAtPosition(position);
                final String name = map.get("title");
                new Thread()
                {
                    public void run()
                    {
                        try{
                            RecipeCollectionAPI recipeCollectionAPI = new RecipeCollectionAPI();
                            RecipeInfo recipeInfo =  recipeCollectionAPI.getRequest1(name);
                            System.out.println("你点击了" + recipeInfo.getTitle());
                            Message msg = Message.obtain();
                            msg.obj = recipeInfo;
                            msg.what = SUCCESS;
                            handler.sendMessage(msg);
                        }catch (Exception e)
                        {
                            Message msg = Message.obtain();
                            msg.what = NETWORK_ERROR;
                            handler.sendMessage(msg);
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        };

        listView.setOnItemClickListener(onItemClickListener);
    }

    protected Bitmap stringToBitmap(String string){
        //数据库中的String类型转换成Bitmap
        Bitmap bitmap;
        if(string!=null){
            byte[] bytes= Base64.decode(string,Base64.DEFAULT);
            bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            return bitmap;
        }
        else {
            return null;
        }
    }

    private Handler handler = new Handler(){
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case SUCCESS:
                    RecipeInfo recipeInfo = (RecipeInfo) msg.obj;
                    Intent intent = new Intent(Main2Activity.this,RecipeDetails.class);
                    intent.putExtra("RecipeDetails",recipeInfo);
                    startActivityForResult(intent,SEARCH_RECIPE);
                    break;
                case NETWORK_ERROR:
                    System.out.println("sorry");
                    break;
            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
