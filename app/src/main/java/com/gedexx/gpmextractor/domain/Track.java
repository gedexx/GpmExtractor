package com.gedexx.gpmextractor.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "tracks")
public class Track implements Serializable {

    @DatabaseField(id = true)
    private long id;

    @DatabaseField
    private int number;

    @DatabaseField
    private String title;

    @DatabaseField
    private long duration;

    @DatabaseField
    private String localCopyPath;

    @DatabaseField
    private String cpData;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Artist artist;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Album album;

    private boolean checked;

    public Track() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
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

    public String getCpData() {
        return cpData;
    }

    public void setCpData(String cpData) {
        this.cpData = cpData;
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

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public String toString() {
        return title;
    }
}
