package com.gedexx.gpmextractor.domain;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "albums")
public class Album implements Serializable {

    @DatabaseField(id = true)
    private long id;

    @DatabaseField
    private String name;

    @DatabaseField
    private String coverArt;

    @DatabaseField(foreign = true)
    private Artist artist;

    @ForeignCollectionField(foreignFieldName = "album")
    private ForeignCollection<Track> trackList;

    public Album() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverArt() {
        return coverArt;
    }

    public void setCoverArt(String coverArt) {
        this.coverArt = coverArt;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public ForeignCollection<Track> getTrackList() {
        return trackList;
    }

    public void setTrackList(ForeignCollection<Track> trackList) {
        this.trackList = trackList;
    }
}
