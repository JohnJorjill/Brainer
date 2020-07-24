package com.example.brain_trainer;

// data structure for holding data from firebase
public class Profile {
    private String imageUrl;
    private String uname;
    private int score;

    public Profile() {

    }

    public Profile(String imageUrl, int score, String uname) {
        this.imageUrl = imageUrl;
        this.uname = uname;
        this.score = score;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setProfilePic(String profilePic) {
        this.imageUrl = profilePic;
    }

    public String getUname()
    {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public int getScore()
    {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
