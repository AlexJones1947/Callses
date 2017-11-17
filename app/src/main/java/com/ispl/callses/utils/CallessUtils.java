package com.ispl.callses.utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.ispl.callses.model.FriendContactModel;

import java.io.File;
import java.util.List;


/**
 * Created by infinium on 25/07/17.
 */

public class CallessUtils {
    private static final String TAG = "Callses";
    private static final boolean isDebug = true;
    public static final String sDirectoryName = "Callses";
    public static File rootPath = null;
    private static List<FriendContactModel> mFriendContactModelList = null;
    private static List<String> mFriendDisplayContactModelList = null;
    private static List<FriendContactModel> mFriendAllContactList = null;
    private static List<FriendContactModel> mSingleFriendContact = null;
    public static Integer sPhoneTextColor = 0;
    public static Integer sPhoneTextBackground = 0;
    public static Integer sNameTextColor = 0;
    public static Integer sNameTextBackground = 0;
    public static Integer sDefaultColor = 0, sBtnTAG = 0;
    public static String sMyCustomFontName = "cambriaz.ttf";
    public static Integer sMyCustomFontSize = 20;
    public static Boolean sIsSettingChecked = false;
    public static Boolean sIsRegistrationChecked = false;
    public static Boolean sIsSpeakerChecked = false;
    public static Integer sScanningInternalBox = 10;
    public static Integer sBackToMainScreen = 10;
    public static String sDefaultRingtonePath = null;
    public static String sMessageBoxText = "Unknown Person";
    public static String sFriendNumber = null;
    public static String sFriendName = null;
    public static Boolean sOutgoingCallEnded = false;
    public static Boolean sOnMissedCall = false;
    public static View sIncomingCallReceiverView;
    public static String sIncomingContactNumber = null;
    public static Boolean sTemp = false;
    public static String sIncomingNumber = null;

    @SuppressLint("StaticFieldLeak")
    public static LinearLayout mSettingLayout, mRegistrationLayout;
    public static int currentPossition = 0;
    public static Handler sSettingHandler = new Handler();
    public static Handler sMainHandler = new Handler();

    public static void v(String value) {
        if (isDebug) Log.v(TAG, value);
    }

    public static void setSelectedContactDetails(List<FriendContactModel> detail) {
        mFriendContactModelList = detail;
    }

    public static List<FriendContactModel> getContactDetailsList() {
        return mFriendContactModelList;
    }

    public static void setDisplayContactDetails(List<String> allDetail) {
        mFriendDisplayContactModelList = allDetail;
    }

    public static List<String> getDisplayContactDetailsList() {
        return mFriendDisplayContactModelList;
    }

    public static void setAllContactDetails(List<FriendContactModel> allDetail) {
        mFriendAllContactList = allDetail;
    }

    public static List<FriendContactModel> getAllContactDetailsList() {
        return mFriendAllContactList;
    }

    public static  void setSingleFriendContact(List<FriendContactModel> singleFriendContact){
        mSingleFriendContact = singleFriendContact;
    }

    public static List<FriendContactModel> getSingleFriendContact(){
        return mSingleFriendContact;
    }
}
