package com.example.administrator.ffff;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*
    데이터 베이스 관리
 */
public class DBAdapter {

    public static final String KEY_TITLE = "title";
    public static final String KEY_CHANGED_DATE = "date_changed";   //수정된 보여주기 날짜 ex)2016. 9. 5 7:56
    public static final String KEY_BODY = "body";
    public static final String KEY_CHANGED_DATE_VALUE = "time_changed_value";   //수정된 정렬용 날짜  ex)201695756
    public static final String KEY_CREATE_DATE = "date_create";   //생성된 보여주기 날짜  ex)2016. 9. 5 7:56
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "notes";
    private static final int DATABASE_VERSION = 2;

    public static final String ASC = "ASC";         //오름차순
    public static final String DESC = "DESC";       //내림차순

    //디비 생성 Create
    private static final String DATABASE_CREATE =
            "create table " + DATABASE_TABLE + "("
                    + KEY_ROWID + " integer primary key autoincrement, "
                    + KEY_TITLE + " text not null, "
                    + KEY_BODY + " text not null, "
                    + KEY_CHANGED_DATE + " text not null, "
                    + KEY_CHANGED_DATE_VALUE + " long not null, "
                    + KEY_CREATE_DATE +  " text not null"  + ");";



    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "업그레이드 " + oldVersion + " to "
                    + newVersion + ", 삭제하고 새롭게");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }


    public DBAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public DBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    //Title = 노트 제목
    //Body = 노트 몸체
    public long createNote(String title, String body, String date, Long time, String date_create) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_BODY, body);
        initialValues.put(KEY_CHANGED_DATE, date);
        initialValues.put(KEY_CHANGED_DATE_VALUE, time);
        initialValues.put(KEY_CREATE_DATE, date_create);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }


    //노트 삭제
    public boolean deleteNote(long rowId) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    //모든 노트 삭제 //삭제시 유의사항
    //바로 삭제되오니 AlterDIalog를 사용해서 삭제할건지
    //물어보는게 예의.
    public boolean deleteAllNote() {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID, null) > 0;
    }

    //모든 노트 보여주기
    public Cursor fetchAllNotes(String key, String str) {
        //첫번째 인자값은 어떤것을 정렬할것인가
        //두번째 인자값은 내림차순인가 오름차순인가
        //내림차순인데 KEY_TIME값이 제일 최근이 위로감
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                        KEY_BODY, KEY_CHANGED_DATE, KEY_CHANGED_DATE_VALUE, KEY_CREATE_DATE}, null, null, null,
                null, key + " " + str, null);    //ASC
        // Order by (내림차순 정렬기능)
    }


    //한 노트만 보여주기
    public Cursor fetchNote(long rowId) throws SQLException {
        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                                KEY_TITLE, KEY_BODY,KEY_CHANGED_DATE, KEY_CHANGED_DATE_VALUE
                                ,KEY_CREATE_DATE}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    //노트 업데이트
    public boolean updateNote(long rowId, String title, String body,String date,
                              Long time, String date_create) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);
        args.put(KEY_CHANGED_DATE, date);
        args.put(KEY_CHANGED_DATE_VALUE, time);
        args.put(KEY_CREATE_DATE, date_create);
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }


    //노트검색
    public Cursor searchNote (String strKeyTitle) {
        //메모 내용까지 검색합니다.
        Cursor mCursor = mDb.query(DATABASE_TABLE,
                new String[] {KEY_ROWID,
                        KEY_TITLE, KEY_BODY, KEY_CHANGED_DATE, KEY_CREATE_DATE},
                KEY_TITLE  + " like ? " + " or " + KEY_BODY + " like? ",
                new String[]{"%" + strKeyTitle + "%", "%" + strKeyTitle + "%"}, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

}
