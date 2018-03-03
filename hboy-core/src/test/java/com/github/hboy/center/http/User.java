package com.github.hboy.center.http;

public class User {

    private long uid;
    private String nick;
    private int sex;
    private boolean isVIP;

    public long getUid() {
        return uid;
    }
    public void setUid(long uid) {
        this.uid = uid;
    }
    public String getNick() {
        return nick;
    }
    public void setNick(String nick) {
        this.nick = nick;
    }
    public int getSex() {
        return sex;
    }
    public void setSex(int sex) {
        this.sex = sex;
    }
    public boolean isVIP() {
        return isVIP;
    }
    public void setVIP(boolean isVIP) {
        this.isVIP = isVIP;
    }
    
    @Override
    public String toString() {
        return "User [uid=" + uid + ", nick=" + nick + ", sex=" + sex + ", isVIP=" + isVIP + "]";
    }
    
}
