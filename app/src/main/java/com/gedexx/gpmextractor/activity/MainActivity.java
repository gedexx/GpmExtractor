package com.gedexx.gpmextractor.activity;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TabHost;

import com.gedexx.gpmextractor.R;
import com.gedexx.gpmextractor.helper.database.GpmDatabaseHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @ViewById(R.id.tabhost)
    TabHost tabHost;

    private GpmDatabaseHelper gpmDatabaseHelper;

    public static int SONG_ID_COLUMN_NUM = 0;
    public static int SONG_TITLE_COLUMN_NUM = 1;
    public static int SONG_DURATION_COLUMN_NUM = 2;
    public static int SONG_LOCAL_COPY_PATH_COLUMN_NUM = 3;
    public static int ARTIST_ID_COLUMN_NUM = 4;
    public static int ARTIST_NAME_COLUMN_NUM = 5;
    public static int ALBUM_ID_COLUMN_NUM = 6;
    public static int ALBUM_NAME_COLUMN_NUM = 7;
    public static int ALBUM_GENRE_COLUMN_NUM = 8;
    public static int ALBUM_ART_LOCATION_COLUMN_NUM = 9;

    private float lastX;

    /**
     * Initialise les onglets
     */
    @AfterViews
    void initTabHost() {
        tabHost.setup();

        // Artists Tab
        final TabHost.TabSpec artistsTab = tabHost.newTabSpec("Artistes");
        artistsTab.setContent(R.id.artists_tab);
        artistsTab.setIndicator("Artistes");
        tabHost.addTab(artistsTab);

        // Albums Tab
        final TabHost.TabSpec albumsTab = tabHost.newTabSpec("Albums");
        albumsTab.setContent(R.id.albums_tab);
        albumsTab.setIndicator("Albums");
        tabHost.addTab(albumsTab);

        // Tracks Tab
        final TabHost.TabSpec tracksTab = tabHost.newTabSpec("Titres");
        tracksTab.setContent(R.id.tracks_tab);
        tracksTab.setIndicator("Titres");
        tabHost.addTab(tracksTab);
    }

    @AfterViews
    void initDB() {
        Log.d(MainActivity.class.getName(), "Initialisation de la BDD");
        gpmDatabaseHelper = new GpmDatabaseHelper(getApplicationContext());
        gpmDatabaseHelper.getWritableDatabase();

        try {
            String dbName = "music.db";
            String packageSrc = "com.google.android.music";
            String pathSource = "/data/data/" + packageSrc + "/databases/" + dbName;

            String commandSelect = " \"SELECT Artist, Album, Title, HEX(CpData) FROM Music WHERE LocalCopyType = 200\";";
            String commandSqliteCsv = "sqlite3 -csv ";
            String command = commandSqliteCsv + pathSource + commandSelect;

            Process p = Runtime.getRuntime().exec("su");

            OutputStream os = p.getOutputStream();

            os.write((command).getBytes("ASCII"));
            os.flush();

            os.close();

            try {
                p.waitFor();
                if (p.exitValue() != 255) {
                    // be happy and work with the database

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    StringBuffer result = new StringBuffer();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                        Log.d(MainActivity.class.getName(),"RESULT:"+result);
                    }
                }
            } catch (InterruptedException e) {
                // error
            } finally {
                p.destroy();
            }
        } catch (IOException e) {
            // error
        }
    }

    /**
     * Permet la gestion du swipe pour les tabs
     *
     * @param motionEvent
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {
            // when user first touches the screen to swap
            case MotionEvent.ACTION_DOWN: {
                lastX = motionEvent.getX();
                break;
            }
            case MotionEvent.ACTION_UP: {
                float currentX = motionEvent.getX();

                // if left to right swipe on screen
                if (lastX < currentX) {

                    switchTabs(true);
                }

                // if right to left swipe on screen
                if (lastX > currentX) {
                    switchTabs(false);
                }

                break;
            }
        }
        return false;
    }

    public void switchTabs(boolean direction) {

        if (direction) // true = move left
        {
            if (tabHost.getCurrentTab() == 0)
                tabHost.setCurrentTab(tabHost.getTabWidget().getTabCount() - 1);
            else
                tabHost.setCurrentTab(tabHost.getCurrentTab() - 1);
        } else
        // move right
        {
            if (tabHost.getCurrentTab() != (tabHost.getTabWidget()
                    .getTabCount() - 1))
                tabHost.setCurrentTab(tabHost.getCurrentTab() + 1);
            else
                tabHost.setCurrentTab(0);
        }
    }
}
