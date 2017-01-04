package com.gedexx.gpmextractor.helper.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gedexx.gpmextractor.domain.Album;
import com.gedexx.gpmextractor.domain.Artist;
import com.gedexx.gpmextractor.domain.Track;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class GpmDatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "gpmextractor.db";
    private static final int DATABASE_VERSION = 1;

    public GpmDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {

        try {
            Log.i(GpmDatabaseHelper.class.getName(), "Création des tables de la database");
            TableUtils.createTableIfNotExists(connectionSource, Artist.class);
            TableUtils.createTableIfNotExists(connectionSource, Album.class);
            TableUtils.createTableIfNotExists(connectionSource, Track.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        Log.i(GpmDatabaseHelper.class.getName(), "Mise à jour des tables de la database");
    }
}
