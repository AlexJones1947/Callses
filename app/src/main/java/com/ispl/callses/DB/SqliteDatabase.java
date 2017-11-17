package com.ispl.callses.DB;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.BaseColumns;

import com.ispl.callses.R;
import com.ispl.callses.model.FriendContactModel;
import com.ispl.callses.utils.CallessUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.ispl.callses.utils.CallessUtils.rootPath;
import static com.ispl.callses.utils.CallessUtils.sIncomingContactNumber;

/**
 * Created by infinium on 25/07/17.
 */

public class SqliteDatabase extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "Callses";
    private static final String TABLE_CALLESS = "callses";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_IMAGEPATH = "imagepath";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_PHONE_SHOW = "phone_show";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_ORDER_LIST_NUMBER = "order_list_number";
    private static final String COLUMN_RECORED_MESSAGE = "record_message";
    private Context mContext;
    private String mDrawableImagePath = null;
    private static int _id = 0;
    private String audio = null;
    ;
    private int largest = 0;

    public SqliteDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // get image from drawable
        Bitmap bitMap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.download);

        String fileName = "download.jpg";

        try {

            FileOutputStream out1 = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);

            bitMap.compress(Bitmap.CompressFormat.JPEG, 100, out1);

            out1.flush();

            out1.close();

            File f = mContext.getFileStreamPath(fileName);

            mDrawableImagePath = f.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
        }
        String recordMessagePath = rootPath + "/test.mp3";

        String CREATE_CALLSES_TABLE = "CREATE TABLE " + TABLE_CALLESS + "(" + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_IMAGEPATH + " TEXT,"
                + COLUMN_PHONE + " TEXT," + COLUMN_PHONE_SHOW + " TEXT," + COLUMN_NAME + " TEXT," + COLUMN_ORDER_LIST_NUMBER + " INTEGER," + COLUMN_RECORED_MESSAGE + " TEXT" + ")";

        db.execSQL(CREATE_CALLSES_TABLE);

        // first time insert static value into database
        db.execSQL("INSERT INTO " + TABLE_CALLESS + " (" + COLUMN_IMAGEPATH + ", " + COLUMN_PHONE + ", " + COLUMN_PHONE_SHOW + ", " + COLUMN_NAME + "," + COLUMN_ORDER_LIST_NUMBER + "," + COLUMN_RECORED_MESSAGE + ")" +
                " Values('" + mDrawableImagePath + "','198','198', 'Customer Care','0',' " + recordMessagePath + "');");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALLESS);
        onCreate(db);
    }

    public List<FriendContactModel> listProducts() {
        String sql = "select * from " + TABLE_CALLESS + " ORDER BY " + COLUMN_ORDER_LIST_NUMBER + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        List<FriendContactModel> storeData = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(0));
                String imagePath = cursor.getString(1);
                String number = cursor.getString(2);
                String number_show = cursor.getString(3);
                String name = cursor.getString(4);
                String listOfOrder = cursor.getString(5);
                String recordAudio = cursor.getString(6);
                storeData.add(new FriendContactModel(id, imagePath, number, number_show, name, listOfOrder, recordAudio));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return storeData;
    }

    public void addFriendContact(FriendContactModel friendContactModel) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_IMAGEPATH, friendContactModel.getFriendImagePath());
        values.put(COLUMN_PHONE, friendContactModel.getFriendNumber());
        values.put(COLUMN_PHONE_SHOW, friendContactModel.getFriendNumberShow());
        values.put(COLUMN_NAME, friendContactModel.getFriendName());
        values.put(COLUMN_ORDER_LIST_NUMBER, friendContactModel.getFriendListOfOreder());
        values.put(COLUMN_RECORED_MESSAGE, friendContactModel.getFriendAudioMessage());
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_CALLESS, null, values);
    }

    public void deleteFriendContact() {
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.query(TABLE_CALLESS, new String[]{BaseColumns._ID}, null, null, null, null, null);
        if (cursor.moveToLast()) {
            _id = cursor.getInt(0);
            db.delete(TABLE_CALLESS, COLUMN_ID + " = ?", new String[]{String.valueOf(_id)});
        }
    }

    public int currentContactId() {
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.query(TABLE_CALLESS, new String[]{BaseColumns._ID}, null, null, null, null, null);
        if (cursor.moveToLast()) {
            _id = cursor.getInt(0);
        }
        return _id;
    }

    public void updateFriendContact(FriendContactModel friendContactModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.query(TABLE_CALLESS, new String[]{BaseColumns._ID}, null, null, null, null, null);
        if (cursor.moveToLast()) {
            _id = cursor.getInt(0);
            ContentValues values = new ContentValues();
            values.put(COLUMN_IMAGEPATH, friendContactModel.getFriendImagePath());
            values.put(COLUMN_PHONE, friendContactModel.getFriendNumber());
            values.put(COLUMN_PHONE_SHOW, friendContactModel.getFriendNumberShow());
            values.put(COLUMN_NAME, friendContactModel.getFriendName());
            values.put(COLUMN_ORDER_LIST_NUMBER, friendContactModel.getFriendListOfOreder());
            values.put(COLUMN_RECORED_MESSAGE, friendContactModel.getFriendAudioMessage());
            db.update(TABLE_CALLESS, values, COLUMN_ID + "    = ?", new String[]{String.valueOf(_id)});
        }
    }

    public void displayFriendContact() {
        String sql = "select * from " + TABLE_CALLESS;
        SQLiteDatabase db = this.getReadableDatabase();
        List<FriendContactModel> storeData = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToLast()) {
            int id = Integer.parseInt(cursor.getString(0));
            String imagePath = cursor.getString(1);
            String number = cursor.getString(2);
            String number_show = cursor.getString(3);
            String name = cursor.getString(4);
            String listOfOrder = cursor.getString(5);
            String recordAudio = cursor.getString(6);
            FriendContactModel friendContactModel = new FriendContactModel(id, imagePath, number, number_show, name, listOfOrder, recordAudio);
            storeData.add(friendContactModel);
            CallessUtils.setSelectedContactDetails(storeData);
        }
        cursor.close();

    }

    public void displayFriendContactOrderNumber() {
        String sql = "select * from " + TABLE_CALLESS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        List<String> storeData = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                String listOfOrder = cursor.getString(5);
                storeData.add(listOfOrder);
                CallessUtils.setDisplayContactDetails(storeData);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    public Boolean displayAllFriendContact() {
        String sql = "select * from " + TABLE_CALLESS + " where " + COLUMN_PHONE +  " = '" + sIncomingContactNumber + "' ";

        SQLiteDatabase db = this.getReadableDatabase();
        List<FriendContactModel> storeData = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(0));
                String imagePath = cursor.getString(1);
                String number = cursor.getString(2);
                String number_show = cursor.getString(3);
                String name = cursor.getString(4);
                String listOfOrder = cursor.getString(5);
                String recordAudio = cursor.getString(6);
                storeData.add(new FriendContactModel(id, imagePath, number, number_show, name, listOfOrder, recordAudio));
                CallessUtils.setAllContactDetails(storeData);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return storeData.size() != 0;
    }

    public String getRingtoneFromDatabase() {
        String sql = "select * from " + TABLE_CALLESS + " where " + COLUMN_PHONE +  " = '" + sIncomingContactNumber + "' ";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                audio = cursor.getString(6);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return audio;
    }

    public int getLargestListOfOrder() {
        String sql = "SELECT * FROM " + TABLE_CALLESS + " ORDER BY " + COLUMN_ORDER_LIST_NUMBER + " DESC LIMIT 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                largest = cursor.getInt(5);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return largest;
    }

    public boolean isExist(String phone) {
        String sql = "SELECT * FROM " + TABLE_CALLESS + " WHERE " + COLUMN_PHONE + " = '" + phone + "' ";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        boolean exist = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exist;
    }

    public int idFromDataBase(String phone) {
        int id = 0;
        String sql = "SELECT * FROM " + TABLE_CALLESS + " WHERE " + COLUMN_PHONE + " = '" + phone + "' ";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(0);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return id;
    }

    public String nameFromDataBase(String phone) {
        String name = null;
        CallessUtils.v("Phone Check :- " + phone);
        String sql = "SELECT * FROM " + TABLE_CALLESS + " WHERE " + COLUMN_PHONE + " = '" + phone + "' ";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                name = cursor.getString(4);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return name;
    }

    public void deleteFriendContactFromMain(int id) {
        String sql = "DELETE FROM " + TABLE_CALLESS + " WHERE " + COLUMN_ID + " = " + id;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(sql);
    }

    public void updateFriendContactFromMain(FriendContactModel friendContactModel, int contact_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.query(TABLE_CALLESS, new String[]{BaseColumns._ID}, null, null, null, null, null);
        if (cursor.moveToLast()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_IMAGEPATH, friendContactModel.getFriendImagePath());
            values.put(COLUMN_PHONE, friendContactModel.getFriendNumber());
            values.put(COLUMN_PHONE_SHOW, friendContactModel.getFriendNumberShow());
            values.put(COLUMN_NAME, friendContactModel.getFriendName());
            values.put(COLUMN_ORDER_LIST_NUMBER, friendContactModel.getFriendListOfOreder());
            values.put(COLUMN_RECORED_MESSAGE, friendContactModel.getFriendAudioMessage());
            db.update(TABLE_CALLESS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(contact_id)});
        }
    }

    public void currentRecord(int id) {
        String sql = "SELECT * FROM " + TABLE_CALLESS + " WHERE " + COLUMN_ID + " = " + id;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        List<FriendContactModel> storeData = new ArrayList<>();

        if (cursor.moveToFirst()) {
            String imagePath = cursor.getString(1);
            String number = cursor.getString(2);
            String number_show = cursor.getString(3);
            String name = cursor.getString(4);
            String listOfOrder = cursor.getString(5);
            String recordAudio = cursor.getString(6);
            storeData.add(new FriendContactModel(id, imagePath, number, number_show, name, listOfOrder, recordAudio));
            CallessUtils.setSingleFriendContact(storeData);
        }
        cursor.close();
    }
}
