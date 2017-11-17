package com.ispl.callses.model;

/**
 * Created by infinium on 25/07/17.
 */

public class FriendContactModel {
    private int mID = 0;
    private String mFriendName = null;
    private String mFriendNumber = null;
    private String mFriendNumberShow = null;
    private String mFriendListOfOreder = null;
    private String mFriendImagePath = null;
    private String mFriendAudioMessage = null;

    public FriendContactModel(String imagePath, String number,String number_show, String name, String listOfOreder, String audioMessage) {
        this.mFriendImagePath = imagePath;
        this.mFriendNumber = number;
        this.mFriendNumberShow = number_show;
        this.mFriendName = name;
        this.mFriendListOfOreder = listOfOreder;
        this.mFriendAudioMessage = audioMessage;
    }

    public FriendContactModel(int id, String imagePath, String number,String number_show, String name, String listOfOreder, String audioMessage) {
        this.mID = id;
        this.mFriendImagePath = imagePath;
        this.mFriendName = name;
        this.mFriendNumber = number;
        this.mFriendNumberShow = number_show;
        this.mFriendListOfOreder = listOfOreder;
        this.mFriendAudioMessage = audioMessage;
    }

    public int getID() {
        return mID;
    }

    public String getFriendImagePath() {
        return mFriendImagePath;
    }

    public String getFriendName() {
        return mFriendName;
    }

    public String getFriendNumber() {
        return mFriendNumber;
    }

    public String getFriendNumberShow() {
        return mFriendNumberShow;
    }

    public String getFriendListOfOreder() {
        return mFriendListOfOreder;
    }

    public String getFriendAudioMessage() {
        return mFriendAudioMessage;
    }
}
