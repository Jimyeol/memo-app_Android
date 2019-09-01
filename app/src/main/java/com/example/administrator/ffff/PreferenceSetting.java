package com.example.administrator.ffff;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2016-08-26.
 */
public class PreferenceSetting extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content,
                        new MyPreferenceFragment()).commit();
    }

    // PreferenceFragment 클래스 사용
    public static class MyPreferenceFragment extends
            PreferenceFragment {
        Preference popupDesignChoice;
        Preference popupTranparent;
        Preference popupTextSize;
        Preference noteDeleteAll;
        Preference appver;
        SwitchPreference popupSwitchOnOff;

        //투명도 조절
        private SeekBar seekBar;
        private int GetTransparentProgress = 0;

        //컬러 초이스 라디오 그룹
        private RadioGroup rg;

        //글자 크기 조절
        private NumberPicker numberPicker;
        private float GetTextSize;

        //환경변수
        private static final String KEY_MY_PREFERENCE = "option";
        private static final String KEY_OPTION_TRANPARENT = "Transparent";
        private static final String KEY_OPTION_TEXTSIZE = "TextSize";
        private static final String KEY_OPTION_POPUP = "PopupOn";

        //매크로 팝업 온오프
        private static final int OFF = 0;
        private static final int ON = 1;
        private int bPopupOn = OFF;

        private Intent intent;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_activity);

            //인텐트 불러오기
            intent = new Intent(getActivity().getApplicationContext(),AlwaysOnTopView.class);

            //팝업기능 불러오기
            bPopupOn = optionFileRead(KEY_OPTION_POPUP);

            //팝업디자인 선택
            popupDesignChoice =
                    (Preference)findPreference("setting_popup_design_choice");
            //popupDesignChoice.setEnabled(false);

            popupDesignChoice.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if( bPopupOn == ON)
                        PopupColorpicker();
                    else
                        Toast.makeText(getActivity().getApplicationContext(),
                                R.string.nopopup_setting_toast, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });


            //팝업 투명도 조절
            popupTranparent =
                    (Preference)findPreference("setting_popup_transparent");
            popupTranparent.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    GetTransparentProgress = optionFileRead(KEY_OPTION_TRANPARENT);
                    TransparentOptionValue();   //투명도 다이얼로그 함수
                    return true;
                }
            });

            //팝업 텍스트 사이즈 조절
            popupTextSize =
                    (Preference)findPreference("setting_popup_text_size");
            popupTextSize.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    GetTextSize = optionFileRead(KEY_OPTION_TEXTSIZE);
                    textSizePicker();   //텍스트사이즈 다이얼로그 함수
                    return true;
                }
            });


            //팝업 온오프 기능
            popupSwitchOnOff =
                    (SwitchPreference)findPreference("setting_popup_on_off");
            if( MainActivity.checkOpenPopup = true ) {
                popupSwitchOnOff.setChecked(true);

            } else {
                popupSwitchOnOff.setChecked(false);
            }
            popupSwitchOnOff.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    PopupSetting(!popupSwitchOnOff.isChecked());
                    return true;
                }
            });

            noteDeleteAll =
                    (Preference)findPreference("setting_memo_all_delete");
            noteDeleteAll.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AllDeleteAlertDialog();
                    return true;
                }
            });

            appver = (Preference)findPreference("setting_activity_app_version");
            appver.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(),
                            R.style.AlertDialogCustom);
                    dialog.setTitle(R.string.app_name);
                    dialog.setMessage(R.string.option_version);
                    dialog.show();
                    return true;
                }
            });

        }

        //팝업 ON/OFF 세팅 함수
        private void PopupSetting(boolean b ) {
            MainActivity.popupData.checkPop = b;
            MainActivity.checkOpenPopup = b;
            MainActivity.popupData.putSharedPreference(getActivity().getApplicationContext());
            if( b ){
                bPopupOn = ON;
                intent.putExtra("Data",MainActivity.popupData);
                getActivity().startService(intent);
            } else {
                bPopupOn = OFF;
                getActivity().stopService(intent);
            }
            optionFileWrite(KEY_OPTION_POPUP, bPopupOn);
        }

        //전체 모든 노트 삭제 AlertDialog
        private void AllDeleteAlertDialog(){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
            builder.setTitle(R.string.option_delete_all_title)
                    .setMessage(R.string.option_delete_all_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.option_delete_all_yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    MainActivity.mDbHelper.deleteAllNote();
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            R.string.option_delete_all_yes_message,
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                    .setNegativeButton(R.string.option_delete_all_no,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            R.string.option_delete_all_no_message,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
            builder.show();
        }

        //팝업 색상 변경 AlertDialog
        private void PopupColorpicker() {
            LayoutInflater inflater = (LayoutInflater) getActivity().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewInDialog = inflater.inflate(
                    R.layout.popupcolor_dlg, null);

            final AlertDialog DlgPopupColorSetting = new AlertDialog.Builder(
                    getActivity()).setView(viewInDialog).create();
            DlgPopupColorSetting.setTitle(R.string.option_popup_color_pick);

            rg = (RadioGroup) viewInDialog.findViewById(R.id.radioColor);
            Button bt = (Button)viewInDialog.findViewById(R.id.popup_color_ok);
            rg.check(MainActivity.popupData.memoColor);

            rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    MainActivity.popupData.memoColor = checkedId;
                    MainActivity.popupData.putSharedPreference(getActivity().getApplicationContext());
                    intent.putExtra("Data", MainActivity.popupData);
                    getActivity().startService(intent);
                }
            });
            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DlgPopupColorSetting.dismiss();
                }
            });

            DlgPopupColorSetting.show();
        }

        private void TransparentOptionValue() {
            LayoutInflater inflater = (LayoutInflater) getActivity().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewInDialog = inflater.inflate(
                    R.layout.transparent_dlg, null);

            final AlertDialog DlgTransparentSetting = new AlertDialog.Builder(
                    getActivity()).setView(viewInDialog).create();
            DlgTransparentSetting.setTitle(R.string.option_transparent);

            seekBar = (SeekBar) viewInDialog.
                    findViewById(R.id.seekBar_transparent_set);

            Button btnOk = (Button) viewInDialog.
                    findViewById(R.id.btnOk);
            GetTransparentProgress = MainActivity.popupData.clearValue;

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
                    MainActivity.popupData.putSharedPreference(getActivity().getApplicationContext());
                    if(MainActivity.checkOpenPopup==true) {
                        intent.putExtra("Data",MainActivity.popupData);
                        getActivity().startService(intent);
                    }
                    DlgTransparentSetting.dismiss();
                }
            });

            DlgTransparentSetting.show();

            DlgTransparentSetting.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    optionFileWrite(KEY_OPTION_TRANPARENT, GetTransparentProgress);
                    Log.v("File", "출력");
                }
            });
        }

        //텍스트 사이즈 조절 다이얼로그 16.09.16 추가
        private void textSizePicker() {
            LayoutInflater inflater = (LayoutInflater) getActivity().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewInDialog = inflater.inflate(
                    R.layout.poptextsize_dlg, null);

            final AlertDialog DlgTransparentSetting = new AlertDialog.Builder(
                    getActivity()).setView(viewInDialog).create();
            DlgTransparentSetting.setTitle(R.string.option_popup_text_size_title);

            numberPicker=(NumberPicker) viewInDialog.findViewById(R.id.textSizePicker);
            Button btnOk = (Button) viewInDialog.findViewById(R.id.text_size_OK);
            final TextView sizePreview = (TextView) viewInDialog.findViewById(R.id.sizePreview);
            GetTextSize=MainActivity.popupData.textSize;

            sizePreview.setTextSize(GetTextSize);

            numberPicker.setMinValue(8);
            numberPicker.setMaxValue(28);
            numberPicker.setValue((int)GetTextSize);
            numberPicker.setWrapSelectorWheel(false);
            numberPickerTextColor(numberPicker, Color.BLACK);


            numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    sizePreview.setTextSize((float) newVal);
                }
            });

            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GetTextSize = (float)(numberPicker.getValue());
                    MainActivity.popupData.textSize = GetTextSize;
                    MainActivity.popupData.putSharedPreference(getActivity().getApplicationContext());
                    if(MainActivity.checkOpenPopup==true) {
                        intent.putExtra("Data",MainActivity.popupData);
                        getActivity().startService(intent);
                    }
                    DlgTransparentSetting.dismiss();
                }
            });

            DlgTransparentSetting.show();

            DlgTransparentSetting.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    optionFileWrite(KEY_OPTION_TEXTSIZE, (int)GetTextSize);
                    Log.v("File", "출력");
                }
            });
        }


        private void optionFileWrite(String kindOption, int value) {
            //파일저장
            SharedPreferences prefs = getActivity().getSharedPreferences(kindOption, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_MY_PREFERENCE, value);
            editor.commit();
        }

        public int optionFileRead(String kindOption) {
            //파일읽어오기
            SharedPreferences prefs = getActivity().getSharedPreferences(kindOption, MODE_PRIVATE);
            int value = prefs.getInt(KEY_MY_PREFERENCE, 0);
            return value;
        }

        public int getRadioValue(String str) {
            switch (str) {
                case "radioDefault":
                    return R.id.radioDefalt;
                case "radioBlue":
                    return R.id.radioBlue;
                case "radioGreen":
                    return R.id.radioGreen;
                case "radioPink":
                    return R.id.radioPink;
                case "radioPuple":
                    return R.id.radioPuple;
                default:
                    return R.id.radioDefalt;
            }
        }
    }

    //넘버 피커 텍스트 색깔 오류 해결
    static void numberPickerTextColor( NumberPicker $v, int $c ){
        for(int i = 0, j = $v.getChildCount() ; i < j; i++){
            View t0 = $v.getChildAt(i);
            if( t0 instanceof EditText){
                try{
                    Field t1 = $v.getClass() .getDeclaredField( "mSelectorWheelPaint" );
                    t1.setAccessible(true);
                    ((Paint)t1.get($v)) .setColor($c);
                    ((EditText)t0) .setTextColor($c);
                    $v.invalidate();
                }catch(Exception e){}
            }
        }
    }
}