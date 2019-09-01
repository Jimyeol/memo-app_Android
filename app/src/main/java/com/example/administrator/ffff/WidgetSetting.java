package com.example.administrator.ffff;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;

/**
 * Created by soomi on 2016-10-03.
 */

public class WidgetSetting extends Activity {


    //메뉴 선택 코드  //옵션/정렬
    private static final int MENU_TIME_SORT=0;   //시간순으로 정렬
    private static final int MENU_CRT_ALL=1;    //생성순으로 정렬
    private static final int MENU_TLT_SORT=2;    //제목순으로 정렬
    private static final int MENU_BDY_SORT=3;    //내용순으로 정렬

    public static int sort_option = MENU_CRT_ALL;
    private static final String KEY_MY_PREFERENCE = "option";
    private static final String KEY_OPTION_SORT = "sort";

    //데이터 베이스 어답터
    static public DBAdapter mDbHelper;

    //리스트뷰 생성, 어댑터
    private ListView w_slt_listview ;
    private int mAppWidgetId=0;

    //위젯 정보 가져오는 번들
    private Bundle mExtras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_setting);

        //해당 위젯의 아이디를 받아옴
        mExtras = getIntent().getExtras();
        if (mExtras != null) {
            mAppWidgetId = mExtras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        //DB오픈
        mDbHelper = new DBAdapter (this);
        mDbHelper.open();

        //리스트뷰
        w_slt_listview = (ListView) findViewById(R.id.w_list_memo);
        setOptionSort(getOptionSort()); //정렬 저장되어있는거 불러오기
        fillData(sort_option);

        //클릭시 메모 수정
        w_slt_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                getWidget(position);
            }
        }) ;
    }

    //아이템 클릭시 해당 메모를 위젯에 노출
    private void getWidget(int position){
        Intent resultValue = new Intent();

        //앱위젯을 수정하기위한 뷰값을 가져와 텍스트 설정
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        RemoteViews remoteView = new RemoteViews(this.getPackageName(),
                R.layout.widget);
        remoteView.setTextViewText(R.id.widtext, getDBString(position, DBAdapter.KEY_BODY));

        //앱위젯 업데이트를 위한 프레퍼런스
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("wid"+mAppWidgetId, getDBString(position, DBAdapter.KEY_BODY));
        editor.putInt("wid_key"+mAppWidgetId, Integer.valueOf(getDBString(position, DBAdapter.KEY_ROWID)));
        editor.commit();

        //앱 위젯에 클릭 이벤트 추가
        Intent intent = new Intent(getApplicationContext(),
                MemoAddActivity.class);
        //setData로 현재 위젯의 ROWID를 intent에 저장한다. 클릭시 해당 메모로 이동
        intent.setData( Uri.parse(getDBString(position, DBAdapter.KEY_ROWID)));
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                intent, 0);
        remoteView.setOnClickPendingIntent(R.id.widtext, pendingIntent);

        //위젯 업데이트
        appWidgetManager.updateAppWidget(mAppWidgetId, remoteView);

        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    //데이터 베이스에서 유저가 선택한 노트의 String을 리턴해준다 ㅋㅋ
    private String getDBString(int position, String selectKey) {
        //모든 노트 검색해서
        Cursor notesCursor = getSortOption();
        startManagingCursor(notesCursor);
        //포지션에 맞는거 찾으면
        notesCursor.moveToPosition(position);

        String labelColumn_body = notesCursor.getString(notesCursor.
                getColumnIndex(selectKey));
        //그거 리턴
        return labelColumn_body;
    }

    //데이터 채우기 (모든 보여주기)
    private void fillData(int n) {
        Cursor notesCursor =  getSortOption();
        // Get all of the notes from the database and create the item list
        startManagingCursor(notesCursor);


        String[] from = new String[] { DBAdapter.KEY_TITLE , DBAdapter.KEY_BODY, DBAdapter.KEY_CHANGED_DATE};
        int[] to = new int[] { R.id.textView1,R.id.textbody,R.id.date_row};

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.note_row, notesCursor, from, to);
        w_slt_listview.setAdapter(notes);
    }
    //소트 옵션 세팅
    public void setOptionSort(int c) {
        sort_option = c;
        optionFileWrite(KEY_OPTION_SORT, sort_option);
    }
    public int getOptionSort() {
        return optionFileRead(KEY_OPTION_SORT);
    }

    private void optionFileWrite(String kindOption, int value) {
        //파일저장
        SharedPreferences prefs = getSharedPreferences(kindOption, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_MY_PREFERENCE, value);
        editor.commit();
    }

    public int optionFileRead(String kindOption) {
        //파일읽어오기
        SharedPreferences prefs = getSharedPreferences(kindOption, MODE_PRIVATE);
        int value = prefs.getInt(KEY_MY_PREFERENCE, 0);
        return value;
    }

    //정렬에 따라 Cursor를 반환해준다
    private Cursor getSortOption(){
        switch(sort_option) {
            case MENU_CRT_ALL:
                return mDbHelper.fetchAllNotes(mDbHelper.KEY_CHANGED_DATE_VALUE, "");
            case MENU_TIME_SORT:
                return mDbHelper.fetchAllNotes(mDbHelper.KEY_CHANGED_DATE_VALUE, mDbHelper.DESC);
            case MENU_TLT_SORT:
                return mDbHelper.fetchAllNotes(mDbHelper.KEY_TITLE, mDbHelper.ASC);
            case MENU_BDY_SORT:
                return mDbHelper.fetchAllNotes(mDbHelper.KEY_BODY, mDbHelper.ASC);
            default:
                return mDbHelper.fetchAllNotes(mDbHelper.KEY_CHANGED_DATE_VALUE, "");
        }
    }
}
