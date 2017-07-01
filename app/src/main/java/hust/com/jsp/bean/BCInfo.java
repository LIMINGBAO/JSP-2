package hust.com.jsp.bean;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import hust.com.jsp.db.DYDBHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by admin on 2017/5/9.
 */

public class BCInfo {



    private int id;
    private String name;
    private Calendar time;
    private String type;

    List<BCInfoItem> bcInfoItemList;
    public BCInfo(String name,Calendar time){
        this.name=name;
        this.time=time;
    }
    public BCInfo(){
        this.id = new Random().nextInt();
        this.bcInfoItemList = new ArrayList<>();
    }

    public void setTime(Calendar time) {
        this.time = time;
    }

    public String getName(){
        return this.name;
    }

    public int Count() {
        return bcInfoItemList.size();
    }

    public String getDisplayTime() {
        if(time != null){
            SimpleDateFormat pFormat = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
            Log.v("Time",pFormat.format(time.getTime()));
            return pFormat.format(time.getTime());
        }
        else{
            return "";
        }
    }

    public String getJZJString() {
        StringBuilder sb = new StringBuilder();
        for (BCInfoItem bcInfoItem :
                bcInfoItemList) {
            sb.append(bcInfoItem.getName());
            sb.append(" ");
        }
        return sb.toString();
    }

    public BCInfoItem get(int position) {
        return bcInfoItemList.get(position);
    }

    public void addBCInfoItem(BCInfoItem item){
        bcInfoItemList.add(item);
    }

    public void setTime(long aLong) {
        this.time = Calendar.getInstance();
        this.time.setTimeInMillis(aLong);
    }

    public void setID(int anInt) {
        this.id = anInt;
    }
    public int getId() {
        return id;
    }
    public void setName(String string) {
        this.name = string;
    }

    public void readFromDB(SQLiteDatabase db) {
        this.bcInfoItemList = new ArrayList<>();
        Cursor cursor = db.query(DYDBHelper.BC_ITEM_TABLE,new String[]{"id","jzjid","actionlist","name","time","bctype"},"",new String[]{},"","","");
        while(cursor.moveToNext()){
            JZJ jzj=new JZJ(cursor.getInt(1));
            jzj.setDisplayName(cursor.getString(3));
            BCInfoItem bcInfoItem = new BCInfoItem(jzj);
            bcInfoItem.setID(cursor.getInt(0));
            bcInfoItem.setJZJ(cursor.getInt(1));
            JZJAction action=new JZJAction();
            action.setName(cursor.getString(2).split(" ")[1]);
            bcInfoItem.addAction(action);
            bcInfoItem.setName(cursor.getString(3));
            bcInfoItem.setTime(cursor.getLong(4));
            bcInfoItem.setType(cursor.getString(5));
            bcInfoItemList.add(bcInfoItem);
        }
    }

    public String getType() {
        return type;
    }
}