package com.ispl.callses.Fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ispl.callses.DB.SqliteDatabase;
import com.ispl.callses.MainActivity;
import com.ispl.callses.R;
import com.ispl.callses.model.FriendContactModel;
import com.ispl.callses.utils.CallessUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.ispl.callses.utils.CallessUtils.mRegistrationLayout;
import static com.ispl.callses.utils.CallessUtils.mSettingLayout;
import static com.ispl.callses.utils.CallessUtils.rootPath;
import static com.ispl.callses.utils.CallessUtils.sIsRegistrationChecked;
import static com.ispl.callses.utils.CallessUtils.sMainHandler;
import static com.ispl.callses.utils.CallessUtils.sSettingHandler;

/**
 * Created by infinium on 20/07/17.
 */

public class InsertRegistrationFragment extends Fragment {
    private Switch mMainSwitch, mRecordMessageSwitch;
    private LinearLayout mRegistrationSubLL;
    private ImageView mFriendContactAddImage;
    private final int PICK_PHOTO = 1;
    private String mSelectedImagePath = null;
    Bitmap mBitmap;
    private EditText mFriendContactNumber, mFriendCotactName, mFriendListOfOderNumber;
    private Button mBtnFriendContactSave, mBtnFriendContactDelete, mBtnFriendContactModify, mBtnFriendContactClearFiled,
            mBtnFriendContactShowLastRecordClear, mBtnStartRecording, mBtnStopRecording, mBtnPlayLastRecordedAudio, mBtnStopPlayingRecording;
    private LinearLayout mRecordMessageLL;
    private LinearLayout mSaveContactLL, mDeleteContactLL, mModifyContactLL;
    private SharedPreferences sharedPreferencesCustomFontSize;
    public static final String MyPreferencesCustomFontSize = "MyPrefesCusomFontSize";
    public String AudioSavePath = null;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    private SqliteDatabase mDatabase;
    public static String mUserFileRecordingName = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View layout_view = inflater.inflate(R.layout.fragment_edit_registration, container, false);

        sMainHandler.removeCallbacksAndMessages(null);
        ((MainActivity) getActivity()).setPhoneCallVisibility(false);
        mRegistrationLayout.setTag("2");
        mSettingLayout.setTag("1");
        sSettingHandler.removeCallbacksAndMessages(null);
        AudioSavePath = null;

        mMainSwitch = (Switch) layout_view.findViewById(R.id.registration_main_switch);
        mRecordMessageSwitch = (Switch) layout_view.findViewById(R.id.record_message_switch);

        mRegistrationSubLL = (LinearLayout) layout_view.findViewById(R.id.registration_sub_ll);

        mFriendContactAddImage = (ImageView) layout_view.findViewById(R.id.add_cotacts_image);

        mFriendContactNumber = (EditText) layout_view.findViewById(R.id.friend_phone_no_edit_text);
        mFriendCotactName = (EditText) layout_view.findViewById(R.id.friend_phone_name_edit_text);
        mFriendListOfOderNumber = (EditText) layout_view.findViewById(R.id.list_order_no_edit_text);

        mBtnFriendContactSave = (Button) layout_view.findViewById(R.id.save_btn);
        mBtnFriendContactDelete = (Button) layout_view.findViewById(R.id.delete_btn);
        mBtnFriendContactModify = (Button) layout_view.findViewById(R.id.modify_btn);
        mBtnFriendContactClearFiled = (Button) layout_view.findViewById(R.id.clear_filed_btn);
        mBtnFriendContactShowLastRecordClear = (Button) layout_view.findViewById(R.id.clear_show_last_record_btn);

        mBtnStartRecording = (Button) layout_view.findViewById(R.id.start_record_btn);
        mBtnStopRecording = (Button) layout_view.findViewById(R.id.stop_record_btn);
        mBtnPlayLastRecordedAudio = (Button) layout_view.findViewById(R.id.play_record_btn);
        mBtnStopPlayingRecording = (Button) layout_view.findViewById(R.id.stop_playing_record_btn);

        mRecordMessageLL = (LinearLayout) layout_view.findViewById(R.id.record_message_ll);

        sharedPreferencesCustomFontSize = getActivity().getSharedPreferences(MyPreferencesCustomFontSize, Context.MODE_PRIVATE);

        mSaveContactLL = (LinearLayout) layout_view.findViewById(R.id.save_contact_ll);
        mDeleteContactLL = (LinearLayout) layout_view.findViewById(R.id.delete_contact_ll);
        mModifyContactLL = (LinearLayout) layout_view.findViewById(R.id.modify_contact_ll);

        mBtnStopRecording.setVisibility(View.GONE);
        mBtnStopPlayingRecording.setVisibility(View.GONE);

        mDatabase = new SqliteDatabase(getContext());
        mDatabase.displayFriendContactOrderNumber();

        mBtnFriendContactSave.setTag("1");

        if (sIsRegistrationChecked) {
            mMainSwitch.setChecked(true);
            setMainSwitchOn(mRegistrationSubLL, true);
            deleteAndModifyBtn(false);
        } else {
            mMainSwitch.setChecked(false);
            setMainSwitchOn(mRegistrationSubLL, false);

        }
        mRecordMessageSwitch.setChecked(false);
        setRecordMesageSwitchOn(mRecordMessageLL, false);

        mMainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                sIsRegistrationChecked = isChecked;
                SharedPreferences.Editor editor = sharedPreferencesCustomFontSize.edit();
                editor.putBoolean("Registration_Switch", sIsRegistrationChecked);
                editor.apply();

                if (sIsRegistrationChecked) {
                    setMainSwitchOn(mRegistrationSubLL, true);
                    deleteAndModifyBtn(false);

                } else {
                    setMainSwitchOn(mRegistrationSubLL, false);
                    deleteAndModifyBtn(false);
                }
            }
        });

        mRecordMessageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    setRecordMesageSwitchOn(mRecordMessageLL, true);
                } else {
                    setRecordMesageSwitchOn(mRecordMessageLL, false);
                }
            }
        });

        mFriendContactAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_PHOTO);
            }
        });

        mBtnStartRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                String phoneMumber = mFriendContactNumber.getText().toString();

                if (TextUtils.isEmpty(phoneMumber)) {
                    Toast.makeText(getActivity(), "Enter Friend Phone Number ", Toast.LENGTH_SHORT).show();
                } else {
                    mUserFileRecordingName = mFriendContactNumber.getText().toString();

                    mMainSwitch.setClickable(false);
                    mRecordMessageSwitch.setClickable(false);
                    mBtnPlayLastRecordedAudio.setEnabled(false);
                    mBtnPlayLastRecordedAudio.setBackgroundColor(Color.parseColor("#D1C4E9"));
                    AudioSavePath = rootPath + "/" + mUserFileRecordingName + ".mp3";

                    MediaRecorderReady();

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException | IOException e) {
                        e.printStackTrace();
                    }

                    mBtnStartRecording.setVisibility(View.GONE);
                    mBtnStopRecording.setVisibility(View.VISIBLE);
                }
            }
        });

        mBtnStopRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                try {
                    mediaRecorder.stop();
                } catch (RuntimeException ex) {
                    //Ignore
                    ex.printStackTrace();
                }

                mBtnStopRecording.setVisibility(View.GONE);
                mBtnStartRecording.setVisibility(View.VISIBLE);
                mBtnPlayLastRecordedAudio.setEnabled(true);
                mBtnPlayLastRecordedAudio.setBackgroundColor(getResources().getColor(R.color.play_record_btn));
                mMainSwitch.setClickable(true);
                mRecordMessageSwitch.setClickable(true);
            }
        });

        mBtnPlayLastRecordedAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                mMainSwitch.setClickable(false);
                mRecordMessageSwitch.setClickable(false);
                mBtnPlayLastRecordedAudio.setVisibility(View.GONE);
                mBtnStopPlayingRecording.setVisibility(View.VISIBLE);
                mBtnStartRecording.setEnabled(false);
                mBtnStartRecording.setBackgroundColor(Color.parseColor("#FFCCBC"));

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioSavePath);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mMainSwitch.setClickable(true);
                        mRecordMessageSwitch.setClickable(true);
                        mBtnPlayLastRecordedAudio.setVisibility(View.VISIBLE);
                        mBtnStopPlayingRecording.setVisibility(View.GONE);
                        mBtnStartRecording.setEnabled(true);
                        mBtnStartRecording.setBackgroundColor(getResources().getColor(R.color.start_record_btn));
                    }
                });
            }
        });

        mBtnStopPlayingRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainSwitch.setClickable(true);
                mRecordMessageSwitch.setClickable(true);
                mBtnStopPlayingRecording.setVisibility(View.GONE);
                mBtnPlayLastRecordedAudio.setVisibility(View.VISIBLE);
                mBtnStartRecording.setEnabled(true);
                mBtnStartRecording.setBackgroundColor(getResources().getColor(R.color.start_record_btn));

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    MediaRecorderReady();
                }
            }
        });

        mBtnFriendContactDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_delete);
                dialog.getWindow().setLayout(DrawerLayout.LayoutParams.MATCH_PARENT, DrawerLayout.LayoutParams.WRAP_CONTENT);
                dialog.setCanceledOnTouchOutside(false);
                TextView textView = (TextView) dialog.findViewById(R.id.progress_tv);
                Button delete = (Button) dialog.findViewById(R.id.delete_btn);
                Button cancel = (Button) dialog.findViewById(R.id.cancel_btn);

                textView.setText("Are you sure you want to delete ??");
                dialog.show();

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clearFormData();
                        deleteAndModifyBtn(false);
                        for (int i = 0; i < mSaveContactLL.getChildCount(); i++) {
                            View child = mSaveContactLL.getChildAt(i);
                            child.setEnabled(false);
                        }

                        for (int i = 0; i < mSaveContactLL.getChildCount(); i++) {
                            View child = mSaveContactLL.getChildAt(i);
                            child.setEnabled(true);
                        }
                        mBtnFriendContactSave.setBackgroundColor(getResources().getColor(R.color.save_btn));
                        mBtnFriendContactClearFiled.setVisibility(View.VISIBLE);
                        mBtnFriendContactShowLastRecordClear.setVisibility(View.GONE);
                        mDatabase.deleteFriendContact();
                        dialog.dismiss();
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });

        mBtnFriendContactModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                String friendContactNumber = mFriendContactNumber.getText().toString();
                String friendContactName = mFriendCotactName.getText().toString();
                String friendContactListOfOrder = mFriendListOfOderNumber.getText().toString();

                if (TextUtils.isEmpty(mSelectedImagePath)) {
                    Toast.makeText(getContext(), "Please Select Image", Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(friendContactNumber)) {
                    Toast.makeText(getContext(), "Enter Phone Number", Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(friendContactName)) {
                    Toast.makeText(getContext(), "Enter Name", Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(friendContactListOfOrder)) {
                    Toast.makeText(getContext(), "Enter List Order Number", Toast.LENGTH_LONG).show();
                } else {
                    Boolean isPhoneExist = mDatabase.isExist(friendContactNumber);
                    int idOldId = mDatabase.idFromDataBase(friendContactNumber);
                    int currentContactId = mDatabase.currentContactId();
                    if (isPhoneExist) {
                        if (idOldId == currentContactId) {
                            FriendContactModel modifyFriendContact = new FriendContactModel(mSelectedImagePath, friendContactNumber, friendContactNumber, friendContactName, friendContactListOfOrder, AudioSavePath);
                            mDatabase.updateFriendContact(modifyFriendContact);
                            Toast.makeText(getContext(), "Data is Updated ..!!", Toast.LENGTH_LONG).show();
                        } else {
                            mFriendContactNumber.requestFocus();
                            Toast.makeText(getActivity(), "Number " + friendContactNumber + " is already is exist.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        FriendContactModel modifyFriendContact = new FriendContactModel(mSelectedImagePath, friendContactNumber, friendContactNumber, friendContactName, friendContactListOfOrder, AudioSavePath);
                        mDatabase.updateFriendContact(modifyFriendContact);
                        Toast.makeText(getContext(), "Data is Updated ..!!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        mBtnFriendContactClearFiled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFormData();
            }
        });

        mBtnFriendContactShowLastRecordClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.displayFriendContact();

                List<FriendContactModel> selectedFriendContactList = CallessUtils.getContactDetailsList();

                for (FriendContactModel model : selectedFriendContactList) {
                    String imagepathContact = model.getFriendImagePath();
                    String number = model.getFriendNumber();
                    String name = model.getFriendName();
                    String listofOrder = model.getFriendListOfOreder();
                    String audioMessage = model.getFriendAudioMessage();

                    Glide.with(getActivity()).load(imagepathContact).into(mFriendContactAddImage);

                    mFriendContactNumber.setText(number);
                    mFriendCotactName.setText(name);
                    mFriendListOfOderNumber.setText(listofOrder);
                    AudioSavePath = audioMessage;
                }
            }
        });

        mBtnFriendContactSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                String friendContactNumber = mFriendContactNumber.getText().toString();
                String friendContactName = mFriendCotactName.getText().toString();
                String friendContactListOfOrder = mFriendListOfOderNumber.getText().toString();

                if (TextUtils.isEmpty(mSelectedImagePath)) {
                    Toast.makeText(getContext(), "Please Select Image", Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(friendContactNumber)) {
                    Toast.makeText(getContext(), "Please Enter Phone Number", Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(friendContactName)) {
                    Toast.makeText(getContext(), "Please Enter Name", Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(friendContactListOfOrder)) {
                    Toast.makeText(getContext(), "Please Enter List Order Number", Toast.LENGTH_LONG).show();
                } else {

                    Boolean isCheck = mDatabase.isExist(friendContactNumber);
                    if (isCheck) {
                        mFriendContactNumber.getText().clear();
                        mFriendContactNumber.requestFocus();
                        Toast.makeText(getActivity(), "Number " + friendContactNumber + " is already is exist.", Toast.LENGTH_SHORT).show();
                    } else {

                        FriendContactModel newFriendContact = new FriendContactModel(mSelectedImagePath, friendContactNumber, friendContactNumber, friendContactName, friendContactListOfOrder, AudioSavePath);
                        mDatabase.addFriendContact(newFriendContact);
                        Toast.makeText(getContext(), "Contact is added successfully ..!!", Toast.LENGTH_LONG).show();

                        deleteAndModifyBtn(true);

                        mMainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(CompoundButton buttonView,
                                                         boolean isChecked) {

                                if (isChecked) {
                                    setMainSwitchOn(mRegistrationSubLL, true);
                                    deleteAndModifyBtn(true);

                                } else {
                                    setMainSwitchOn(mRegistrationSubLL, false);
                                }
                            }
                        });

                        for (int i = 0; i < mSaveContactLL.getChildCount(); i++) {
                            View child = mSaveContactLL.getChildAt(i);
                            child.setEnabled(false);
                        }
                        mBtnFriendContactSave.setBackgroundColor(Color.parseColor("#C8E6C9"));
                        mBtnFriendContactSave.setTag("2");
                        mBtnFriendContactClearFiled.setVisibility(View.GONE);
                        mBtnFriendContactShowLastRecordClear.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        return layout_view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDatabase != null) {
            mDatabase.close();
        }
        ((MainActivity) getActivity()).getColorFromPrefrances();
        ((MainActivity) getActivity()).setPhoneCallData();
        ((MainActivity) getActivity()).setPhoneCallVisibility(true);
        mRegistrationLayout.setTag("1");
    }

    private void setMainSwitchOn(ViewGroup layout, Boolean enable) {
        if (enable) {
            layout.setEnabled(true);
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof ViewGroup) {
                    setMainSwitchOn((ViewGroup) child, true);

                    if (mRecordMessageSwitch.isChecked()) {
                        mBtnStartRecording.setBackgroundColor(getResources().getColor(R.color.start_record_btn));

                        if (AudioSavePath != null) {
                            mBtnPlayLastRecordedAudio.setEnabled(true);
                            mBtnPlayLastRecordedAudio.setBackgroundColor(getResources().getColor(R.color.play_record_btn));
                        } else {
                            mBtnPlayLastRecordedAudio.setBackgroundColor(Color.parseColor("#D1C4E9"));
                            mBtnPlayLastRecordedAudio.setEnabled(false);
                        }
                    } else {
                        setRecordMesageSwitchOn(mRecordMessageLL, false);

                    }

                    if (mBtnFriendContactSave.getTag().toString().equals("1")) {
                        mBtnFriendContactSave.setBackgroundColor(getResources().getColor(R.color.save_btn));
                    } else {
                        for (int k = 0; k < mSaveContactLL.getChildCount(); k++) {
                            View child1 = mSaveContactLL.getChildAt(k);
                            child1.setEnabled(false);
                        }
                        mBtnFriendContactSave.setBackgroundColor(Color.parseColor("#C8E6C9"));
                    }
                    mBtnFriendContactDelete.setBackgroundColor(getResources().getColor(R.color.delete_btn));
                    mBtnFriendContactModify.setBackgroundColor(getResources().getColor(R.color.modify_btn));
                    mBtnFriendContactClearFiled.setBackgroundColor(getResources().getColor(R.color.clear_btn));

                    if (mBtnFriendContactShowLastRecordClear.isEnabled()) {
                        mBtnFriendContactShowLastRecordClear.setEnabled(true);
                        mBtnFriendContactShowLastRecordClear.setBackgroundColor(getResources().getColor(R.color.clear_btn));
                    }
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
                    mBtnStartRecording.setBackgroundColor(Color.parseColor("#FFCCBC"));
                    mBtnPlayLastRecordedAudio.setBackgroundColor(Color.parseColor("#D1C4E9"));
                    mBtnFriendContactSave.setBackgroundColor(Color.parseColor("#C8E6C9"));
                    mBtnFriendContactDelete.setBackgroundColor(Color.parseColor("#EF9A9A"));
                    mBtnFriendContactModify.setBackgroundColor(Color.parseColor("#F0F4C3"));
                    mBtnFriendContactClearFiled.setBackgroundColor(Color.parseColor("#B2EBF2"));

                    if (mBtnFriendContactShowLastRecordClear.isEnabled()) {
                        mBtnFriendContactShowLastRecordClear.setEnabled(false);
                        mBtnFriendContactShowLastRecordClear.setBackgroundColor(Color.parseColor("#B2EBF2"));
                    }
                } else {
                    child.setEnabled(false);
                }
            }
        }
    }
    public void setRecordMesageSwitchOn(ViewGroup layout, Boolean enable) {
        if (enable) {
            layout.setEnabled(true);
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof ViewGroup) {
                    setMainSwitchOn((ViewGroup) child, true);

                    if (AudioSavePath != null) {
                        mBtnPlayLastRecordedAudio.setEnabled(true);
                        mBtnPlayLastRecordedAudio.setBackgroundColor(getResources().getColor(R.color.play_record_btn));
                    } else {
                        mBtnPlayLastRecordedAudio.setBackgroundColor(Color.parseColor("#D1C4E9"));
                        mBtnPlayLastRecordedAudio.setEnabled(false);
                    }

                    mBtnStartRecording.setBackgroundColor(getResources().getColor(R.color.start_record_btn));

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
                    mBtnStartRecording.setBackgroundColor(Color.parseColor("#FFCCBC"));
                    mBtnPlayLastRecordedAudio.setBackgroundColor(Color.parseColor("#D1C4E9"));
                } else {
                    child.setEnabled(false);
                }
            }
        }
    }

    public void deleteAndModifyBtn(Boolean enable) {
        if (enable) {
            for (int i = 0; i < mDeleteContactLL.getChildCount(); i++) {
                View child = mDeleteContactLL.getChildAt(i);
                child.setEnabled(true);
            }
            for (int i = 0; i < mModifyContactLL.getChildCount(); i++) {
                View child = mModifyContactLL.getChildAt(i);
                child.setEnabled(true);
            }
            mBtnFriendContactDelete.setBackgroundColor(getResources().getColor(R.color.delete_btn));
            mBtnFriendContactModify.setBackgroundColor(getResources().getColor(R.color.modify_btn));
        } else {
            for (int i = 0; i < mDeleteContactLL.getChildCount(); i++) {
                View child = mDeleteContactLL.getChildAt(i);
                child.setEnabled(false);
            }
            for (int i = 0; i < mModifyContactLL.getChildCount(); i++) {
                View child = mModifyContactLL.getChildAt(i);
                child.setEnabled(false);
            }
            mBtnFriendContactDelete.setBackgroundColor(Color.parseColor("#EF9A9A"));
            mBtnFriendContactModify.setBackgroundColor(Color.parseColor("#F0F4C3"));
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PICK_PHOTO:
                if (resultCode == RESULT_OK) {
                    onSelectFromGalleryResult(data);
                }
        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageURI = data.getData();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mSelectedImagePath = getRealPathFromURI(getActivity(), selectedImageURI);
            if (mSelectedImagePath == null) {
                mSelectedImagePath = getRealPathFromURI(selectedImageURI);
            }
        } else {
            mSelectedImagePath = getRealPathFromURI(selectedImageURI);
        }

        Glide.with(getActivity())
                .load(new File(mSelectedImagePath))
                .asBitmap()
                .into(new BitmapImageViewTarget(mFriendContactAddImage) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        // Do bitmap magic here
                        super.setResource(resource);
                        mBitmap = resource;
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getRealPathFromURI(Context context, Uri uri) {
        String filePath = null;
        try {
            String wholeID = null;
            wholeID = DocumentsContract.getDocumentId(uri);
// Split at colon, use second item in the array
            String id = wholeID.split(":")[1];
            String[] column = {MediaStore.Images.Media.DATA};
// where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";
            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{id}, null);
            int columnIndex = cursor.getColumnIndex(column[0]);
            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
            return filePath;
        } catch (Exception e) {
            return null;
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result = null;
        try {
            Cursor cursor = getActivity().getContentResolver().query(contentURI, null, null, null, null);
            if (cursor == null) { // Source is Dropbox or other similar local file path
                result = contentURI.getPath();
            } else {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
                cursor.close();
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public void MediaRecorderReady() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePath);
    }

    public void clearFormData() {
        mFriendContactAddImage.setImageResource(R.drawable.contacts);
        mSelectedImagePath = "";
        mFriendContactNumber.getText().clear();
        mFriendCotactName.getText().clear();
        mFriendListOfOderNumber.getText().clear();
        mBtnPlayLastRecordedAudio.setEnabled(false);
        mBtnPlayLastRecordedAudio.setBackgroundColor(Color.parseColor("#D1C4E9"));
        mFriendContactNumber.requestFocus();
    }
}
