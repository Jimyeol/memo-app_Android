package com.example.administrator.ffff;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by Administrator on 2016-05-09.
 *  edit 2016-09-14
 * 텍스트 사이즈 추가
 * edit 2016-10-03
 * 노티피케이션 메모 노출 추가
 */
public class AlwaysOnTopView extends Service {
    private WindowManager.LayoutParams params;
    private WindowManager wm;

    private Resources res;

    private View pop;
    private TextView tv; //메모 텍스트

    private ImageView image; //메모 이미지
    private ImageButton image_btn; //메모 버튼화
    private ImageButton side_btn; //사이즈 조절 버튼
    private int memoBackId = R.drawable.popup_back;
    private int memoPopId = R.drawable.popup_button;
    private int memoSizeId = R.drawable.size_btn;

    private int mc = 1; //메모 버튼화 구별 1(메모활성화), -1(메모 비활성화)

    //인텐트에 사용하는 변수
    public PopupData popupData;
    public int pop_width = 368;
    public int pop_height = 325;
    //사이즈와 메모 이동에 관한 좌표변수
    private float START_X, START_Y;
    private int PREV_X, PREV_Y;

    private View.OnTouchListener mViewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    START_X = event.getRawX();
                    START_Y = event.getRawY();
                    PREV_X = params.x;
                    PREV_Y = params.y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int x = (int) (event.getRawX() - START_X);
                    int y = (int) (event.getRawY() - START_Y);
                    params.x = PREV_X + x;
                    params.y = PREV_Y + y;
                    wm.updateViewLayout(pop, params);
                    break;
            }
            return true;
        }
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //인텐트 수신과 메모 기타 설정 적용
        popupData = (PopupData) intent.getSerializableExtra("Data");

        Intent startApp = new Intent(this, MainActivity.class);
        intent.setFlags(startApp.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(startApp.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, startApp, 0);

        res = getResources();
        Notification.Builder noti = new Notification.Builder(this);
        noti.setSmallIcon(R.mipmap.icon_memo);
        noti.setContentTitle(getString(R.string.app_name));
        noti.setContentText(popupData.text);
        noti.setContentIntent(pi);
        startForeground(1, noti.build());

   /*     DayCheck thread = new DayCheck();
        if (popupData.d_day == true) {
            //날짜가 바뀌면 디데이 값이 바뀌도록 설정하기위한 스레드
            thread.setDaemon(true);
            thread.start();
            tv.setText("D-day " + (int) popupData.date + "\n" + popupData.text);
        } else {*/
            tv.setText(popupData.text);
      //      thread.interrupt();
      //  }
        tv.setTextSize(popupData.textSize); //텍스트 사이즈 설정

        switch (popupData.memoColor) {
            case R.id.radioDefalt:
                memoBackId = R.drawable.popup_back;
                memoPopId = R.drawable.popup_button;
                memoSizeId = R.drawable.size_btn;
                break;
            case R.id.radioBlue:
                memoBackId = R.drawable.popup_back_blue;
                memoPopId = R.drawable.popup_button_blue;
                memoSizeId = R.drawable.size_btn_blue;
                break;
            case R.id.radioGreen:
                memoBackId = R.drawable.popup_back_green;
                memoPopId = R.drawable.popup_button_green;
                memoSizeId = R.drawable.size_btn_green;
                break;
            case R.id.radioPink:
                memoBackId = R.drawable.popup_back_pink;
                memoPopId = R.drawable.popup_button_pink;
                memoSizeId = R.drawable.size_btn_pink;
                break;
            case R.id.radioPuple:
                memoBackId = R.drawable.popup_back_puple;
                memoPopId = R.drawable.popup_button_puple;
                memoSizeId = R.drawable.size_btn_puple;
                break;
            default:
                break;
        }

        params.alpha = popupData.clearValue / 100.0f;
        params.width = pop_width;
        params.height = pop_height;
        image.setImageResource(memoBackId);
        side_btn.setImageResource(memoSizeId);
        if (mc == -1) {
            image_btn.setImageResource(memoPopId);
            params.alpha = 1;
            params.width = 60;
            params.height = 60;
        }
        wm.updateViewLayout(pop, params);

        //노티피케이션에 메모 노출 16.10.13

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        // unregisterRestartAlarm();
        super.onCreate();

        pop = View.inflate(getApplicationContext(), R.layout.always_on_top_view_touch, null);
        tv = (TextView) pop.findViewById(R.id.poptext);
        image = (ImageView) pop.findViewById(R.id.popup_back);
        image_btn = (ImageButton) pop.findViewById(R.id.popbtn);
        side_btn = (ImageButton) pop.findViewById(R.id.sideBtn);

        pop.setOnTouchListener(mViewTouchListener);

        //서비스 재시작시 옵션
        SharedPreferences size = getSharedPreferences("size", MODE_PRIVATE);
        pop_width = size.getInt("width", 368);
        pop_height = size.getInt("height", 325);


        //최상위 윈도우에 넣기 위한 설정
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.width = pop_width;
        params.height = pop_height;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.alpha = 100 / 100.0f;
        wm = (WindowManager) getSystemService(WINDOW_SERVICE); //윈도 매니저
        wm.addView(pop, params);
        image.setMaxWidth(params.width);
        image.setMaxHeight(params.height);

        //여기부터 이미지버튼의 이벤트 처리(최소화)
        image_btn = (ImageButton) pop.findViewById(R.id.popbtn);

        image_btn.setOnTouchListener(new View.OnTouchListener() {
            float down_x = 0;
            float down_y = 0;
            float pre_x = 0;
            float pre_y = 0;
            float up_x = 0;
            float up_y = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        down_x = event.getRawX();
                        down_y = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mc == -1) {
                            pre_x = up_x;
                            pre_y = up_y;
                            up_x = event.getRawX();
                            up_y = event.getRawY();

                            if (Math.abs(down_x - up_x) > 15 || Math.abs(down_y - up_y) > 15) {
                                params.x = params.x - (int) (pre_x - up_x);
                                params.y = params.y - (int) (pre_y - up_y);
                                wm.updateViewLayout(pop, params);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        up_x = event.getRawX();
                        up_y = event.getRawY();
                        if (Math.abs(down_x - up_x) < 30 && Math.abs(down_y - up_y) < 30) {
                            mc *= -1;
                            if (mc == -1) {
                                image_btn.setImageResource(memoPopId);
                                image.setVisibility(image.GONE);
                                tv.setVisibility(View.GONE);
                                side_btn.setVisibility(View.GONE);

                                params.width = image_btn.getWidth();
                                params.height = image_btn.getHeight();
                                params.alpha = 100 / 100.0f;
                                wm.updateViewLayout(pop, params);
                            } else {
                                image_btn.setImageResource(R.drawable.popup_btn);
                                image.setVisibility(image.VISIBLE);
                                tv.setVisibility(View.VISIBLE);
                                side_btn.setVisibility(View.VISIBLE);
                                params.width = pop_width;
                                params.height = pop_height;
                                params.alpha = popupData.clearValue / 100.0f;
                                wm.updateViewLayout(pop, params);
                            }
                        }
                        break;
                }
                return false;
            }
        });

        //메모뷰 크기조절 이벤트처리
        side_btn = (ImageButton) pop.findViewById(R.id.sideBtn);
        side_btn.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (mc == -1) return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        START_X = event.getRawX();
                        START_Y = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int x = (int) (event.getRawX() - START_X);
                        int y = (int) (event.getRawY() - START_Y);
                        if (params.width >= 300) params.width += x;
                        else if (x > 0) params.width += x;
                        if (params.height >= 180) params.height += y;
                        else if (y > 0) params.height += y;
                        image.setMaxWidth(params.width);
                        image.setMaxHeight(params.height);
                        pop_width = params.width;
                        pop_height = params.height;
                        START_X = event.getRawX();
                        START_Y = event.getRawY();

                        wm.updateViewLayout(pop, params);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    //서비스가 죽으면 기존 메모의 사이즈를 저장
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences size = getSharedPreferences("size", MODE_PRIVATE);
        SharedPreferences.Editor editor = size.edit();
        editor.putInt("width", pop_width);
        editor.putInt("height", pop_height);
        editor.commit();
        if (pop != null) {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(pop);
            pop = null;
        }
    }
/*
    //디데이 날짜를 체크하고 변경하기위한 스레드 클래스
    public class DayCheck extends Thread {
        @Override
        public void run() {
            while (true) {
                Calendar dCalendar = Calendar.getInstance();
                Calendar nowCalendar = Calendar.getInstance();
                dCalendar.set(popupData.d_year, popupData.d_month, popupData.d_date);
                long nowDay = nowCalendar.getTimeInMillis();
                long d_Day = dCalendar.getTimeInMillis();
                long new_day = (d_Day - nowDay) / (24 * 60 * 60 * 1000);
                if (new_day != popupData.date) {
                    popupData.date = new_day;
                    pop.post(new Runnable() {
                        @Override
                        public void run() {
                            tv.setText("D-day " + popupData.date + "\n" + popupData.text);
                            wm.updateViewLayout(pop, params);
                        }
                    });
                }
                SystemClock.sleep(10 * 60 * 1000);
            }
        }
    }
    */
}
