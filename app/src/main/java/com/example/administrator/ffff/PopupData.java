package com.example.administrator.ffff;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.Serializable;

/**
 * Created by soomi on 2016-05-12.
 * 팝업 뷰의 설정값 텍스트값 디데이계산에 필요한 값을 가지고 있는 클래스
 * * edit 2016-09-14
 * 텍스트 사이즈 추가
 * *
 * nCurrentRowID 추가 (팝업 켜진상태에서 다른 메모 수정하면 팝업 바뀜)
 */
public class PopupData implements Serializable{
    public String text="";      //텍스트
    public float textSize=18;     //텍스트 사이즈
    public int clearValue=100;  //투명도

    //디데이 체크와 디데이 계산을 위한 변수들
    public  boolean d_day=false;
    public long date=0;
    public int d_year=0;
    public int d_month=0;
    public int d_date=0;

    public int memoColor = R.id.radioDefalt;//메모의 배경이미지
    public boolean checkPop = false;//메모가 떠있는지 여부를 체크
    public int nCurrentRowID = 1;   //현재 담겨져 있는 DB의 RowID값


    //메모 설정 값들을 저장
    public void putSharedPreference
            (Context context)
    {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("text",this.text);
        editor.putFloat("textSize", this.textSize);
        editor.putInt("clearValue", this.clearValue);
        editor.putLong("date", this.date);
        editor.putBoolean("d_day", this.d_day);
        editor.putInt("d_year", this.d_year);
        editor.putInt("d_month",this.d_month);
        editor.putInt("d_date",this.d_date);
        editor.putBoolean("checkPop",this.checkPop);
        editor.putInt("memoColor", this.memoColor);
        editor.putInt("crntId", this.nCurrentRowID);
        editor.commit();
    }

    //메모 설정값들을 불러옴
    public void getSharedPreference
            (Context context)
    {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        this.text=prefs.getString("text","");
        this.textSize=prefs.getFloat("textSize",18);
        this.clearValue=prefs.getInt("clearValue", 100);
        this.date=prefs.getLong("date", 0);
        this.d_day=prefs.getBoolean("d_day", false);
        this.d_year=prefs.getInt("d_year", 0);
        this.d_month=prefs.getInt("d_month",0);
        this.d_date=prefs.getInt("d_date",0);
        this.checkPop=prefs.getBoolean("checkPop",false);
        this.memoColor=prefs.getInt("memoColor", R.id.radioDefalt);
        this.nCurrentRowID=prefs.getInt("memoColor", 1);
    }
}
