package com.example.administrator.ffff;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by soomi on 2016-10-03.
 */

public class WidgetProvider extends AppWidgetProvider {
    static String Text=null;
    static int key=0;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager,appWidgetIds);
      appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
        for (int i = 0; i < appWidgetIds.length; i++) {
           updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    public static void updateAppWidget(Context context,
                                       AppWidgetManager appWidgetManager, int appWidgetId) {

        /**
         * RemoteViews를 이용해 Text설정
         */
        RemoteViews updateViews = new RemoteViews(context.getPackageName(),
                R.layout.widget);
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        Text = prefs.getString("wid"+appWidgetId, "");
        key = prefs.getInt("wid_key"+appWidgetId, 0);   //key저장
        updateViews.setTextViewText(R.id.widtext, Text);

        //앱 위젯에 클릭 이벤트 추가
        Intent intent = new Intent(context,
                MemoAddActivity.class);
        //setData로 현재 위젯의 ROWID를 intent에 저장한다. 클릭시 해당 메모로 이동
        //putExtras대신 setData를 사용했다. 에러떠서
        intent.setData( Uri.parse(String.valueOf(key)));

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, 0);
        updateViews.setOnClickPendingIntent(R.id.widtext, pendingIntent);

        /**
         * 위젯 업데이트
         */
        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }
}
