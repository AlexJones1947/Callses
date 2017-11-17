package com.ispl.callses.Adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ispl.callses.DB.SqliteDatabase;
import com.ispl.callses.Fragment.EditRegistrationFragment;
import com.ispl.callses.MainActivity;
import com.ispl.callses.R;
import com.ispl.callses.model.FriendContactModel;
import com.ispl.callses.utils.CallessUtils;
import com.ispl.callses.utils.KeyUtils;

import java.util.List;

import static com.ispl.callses.utils.CallessUtils.currentPossition;
import static com.ispl.callses.utils.CallessUtils.sFriendName;
import static com.ispl.callses.utils.CallessUtils.sFriendNumber;
import static com.ispl.callses.utils.CallessUtils.sIsSpeakerChecked;
import static com.ispl.callses.utils.CallessUtils.sMainHandler;
import static com.ispl.callses.utils.CallessUtils.sMyCustomFontName;
import static com.ispl.callses.utils.CallessUtils.sMyCustomFontSize;


/**
 * Created by infinium on 21/07/17.
 */

public class FriendContactAdapter extends RecyclerView.Adapter<FriendContactAdapter.FriendContactViewHolder> {
    private Context mContext;
    public List<FriendContactModel> listProducts;
    private SqliteDatabase mDatabase;
    private TelephonyManager mTelephonyManager;
    private StatePhoneReceiver myPhoneStateListener;

    private boolean callFromApp = false; // To control the call has been made from the application
    private boolean callFromOffHook = false; // To control the change to idle state is from the app call

    public FriendContactAdapter(Context context, List<FriendContactModel> listProducts) {
        this.mContext = context;
        this.listProducts = listProducts;
        mDatabase = new SqliteDatabase(context);
    }
    /*@Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }*/

    @Override
    public FriendContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_friend_layout, parent, false);
        return new FriendContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendContactViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final FriendContactModel singleProduct = listProducts.get(position);

        currentPossition = position;

        if (CallessUtils.sPhoneTextColor == 0) {
            holder.incoming_call_number.setTextColor(mContext.getResources().getColor(R.color.black));
        } else {
            holder.incoming_call_number.setTextColor(CallessUtils.sPhoneTextColor);
        }
        if (CallessUtils.sPhoneTextBackground == 0) {
            holder.incoming_call_number.setBackgroundColor(mContext.getResources().getColor(R.color.orange));
        } else {
            holder.incoming_call_number.setBackgroundColor(CallessUtils.sPhoneTextBackground);
        }
        if (CallessUtils.sNameTextColor == 0) {
            holder.incoming_call_name.setTextColor(mContext.getResources().getColor(R.color.black));
        } else {
            holder.incoming_call_name.setTextColor(CallessUtils.sNameTextColor);
        }
        if (CallessUtils.sNameTextBackground == 0) {
            holder.incoming_call_rl.setBackgroundColor(mContext.getResources().getColor(R.color.orange));
        } else {
            holder.incoming_call_rl.setBackgroundColor(CallessUtils.sNameTextBackground);
        }

        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), sMyCustomFontName);
        holder.incoming_call_number.setTypeface(typeface);
        holder.incoming_call_number.setTextSize(sMyCustomFontSize);
        holder.incoming_call_name.setTypeface(typeface);
        holder.incoming_call_name.setTextSize(sMyCustomFontSize);

        holder.incoming_call_number.setText(singleProduct.getFriendNumberShow());
        holder.incoming_call_name.setText(singleProduct.getFriendName());
        String imageUrl = singleProduct.getFriendImagePath();
        if (imageUrl != null && !imageUrl.equalsIgnoreCase("")) {
            Glide.with(mContext).load(imageUrl).into(holder.dial_icon);
        } else {
            holder.dial_icon.setImageResource(R.drawable.contacts);
        }

        holder.incoming_call_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String friendNumber = singleProduct.getFriendNumberShow();
                String friendName = singleProduct.getFriendName();

                sFriendNumber = friendNumber;
                sFriendName = friendName;

                if (sIsSpeakerChecked) {
                    myPhoneStateListener = new StatePhoneReceiver(mContext);
                    mTelephonyManager = ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE));

                    mTelephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE); // start listening to the phone changes
                    callFromApp = true;
                }

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + friendNumber));
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mContext.startActivity(callIntent);
            }
        });

        holder.dial_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String friendNumber = singleProduct.getFriendNumberShow();
                String friendName = singleProduct.getFriendName();

                sFriendNumber = friendNumber;
                sFriendName = friendName;

                if (sIsSpeakerChecked) {
                    myPhoneStateListener = new StatePhoneReceiver(mContext);
                    mTelephonyManager = ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE));

                    mTelephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE); // start listening to the phone changes
                    callFromApp = true;
                }

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + friendNumber));
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mContext.startActivity(callIntent);
            }
        });

        holder.delete_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int id = mDatabase.idFromDataBase(singleProduct.getFriendNumber());

                final Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_delete);
                dialog.getWindow().setLayout(DrawerLayout.LayoutParams.MATCH_PARENT, DrawerLayout.LayoutParams.WRAP_CONTENT);
                dialog.setCanceledOnTouchOutside(false);
                TextView textView = (TextView) dialog.findViewById(R.id.progress_tv);
                Button delete = (Button) dialog.findViewById(R.id.delete_btn);
                Button cancel = (Button) dialog.findViewById(R.id.cancel_btn);

                textView.setText("Are you sure you want to delete ??");
                sMainHandler.removeCallbacksAndMessages(null);
                dialog.show();

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDatabase.deleteFriendContactFromMain(id);
                        listProducts.remove(position);
                        updateList(listProducts);
                        //notifyDataSetChanged();
                        //((MainActivity) mContext).setPhoneCallData();
                        dialog.dismiss();
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //((MainActivity) mContext).setPhoneCallData();
                        updateList(listProducts);
                        dialog.dismiss();
                    }
                });
            }
        });

        holder.edit_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = mDatabase.idFromDataBase(singleProduct.getFriendNumber());

                Fragment registrationFragment = new EditRegistrationFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("id", +id);
                bundle.putString("image", singleProduct.getFriendImagePath());
                bundle.putString("phone", singleProduct.getFriendNumber());
                bundle.putString("phone_show", singleProduct.getFriendNumberShow());
                bundle.putString("name", singleProduct.getFriendName());
                bundle.putString("listorder", singleProduct.getFriendListOfOreder());
                bundle.putString("reord_msg", singleProduct.getFriendAudioMessage());
                registrationFragment.setArguments(bundle);
                ((MainActivity) mContext).switchFragment(registrationFragment, true, KeyUtils.REGISTRATION_FRAGMENT_TAG);
            }
        });
    }

    public void updateList(List<FriendContactModel> contactList) {
        this.listProducts = contactList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listProducts.size();
    }

    static class FriendContactViewHolder extends RecyclerView.ViewHolder {
        ImageView dial_icon;
        TextView incoming_call_number, incoming_call_name;
        RelativeLayout incoming_call_rl;
        ImageView delete_iv, edit_iv;

        FriendContactViewHolder(View itemView) {
            super(itemView);
            dial_icon = (ImageView) itemView.findViewById(R.id.dial_icon);
            incoming_call_number = (TextView) itemView.findViewById(R.id.incoming_call_number);
            incoming_call_name = (TextView) itemView.findViewById(R.id.incoming_call_name);
            incoming_call_rl = (RelativeLayout) itemView.findViewById(R.id.bottom_friend_rl);
            delete_iv = (ImageView) itemView.findViewById(R.id.delete_iv);
            edit_iv = (ImageView) itemView.findViewById(R.id.edit_iv);
        }
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
                        //Activate loudspeaker
                        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setMode(AudioManager.MODE_IN_CALL);
                        audioManager.setSpeakerphoneOn(true);
                    }
                    break;

                case TelephonyManager.CALL_STATE_IDLE: //Call is finished
                    if (callFromOffHook) {
                        callFromOffHook = false;
                        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setMode(AudioManager.MODE_NORMAL); //Deactivate loudspeaker
                        mTelephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_NONE);// Remove listener
                    }
                    break;
            }
        }
    }
}
