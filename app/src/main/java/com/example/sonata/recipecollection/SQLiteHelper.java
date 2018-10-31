package com.example.sonata.recipecollection;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteHelper extends SQLiteOpenHelper {

    @Override
    //数据库第一次被创建时调用的方法
    public void onCreate(SQLiteDatabase db)
    {
        Log.e("SQLiteHelper","数据库创建");
        String sql = "create table RecipeCollection(id integer primary key, title varchar(20), tags varchar(250), imtro varchar(200),ingredients varchar(100), burden varchar(250), albums varchar(250), steps varchar(500));";
        db.execSQL(sql);
        String sql2 = "create table PersonalCollection(id integer primary key,title varchar(20),ingredients varchar(100),albums varchar(250));";
        db.execSQL(sql2);
    }

    public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    //数据库更新
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.e("SQLiteHelper","数据库更新");
    }

    public List<Map<String,Object>> queryAllRecipe()
    {
        Log.e("SQLiteHelper","查询数据库");
        List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
        SQLiteDatabase db = getReadableDatabase();
        String sql = "select * from RecipeCollection";
        Cursor cursor = db.rawQuery(sql,null);

        while (cursor.moveToNext())
        {
            Map<String,Object> item = new HashMap<String,Object>();

            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String tags = cursor.getString(cursor.getColumnIndex("tags"));
            String imtro = cursor.getString(cursor.getColumnIndex("imtro"));
            String ingregients = cursor.getString(cursor.getColumnIndex("ingredients"));
            String burden = cursor.getString(cursor.getColumnIndex("burden"));
            String albums = cursor.getString(cursor.getColumnIndex("albums"));
            String steps = cursor.getString(cursor.getColumnIndex("steps"));

            Bitmap bitmap = stringToBitmap(albums);
            item.put("id",id);
            item.put("title",title);
            item.put("tags",tags);
            item.put("imtro",imtro);
            item.put("ingredients",ingregients);
            item.put("burden",burden);
            item.put("albums",bitmap);
            item.put("steps",steps);
            items.add(item);
        }
        cursor.close();
        db.close();
        return items;
    }

    //插入随机菜谱至数据库
    public void insertCollection(RecipeInfo recipeInfo)
    {
        Log.e("SQLiteHelper","插入每日推荐");
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(
                "insert into RecipeCollection(id,title,tags,imtro,ingredients,burden,albums,steps) values("
                        + String.format("'%d'", recipeInfo.getId()) + ","
                        + String.format("'%s'", recipeInfo.getTitle()) + ","
                        + String.format("'%s'", recipeInfo.getTags()) + ","
                        + String.format("'%s'", recipeInfo.getImtro()) + ","
                        + String.format("'%s'",recipeInfo.getIngredients()) + ","
                        + String.format("'%s'",recipeInfo.getBurden()) + ","
                        + String.format("'%s'",recipeInfo.getAlbums()) + ","
                        + String.format("'%s'",recipeInfo.getSteps()) +
                        ");"
        );
        db.close();
        Log.e("SQLiteHelper","插入成功");
    }

    public static Bitmap getHttpBitmap(String url)
    {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try{
            Log.d("mytag",url);
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

}
