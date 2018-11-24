package com.github.kooroshh.wpsauditor;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Oplus on 01/03/2016.
 */
public class SQLiteClient extends SQLiteOpenHelper{
    Context mContext ;
    private final static String mDatabaseName = "oui.db";
    private final static int mVersion = 1;
    String mDatabasePath;
    public SQLiteClient(Context context) {
        super(context, mDatabaseName, null, mVersion);
        this.mContext = context;
        mDatabasePath = new StringBuffer(mContext.getFilesDir().getAbsolutePath()).append("/").append(mDatabaseName).toString();
        prepareDB();
    }
    private void copyDataBase() throws IOException {
        OutputStream os = new FileOutputStream(mDatabasePath);
        InputStream is = mContext.getAssets().open(mDatabaseName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }
        is.close();
        os.flush();
        os.close();
    }
    private boolean checkDBExists(){
        boolean checkDB = false;
        try {
            File file = new File(mDatabasePath);
            checkDB = file.exists();
        } catch(SQLiteException e) {
            e.printStackTrace();
        }
        return checkDB;
    }
    private void prepareDB(){
        boolean dbExist = checkDBExists();
        if(!dbExist) {
            try{
                copyDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public String Mac2Vendor(String mac){
        mac = mac.toUpperCase();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(mDatabasePath, null, SQLiteDatabase.OPEN_READONLY);
        String query = "SELECT vendor FROM records where mac = \"" + mac.replace(":","-") + "\"";
        Cursor cursor = db.rawQuery(query, null);
        String vendor = "Unkown Vendor";
        if(cursor.moveToFirst()){

            vendor = cursor.getString(0);
        }
        db.close();
        return vendor;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
