package com.SoftEngUniNA.CineMates20Desktop.models;

import java.util.Objects;

/**
 *
 * @author marco
 */
public class MovieList {
    private String title;
    private String description;
    private boolean movies;

    public MovieList(String title, String description, boolean movies){
        this.title=title;
        this.description=description;
        this.movies=movies;
    }
    public String getTitle() {
        return title;
    }

    public String getDecription() {
        return description;
    }

    public boolean isMovies() {
        return movies;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDecription(String description) {
        this.description = description;
    }

    public void setMovies(boolean movies) {
        this.movies = movies;
    }

    @Override
    public String toString() {
        return "MovieList{" + "title=" + title + ", description=" + description + ", movies=" + movies + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        
        if (obj == null)
            return false;
        
        if (getClass() != obj.getClass())
            return false;
        
        final MovieList other = (MovieList) obj;
        if (!Objects.equals(this.title, other.title)) 
            return false;
        
        return true;
    }
    
    
}
