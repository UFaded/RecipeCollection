package com.example.sonata.recipecollection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class RecipeCollectionAPI {
    public static final String DEF_CHATSET = "UTF-8";
    public static final int DEF_CONN_TIMEOUT = 30000;
    public static final int DEF_READ_TIMEOUT = 30000;
    public static String userAgent =  "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";
    public static final String APPKEY ="1ada1f9938338ec1c778d3d41af44ace";

    //1.菜谱大全
    public RecipeInfo getRequest1(String recipe_title){
        String result =null;
        String url ="http://apis.juhe.cn/cook/query.php";//请求接口地址
        Map params = new HashMap();//请求参数
        params.put("menu",recipe_title);//需要查询的菜谱名
        params.put("key",APPKEY);//应用APPKEY(应用详细页查询)
        params.put("dtype","");//返回数据的格式,xml或json，默认json
        params.put("pn","");//数据返回起始下标
        params.put("rn","");//数据返回条数，最大30
        params.put("albums","");//albums字段类型，1字符串，默认数组

        try {
            result =net(url, params, "GET");//这应该还是一种很诡异的格式
            //转换成jsonObject，所有结果
            JSONObject object = new JSONObject(result);
            if(object.getInt("error_code")==0){
                RecipeInfo recipeInfo = jsonToRecipeInfo(object);
                return recipeInfo;
            }else{

                System.out.println(object.get("error_code")+":"+object.get("reason"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void getRequest2(){
        String result =null;
        String url ="http://apis.juhe.cn/cook/category";//请求接口地址
        Map params = new HashMap();//请求参数
        params.put("parentid","");//分类ID，默认全部
        params.put("key",APPKEY);//应用APPKEY(应用详细页查询)
        params.put("dtype","");//返回数据的格式,xml或json，默认json

        try {
            result =net(url, params, "GET");
            JSONObject object = new JSONObject(result);
            if(object.getInt("error_code")==0){
                System.out.println(object.get("result"));
            }else{
                System.out.println(object.get("error_code")+":"+object.get("reason"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getRequest3(){
        String result =null;
        String url ="http://apis.juhe.cn/cook/index";//请求接口地址
        Map params = new HashMap();//请求参数
        params.put("cid","");//标签ID
        params.put("key",APPKEY);//应用APPKEY(应用详细页查询)
        params.put("dtype","");//返回数据的格式,xml或json，默认json
        params.put("pn","");//数据返回起始下标，默认0
        params.put("rn","");//数据返回条数，最大30，默认10
        params.put("format","");//steps字段屏蔽，默认显示，format=1时屏蔽

        try {
            result =net(url, params, "GET");
            JSONObject object = new JSONObject(result);
            if(object.getInt("error_code")==0){
                System.out.println(object.get("result"));
            }else{
                System.out.println(object.get("error_code")+":"+object.get("reason"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RecipeInfo getRequest4(int ID){
        String result =null;
        String url ="http://apis.juhe.cn/cook/queryid";//请求接口地址
        Map params = new HashMap();//请求参数
        params.put("id",ID);//菜谱的ID
        params.put("key",APPKEY);//应用APPKEY(应用详细页查询)
        params.put("dtype","");//返回数据的格式,xml或json，默认json

        try {
            result =net(url, params, "GET");
            JSONObject object = new JSONObject(result);
            if(object.getInt("error_code")==0){
                RecipeInfo recipeInfo = jsonToRecipeInfo(object);
                return recipeInfo;
            }else{
                System.out.println(object.get("error_code")+":"+object.get("reason"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected RecipeInfo jsonToRecipeInfo(JSONObject object) throws JSONException {
        String result1 = object.getString("result");
        JSONObject object1 = new JSONObject(result1);

        //获取result数组
        JSONArray searchResult = object1.getJSONArray("data");
        RecipeInfo recipeInfo = null;
        for (int i = 0;i < 1;i++)
        {
            //第一层
            JSONObject object2 = searchResult.getJSONObject(i);

            int id = object2.getInt("id");
            String title = object2.getString("title");
            String tags = object2.getString("tags");
            String imtro = object2.getString("imtro");
            String ingredients = object2.getString("ingredients");
            String burden = object2.getString("burden");

            JSONArray Aalbums = object2.getJSONArray("albums");
            String albums = Aalbums.getString(0); //url
            Bitmap bitmap = getHttpBitmap(albums);
            String albums2 = bitmapToString(bitmap);

            JSONArray steps = object2.getJSONArray("steps");
            String Ssteps = "";

            for (int j= 0; j< object2.length(); j++)
            {
                JSONObject object3 = steps.getJSONObject(j);

                String img = object3.getString("img");
                String step = object3.getString("step");

                //第二层
                if(j!=object2.length()-1) {
                    Ssteps = Ssteps + img + " " + step + " ";
                }

                else
                {
                    Ssteps = Ssteps + img + " " + step;
                }
            }
            recipeInfo = makeRecipeInfo(id,title,tags,imtro,ingredients,burden,albums2,Ssteps);

        }
        return recipeInfo;
    }

    public static void main(String[] args) {

    }

    public static String net(String strUrl, Map params,String method) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String rs = null;
        try {
            StringBuffer sb = new StringBuffer();
            if(method==null || method.equals("GET")){
                strUrl = strUrl+"?"+urlencode(params);
            }
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            if(method==null || method.equals("GET")){
                conn.setRequestMethod("GET");
            }else{
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
            }
            conn.setRequestProperty("User-agent", userAgent);
            conn.setUseCaches(false);
            conn.setConnectTimeout(DEF_CONN_TIMEOUT);
            conn.setReadTimeout(DEF_READ_TIMEOUT);
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            if (params!= null && method.equals("POST")) {
                try {
                    DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                    out.writeBytes(urlencode(params));
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
            InputStream is = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, DEF_CHATSET));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sb.append(strRead);
            }
            rs = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return rs;
    }

    //将map型转为请求参数型
    public static String urlencode(Map<String,Object>data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry i : data.entrySet()) {
            try {
                sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue()+"","UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static RecipeInfo makeRecipeInfo(int id,String title,String tags,String imtro,String ingredients,String burden,String albums,String steps)
    {
        RecipeInfo recipeInfo = new RecipeInfo();
        recipeInfo.setId(id);
        recipeInfo.setTitle(title);
        recipeInfo.setTags(tags);
        recipeInfo.setImtro(imtro);
        recipeInfo.setIngredients(ingredients);
        recipeInfo.setBurden(burden);
        recipeInfo.setAlbums(albums);
        recipeInfo.setSteps(steps);
        return recipeInfo;
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

    protected String bitmapToString(Bitmap bitmap) {
        //用户在活动中上传的图片转换成String进行存储
        String string;
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bytes = stream.toByteArray();// 转为byte数组
            string = Base64.encodeToString(bytes, Base64.DEFAULT);
            return string;
        } else {
            return "";
        }
    }

    protected void test()
    {
        Log.e("test","test");
    }
}
