package com.SoftEngUniNA.CineMates20Desktop.models;

public class Operator {
    private boolean first_access;
    private String password;
    private String username;
    
    public boolean login(String username, String password){
        return (username.compareTo(this.username)+ password.compareTo(this.password))==0;
    }
    
    public boolean checkPassword(String password){
        return password.compareTo(this.password)==0;
    }
    
    public boolean isFirstAccess(){
        return first_access;
    }
    
    public void changePassword(String old_password, String new_password){
        if (old_password.compareTo(password)==0)
            password=new_password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstAccess(boolean first_access) {
        this.first_access = first_access;
    }
    
    public String getUsername(){
        return username;
    }

    public boolean isFirst_access() {
        return first_access;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "Operator{" + "username=" + username + ", password=" + password + ", first_access=" + first_access + '}';
    }
}
