package com.gedexx.gpmextractor.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "tracks")
public class Track implements Serializable {

    @DatabaseField(id = true)
    private long id;

    @DatabaseField
    private String title;

    @DatabaseField
    private long duration;

    @DatabaseField
    private String localCopyPath;

    @DatabaseField(foreign = true)
    private Artist artist;

    @DatabaseField(foreign = true)
    private Album album;

    public Track() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getLocalCopyPath() {
        return localCopyPath;
    }

    public void setLocalCopyPath(String localCopyPath) {
        this.localCopyPath = localCopyPath;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }
}
