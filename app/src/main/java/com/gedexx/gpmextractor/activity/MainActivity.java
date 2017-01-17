package com.gedexx.gpmextractor.activity;

import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.Toast;

import com.gedexx.gpmextractor.R;
import com.gedexx.gpmextractor.adapter.AlbumAdapter;
import com.gedexx.gpmextractor.adapter.ArtistAdapter;
import com.gedexx.gpmextractor.adapter.TrackAdapter;
import com.gedexx.gpmextractor.domain.Album;
import com.gedexx.gpmextractor.domain.Artist;
import com.gedexx.gpmextractor.domain.Track;
import com.gedexx.gpmextractor.helper.database.GpmDatabaseHelper;
import com.gedexx.gpmextractor.service.GpmDbService_;
import com.j256.ormlite.dao.Dao;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.ormlite.annotations.OrmLiteDao;

import java.sql.SQLException;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @OrmLiteDao(helper = GpmDatabaseHelper.class)
    Dao<Artist, Long> artistDao;

    @OrmLiteDao(helper = GpmDatabaseHelper.class)
    Dao<Album, Long> albumDao;

    @OrmLiteDao(helper = GpmDatabaseHelper.class)
    Dao<Track, Long> trackDao;

    @ViewById(R.id.tabhost)
    TabHost tabHost;

    @ViewById(R.id.progressBar)
    ProgressBar progressBar;

    @ViewById(R.id.lvArtists)
    ListView lvArtists;

    @ViewById(R.id.lvAlbums)
    ListView lvAlbums;

    @ViewById(R.id.lvTracks)
    ListView lvTracks;

    private float lastX;

    @AfterViews
    void init() {
        initTabHost();
        extractDB();
    }

    /**
     * Initialise les onglets
     */
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

    /**
     * Extraction de la BDD de GPM
     */
    void extractDB() {
        GpmDbService_.intent(getApplication()).extractDb().start();
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.extraction_start")
    void onStartingExtraction() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.extraction_error")
    void onErrorExtraction() {
        Toast.makeText(getApplicationContext(), "Erreur !", Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.GONE);
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.extraction_finished")
    void onFinishedExtraction() {
        progressBar.setVisibility(View.GONE);

        try {
            lvArtists.setAdapter(new ArtistAdapter(this, artistDao.queryForAll()));
            lvAlbums.setAdapter(new AlbumAdapter(this, albumDao.queryForAll()));
            lvTracks.setAdapter(new TrackAdapter(this, trackDao.queryForAll()));
        } catch (SQLException e) {
            e.printStackTrace();
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
