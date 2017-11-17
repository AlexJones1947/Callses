package com.ispl.callses.Receiver;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.telephony.ITelephony;
import com.bumptech.glide.Glide;
import com.ispl.callses.DB.SqliteDatabase;
import com.ispl.callses.R;
import com.ispl.callses.model.FriendContactModel;
import com.ispl.callses.utils.CallessUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import static android.R.attr.ringtoneType;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;
import static com.ispl.callses.utils.CallessUtils.rootPath;
import static com.ispl.callses.utils.CallessUtils.sFriendName;
import static com.ispl.callses.utils.CallessUtils.sFriendNumber;
import static com.ispl.callses.utils.CallessUtils.sIncomingCallReceiverView;
import static com.ispl.callses.utils.CallessUtils.sIncomingContactNumber;
import static com.ispl.callses.utils.CallessUtils.sIncomingNumber;
import static com.ispl.callses.utils.CallessUtils.sIsSpeakerChecked;
import static com.ispl.callses.utils.CallessUtils.sMessageBoxText;
import static com.ispl.callses.utils.CallessUtils.sMyCustomFontName;
import static com.ispl.callses.utils.CallessUtils.sMyCustomFontSize;
import static com.ispl.callses.utils.CallessUtils.sNameTextBackground;
import static com.ispl.callses.utils.CallessUtils.sNameTextColor;
import static com.ispl.callses.utils.CallessUtils.sOnMissedCall;
import static com.ispl.callses.utils.CallessUtils.sOutgoingCallEnded;
import static com.ispl.callses.utils.CallessUtils.sPhoneTextBackground;
import static com.ispl.callses.utils.CallessUtils.sPhoneTextColor;
import static com.ispl.callses.utils.CallessUtils.sTemp;

public class CallReceiver extends PhoneCallReceiver {
    Context mContext;
    private SqliteDatabase mDatabase;
    String AudioPath = null;

    @Override
    protected void onIncomingCallStarted(final Context ctx, String number, final Date start, int state) {
        sOnMissedCall = false;
        //incomingNumber = number;
        mContext = ctx;
        sIncomingContactNumber = number.substring(Math.max(number.length() - 10, 0));

        CallessUtils.v(" Phone Call Sate IncomingCall Started " + state);

        if (!sTemp) {
            sTemp = true;
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
            sIncomingCallReceiverView = layoutInflater.inflate(R.layout.activity_call_receive, null);

            final WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    PixelFormat.TRANSLUCENT);

            mParams.gravity = Gravity.FILL_VERTICAL;
            mParams.systemUiVisibility = 0;

            wm.addView(sIncomingCallReceiverView, mParams);

            mDatabase = new SqliteDatabase(mContext);

            AudioPath = mDatabase.getRingtoneFromDatabase();

            if (AudioPath == null || AudioPath.equals("")) {
                AudioPath = rootPath + "/test.mp3";
                setRingtone(AudioPath);
            } else {
                setRingtone(AudioPath);
            }

            TextView incoming_call_number = (TextView) sIncomingCallReceiverView.findViewById(R.id.incoming_call_number);
            TextView incoming_call_name = (TextView) sIncomingCallReceiverView.findViewById(R.id.incoming_call_name);
            ImageView dial_icon = (ImageView) sIncomingCallReceiverView.findViewById(R.id.dial_icon);
            final ImageView ivBtnAcceptCall = (ImageView) sIncomingCallReceiverView.findViewById(R.id.imageBtnAccept);
            final ImageView ivBtnIgnoreCall = (ImageView) sIncomingCallReceiverView.findViewById(R.id.imageBtnIgnore);

            if (sPhoneTextColor == 0) {
                incoming_call_number.setTextColor(mContext.getResources().getColor(R.color.black));
            } else {
                incoming_call_number.setTextColor(sPhoneTextColor);
            }
            if (sPhoneTextBackground == 0) {
                incoming_call_number.setBackgroundColor(mContext.getResources().getColor(R.color.orange));
            } else {
                incoming_call_number.setBackgroundColor(sPhoneTextBackground);
            }
            if (sNameTextColor == 0) {
                incoming_call_name.setTextColor(mContext.getResources().getColor(R.color.black));
            } else {
                incoming_call_name.setTextColor(sNameTextColor);
            }
            if (sNameTextBackground == 0) {
                incoming_call_name.setBackgroundColor(mContext.getResources().getColor(R.color.orange));
            } else {
                incoming_call_name.setBackgroundColor(sNameTextBackground);
            }

            Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), sMyCustomFontName);
            incoming_call_number.setTypeface(typeface);
            incoming_call_number.setTextSize(sMyCustomFontSize);
            incoming_call_name.setTypeface(typeface);
            incoming_call_name.setTextSize(sMyCustomFontSize);

            String[] phoneContact = getContactName(number, mContext);
            String namePhoneContact = phoneContact[0];
            String imagePhoneContact = phoneContact[1];

            String imagepathContact = null, nameContact = null;
            if (mDatabase.displayAllFriendContact()) {

                List<FriendContactModel> selectedFriendContactList = CallessUtils.getAllContactDetailsList();

                for (FriendContactModel model : selectedFriendContactList) {
                    imagepathContact = model.getFriendImagePath();
                    nameContact = model.getFriendName();
                }
            } else if (namePhoneContact != null && !namePhoneContact.equals("")) {
                sIncomingContactNumber = number;
                nameContact = namePhoneContact;
            } else {
                sIncomingContactNumber = number;
                nameContact = sMessageBoxText;
            }

            if (imagepathContact != null && !imagepathContact.equals("")) {
                Glide.with(mContext).load(imagepathContact).into(dial_icon);
            } else if (imagePhoneContact != null && !imagePhoneContact.equals("")) {
                Glide.with(mContext).load(imagePhoneContact).into(dial_icon);
            } else {
                dial_icon.setImageResource(R.drawable.contacts);
            }

            incoming_call_number.setText(sIncomingContactNumber);
            incoming_call_name.setText(nameContact);

            final String finalNameContact = nameContact;
            ivBtnAcceptCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    answerCall();
                    sTemp = false;
                    sOnMissedCall = true;
                    wm.removeView(sIncomingCallReceiverView);
                    if (!sTemp) {
                        sTemp = true;
                        callAnswerView(mContext, sIncomingContactNumber, finalNameContact);
                    }
                }
            });
            ivBtnIgnoreCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    killCall();
                    sOnMissedCall = true;
                    wm.removeView(sIncomingCallReceiverView);
                    //sTemp = false;
                }
            });
        }
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end, int state) {
        mContext = ctx;
        CallessUtils.v(" Incoming number is :- " + number);

        final WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wm.removeView(sIncomingCallReceiverView);
                sTemp = false;
            }
        }, 1000);
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start, int state) {
        super.onOutgoingCallStarted(ctx, number, start, state);
        mContext = ctx;
        sIncomingNumber = number;
        Boolean isRunning = isAppRunning(mContext);
        if (isRunning) {
            if (!sTemp) {
                sTemp = true;
                CallessUtils.v(" Phone Call Sate onOutgoingCallStarted " + state);
                callAnswerView(mContext, sFriendNumber, sFriendName);
            }
        }
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end, int state) {
        mContext = ctx;
        Boolean isRunning = isAppRunning(mContext);
        if (isRunning) {
            if (!sOutgoingCallEnded) {
                sOutgoingCallEnded = true;
            } else {
                sOutgoingCallEnded = false;

                CallessUtils.v(" Phone Call Sate onOutgoingCallEnded " + state);
                CallessUtils.v(" Incoming Number 1 " + sIncomingNumber);
                CallessUtils.v(" Incoming Number 2 " + number);
                if (sIncomingNumber.equals(number)) {
                    final WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            wm.removeView(sIncomingCallReceiverView);
                            sTemp = false;
                        }
                    }, 1000);
                }
            }
        }
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start, int state) {
        mContext = ctx;

            if (sOnMissedCall)
                return;
        CallessUtils.v(" Phone Call Sate onMissedCall " + state);
        if(state == 0){
            sTemp = false;
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(sIncomingCallReceiverView);
        }
    }

    public String[] getContactName(final String phoneNumber, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projectionName = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};
        String[] projectionImage = new String[]{ContactsContract.PhoneLookup.PHOTO_URI};

        String contactName = "", imageURI = "";
        Cursor cursor = context.getContentResolver().query(uri, projectionName, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
            }
            cursor.close();
        }
        Cursor cursorImage = context.getContentResolver().query(uri, projectionImage, null, null, null);

        if (cursorImage != null) {
            if (cursorImage.moveToFirst()) {
                imageURI = cursorImage.getString(0);
            }
            cursorImage.close();
        }

        return new String[]{contactName, imageURI};
    }

    private void acceptCall() {
        try {
            // Get the getITelephony() method
            TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> classTelephony = Class.forName(telephonyManager.getClass().getName());
            Method method = classTelephony.getDeclaredMethod("getITelephony");
            // Disable access check
            method.setAccessible(true);
            // Invoke getITelephony() to get the ITelephony interface
            Object telephonyInterface = method.invoke(telephonyManager);
            // Get the endCall method from ITelephony
            Class<?> telephonyInterfaceClass = Class.forName(telephonyInterface.getClass().getName());
            Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod("answerRingingCall");
            // Invoke endCall()
            methodEndCall.invoke(telephonyInterface);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

   /* public void acceptCall() {

        // Make sure the phone is still ringing
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getCallState() != TelephonyManager.CALL_STATE_RINGING) {
            return;
        }

        // Answer the phone
        try {
            answerPhoneAidl(mContext);
        } catch (Exception e) {
            e.printStackTrace();
            answerPhoneHeadsethook(mContext);
        }
    }*/

    private void answerPhoneHeadsethook(Context context) {
        // Simulate a press of the headset button to pick up the call
        Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
        buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
        context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");

        // froyo and beyond trigger on buttonUp instead of buttonDown
        Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
        buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
        context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void answerPhoneAidl(Context context) throws Exception {
        // Set up communication with the telephony service (thanks to Tedd's Droid Tools!)
        TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        Class c = Class.forName(tm.getClass().getName());
        Method m = c.getDeclaredMethod("getITelephony");
        m.setAccessible(true);
        ITelephony telephonyService;
        telephonyService = (ITelephony) m.invoke(tm);

        // Silence the ringer and answer the call!
        telephonyService.silenceRinger();
        telephonyService.answerRingingCall();
    }

    public void answerCall() {
        try {
            Runtime.getRuntime().exec("input keyevent " + Integer.toString(KeyEvent.KEYCODE_HEADSETHOOK));

        } catch (IOException e) {
            answerRingingCallWithIntent();
        }
    }

    public void answerRingingCallWithIntent() {
        try {
            Intent localIntent1 = new Intent(Intent.ACTION_HEADSET_PLUG);
            localIntent1.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            localIntent1.putExtra("state", 1);
            localIntent1.putExtra("microphone", 1);
            localIntent1.putExtra("name", "Headset");
            mContext.sendOrderedBroadcast(localIntent1, "android.permission.CALL_PRIVILEGED");

            Intent localIntent2 = new Intent(Intent.ACTION_MEDIA_BUTTON);
            KeyEvent localKeyEvent1 = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK);
            localIntent2.putExtra(Intent.EXTRA_KEY_EVENT, localKeyEvent1);
            mContext.sendOrderedBroadcast(localIntent2, "android.permission.CALL_PRIVILEGED");

            Intent localIntent3 = new Intent(Intent.ACTION_MEDIA_BUTTON);
            KeyEvent localKeyEvent2 = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);
            localIntent3.putExtra(Intent.EXTRA_KEY_EVENT, localKeyEvent2);
            mContext.sendOrderedBroadcast(localIntent3, "android.permission.CALL_PRIVILEGED");

            Intent localIntent4 = new Intent(Intent.ACTION_HEADSET_PLUG);
            localIntent4.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            localIntent4.putExtra("state", 0);
            localIntent4.putExtra("microphone", 1);
            localIntent4.putExtra("name", "Headset");
            mContext.sendOrderedBroadcast(localIntent4, "android.permission.CALL_PRIVILEGED");

        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public void killCall() {
        try {
            TelephonyManager telephonyManager =
                    (TelephonyManager) mContext.getSystemService(TELEPHONY_SERVICE);

            Class classTelephony = Class.forName(telephonyManager.getClass().getName());
            Method methodGetITelephony = classTelephony.getDeclaredMethod("getITelephony");

            methodGetITelephony.setAccessible(true);

            Object telephonyInterface = methodGetITelephony.invoke(telephonyManager);

            Class telephonyInterfaceClass =
                    Class.forName(telephonyInterface.getClass().getName());
            Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall");

            methodEndCall.invoke(telephonyInterface);

        } catch (Exception ex) {
            // Log.d(TAG, "PhoneStateReceiver **" + ex.toString());
        }
    }

    public void setRingtone(String audioSavePath) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, audioSavePath);
        values.put(MediaStore.MediaColumns.TITLE, "ring");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
        values.put(MediaStore.MediaColumns.SIZE, 0);
        values.put(MediaStore.Audio.Media.ARTIST, R.string.app_name);
        values.put(MediaStore.Audio.Media.IS_RINGTONE, ringtoneType == RingtoneManager.TYPE_RINGTONE);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, ringtoneType == RingtoneManager.TYPE_NOTIFICATION);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(mContext.getApplicationContext())) {
                Uri uri = MediaStore.Audio.Media.getContentUriForPath(audioSavePath);
                if (uri != null) {
                    mContext.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + mContext + "\"", null);
                    Uri newUri = mContext.getContentResolver().insert(uri, values);
                    try {
                        RingtoneManager.setActualDefaultRingtoneUri(
                                mContext, RingtoneManager.TYPE_RINGTONE, newUri);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        } else {
            Uri uri = MediaStore.Audio.Media.getContentUriForPath(audioSavePath);
            if (uri != null) {
                mContext.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + audioSavePath + "\"", null);
                Uri newUri = mContext.getContentResolver().insert(uri, values);
                try {
                    RingtoneManager.setActualDefaultRingtoneUri(
                            mContext, RingtoneManager.TYPE_RINGTONE,
                            newUri);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    public void callAnswerView(Context ctx, String number, String name) {
        mContext = ctx;

        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        sIncomingCallReceiverView = layoutInflater.inflate(R.layout.activity_call_in_layout, null);

        final WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.CENTER_VERTICAL;
        wm.addView(sIncomingCallReceiverView, mParams);

        TextView incoming_call_number = (TextView) sIncomingCallReceiverView.findViewById(R.id.incoming_call_number);
        TextView incoming_call_name = (TextView) sIncomingCallReceiverView.findViewById(R.id.incoming_call_name);
        ImageView dial_icon = (ImageView) sIncomingCallReceiverView.findViewById(R.id.dial_icon);
        final ImageView speaker_off_iv = (ImageView) sIncomingCallReceiverView.findViewById(R.id.speaker_off_iv);
        final ImageView speaker_on_iv = (ImageView) sIncomingCallReceiverView.findViewById(R.id.speaker_on_iv);

        if (sPhoneTextColor == 0) {
            incoming_call_number.setTextColor(mContext.getResources().getColor(R.color.black));
        } else {
            incoming_call_number.setTextColor(sPhoneTextColor);
        }
        if (sPhoneTextBackground == 0) {
            incoming_call_number.setBackgroundColor(mContext.getResources().getColor(R.color.orange));
            speaker_off_iv.setBackgroundColor(mContext.getResources().getColor(R.color.orange));
            speaker_on_iv.setBackgroundColor(mContext.getResources().getColor(R.color.orange));
        } else {
            incoming_call_number.setBackgroundColor(sPhoneTextBackground);
            speaker_off_iv.setBackgroundColor(sPhoneTextBackground);
            speaker_on_iv.setBackgroundColor(sPhoneTextBackground);
        }
        if (sNameTextColor == 0) {
            incoming_call_name.setTextColor(mContext.getResources().getColor(R.color.black));
        } else {
            incoming_call_name.setTextColor(sNameTextColor);
        }
        if (sNameTextBackground == 0) {
            incoming_call_name.setBackgroundColor(mContext.getResources().getColor(R.color.orange));
        } else {
            incoming_call_name.setBackgroundColor(sNameTextBackground);
        }

        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), sMyCustomFontName);
        incoming_call_number.setTypeface(typeface);
        incoming_call_number.setTextSize(sMyCustomFontSize);
        incoming_call_name.setTypeface(typeface);
        incoming_call_name.setTextSize(sMyCustomFontSize);

        incoming_call_number.setText(number);
        incoming_call_name.setText(name);

        dial_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                killCall();
                sTemp =false;
            }
        });

        if (sIsSpeakerChecked)
            speaker_on_iv.setVisibility(View.VISIBLE);

        if (speaker_on_iv.getVisibility() == View.VISIBLE) {
            AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setSpeakerphoneOn(true);
        }

        speaker_off_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //speaker_off_iv.setVisibility(View.GONE);
                if (speaker_on_iv.getVisibility() == View.GONE) {
                    speaker_on_iv.setVisibility(View.VISIBLE);
                    AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                    audioManager.setSpeakerphoneOn(true);
                }
            }
        });
        speaker_on_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speaker_off_iv.setVisibility(View.VISIBLE);
                speaker_on_iv.setVisibility(View.GONE);
                AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setSpeakerphoneOn(false);
            }
        });
    }

    public static boolean isAppRunning(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
                return true;
        }
        return false;
    }
}