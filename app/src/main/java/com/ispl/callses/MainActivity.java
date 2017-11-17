package com.ispl.callses;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ispl.callses.Adapter.FriendContactAdapter;
import com.ispl.callses.DB.SqliteDatabase;
import com.ispl.callses.Fragment.InsertRegistrationFragment;
import com.ispl.callses.Fragment.SettingFragment;
import com.ispl.callses.model.FriendContactModel;
import com.ispl.callses.utils.KeyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.ispl.callses.Fragment.SettingFragment.MyPreferencesColor;
import static com.ispl.callses.Fragment.SettingFragment.MyPreferencesCustomFont;
import static com.ispl.callses.Fragment.SettingFragment.MyPreferencesCustomFontSize;
import static com.ispl.callses.utils.CallessUtils.currentPossition;
import static com.ispl.callses.utils.CallessUtils.mRegistrationLayout;
import static com.ispl.callses.utils.CallessUtils.mSettingLayout;
import static com.ispl.callses.utils.CallessUtils.rootPath;
import static com.ispl.callses.utils.CallessUtils.sBackToMainScreen;
import static com.ispl.callses.utils.CallessUtils.sDefaultRingtonePath;
import static com.ispl.callses.utils.CallessUtils.sDirectoryName;
import static com.ispl.callses.utils.CallessUtils.sIsRegistrationChecked;
import static com.ispl.callses.utils.CallessUtils.sIsSettingChecked;
import static com.ispl.callses.utils.CallessUtils.sIsSpeakerChecked;
import static com.ispl.callses.utils.CallessUtils.sMainHandler;
import static com.ispl.callses.utils.CallessUtils.sMessageBoxText;
import static com.ispl.callses.utils.CallessUtils.sMyCustomFontName;
import static com.ispl.callses.utils.CallessUtils.sMyCustomFontSize;
import static com.ispl.callses.utils.CallessUtils.sNameTextBackground;
import static com.ispl.callses.utils.CallessUtils.sNameTextColor;
import static com.ispl.callses.utils.CallessUtils.sPhoneTextBackground;
import static com.ispl.callses.utils.CallessUtils.sPhoneTextColor;
import static com.ispl.callses.utils.CallessUtils.sScanningInternalBox;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private LinearLayout mPhoenCallLL;
    private SqliteDatabase mDatabase;
    private List<FriendContactModel> mFriendContactList;
    private FriendContactAdapter mAdapter;
    private RecyclerView mFriendRecyclerView;
    private SharedPreferences mMyPreferencesColor, mMyPreferencesCustomFont, mMyPreferencesCustomFontSize;
    TelephonyManager mTelephonyManager;
    StatePhoneReceiver myPhoneStateListener;
    boolean callFromApp = false; // To control the call has been made from the application
    boolean callFromOffHook = false; // To control the change to idle state is from the app call
    Context context;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manageOverlayPermission();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(getApplicationContext())) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, 200);
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(myIntent);
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootPath = new File(Environment.getExternalStorageDirectory(), sDirectoryName);
        if (!rootPath.exists()) {
            rootPath.mkdirs();

            String name = "test";
            File f = new File(rootPath + "/", name + ".mp3");
            Uri mUri = Uri.parse("android.resource://"
                    + getApplication().getPackageName() + "/raw/" + name);
            ContentResolver mCr = getApplication().getContentResolver();
            AssetFileDescriptor soundFile;
            try {
                soundFile = mCr.openAssetFileDescriptor(mUri, "r");
            } catch (FileNotFoundException e) {
                soundFile = null;
            }

            try {
                byte[] readData = new byte[1024];
                assert soundFile != null;
                FileInputStream fis = soundFile.createInputStream();
                FileOutputStream fos = new FileOutputStream(f);
                int i = fis.read(readData);
                while (i != -1) {
                    fos.write(readData, 0, i);
                    i = fis.read(readData);
                }
                fos.close();
            } catch (IOException io) {
                io.printStackTrace();
            }
            sDefaultRingtonePath = f.getAbsolutePath();
        }

        context = getApplicationContext();
        mSettingLayout = (LinearLayout) findViewById(R.id.settings_layout);
        mRegistrationLayout = (LinearLayout) findViewById(R.id.registration_layout);
        ImageView searchIV = (ImageView) findViewById(R.id.search_contact);
        mSearchView= (SearchView) findViewById(R.id.search_view);

        mFriendRecyclerView = (RecyclerView) findViewById(R.id.friend_list);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        mFriendRecyclerView.setLayoutManager(linearLayoutManager);
        mFriendRecyclerView.setHasFixedSize(true);

        mPhoenCallLL = (LinearLayout) findViewById(R.id.phone_call_ll);
        // get Data From SharedPreferences
        getColorFromPrefrances();
        //set CallOut Screen Layout
        setPhoneCallData();

        mFriendRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    ////here i want to change imageview image
                    sMainHandler.removeCallbacksAndMessages(null);
                    sMainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (currentPossition == mAdapter.getItemCount()) {
                                currentPossition = 0;
                                mFriendRecyclerView.scrollToPosition(currentPossition);
                            } else {
                                currentPossition++;
                                mFriendRecyclerView.scrollToPosition(currentPossition);
                            }

                            if (currentPossition == mAdapter.getItemCount()) {
                                int tempScanningInternalBox = 0;
                                sMainHandler.postDelayed(this, tempScanningInternalBox * 1000);
                            } else {
                                sMainHandler.postDelayed(this, sScanningInternalBox * 1000);
                            }
                        }

                    }, sScanningInternalBox * 1000);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentPossition = linearLayoutManager.findFirstVisibleItemPosition();
            }
        });

        mSettingLayout.setTag("1");
        mSettingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSettingLayout.getTag().toString().equals("1")) {

                    Fragment settingFragment = new SettingFragment();
                    switchFragment(settingFragment, true, KeyUtils.SETTING_FRAGMENT_TAG);
                }
            }
        });

        mRegistrationLayout.setTag("1");
        mRegistrationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRegistrationLayout.getTag().toString().equals("1")) {
                    Fragment registrationFragment = new InsertRegistrationFragment();
                    switchFragment(registrationFragment, true, KeyUtils.REGISTRATION_FRAGMENT_TAG);
                }
            }
        });

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchView.onActionViewExpanded();
                mSearchView.setVisibility(View.VISIBLE);
                mSearchView.onActionViewExpanded();
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                //FILTER AS YOU TYPE
                filterContact(query);
                return false;
            }
        });
    }

    private void filterContact(String searchString) {
        List<FriendContactModel> filterContactList = new ArrayList<>();
        if (mFriendContactList != null) {
            for (int i = 0; i < mFriendContactList.size(); i++) {
                String displayName = mFriendContactList.get(i).getFriendName();
                if (displayName != null) {
                    String smallName = displayName.toLowerCase();
                    String phoneShow = mFriendContactList.get(i).getFriendNumberShow();
                    if (displayName.contains(searchString) || smallName.contains(searchString) || phoneShow.contains(searchString)) {
                        filterContactList.add(mFriendContactList.get(i));
                    }
                }
            }
        }
        mAdapter.updateList(filterContactList);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
        if (mPhoenCallLL.getVisibility() == View.VISIBLE) {
            finish();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (!Settings.canDrawOverlays(this)) {
                // You don't have permission
                manageOverlayPermission();

            } else {
                //do as per your logic
                SharedPreferences.Editor editorColor = mMyPreferencesColor.edit();
                editorColor.putInt("Phone_Text", 0);
                editorColor.putInt("Phone_Background", 0);
                editorColor.putInt("Name_Text", 0);
                editorColor.putInt("Name_Background", 0);
                editorColor.apply();

                SharedPreferences.Editor editorFont = mMyPreferencesCustomFont.edit();
                editorFont.putString("Custom_Font", sMyCustomFontName);
                editorFont.apply();

                SharedPreferences.Editor editorFontSize = mMyPreferencesCustomFontSize.edit();
                editorFontSize.putInt("Back_To_Main", sBackToMainScreen);
                editorFontSize.putInt("Custom_Font_Size", sMyCustomFontSize);
                editorFontSize.putBoolean("Setting_Switch", sIsSettingChecked);
                editorFontSize.putBoolean("Registration_Switch", sIsRegistrationChecked);
                editorFontSize.putBoolean("In_Call_Speaker", sIsSpeakerChecked);
                editorFontSize.putInt("Msg_Box", sScanningInternalBox);
                editorFontSize.putString("Message_Box_Text", sMessageBoxText);
                editorFontSize.apply();
            }
        }
    }

    public void manageOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 101);
            }
        }
    }

    public void switchFragment(Fragment fragment, boolean isAddBackStack, String tag) {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment, tag);
        if (isAddBackStack) transaction.addToBackStack(null);
        transaction.commit();
    }

    public void setPhoneCallVisibility(Boolean visible) {
        if (visible) {
            mPhoenCallLL.setVisibility(View.VISIBLE);
        } else {
            mPhoenCallLL.setVisibility(View.GONE);
        }
    }

    @SuppressLint("NewApi")
    public void setPhoneCallData() {
        sIsSpeakerChecked = mMyPreferencesCustomFontSize.getBoolean("In_Call_Speaker", sIsSpeakerChecked);

        mFriendRecyclerView.scrollToPosition(currentPossition);

        sMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mAdapter != null) {
                    if (currentPossition == mAdapter.getItemCount()) {
                        currentPossition = 0;
                        mFriendRecyclerView.scrollToPosition(currentPossition);
                    } else {
                        currentPossition++;
                        mFriendRecyclerView.scrollToPosition(currentPossition);
                    }
                    if (currentPossition == mAdapter.getItemCount()) {
                        int tempScanningInternalBox = 0;
                        sMainHandler.postDelayed(this, tempScanningInternalBox * 1000);
                    } else {
                        sMainHandler.postDelayed(this, sScanningInternalBox * 1000);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No data found", Toast.LENGTH_LONG).show();
                }
            }
        }, sScanningInternalBox * 1000);

        mDatabase = new SqliteDatabase(this);

        mFriendContactList = mDatabase.listProducts();
        if (mFriendContactList.size() > 0) {
            mAdapter = new FriendContactAdapter(this, mFriendContactList);
            mFriendRecyclerView.setAdapter(mAdapter);
        }
    }

    public void getColorFromPrefrances() {
        mMyPreferencesColor = getSharedPreferences(MyPreferencesColor, Context.MODE_PRIVATE);
        mMyPreferencesCustomFont = getSharedPreferences(MyPreferencesCustomFont, Context.MODE_PRIVATE);
        mMyPreferencesCustomFontSize = getSharedPreferences(MyPreferencesCustomFontSize, Context.MODE_PRIVATE);

        sPhoneTextColor = mMyPreferencesColor.getInt("Phone_Text", 0);
        sPhoneTextBackground = mMyPreferencesColor.getInt("Phone_Background", 0);
        sNameTextColor = mMyPreferencesColor.getInt("Name_Text", 0);
        sNameTextBackground = mMyPreferencesColor.getInt("Name_Background", 0);
        sBackToMainScreen = mMyPreferencesCustomFontSize.getInt("Back_To_Main", sBackToMainScreen);
        sMyCustomFontName = mMyPreferencesCustomFont.getString("Custom_Font", sMyCustomFontName);
        sMyCustomFontSize = mMyPreferencesCustomFontSize.getInt("Custom_Font_Size", sMyCustomFontSize);
        sScanningInternalBox = mMyPreferencesCustomFontSize.getInt("Msg_Box", sScanningInternalBox);
        sMessageBoxText = mMyPreferencesCustomFontSize.getString("Message_Box_Text", sMessageBoxText);
        sIsSettingChecked = mMyPreferencesCustomFontSize.getBoolean("Setting_Switch", sIsSettingChecked);
        sIsRegistrationChecked = mMyPreferencesCustomFontSize.getBoolean("Registration_Switch", sIsRegistrationChecked);
    }

    private class StatePhoneReceiver extends PhoneStateListener {
        Context context;
        StatePhoneReceiver(Context context) {
            this.context = context;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_OFFHOOK: //Call is established
                    if (callFromApp) {
                        callFromApp = false;
                        callFromOffHook = true;
                        try {
                            Thread.sleep(500); // Delay 0,5 seconds to handle better turning on loudspeaker
                        } catch (InterruptedException ignored) {
                        }
                        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setMode(AudioManager.MODE_IN_CALL);
                        audioManager.setSpeakerphoneOn(true);
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE: //Call is finished
                    if (callFromOffHook) {
                        callFromOffHook = false;
                        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setMode(AudioManager.MODE_NORMAL); //Deactivate loudspeaker
                        mTelephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_NONE);// Remove listener
                    }
                    break;
            }
        }
    }
}
