package com.ispl.callses.Fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ispl.callses.DB.SqliteDatabase;
import com.ispl.callses.MainActivity;
import com.ispl.callses.R;
import com.ispl.callses.model.FriendContactModel;
import com.ispl.callses.utils.CallessUtils;
import com.obsez.android.lib.filechooser.ChooserDialog;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

import static com.ispl.callses.utils.CallessUtils.mRegistrationLayout;
import static com.ispl.callses.utils.CallessUtils.mSettingLayout;
import static com.ispl.callses.utils.CallessUtils.rootPath;
import static com.ispl.callses.utils.CallessUtils.sBackToMainScreen;
import static com.ispl.callses.utils.CallessUtils.sIsSettingChecked;
import static com.ispl.callses.utils.CallessUtils.sIsSpeakerChecked;
import static com.ispl.callses.utils.CallessUtils.sMainHandler;
import static com.ispl.callses.utils.CallessUtils.sMessageBoxText;
import static com.ispl.callses.utils.CallessUtils.sMyCustomFontName;
import static com.ispl.callses.utils.CallessUtils.sMyCustomFontSize;
import static com.ispl.callses.utils.CallessUtils.sScanningInternalBox;
import static com.ispl.callses.utils.CallessUtils.sSettingHandler;


/**
 * Created by infinium on 01/08/17.   String customFont = "snell-roundhand-black-script.ttf";
 */

public class SettingFragment extends Fragment {
    private LinearLayout mSettingSubLL;
    private TextView mPhoneNumberTextColorTV, mPhoneNumberBackgroundColorTV, mPhoneNameTextColorTV, mPhoneNameBackgroundColorTV, mMessageShowTV;
    private EditText mMessageWriteET;
    private SharedPreferences sharedPreferencesColor, sharedPreferencesCustomFont, sharedPreferencesCustomFontSize;
    public static final String MyPreferencesColor = "MyPrefsColor", MyPreferencesCustomFont = "MyPrefesCusomFont",
            MyPreferencesCustomFontSize = "MyPrefesCusomFontSize";
    Runnable runnable;
    int mSpinnerTimeItem = 0, mSpinnerCustomFontName = 0, mSpinnerCustomFontSize = 0, mSpinnerScannigMsgBoxValue = 0;
    String cambriaBold = "cambriaz.ttf", dancingScriptBold = "DancingScript-Bold.ttf", droidSerifRegular = "DroidSerif-Regular.ttf", fjallaOneRegular = "FjallaOne-Regular.ttf",
            gillSansMT = "gill-sans-mt.ttf", inconsolataRegular = "Inconsolata-Regular.ttf", lobsterRegular = "Lobster-Regular.ttf",
            montserratRegular = "Montserrat-Regular.ttf", myriadWebPro = "myriad-web-pro.ttf", snellRoundhandBlackScript = "snell-roundhand-black-script.ttf";
    private SqliteDatabase mDatabase;
    private ProgressDialog mProgressDialog = null;

    private TextView mProgressTV;
    private Button mBackUpBtn, mCancelBtn;
    private String mRestorePath = null;
    private Dialog mDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View layout_view = inflater.inflate(R.layout.fragment_setting, container, false);

        sMainHandler.removeCallbacksAndMessages(null);
        ((MainActivity) getActivity()).setPhoneCallVisibility(false);
        mSettingLayout.setTag("2");
        mRegistrationLayout.setTag("1");

        mDatabase = new SqliteDatabase(getContext());

        Switch mMainSwitch = (Switch) layout_view.findViewById(R.id.setting_main_switch);
        Switch mInCallSpeakerSwitch = (Switch) layout_view.findViewById(R.id.in_call_speaker_switch);

        Spinner mTimeSpinner = (Spinner) layout_view.findViewById(R.id.time_select_spinner);
        Spinner mCustomFontSpinner = (Spinner) layout_view.findViewById(R.id.custom_font_spinner);
        Spinner mCustomFontSizeSpinner = (Spinner) layout_view.findViewById(R.id.custom_font_size_spinner);
        Spinner mScanningIntervalMsgboxSpinner = (Spinner) layout_view.findViewById(R.id.scanning_interval_msgbox_spinner);

        LinearLayout mPhoneNumberTextColorLL = (LinearLayout) layout_view.findViewById(R.id.phone_number_text_color_ll);
        LinearLayout mPhoneNumberBackgroundColorLL = (LinearLayout) layout_view.findViewById(R.id.phone_number_background_color_ll);
        LinearLayout mPhoneNameTextColorLL = (LinearLayout) layout_view.findViewById(R.id.phone_name_text_color_ll);
        LinearLayout mPhoneNameBackgroundColorLL = (LinearLayout) layout_view.findViewById(R.id.phone_name_background_color_ll);

        mSettingSubLL = (LinearLayout) layout_view.findViewById(R.id.setting_sub_ll);

        mPhoneNumberTextColorTV = (TextView) layout_view.findViewById(R.id.phone_number_text_color_picker);
        mPhoneNumberBackgroundColorTV = (TextView) layout_view.findViewById(R.id.phone_number_background_color_picker);
        mPhoneNameTextColorTV = (TextView) layout_view.findViewById(R.id.phone_name_text_color_picker);
        mPhoneNameBackgroundColorTV = (TextView) layout_view.findViewById(R.id.phone_name_bacground_color_picker);
        mMessageShowTV = (TextView) layout_view.findViewById(R.id.set_msg_box_write_unknown_person);

        mMessageWriteET = (EditText) layout_view.findViewById(R.id.msg_box_write_unknown_person);

        Button mBtnConfirmMsg = (Button) layout_view.findViewById(R.id.confirm_msg);
        Button mBtnResetMsg = (Button) layout_view.findViewById(R.id.reset_msg);

        Button mImpprtContactBtn = (Button) layout_view.findViewById(R.id.import_contact_btn);

        Button mBackUpFriendContactBtn = (Button) layout_view.findViewById(R.id.back_up_friend_contact_btn);
        Button mRestoreFriendContactBtn = (Button) layout_view.findViewById(R.id.restore_friend_contact_btn);

        sharedPreferencesColor = getActivity().getSharedPreferences(MyPreferencesColor, Context.MODE_PRIVATE);
        sharedPreferencesCustomFont = getActivity().getSharedPreferences(MyPreferencesCustomFont, Context.MODE_PRIVATE);
        sharedPreferencesCustomFontSize = getActivity().getSharedPreferences(MyPreferencesCustomFontSize, Context.MODE_PRIVATE);

        mMessageShowTV.setText(sMessageBoxText);

        if (sIsSettingChecked) {
            mMainSwitch.setChecked(true);
            setMainSwitchOn(mSettingSubLL, true);
            setTimer(true);
        } else {
            mMainSwitch.setChecked(false);
            setMainSwitchOn(mSettingSubLL, false);
            setTimer(false);
        }
        mMainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                sIsSettingChecked = isChecked;
                SharedPreferences.Editor editor = sharedPreferencesCustomFontSize.edit();
                editor.putBoolean("Setting_Switch", sIsSettingChecked);
                editor.apply();
                if (sIsSettingChecked) {
                    setMainSwitchOn(mSettingSubLL, true);
                    setTimer(true);
                } else {
                    setMainSwitchOn(mSettingSubLL, false);
                    setTimer(false);
                }
            }
        });

        if (sIsSpeakerChecked) {
            mInCallSpeakerSwitch.setChecked(true);
        } else {
            mInCallSpeakerSwitch.setChecked(false);
        }

        mInCallSpeakerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                sIsSpeakerChecked = isChecked;
                SharedPreferences.Editor editor = sharedPreferencesCustomFontSize.edit();
                editor.putBoolean("In_Call_Speaker", sIsSpeakerChecked);
                editor.apply();
            }
        });

        List<Integer> backToMainList = new ArrayList<Integer>();
        backToMainList.add(2);
        backToMainList.add(3);
        backToMainList.add(4);
        backToMainList.add(5);
        backToMainList.add(6);
        backToMainList.add(7);
        backToMainList.add(8);
        backToMainList.add(9);
        backToMainList.add(10);

        // Creating adapter for spinner for selecting seconds
        ArrayAdapter<Integer> backToMainAdapter = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_spinner_item, backToMainList);

        // Drop down layout style - list view with radio button
        backToMainAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        mTimeSpinner.setAdapter(backToMainAdapter);
        int spinnerPositionBackToMain = backToMainAdapter.getPosition(sBackToMainScreen);
        mTimeSpinner.setSelection(spinnerPositionBackToMain);

        mTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                mSpinnerTimeItem = position;
                SharedPreferences.Editor editor = sharedPreferencesCustomFontSize.edit();
                switch (mSpinnerTimeItem) {
                    case 0:
                        sBackToMainScreen = 2;
                        editor.putInt("Back_To_Main", sBackToMainScreen);
                        editor.apply();
                        break;
                    case 1:
                        sBackToMainScreen = 3;
                        editor.putInt("Back_To_Main", sBackToMainScreen);
                        editor.apply();
                        break;
                    case 2:
                        sBackToMainScreen = 4;
                        editor.putInt("Back_To_Main", sBackToMainScreen);
                        editor.apply();
                        break;
                    case 3:
                        sBackToMainScreen = 5;
                        editor.putInt("Back_To_Main", sBackToMainScreen);
                        editor.apply();
                        break;
                    case 4:
                        sBackToMainScreen = 6;
                        editor.putInt("Back_To_Main", sBackToMainScreen);
                        editor.apply();
                        break;
                    case 5:
                        sBackToMainScreen = 7;
                        editor.putInt("Back_To_Main", sBackToMainScreen);
                        editor.apply();
                        break;
                    case 6:
                        sBackToMainScreen = 8;
                        editor.putInt("Back_To_Main", sBackToMainScreen);
                        editor.apply();
                        break;
                    case 7:
                        sBackToMainScreen = 9;
                        editor.putInt("Back_To_Main", sBackToMainScreen);
                        editor.apply();
                        break;
                    case 8:
                        sBackToMainScreen = 10;
                        editor.putInt("Back_To_Main", sBackToMainScreen);
                        editor.apply();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        List<String> customFontList = new ArrayList<String>();
        customFontList.add(cambriaBold);
        customFontList.add(dancingScriptBold);
        customFontList.add(droidSerifRegular);
        customFontList.add(fjallaOneRegular);
        customFontList.add(gillSansMT);
        customFontList.add(inconsolataRegular);
        customFontList.add(lobsterRegular);
        customFontList.add(montserratRegular);
        customFontList.add(myriadWebPro);
        customFontList.add(snellRoundhandBlackScript);

        // Creating adapter for spinner for selecting seconds
        final ArrayAdapter<String> customFontAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, customFontList);
        // Drop down layout style - list view with radio button
        customFontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        mCustomFontSpinner.setAdapter(customFontAdapter);
        int spinnerPositionName = customFontAdapter.getPosition(sMyCustomFontName);
        mCustomFontSpinner.setSelection(spinnerPositionName);
        mCustomFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                SharedPreferences.Editor editor = sharedPreferencesCustomFont.edit();
                mSpinnerCustomFontName = position;
                switch (mSpinnerCustomFontName) {
                    case 0:
                        sMyCustomFontName = cambriaBold;
                        editor.putString("Custom_Font", sMyCustomFontName);
                        editor.apply();
                        break;
                    case 1:
                        sMyCustomFontName = dancingScriptBold;
                        editor.putString("Custom_Font", sMyCustomFontName);
                        editor.apply();
                        break;
                    case 2:
                        sMyCustomFontName = droidSerifRegular;
                        editor.putString("Custom_Font", sMyCustomFontName);
                        editor.apply();
                        break;
                    case 3:
                        sMyCustomFontName = fjallaOneRegular;
                        editor.putString("Custom_Font", sMyCustomFontName);
                        editor.apply();
                        break;
                    case 4:
                        sMyCustomFontName = gillSansMT;
                        editor.putString("Custom_Font", sMyCustomFontName);
                        editor.apply();
                        break;
                    case 5:
                        sMyCustomFontName = inconsolataRegular;
                        editor.putString("Custom_Font", sMyCustomFontName);
                        editor.apply();
                        break;
                    case 6:
                        sMyCustomFontName = lobsterRegular;
                        editor.putString("Custom_Font", sMyCustomFontName);
                        editor.apply();
                        break;
                    case 7:
                        sMyCustomFontName = montserratRegular;
                        editor.putString("Custom_Font", sMyCustomFontName);
                        editor.apply();
                        break;
                    case 8:
                        sMyCustomFontName = myriadWebPro;
                        editor.putString("Custom_Font", sMyCustomFontName);
                        editor.apply();
                        break;
                    case 9:
                        sMyCustomFontName = snellRoundhandBlackScript;
                        editor.putString("Custom_Font", sMyCustomFontName);
                        editor.apply();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        List<Integer> fontSizeList = new ArrayList<Integer>();
        fontSizeList.add(18);
        fontSizeList.add(20);
        fontSizeList.add(22);
        fontSizeList.add(25);
        fontSizeList.add(28);
        fontSizeList.add(30);
        fontSizeList.add(33);
        fontSizeList.add(37);

        // Creating adapter for spinner for selecting seconds
        ArrayAdapter<Integer> fontSizeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, fontSizeList);

        // Drop down layout style - list view with radio button
        fontSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        mCustomFontSizeSpinner.setAdapter(fontSizeAdapter);
        int spinnerPositionSize = fontSizeAdapter.getPosition(sMyCustomFontSize);
        mCustomFontSizeSpinner.setSelection(spinnerPositionSize);
        mCustomFontSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                SharedPreferences.Editor editor = sharedPreferencesCustomFontSize.edit();
                mSpinnerCustomFontSize = position;
                switch (mSpinnerCustomFontSize) {
                    case 0:
                        sMyCustomFontSize = 18;
                        editor.putInt("Custom_Font_Size", sMyCustomFontSize);
                        editor.apply();
                        break;
                    case 1:
                        sMyCustomFontSize = 20;
                        editor.putInt("Custom_Font_Size", sMyCustomFontSize);
                        editor.apply();
                        break;
                    case 2:
                        sMyCustomFontSize = 22;
                        editor.putInt("Custom_Font_Size", sMyCustomFontSize);
                        editor.apply();
                        break;
                    case 3:
                        sMyCustomFontSize = 25;
                        editor.putInt("Custom_Font_Size", sMyCustomFontSize);
                        editor.apply();
                        break;
                    case 4:
                        sMyCustomFontSize = 28;
                        editor.putInt("Custom_Font_Size", sMyCustomFontSize);
                        editor.apply();
                        break;
                    case 5:
                        sMyCustomFontSize = 30;
                        editor.putInt("Custom_Font_Size", sMyCustomFontSize);
                        editor.apply();
                        break;
                    case 6:
                        sMyCustomFontSize = 33;
                        editor.putInt("Custom_Font_Size", sMyCustomFontSize);
                        editor.apply();
                        break;
                    case 7:
                        sMyCustomFontSize = 37;
                        editor.putInt("Custom_Font_Size", sMyCustomFontSize);
                        editor.apply();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        List<Integer> msgBoxList = new ArrayList<Integer>();
        msgBoxList.add(1);
        msgBoxList.add(2);
        msgBoxList.add(3);
        msgBoxList.add(4);
        msgBoxList.add(5);
        msgBoxList.add(6);
        msgBoxList.add(7);
        msgBoxList.add(8);
        msgBoxList.add(9);
        msgBoxList.add(10);

        // Creating adapter for spinner for selecting seconds
        ArrayAdapter<Integer> msgBoxAdapter = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_spinner_item, msgBoxList);

        // Drop down layout style - list view with radio button
        msgBoxAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        mScanningIntervalMsgboxSpinner.setAdapter(msgBoxAdapter);

        int spinnerPositionMsgBox = msgBoxAdapter.getPosition(sScanningInternalBox);
        mScanningIntervalMsgboxSpinner.setSelection(spinnerPositionMsgBox);
        mScanningIntervalMsgboxSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                SharedPreferences.Editor editor = sharedPreferencesCustomFontSize.edit();
                mSpinnerScannigMsgBoxValue = position;

                switch (mSpinnerScannigMsgBoxValue) {
                    case 0:
                        sScanningInternalBox = 1;
                        editor.putInt("Msg_Box", sScanningInternalBox);
                        editor.apply();
                        break;
                    case 1:
                        sScanningInternalBox = 2;
                        editor.putInt("Msg_Box", sScanningInternalBox);
                        editor.apply();
                        break;
                    case 2:
                        sScanningInternalBox = 3;
                        editor.putInt("Msg_Box", sScanningInternalBox);
                        editor.apply();
                        break;
                    case 3:
                        sScanningInternalBox = 4;
                        editor.putInt("Msg_Box", sScanningInternalBox);
                        editor.apply();
                        break;
                    case 4:
                        sScanningInternalBox = 5;
                        editor.putInt("Msg_Box", sScanningInternalBox);
                        editor.apply();
                        break;
                    case 5:
                        sScanningInternalBox = 6;
                        editor.putInt("Msg_Box", sScanningInternalBox);
                        editor.apply();
                        break;
                    case 6:
                        sScanningInternalBox = 7;
                        editor.putInt("Msg_Box", sScanningInternalBox);
                        editor.apply();
                        break;
                    case 7:
                        sScanningInternalBox = 8;
                        editor.putInt("Msg_Box", sScanningInternalBox);
                        editor.apply();
                        break;
                    case 8:
                        sScanningInternalBox = 9;
                        editor.putInt("Msg_Box", sScanningInternalBox);
                        editor.apply();
                        break;
                    case 9:
                        sScanningInternalBox = 10;
                        editor.putInt("Msg_Box", sScanningInternalBox);
                        editor.apply();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mBtnConfirmMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = mMessageWriteET.getText().toString();
                if (msg.matches("")) {
                    Toast.makeText(getContext(), "Please enter message ", Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferences.Editor editor = sharedPreferencesCustomFontSize.edit();
                sMessageBoxText = msg;
                mMessageWriteET.getText().clear();
                editor.putString("Message_Box_Text", sMessageBoxText);
                mMessageShowTV.setText(sMessageBoxText);
                editor.apply();

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

        mBtnResetMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferencesCustomFontSize.edit();
                mMessageWriteET.getText().clear();
                sMessageBoxText = "Unknown Person";
                editor.putString("Message_Box_Text", sMessageBoxText);
                mMessageShowTV.setText(sMessageBoxText);
                editor.apply();

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            }
        });

        if (CallessUtils.sPhoneTextColor == 0) {
            mPhoneNumberTextColorTV.setBackgroundColor(getActivity().getResources().getColor(R.color.black));
        } else {
            mPhoneNumberTextColorTV.setBackgroundColor(CallessUtils.sPhoneTextColor);
        }
        if (CallessUtils.sPhoneTextBackground == 0) {
            mPhoneNumberBackgroundColorTV.setBackgroundColor(getActivity().getResources().getColor(R.color.orange));
        } else {
            mPhoneNumberBackgroundColorTV.setBackgroundColor(CallessUtils.sPhoneTextBackground);
        }
        if (CallessUtils.sNameTextColor == 0) {
            mPhoneNameTextColorTV.setBackgroundColor(getActivity().getResources().getColor(R.color.black));
        } else {
            mPhoneNameTextColorTV.setBackgroundColor(CallessUtils.sNameTextColor);
        }
        if (CallessUtils.sNameTextBackground == 0) {
            mPhoneNameBackgroundColorTV.setBackgroundColor(getActivity().getResources().getColor(R.color.orange));
        } else {
            mPhoneNameBackgroundColorTV.setBackgroundColor(CallessUtils.sNameTextBackground);
        }


        mPhoneNumberTextColorLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallessUtils.sBtnTAG = 1;
                colorPicker();
            }
        });

        mPhoneNumberBackgroundColorLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallessUtils.sBtnTAG = 2;
                colorPicker();
            }
        });
        mPhoneNameTextColorLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallessUtils.sBtnTAG = 3;
                colorPicker();
            }
        });
        mPhoneNameBackgroundColorLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallessUtils.sBtnTAG = 4;
                colorPicker();
            }
        });


        mImpprtContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDialog = new Dialog(getActivity());
                mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mDialog.setContentView(R.layout.dialog_backup);
                mDialog.getWindow().setLayout(DrawerLayout.LayoutParams.MATCH_PARENT, DrawerLayout.LayoutParams.WRAP_CONTENT);
                mDialog.setCanceledOnTouchOutside(false);

                mBackUpBtn = (Button) mDialog.findViewById(R.id.backup_btn);
                mProgressTV = (TextView) mDialog.findViewById(R.id.progress_tv);
                mCancelBtn = (Button) mDialog.findViewById(R.id.cancel_btn);
                mCancelBtn.setVisibility(View.GONE);
                mBackUpBtn.setVisibility(View.GONE);

                ImportAsyncTaskRunner runner = new ImportAsyncTaskRunner();
                runner.execute();

            }
        });

        mBackUpFriendContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog = new Dialog(getActivity());
                mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mDialog.setContentView(R.layout.dialog_backup);
                mDialog.getWindow().setLayout(DrawerLayout.LayoutParams.MATCH_PARENT, DrawerLayout.LayoutParams.WRAP_CONTENT);
                mDialog.setCanceledOnTouchOutside(false);

                mProgressTV = (TextView) mDialog.findViewById(R.id.progress_tv);
                mBackUpBtn = (Button) mDialog.findViewById(R.id.backup_btn);
                mCancelBtn = (Button) mDialog.findViewById(R.id.cancel_btn);
                mBackUpBtn.setVisibility(View.GONE);
                mCancelBtn.setVisibility(View.GONE);

                BackUpAsyncTaskRunner runner = new BackUpAsyncTaskRunner();
                runner.execute();
            }
        });

        mRestoreFriendContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog = new Dialog(getActivity());
                mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mDialog.setContentView(R.layout.dialog_backup);
                mDialog.getWindow().setLayout(DrawerLayout.LayoutParams.MATCH_PARENT, DrawerLayout.LayoutParams.WRAP_CONTENT);
                mDialog.setCanceledOnTouchOutside(false);

                mProgressTV = (TextView) mDialog.findViewById(R.id.progress_tv);
                mBackUpBtn = (Button) mDialog.findViewById(R.id.backup_btn);
                mCancelBtn = (Button) mDialog.findViewById(R.id.cancel_btn);
                mBackUpBtn.setVisibility(View.GONE);
                mCancelBtn.setVisibility(View.GONE);

                new ChooserDialog().with(getActivity())
                        .withStartFile(String.valueOf(Environment.getExternalStorageDirectory()))
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String path, File pathFile) {
                                mRestorePath = path;
                                RestoreAsyncTaskRunner runner = new RestoreAsyncTaskRunner();
                                runner.execute();
                            }
                        })
                        .build()
                        .show();
            }

        });
        return layout_view;
    }


    private class ImportAsyncTaskRunner extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.custom_progress_background));
                mProgressDialog.setTitle("Please wait...");
                mProgressDialog.setMessage("Contact is Importing ...!!");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            int largestListOfOrderDB = mDatabase.getLargestListOfOrder();

            int listofOrder = largestListOfOrderDB + 1;

            ContentResolver contentResolver = getContext().getContentResolver();
            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {

                    String contactId, imageURI = "", contactName, contactMobile = "", contactHome = "", contactWork = "";
                    int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                    if (hasPhoneNumber > 0) {
                        contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                        contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                        if (contactName == null) contactName = "";
                        Cursor phoneCursor = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{contactId},
                                null);
                        while (phoneCursor.moveToNext()) {
                            int phoneType = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                            switch (phoneType) {
                                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                    contactMobile = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    break;
                                default:
                                    break;
                            }
                        }
                        phoneCursor.close();

                        // GET IMAGE
                        //------------------------------------------------------------------------------------------------------------------------
                        Cursor imageCursor = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{contactId}, null);
                        if (imageCursor.moveToNext()) {
                            imageURI = imageCursor.getString(imageCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                        }
                        imageCursor.close();

                        if (contactMobile.equals("") && contactHome.equals("") && contactWork.equals("")) {
                            CallessUtils.v("Phone numger is :- No phone is there ");

                        } else {

                            String newPhoneNumber = contactMobile.replaceAll("[-*#./@%^() ]", "");
                            String phoneNumber = newPhoneNumber.substring(Math.max(newPhoneNumber.length() - 10, 0));

                            String listOfOrder = String.valueOf(listofOrder);

                            if (!contactMobile.equals("")) {
                                Boolean isCheck = mDatabase.isExist(phoneNumber);
                                if (!isCheck) {
                                    FriendContactModel newFriendContact = new FriendContactModel(imageURI, phoneNumber, contactMobile, contactName, listOfOrder, "");
                                    mDatabase.addFriendContact(newFriendContact);
                                    listofOrder++;
                                }
                            }
                        }
                    }
                }
            }
            cursor.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
            }

            mDialog.show();
            mProgressTV.setText("Importing Contact is Completed");
            mCancelBtn.setVisibility(View.VISIBLE);
            mCancelBtn.setText("Close");
            mCancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });
        }
    }

    private class BackUpAsyncTaskRunner extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.custom_progress_background));
                mProgressDialog.setTitle("Please wait...");
                mProgressDialog.setMessage("Contact is Backing Up ...!!");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            int progressStatus = 0;

            try {

                Element company = new Element("company");

                Document doc = new Document(company);

                List<FriendContactModel> selectedFriendContactList = mDatabase.listProducts();

                for (FriendContactModel model : selectedFriendContactList) {

                    Element contact = new Element("contact");
                    String id = String.valueOf(model.getID());
                    contact.setAttribute(new Attribute("id", id));
                    contact.addContent(new Element("image").setText(model.getFriendImagePath()));
                    contact.addContent(new Element("phone").setText(model.getFriendNumber()));
                    contact.addContent(new Element("phone_show").setText(model.getFriendNumberShow()));
                    contact.addContent(new Element("name").setText(model.getFriendName()));
                    contact.addContent(new Element("list_order").setText(model.getFriendListOfOreder()));
                    contact.addContent(new Element("record_message").setText(model.getFriendAudioMessage()));
                    doc.getRootElement().addContent(contact);

                    progressStatus++;
                    try {
                        Thread.sleep(100);
                    } catch (Exception ignored) {
                    }
                    publishProgress(progressStatus * 100 / selectedFriendContactList.size());
                }

                XMLOutputter xmlOutput = new XMLOutputter();
                xmlOutput.setFormat(Format.getPrettyFormat());
                xmlOutput.output(doc, new FileWriter(rootPath + "/Callses_BackUp.xml"));
            } catch (IOException io) {
                System.out.println(io.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
            }
            mDialog.show();
            mProgressTV.setText("BackUp Contact is Completed");
            mBackUpBtn.setVisibility(View.VISIBLE);
            mCancelBtn.setVisibility(View.VISIBLE);
            mBackUpBtn.setText("Send Mail");
            mBackUpBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent sendEmail = new Intent(Intent.ACTION_SEND);
                    sendEmail.setType("plain/tex");
                    sendEmail = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", "", null));
                    sendEmail.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new
                            File(rootPath + "/Callses_BackUp.xml")));
                    sendEmail.putExtra(Intent.EXTRA_SUBJECT, "Callses Back Up");
                    sendEmail.putExtra(Intent.EXTRA_TEXT, "This is contact backup file of Callses App.");
                    startActivity(Intent.createChooser(sendEmail, "Email:"));
                    mDialog.dismiss();
                }
            });
            mCancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });

        }
    }

    private class RestoreAsyncTaskRunner extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.custom_progress_background));
                mProgressDialog.setTitle("Please wait...");
                mProgressDialog.setMessage("Contact is Restoring ...!!");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            int progressStatus = 0;
            Boolean isContactAdded = false;

            SAXBuilder builder = new SAXBuilder();
            File xmlFile = new File(mRestorePath);

            try {

                Document document = (Document) builder.build(xmlFile);
                Element rootNode = document.getRootElement();
                List list = rootNode.getChildren("contact");

                for (int i = 0; i < list.size(); i++) {

                    isContactAdded = true;

                    Element node = (Element) list.get(i);

                    String image = node.getChildText("image");
                    String name = node.getChildText("name");
                    String phone = node.getChildText("phone");
                    String phone_show = node.getChildText("phone_show");
                    String listOrder = node.getChildText("list_order");
                    String record_message = node.getChildText("record_message");

                    if (!phone.equals("")) {
                        Boolean isCheck = mDatabase.isExist(phone);
                        if (!isCheck) {
                            FriendContactModel newFriendContact = new FriendContactModel(image, phone, phone_show, name, listOrder, record_message);

                            mDatabase.addFriendContact(newFriendContact);
                        }
                    }
                    progressStatus++;
                    try {
                        Thread.sleep(100);
                    } catch (Exception ignored) {
                    }
                    publishProgress(progressStatus * 100 / list.size());
                }


            } catch (IOException | JDOMException io) {
                System.out.println(io.getMessage());
            }
            if (!isContactAdded) {
                publishProgress(100);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
            }
            mDialog.show();
            mProgressTV.setText("Restoring Contact is Completed");
            mCancelBtn.setVisibility(View.VISIBLE);
            mCancelBtn.setText("Close");
            mCancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ((MainActivity) getActivity()).getColorFromPrefrances();
        ((MainActivity) getActivity()).setPhoneCallData();
        ((MainActivity) getActivity()).setPhoneCallVisibility(true);
        sSettingHandler.removeCallbacksAndMessages(null);
        mSettingLayout.setTag("1");
    }


    public void colorPicker() {

        AmbilWarnaDialog dialog = new AmbilWarnaDialog(getActivity(), 0xff0000ff, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            // Executes, when user click Cancel button
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                CallessUtils.sDefaultColor = color;
                setColor();
            }
        });
        dialog.show();
    }

    public void setColor() {
        if (CallessUtils.sBtnTAG == 1) {
            mPhoneNumberTextColorTV.setBackgroundColor(CallessUtils.sDefaultColor);
            SharedPreferences.Editor editor = sharedPreferencesColor.edit();
            editor.putInt("Phone_Text", CallessUtils.sDefaultColor);
            editor.apply();
        } else if (CallessUtils.sBtnTAG == 2) {
            mPhoneNumberBackgroundColorTV.setBackgroundColor(CallessUtils.sDefaultColor);
            SharedPreferences.Editor editor = sharedPreferencesColor.edit();
            editor.putInt("Phone_Background", CallessUtils.sDefaultColor);
            editor.apply();
        } else if (CallessUtils.sBtnTAG == 3) {
            mPhoneNameTextColorTV.setBackgroundColor(CallessUtils.sDefaultColor);
            SharedPreferences.Editor editor = sharedPreferencesColor.edit();
            editor.putInt("Name_Text", CallessUtils.sDefaultColor);
            editor.apply();
        } else if (CallessUtils.sBtnTAG == 4) {
            mPhoneNameBackgroundColorTV.setBackgroundColor(CallessUtils.sDefaultColor);
            SharedPreferences.Editor editor = sharedPreferencesColor.edit();
            editor.putInt("Name_Background", CallessUtils.sDefaultColor);
            editor.apply();
        }
    }

    public void setTimer(Boolean enanle) {
        if (enanle) {

            sSettingHandler.removeCallbacksAndMessages(null);
        } else {
            runnable = new Runnable() {
                @Override
                public void run() {
                    getFragmentManager().popBackStack();
                }
            };
            sSettingHandler.postDelayed(runnable, sBackToMainScreen * 1000);
        }
    }

    private static void setMainSwitchOn(ViewGroup layout, Boolean enable) {
        if (enable) {
            layout.setEnabled(true);
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof ViewGroup) {
                    setMainSwitchOn((ViewGroup) child, true);
                } else {
                    child.setEnabled(true);
                }
            }
        } else {
            layout.setEnabled(false);
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof ViewGroup) {
                    setMainSwitchOn((ViewGroup) child, false);
                } else {
                    child.setEnabled(false);
                }
            }
        }
    }

}
