package com.example.administrator.ffff;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-08-24.
 */
public class OptionActivity extends Activity {


    //옵션 메뉴
    private static final int OPTION_TRANSPARENT = 0;    //투명도 조절
    private static final int OPTION_DELETEALL = 1;      //전체삭제

    //환경변수
    private static final String KEY_MY_PREFERENCE = "option";
    private static final String KEY_OPTION_TRANPARENT = "Transparent";
    private static final String KEY_OPTION_POPUP = "PopupOn";

    //매크로 팝업 온오프
    private static final int OFF = 0;
    private static final int ON = 1;

    private ListView lvOption;
    private optionAdapter mAdapter = null;
    private Intent intent;


    /*
            옵션
     */
    //투명도 조절
    private SeekBar seekBar;
    private int GetTransparentProgress = 0;
    //팝업 키고끄기
    private Switch swPopupOn;
    private int bPopupOn = OFF;
    //팝업 색상 선택
    private RadioGroup rg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        //인텐트
        intent = new Intent(getApplicationContext(),AlwaysOnTopView.class);

        //팝업기능 불러오기
        bPopupOn = optionFileRead(KEY_OPTION_POPUP);

        //팝업 스위치
        swPopupOn = (Switch)findViewById(R.id.swPopupOn);

        //팝업 색상 선택
        //팝업 색상 선택시 팝업이 켜져야만 Layout이 나옵니다.
        rg = (RadioGroup) findViewById(R.id.radioColor);
        rg.check(MainActivity.popupData.memoColor);
        // 메모의 색상을 변경하기위한 리스너
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Toast.makeText(getApplicationContext(), "휴.. 당신의 추억들이 사라질뻔했어요.",
                        Toast.LENGTH_SHORT).show();
                MainActivity.popupData.memoColor = checkedId;
                MainActivity.popupData.putSharedPreference(getApplicationContext());
                intent.putExtra("Data", MainActivity.popupData);
                startService(intent);
            }
        });

        //팝업 설정 스위치 연동
        if( bPopupOn == ON ) {
            swPopupOn.setChecked(true);
            PopupSetting( true );

        } else {
            swPopupOn.setChecked(false);
            PopupSetting(false);
        }
        swPopupOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PopupSetting(b);
            }
        });


        //옵션생성
        lvOption = (ListView) findViewById(R.id.lvOption);
        mAdapter = new optionAdapter(this);
        lvOption.setAdapter(mAdapter);

        mAdapter.addItem("투명도");
        mAdapter.addItem("전체 삭제");
        mAdapter.notifyDataSetChanged();

        //옵션 리스너
        lvOption.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //optionAdapter.ListData mData = mAdapter.mListData.get(position);
                switch (position) {
                    case OPTION_TRANSPARENT:
                        GetTransparentProgress = optionFileRead(KEY_OPTION_TRANPARENT);
                        TransparentOptionValue();
                        break;
                    case OPTION_DELETEALL:
                        AllDeleteAlertDialog();
                        break;

                }
            }
        });


    }

    //전체 모든 노트 삭제 AlertDialog
    private void AllDeleteAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setTitle(R.string.option_delete_all_title)
                .setMessage(R.string.option_delete_all_message)
                .setCancelable(false)
                .setPositiveButton(R.string.option_delete_all_yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MainActivity.mDbHelper.deleteAllNote();
                            }
                        })
                .setNegativeButton(R.string.option_delete_all_no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getApplicationContext(), R.string.option_delete_all_no_message,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
        builder.show();
    }



    //팝업 ON/OFF 세팅 함수
    private void PopupSetting(boolean b ) {
        MainActivity.popupData.checkPop = b;
        MainActivity.checkOpenPopup = b;
        MainActivity.popupData.putSharedPreference(getApplicationContext());
        if( b ){
            bPopupOn = ON;
            intent.putExtra("Data",MainActivity.popupData);
            //색상 Layout 나타나게함
            rg.setVisibility(View.VISIBLE);
            startService(intent);
        } else {
            bPopupOn = OFF;
            //색상 Layout 나타나게함
            rg.setVisibility(View.GONE);
            stopService(intent);
        }
        optionFileWrite(KEY_OPTION_POPUP, bPopupOn);
    }

    private void TransparentOptionValue() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewInDialog = inflater.inflate(
                R.layout.transparent_dlg, null);

        final AlertDialog DlgTransparentSetting = new AlertDialog.Builder(
                this).setView(viewInDialog).create();
        DlgTransparentSetting.getWindow().
                setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        DlgTransparentSetting.setTitle(R.string.option_transparent);

        seekBar = (SeekBar) viewInDialog.
                findViewById(R.id.seekBar_transparent_set);

        Button btnOk = (Button) viewInDialog.
                findViewById(R.id.btnOk);

        seekBar.setMax(100);
        seekBar.setProgress(GetTransparentProgress);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                GetTransparentProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.popupData.clearValue = GetTransparentProgress;
                MainActivity.popupData.putSharedPreference(getApplicationContext());
                if(MainActivity.checkOpenPopup==true) {
                    intent.putExtra("Data",MainActivity.popupData);
                    startService(intent);
                }
                DlgTransparentSetting.dismiss();
            }
        });

        DlgTransparentSetting.show();

        DlgTransparentSetting.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                optionFileWrite(KEY_OPTION_TRANPARENT, GetTransparentProgress);
            }
        });
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


    //Option Adapter
    public class optionAdapter extends BaseAdapter {
        private Context mContext = null;
        public ArrayList<ListData> mListData = new ArrayList<ListData>();

        public optionAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void fixItem(int position, String name) {
            mListData.get(position).mSubject = name;
        }

        public void addItem(String name) {
            ListData addInfo;
            addInfo = new ListData();
            addInfo.mSubject = name;

            mListData.add(addInfo);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View roeView = inflater.inflate(R.layout.option_item, null, true);
            TextView mSubject = (TextView) roeView.findViewById(R.id.textOptionName);

            ListData mData = mListData.get(position);

            mSubject.setText(mData.mSubject);
            mSubject.setTextSize(30);
            mSubject.setPadding(0, 50, 0, 50);

            return roeView;
        }

        public class ListData {
            public String mSubject;
        }
    }

}


