package edu.iu.ocrproject.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yapaytech on 19.05.2016.
 */
public class BaseManager extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "ocr.sqlite";
    private static final int DATABASE_VERSION = 1;

    public BaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void getActive(final IQuery callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String tag = "getActive";
                SQLiteDatabase database = getReadableDatabase();
                Cursor cursor = database.query("users",new String[]{"username","name","surname","age","sex"},
                        "logined=?",new String[]{"1"},null,null,null);
                if(cursor.moveToFirst()){
                    User user = new User();
                    user.username = cursor.getString(0);
                    user.name = cursor.getString(1);
                    user.surname = cursor.getString(2);
                    user.age = cursor.getInt(3);
                    user.sex = cursor.getInt(4);
                    user.likes = getLikesByUser(user.username);
                    user.dislikes = getDislikesByUser(user.username);
                    cursor.close();
                    callback.onResult(tag,user);
                }else{
                    cursor.close();
                    callback.onResult(tag);
                }
            }
        }).start();
    }

    public void login(final IQuery callback, final String username, final String password){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String tag = "login";
                SQLiteDatabase database = getWritableDatabase();
                Cursor cursor = database.query("users",new String[]{"username","name","surname","age","sex"},
                        "username=? and password=?",new String[]{username,password},null,null,null);
                if(cursor.moveToFirst()){
                    User user = new User();
                    user.username = cursor.getString(0);
                    user.name = cursor.getString(1);
                    user.surname = cursor.getString(2);
                    user.age = cursor.getInt(3);
                    user.sex = cursor.getInt(4);
                    user.likes = getLikesByUser(user.username);
                    user.dislikes = getDislikesByUser(user.username);
                    cursor.close();
                    ContentValues values = new ContentValues();
                    values.put("logined",1);
                    int rows = database.update("users",values,"username=?",new String[]{username});
                    if(rows == 1){
                        callback.onResult(tag,true,user);
                    }else{
                        callback.onResult(tag,false);
                    }
                }else{
                    cursor.close();
                    callback.onResult(tag,false);
                }
            }
        }).start();
    }

    public void signup(final IQuery callback, final String username, final String name, final String surname, final String password){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String tag = "signup";
                SQLiteDatabase database = getWritableDatabase();
                Cursor cursor = database.query("users",new String[]{"username"},
                        "username=?",new String[]{username},null,null,null);
                if(cursor.moveToFirst()){
                    cursor.close();
                    callback.onResult(tag,false,"Kullanıcı adı zaten var.");
                }else{
                    cursor.close();
                    ContentValues values = new ContentValues();
                    values.put("username",username);
                    values.put("name",name);
                    values.put("surname",surname);
                    values.put("password",password);
                    values.put("logined",1);
                    long row = database.insert("users","",values);
                    if(row == -1){
                        callback.onResult(tag,false,"Bir hata oluştu");
                    }else{
                        User user = new User();
                        user.username = username;
                        user.name = name;
                        user.surname = surname;
                        callback.onResult(tag,true,user);
                    }

                }
            }
        }).start();
    }

    public void logout(){
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("logined",0);
        database.update("users",values, null, null);
    }

    public void getIngredients(final IQuery callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase database = getReadableDatabase();
                Cursor cursor = database.query("ingredients",new String[]{"id","name"},null,null,null,null,null);
                List<Ingredient> ingredients = new ArrayList<>();
                while (cursor.moveToNext()){
                    Ingredient ingredient = new Ingredient();
                    ingredient.id = cursor.getInt(0);
                    ingredient.name = cursor.getString(1);
                    ingredients.add(ingredient);
                }
                cursor.close();
                callback.onResult("getIngredients",ingredients);
            }
        }).start();
    }

    private Ingredient getIngredientById(int id){
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query("ingredients",new String[]{"id","name","warning"},"id=?",new String[]{String.valueOf(id)},null,null,null);
        Ingredient ingredient = new Ingredient();
        if (cursor.moveToFirst()){
            ingredient.id = cursor.getInt(0);
            ingredient.name = cursor.getString(1);
            ingredient.message = cursor.getString(2);
        }
        cursor.close();
        return ingredient;
    }

    private Set<Integer> getLikesByUser(String username){
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query("likes",new String[]{"ingredient_id"},"username=?",new String[]{username},null,null,null);
        Set<Integer> ingredients = new HashSet<>();
        while (cursor.moveToNext()){
            ingredients.add(cursor.getInt(0));
        }
        cursor.close();
        return ingredients;
    }

    private Set<Integer> getDislikesByUser(String username){
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query("dislikes",new String[]{"ingredient_id"},"username=?",new String[]{username},null,null,null);
        Set<Integer> ingredients = new HashSet<>();
        while (cursor.moveToNext()){
            ingredients.add(cursor.getInt(0));
        }
        cursor.close();
        return ingredients;
    }

    public void updateUser(final User user){
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase database = getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("age",user.age);
                values.put("sex",user.sex);
                database.update("users",values,"username=?",new String[]{user.username});
                database.delete("likes","username=?",new String[]{user.username});
                database.delete("dislikes","username=?",new String[]{user.username});
                for(Integer ingredient:user.likes){
                    ContentValues insert_values = new ContentValues();
                    insert_values.put("username",user.username);
                    insert_values.put("ingredient_id",ingredient);
                    database.insert("likes","",insert_values);
                }
                for(Integer ingredient:user.dislikes){
                    ContentValues insert_values = new ContentValues();
                    insert_values.put("username",user.username);
                    insert_values.put("ingredient_id",ingredient);
                    database.insert("dislikes","",insert_values);
                }
            }
        }).start();
    }

    public void controlProductByUser(final IQuery callback, final String barcode, final String username){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String tag = "controlProductByUser";
                SQLiteDatabase database = getReadableDatabase();
                Cursor cursor = database.query("products",new String[]{"name"},"barcode=?",new String[]{barcode},null,null,null);
                if(cursor.moveToFirst()){
                    String name = cursor.getString(0);

                    Set<Integer> ingredients = getIngredientsByProduct(barcode);

                    Map<Integer,Ingredient> full_list = new HashMap<>();
                    for(Integer item:ingredients){
                        full_list.put(item,getIngredientById(item));
                    }

                    Set<Integer> likes = getLikesByUser(username);
                    Set<Integer> dislikes = getDislikesByUser(username);

                    List<Ingredient> likes_result = new ArrayList<>();
                    for(Integer item:likes){
                        if(ingredients.contains(item)){
                            likes_result.add(full_list.get(item));
                        }
                    }

                    List<Ingredient> dislikes_result = new ArrayList<>();
                    for(Integer item:dislikes){
                        if(ingredients.contains(item)){
                            dislikes_result.add(full_list.get(item));
                        }
                    }

                    List<Ingredient> WarningList = new ArrayList<>();
                    for(Map.Entry<Integer,Ingredient> entry:full_list.entrySet()){
                        String warning = entry.getValue().message;
                        if(warning != null && !warning.contentEquals("")){
                            WarningList.add(entry.getValue());
                        }
                    }

                    callback.onResult(tag,true,name,likes_result,dislikes_result,WarningList);
                }else{
                    callback.onResult(tag,false,"Ürün bulunamadı");
                }
                cursor.close();
            }
        }).start();
    }

    private Set<Integer> getIngredientsByProduct(String barcode){
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query("includes",new String[]{"ingredient_id"},"barcode=?",new String[]{barcode},null,null,null);
        Set<Integer> ingredients = new HashSet<>();
        while (cursor.moveToNext()){
            ingredients.add(cursor.getInt(0));
        }
        cursor.close();
        return ingredients;
    }

    public interface IQuery{
        void onResult(String func, Object... result);
    }

}
