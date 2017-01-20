package com.gedexx.gpmextractor.domain;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "artists")
public class Artist implements Serializable {

    @DatabaseField(id = true)
    private long id;

    @DatabaseField
    private String name;

    @ForeignCollectionField(foreignFieldName = "artist", eager = true)
    private ForeignCollection<Album> albumList;

    @ForeignCollectionField(foreignFieldName = "artist", eager = true)
    private ForeignCollection<Track> trackList;

    public Artist() {
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

    public ForeignCollection<Album> getAlbumList() {
        return albumList;
    }

    public void setAlbumList(ForeignCollection<Album> albumList) {
        this.albumList = albumList;
    }

    public ForeignCollection<Track> getTrackList() {
        return trackList;
    }

    public void setTrackList(ForeignCollection<Track> trackList) {
        this.trackList = trackList;
    }

    @Override
    public String toString() {
        return name;
    }
}
