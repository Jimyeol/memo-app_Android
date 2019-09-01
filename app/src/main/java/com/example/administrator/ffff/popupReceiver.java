package com.example.administrator.ffff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by soomi on 2016-05-18.
 * 부팅시 자동으로 서비스가 재시작 되도록 하는 브로드케스트리시버
 */
public class popupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context,Intent intent){

        PopupData popupData = new PopupData();
        popupData.getSharedPreference(context);

        //부팅시 자동으로 실행되는 사항
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)&&popupData.checkPop==true){
            Intent intent1 = new Intent(context, AlwaysOnTopView.class);
            intent1.putExtra("Data", popupData);
            context.startService(intent1);
        }
    }
}
