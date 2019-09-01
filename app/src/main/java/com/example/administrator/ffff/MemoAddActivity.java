package com.example.administrator.ffff;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.test.LoaderTestCase;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.helpers.LocatorImpl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2016-08-23.
 * edit 2016-10-03
 * 메모수정시 팝업도 자동 수정 추가
 */
public class MemoAddActivity extends Activity {
    public static String strDateChangeValue = "";               //정렬용 Date
    public static String strDateChangeValueView = "";         //보여주기용 (리스트뷰 보여주기) Date
    public static String strDateCreateValueView = "";         //생성된 Date
    public static long longDateChangeValueView = 0;

    private EditText mTitleText;
    private String mBeforeSaveTitleText = ""; //수정된거 확인 여부 String
    private EditText mBodyText;
    private String mBeforeSaveBodyText = ""; //수정된거 확인 여부 String
    private TextView mDateText;
    private Long mRowId;

    private ImageButton btnSave;

    private Cursor note;

    private DBAdapter mDbHelper;

    private static final int MODE_VIEW = 0;
    private static final int MODE_EDIT = 1;
    private int nMode = MODE_VIEW;
    private String preText=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //DB오픈
        mDbHelper = new DBAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.memo_add_activity);
        setTitle(R.string.app_name);

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        mDateText = (TextView) findViewById(R.id.notelist_date);
        btnSave = (ImageButton) findViewById(R.id.btn_Memo_Save);

        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(DBAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();

            //위젯에서 눌린거라면 if에 해당
            if(getIntent().getData() != null) {
                //uri을 받아옴
                Uri imageUri = getIntent().getData();
                mRowId = Long.valueOf(imageUri.toString());
                //RowID에 해당위젯이 눌린 ID값을 저장함.
            } else { /* 위젯에서 누른게 아니라면*/
                mRowId = extras != null ? extras.getLong(DBAdapter.KEY_ROWID)
                        : null;
            }

            //메모가 새로 만든다면
            if( mRowId == null) {   //RowID가없다는 뜻은 새로운 메모라는뜻
                nMode = MODE_EDIT;
                setNewDate();   //메모가 새로 만들어지니까 현재 날짜 받아오기
                mBodyText.requestFocus();   //새로운 메모라면 바로 포커스가 몸체 메모로 잡힌다.

                //생성된날짜 저장
                strDateCreateValueView = getDateFormat("y'년'M'월'd'일' H':'m");
            }
            else { //만약 새로운 메모가 아니라면
                setOpenDate();  //기존의 메모를 수정하는거기 때문에 날짜 불러오기
            }
        }
        populateFields();



        mBodyText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                return gestureDetector.onTouchEvent(motionEvent);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveState();
                finish();
            }
        });
    }

    //제스쳐 이벤트
    final GestureDetector gestureDetector = new GestureDetector(
            new GestureDetector.SimpleOnGestureListener() {
                //더블탭 눌렸을때 제스쳐 처리
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (nMode == MODE_VIEW) {
                        Toast.makeText(getApplicationContext(),
                                R.string.memo_change_mode, Toast.LENGTH_SHORT).show();
                        mTitleText.setFocusable(true);
                        mBodyText.setFocusableInTouchMode(true);    //터치 가능하게 하는 모드
                        mTitleText.setFocusableInTouchMode(true);
                        mBodyText.setFocusable(true);
                        mBodyText.requestFocus();   //포커스가 몸체 메모로 잡힌다.
                    }
                    nMode = MODE_EDIT;
                    return super.onDoubleTap(e);
                }
            });
    private String getDateFormat(String str) {
        String Date;

        long msTime = System.currentTimeMillis();
        java.util.Date curDateTime = new Date(msTime);

        SimpleDateFormat formatter = new SimpleDateFormat(str);
        Date = formatter.format(curDateTime);

        return Date;
    }

    public static class LineEditText extends EditText{
        // we need this constructor for LayoutInflater
        public LineEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(DBAdapter.KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.memomenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.memo_cancel:
                if(note != null){
                    note.close();
                    note = null;
                }
                if(mRowId != null){
                    mDbHelper.deleteNote(mRowId);
                }
                finish();

                return true;
            case R.id.memo_save:
                saveState();
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveState() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();

        //입력 없으면 노트 생성 X
        if( title.equals("") && body.equals(""))
            return;

        //수정된게 없으면 X
        if( mBeforeSaveBodyText.equals(mBodyText.getText().toString()) &&
                mBeforeSaveTitleText.equals(mTitleText.getText().toString())) {
            return;
        }

        //수정된게 있으니까
        //날짜를 세팅 다시해줌
        setNewDate();

        //제목 설정 안하면 내용을 제목으로 설정.
        if( title.equals("")) {
            title = body;
            mTitleText.setText(mBodyText.getText());
        }

        //노트 생성
        if(mRowId == null){
            long id = mDbHelper.createNote(title, body, strDateChangeValueView, longDateChangeValueView,
                    strDateCreateValueView);
            if(id > 0){
                mRowId = id;
            }else{
                Log.e("saveState","노트 생성실패");
            }
        }else{
            if(!mDbHelper.updateNote(mRowId, title, body, strDateChangeValueView, longDateChangeValueView,
                    strDateCreateValueView)){
                Log.e("saveState","노트 생성실패");
            }
        }

        if( nMode == MODE_EDIT) {
            //메모 저장 토스트
            Toast.makeText(getApplicationContext(),
                    R.string.memo_save, Toast.LENGTH_SHORT).show();
        }

        if( MainActivity.popupData.checkPop == true &&
                MainActivity.popupData.nCurrentRowID == mRowId) {
            //현재 메모 수정하는게 팝업 켜져있을시 자동으로 팝업 내용도 수정 16.11. 02
            MainActivity.popupData.text = mBodyText.getText().toString();
            MainActivity.popupData.putSharedPreference(getApplicationContext());
            Intent intent = new Intent(getApplicationContext(), AlwaysOnTopView.class);
            intent.putExtra("Data", MainActivity.popupData);
            startService(intent);
        }


        //위젯 수정
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] nWidgetIDs = appWidgetManager.getAppWidgetIds(new ComponentName(this, WidgetProvider.class));
        //수정 전 메모와 위젯의 메모가 같은 위젯을 탐색 및 수정
        for(int i = 0; i<nWidgetIDs.length; i++){
            if(prefs.getString("wid"+nWidgetIDs[i],"").equals(preText)) {
                RemoteViews remoteView = new RemoteViews(this.getPackageName(),
                        R.layout.widget);
                remoteView.setTextViewText(R.id.widtext, mBodyText.getText().toString());

                appWidgetManager.updateAppWidget(nWidgetIDs[i],remoteView);
            }
        }
    }


    private void populateFields() {
        if (mRowId != null) {
            note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            mTitleText.setText(note.getString(
                    note.getColumnIndexOrThrow(DBAdapter.KEY_TITLE)));
            mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(DBAdapter.KEY_BODY)));
            strDateCreateValueView = (note.getString(
                    note.getColumnIndexOrThrow(DBAdapter.KEY_CREATE_DATE)));

            //위젯 변경을 위한 비교값
            preText = new String();
            preText = mBodyText.getText().toString();

            //만약 안에 내용이 있으면 비활성화
            if( mBodyText.getText().toString() != "" || mTitleText.getText().toString() != "") {
                mTitleText.setFocusable(false);
                mBodyText.setFocusable(false);
            }

            mBeforeSaveBodyText = mBodyText.getText().toString();
            mBeforeSaveTitleText = mTitleText.getText().toString();
        }
    }

    //새로운 메모 생성할때 현재 날짜 세팅
    private void setNewDate() {
        strDateChangeValue = getDateFormat("yyMMddHHmm");    //Long을 저장하기 위해 불러오기
        strDateChangeValueView = getDateFormat("y'.'M'.'d a h':'m");    //뷰에 보여주기 위한

        //정렬을 위한 날짜를 int로 변형합니다. (스트링은 정렬이어려우니까)
        longDateChangeValueView = Long.parseLong(strDateChangeValue);

        mDateText.setText(""+strDateChangeValueView);
    }

    //기존의 메모를 불러올때 저장된 날짜 세팅
    private void setOpenDate() {
        if (mRowId != null) {
            note = mDbHelper.fetchNote(mRowId);

            strDateChangeValueView = note.getString(
                    note.getColumnIndexOrThrow(DBAdapter.KEY_CHANGED_DATE));

            //정렬을 위한 날짜를 int로 변형합니다. (스트링은 정렬이어려우니까)
            longDateChangeValueView = note.getLong(
                    note.getColumnIndexOrThrow(DBAdapter.KEY_CHANGED_DATE_VALUE));

            mDateText.setText(""+strDateChangeValueView);
        }
    }
}
