package com.example.sonata.recipecollection;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeDetails extends AppCompatActivity {

    private ImageView recipe_image;
    private SQLiteHelper db;
    private TextView recipe_title;
    private TextView recipe_tags,recipe_imtro,recipe_ingredients,recipe_burden;
    private ListView listView_burden,listView_steps;
    private Button recipe_subscribe;
    private static Bitmap changeToBitmap;
    protected static final int SUCCESS = 0;
    protected static final int ERROR = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);
        this.recipe_image = (ImageView) findViewById(R.id.recipe_image);
        recipe_title = (TextView) findViewById(R.id.recipe_title);
        recipe_tags = (TextView) findViewById(R.id.recipe_tags);
        recipe_imtro = (TextView) findViewById(R.id.recipe_imtro);
        recipe_ingredients = (TextView) findViewById(R.id.recipe_ingredients);
        recipe_burden = (TextView) findViewById(R.id.recipe_burden);
        listView_burden = (ListView) findViewById(R.id.listView_burden);
        listView_steps = (ListView)findViewById(R.id.listView_steps);
        recipe_subscribe = (Button) findViewById(R.id.recipe_subscribe);


        if((RecipeInfo)getIntent().getSerializableExtra("RecipeDetails")!=null)
        {
            final RecipeInfo recipeInfo = (RecipeInfo) getIntent().getSerializableExtra("RecipeDetails");
            recipe_title.setText(recipeInfo.getTitle());
            recipe_tags.setText(recipeInfo.getTags());
            recipe_imtro.setText(recipeInfo.getImtro());
            recipe_ingredients.setText(recipeInfo.getIngredients());
            changeToBitmap = stringToBitmap(recipeInfo.getAlbums());
            recipe_image.setImageBitmap(changeToBitmap);
            final List<Map<String,Object>> items = burdenSplit(recipeInfo.getBurden());
            SimpleAdapter adapter=new SimpleAdapter(this,items,R.layout.activity_burden_items,new String[]{"component","weight"},new int[]{R.id.recipe_component,R.id.recipe_weight});
            listView_burden.setAdapter(adapter);

            new Thread(){
                public void run()
                {
                    List<Map<String,Object>> items2 = getSteps(recipeInfo.getSteps());
                    Message msg = new Message();
                    msg.obj = items2;
                    msg.what = SUCCESS;
                    handle.sendMessage(msg);
                }
            }.start();

            setListViewHeightBasedOnChildren(listView_burden);

            recipe_subscribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println(recipeInfo.getTitle());
                    int flag = 0;
                    SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("data/data/com.example.sonata.recipecollection/databases/RecipeCollection.db",null);
                    String sql = "select * from PersonalCollection";
                    Cursor cursor = db.rawQuery(sql,null);

                    while (cursor.moveToNext())
                    {
                        int id = cursor.getInt(cursor.getColumnIndex("id"));
                        if (id == recipeInfo.getId())
                        {
                            flag = 1;
                        }
                    }

                    if (flag==0)
                    {
                        db.execSQL(
                                "insert into PersonalCollection(id,title,ingredients,albums) values("
                                        + String.format("'%d'", recipeInfo.getId()) + ","
                                        + String.format("'%s'", recipeInfo.getTitle()) + ","
                                        + String.format("'%s'",recipeInfo.getIngredients()) + ","
                                        + String.format("'%s'",recipeInfo.getAlbums()) +
                                        ");"
                        );
                        db.close();
                        Toast.makeText(RecipeDetails.this,"收藏成功",Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(RecipeDetails.this,"您已收藏",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }

    protected Handler handle = new Handler(){
        public void handleMessage(Message msg)
        {
            if (msg.what == SUCCESS)
            {
                List<Map<String,Object>> items = (List<Map<String, Object>>) msg.obj;
                System.out.println(items.size());
                SimpleAdapter adapter1 = new SimpleAdapter(RecipeDetails.this,items,R.layout.activity_steps_items,new String[]{"step","image"},new int[]{R.id.recipe_steps_text,R.id.recipe_steps_img});
                    adapter1.setViewBinder(new SimpleAdapter.ViewBinder() {
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
                    listView_steps.setAdapter(adapter1);
                setListViewHeightBasedOnChildren(listView_steps);
            }

        }
    };

    protected Bitmap stringToBitmap(String string) {
        //数据库中的String类型转换成Bitmap
        Bitmap bitmap;
        if (string != null) {
            try {
                byte[] bytes = Base64.decode(string, Base64.DEFAULT);
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                return bitmap;
            } catch (IllegalArgumentException exception) {
                Log.e("xhx", "base64转换图片异常");
            }
        } else {
            return null;
        }
        return null;
    }

    public static Bitmap getHttpBitmap(String url)
    {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try{
            myFileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try{
            HttpURLConnection conn = (HttpURLConnection)myFileUrl.openConnection();
            conn.setConnectTimeout(0);
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    protected  List<Map<String,Object>> burdenSplit(String burden)
    {
        String[] sent = burden.split(";");
        String[] component = new String[sent.length];
        String[] weight=new String[sent.length];
        for (int i=0;i< sent.length;i++)
        {
            String str = sent[i].substring(0,sent[i].indexOf(","));
            component[i] = str;
            String str2 = sent[i].substring(sent[i].indexOf(",")+1);
            weight[i] = str2;
        }

        List<Map<String,Object>> items = new ArrayList<Map<String, Object>>();
        for (int i =0;i<sent.length;i++)
        {
            Map<String,Object> item = new HashMap<String,Object>();
            item.put("component",component[i]);
            item.put("weight",weight[i]);

            System.out.println(item.get("component") + " - " + item.get("weight"));
            items.add(item);
        }
        return items;
    }

    protected List<Map<String,Object>> getSteps(String steps)
    {
        String[] sent = steps.split(" ");
        System.out.println(sent.length);
        for (int i=0;i<sent.length;i++)
        {
            System.out.println(sent[i]);
        }

        String[] step = new String[sent.length/2];
        String[] image =new String[sent.length/2];
        int k =0;int p=0;
        for (int i=0;i< sent.length;i++)
        {
            if (i%2!=0)
            {
                step[k] = sent[i];
                k++;
            }
            else
            {
                image[p] = sent[i];
                p++;
            }
        }

        List<Map<String,Object>> items = new ArrayList<Map<String, Object>>();
        for (int i =0;i<step.length;i++)
        {
            Map<String,Object> item = new HashMap<String,Object>();
            item.put("step",step[i]);
            item.put("image",getHttpBitmap(image[i]));
            items.add(item);
        }
        return items;
    }

    //这段简直优秀 虽然不是很懂
    public void setListViewHeightBasedOnChildren(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();

        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));

        ((ViewGroup.MarginLayoutParams) params).setMargins(10, 10, 10, 10); // 可删除

        listView.setLayoutParams(params);
    }
}
