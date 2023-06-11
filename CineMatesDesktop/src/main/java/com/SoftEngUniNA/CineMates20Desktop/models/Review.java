package com.SoftEngUniNA.CineMates20Desktop.models;

public class Review {
    private String author;
    private String image;
    private String movieName;
    private float rating;
    private String text;
    private Object timestamp;
    private String Uid;

    public String getAuthor() { return author; }
    public void setAuthor(String author){ this.author = author; }
    public String getImage() { return image; }
    public void setImage(String image){ this.image = image; }
    public String getMovieName() { return movieName; }
    public void setMovieName(String movieName) { this.movieName = movieName; }
    public float getRating() { return rating; }
    public void setRating(float rating){ this.rating = rating; }
    public String getText() { return text; }
    public void setText(String text){ this.text = text; }
    public Object getTimestamp() { return timestamp; }
    public void setTimestamp(Object timestamp){ this.timestamp = timestamp; }    
    public String getUid() { return Uid; }
    public void setUid(String uid) { Uid = uid; }
    @Override
    public String toString() {
        return "Review{" + "author=" + author + ", movieName=" + movieName + ", rating=" + rating + ", text=" + text + ", timestamp=" + timestamp + ", Uid=" + Uid + '}';
    }
    
    
}
