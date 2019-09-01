package com.example.administrator.ffff;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //퍼미션 코드
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    //팝업
    static public PopupData popupData=new PopupData();
    public Intent intent;
    public int check_position=-1;
    static boolean checkOpenPopup = false;

    //옵션 코드
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    private static final int ACTIVITY_OPTION = 2;

    //메뉴 선택 코드  //옵션/정렬
    private static final int MENU_TIME_SORT=0;   //시간순으로 정렬
    private static final int MENU_CRT_ALL=1;    //생성순으로 정렬
    private static final int MENU_TLT_SORT=2;    //제목순으로 정렬
    private static final int MENU_BDY_SORT=3;    //내용순으로 정렬

    public static int sort_option = MENU_CRT_ALL;
    private static final String KEY_MY_PREFERENCE = "option";
    private static final String KEY_OPTION_SORT = "sort";


    //콘텍스트 메뉴 코드
    private static final int DELETE_ID = Menu.FIRST;
    private static final int SELECT_ID = 2;
    private static final int SHARE_ID = 3;
    private static final int INFOR_ID = 4;


    //데이터 베이스 어답터
    static public DBAdapter mDbHelper;

    //타이틀바 서치뷰
    private MaterialSearchView searchView;
    public static boolean bSearchMode = false;

    //리스트뷰 생성, 어댑터
    private ListView listview ;
    private MyAdapter adapter;


    //안드로이드 6.0이후 권한획득을 위한 함수
    @SuppressLint("NewApi")
    public void someMethod() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Settings.ACTION_MANAGE_OVERLAY_PERMISSION);

        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            }
        }
        else {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent(getApplicationContext(),AlwaysOnTopView.class);

        //안드로이드 6.0이상에서 권한 획득 요청청
        if( Build.VERSION.SDK_INT>=23) someMethod();

        //메모 설정값을 불러옴
        popupData.getSharedPreference(getApplicationContext());

        //DB오픈
        mDbHelper = new DBAdapter (this);
        mDbHelper.open();

        //툴바
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //리스트뷰, 어댑터
        adapter = new MyAdapter() ;
        listview = (ListView) findViewById(R.id.Notelist);
        setOptionSort(getOptionSort()); //정렬 저장되어있는거 불러오기
        fillData(sort_option);

        //컨텍스트 메뉴 사용
        registerForContextMenu(listview);

        //클릭시 메모 수정
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                //수정하기
                Intent i = new Intent(MainActivity.this, MemoAddActivity.class);
                i.putExtra(DBAdapter.KEY_ROWID, id);

                startActivityForResult(i, ACTIVITY_EDIT);
            }
        }) ;


        //타이틀바 서치
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setVoiceSearch(true);
        searchView.setCursorDrawable(R.drawable.color_cursor_white);
        //searchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if( newText.equals(""))
                    fillData(sort_option);

                CharSequence charQuery = newText;
                if (charQuery != null && TextUtils.getTrimmedLength(charQuery) > 0) {
                    doSearch(newText);
                    MainActivity.bSearchMode = true;
                }
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                fillData(sort_option);
            }

            @Override
            public void onSearchViewClosed() {
            }
        });


        //플로팅액션버튼 메모추가
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNote();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //설정시 무엇눌렀는지 팝업메뉴 위에 TITLE띄어줌
        AdapterView.AdapterContextMenuInfo info
                = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(getDBString(info, DBAdapter.KEY_TITLE));
        menu.add(0, DELETE_ID, 0, R.string.contextmenu_Delete);
        menu.add(0, SELECT_ID, 0, R.string.contextmenu_Selecet);
        menu.add(0, SHARE_ID, 0, R.string.contextmenu_Share);
        menu.add(0, INFOR_ID, 0, R.string.contextmenu_Information);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info
                = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case DELETE_ID:
                mDbHelper.deleteNote(info.id);
                fillData(sort_option);
                return true;
            case SELECT_ID:
                //팝업 바로 켜기
                popupData.checkPop = true;
                checkOpenPopup = true;
                popupData.text = getDBString(info, DBAdapter.KEY_BODY);
                if (!popupData.text.equals("")) {
                    popupData.checkPop = true;
                    popupData.nCurrentRowID = getDBRowID(info, DBAdapter.KEY_ROWID);
                    popupData.putSharedPreference(getApplicationContext());
                    intent.putExtra("Data", popupData);
                    startService(intent);
                    check_position = info.position;
                    SharedPreferences cp = getSharedPreferences("check_position", MODE_PRIVATE);
                    SharedPreferences.Editor editor = cp.edit();
                    editor.putInt("check_position", check_position);
                    editor.commit();
                }
                break;
            case SHARE_ID:
                String str = "";
                Intent msg = new Intent(Intent.ACTION_SEND);
                msg.addCategory(Intent.CATEGORY_DEFAULT);
                str = "<--" + getResources().getString(R.string.app_name) + "-->\n"
                        + "[" + getResources().getString(R.string.memo_share_title) +
                        getDBString(info, DBAdapter.KEY_TITLE) + "]" + "\n" +
                        getResources().getString(R.string.memo_share_body) + getDBString(info, DBAdapter.KEY_BODY)
                        + "\n\n" + "[" + getResources().getString(R.string.memo_share_time) +
                        getDBString(info, DBAdapter.KEY_CHANGED_DATE) + "]";
                msg.putExtra(Intent.EXTRA_TEXT, str);
                msg.setType("text/plain");
                startActivity(Intent.createChooser(msg, "공유하기"));
                break;

            case INFOR_ID:
                memoInformationDialog(getDBString(info, DBAdapter.KEY_BODY),
                        getDBString(info, DBAdapter.KEY_CREATE_DATE));
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void memoInformationDialog(String strBody, String strDate) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        dialog.setTitle(R.string.contextmenu_Information);

        dialog.setMessage("길이 : " + Integer.
                valueOf(strBody.length()).toString() +
                "\n생성된 날짜 : " + strDate);
        dialog.show();
    }


    //데이터 베이스에서 유저가 선택한 노트의 String을 리턴해준다 ㅋㅋ
    private String getDBString(AdapterView.AdapterContextMenuInfo info, String selectKey) {
        //모든 노트 검색해서
        Cursor notesCursor = getSortOption();
        startManagingCursor(notesCursor);
        //포지션에 맞는거 찾으면
        notesCursor.moveToPosition(info.position);

        String labelColumn_body = notesCursor.getString(notesCursor.
                getColumnIndex(selectKey));
        //그거 리턴
        return labelColumn_body;
    }

    //데이터 베이스에서 유저가 선택한 노트의 RowID값을 리턴해준다
    private int getDBRowID(AdapterView.AdapterContextMenuInfo info, String selectKey) {
        //모든 노트 검색해서
        Cursor notesCursor = getSortOption();
        startManagingCursor(notesCursor);
        //포지션에 맞는거 찾으면
        notesCursor.moveToPosition(info.position);

        int labelColumn_id = notesCursor.getInt(notesCursor.
                getColumnIndex(selectKey));
        //그거 리턴
        return labelColumn_id;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()) {
            //메뉴 - 정렬
            case R.id.menu_sort:
                sortDialog();
                break;
            //메뉴 - 옵션
            case R.id.menu_option:
                //Intent i = new Intent(MainActivity.this, OptionActivity.class);
                // startActivityForResult(i, ACTIVITY_OPTION);
                Intent i = new Intent(this, PreferenceSetting.class);
                startActivity(i);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //검색된 상황에서 뒤로가기 누르면
        //앱이 꺼지는게 아니라 데이터 보여주기
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
            fillData(sort_option);
        } else {
            if( MainActivity.bSearchMode) {
                MainActivity.bSearchMode = false;
                fillData(sort_option);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        fillData(sort_option);
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void createNote() {
        //노트 생성 인텐트
        Intent i = new Intent(this, MemoAddActivity.class);
        startActivityForResult(i, ACTIVITY_CREATE);
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
        listview.setAdapter(notes);
    }

    //검색전용
    private void doSearch(String search) {

        //mDbHelper.searchEditions();
        Cursor notesCursor = mDbHelper.searchNote(search);

        startManagingCursor(notesCursor);

        String[] from = new String[] { DBAdapter.KEY_TITLE , DBAdapter.KEY_BODY, DBAdapter.KEY_CHANGED_DATE};
        int[] to = new int[] { R.id.textView1,R.id.textbody,R.id.date_row};

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.note_row, notesCursor, from, to);
        listview.setAdapter(notes);
    }

    private void sortDialog(){
        //        MENU_TIME_SORT=0;   //시간순으로 정렬
        //        MENU_CRT_ALL=1;    //생성순으로 정렬
        //        MENU_TLT_SORT=2;    //제목순으로 정렬
        //        MENU_BDY_SORT=3;    //내용순으로 정렬
        final String[] sort = getResources().getStringArray(R.array.title_bar_menu_sort);
        AlertDialog.Builder sortDialog =
                new AlertDialog.Builder(MainActivity.this);
        sortDialog.setTitle(R.string.title_bar_menu_sort);

        sortDialog.setItems(sort, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case MENU_TIME_SORT:
                        Toast.makeText(getApplicationContext(),
                                R.string.title_bar_menu_sort_time, Toast.LENGTH_SHORT).show();
                        setOptionSort(MENU_TIME_SORT);
                        fillData(MENU_TIME_SORT);
                        break;
                    case MENU_CRT_ALL:
                        Toast.makeText(getApplicationContext(),
                                R.string.title_bar_menu_sort_create, Toast.LENGTH_SHORT).show();
                        setOptionSort(MENU_CRT_ALL);
                        fillData(MENU_CRT_ALL);
                        break;
                    case MENU_TLT_SORT:
                        Toast.makeText(getApplicationContext(),
                                R.string.title_bar_menu_sort_title, Toast.LENGTH_SHORT).show();
                        setOptionSort(MENU_TLT_SORT);
                        fillData(MENU_TLT_SORT);
                        break;
                    case MENU_BDY_SORT:
                        Toast.makeText(getApplicationContext(),
                                R.string.title_bar_menu_sort_body, Toast.LENGTH_SHORT).show();
                        setOptionSort(MENU_BDY_SORT);
                        fillData(MENU_BDY_SORT);
                        break;
                }
            }});

        sortDialog.setNegativeButton(R.string.menu_cancel, null);

        sortDialog.show();
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
