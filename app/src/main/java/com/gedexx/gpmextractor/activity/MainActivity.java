package com.gedexx.gpmextractor.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
import com.gedexx.gpmextractor.itemview.AlbumItemView;
import com.gedexx.gpmextractor.itemview.ArtistItemView;
import com.gedexx.gpmextractor.itemview.TrackItemView;
import com.gedexx.gpmextractor.service.DecryptionService_;
import com.gedexx.gpmextractor.service.GpmDbService_;
import com.j256.ormlite.dao.Dao;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.ormlite.annotations.OrmLiteDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    private static final int ARTISTS_TAB = 0;
    private static final int ALBUMS_TAB = 1;
    private static final int TRACKS_TAB = 2;

    // Storage Permissions variables
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

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

    @ViewById(R.id.btnConvert)
    Button btnConvert;

    @ViewById(R.id.progress_overlay)
    View progressOverlay;

    private Collection<Artist> selectedArtists;
    private Collection<Album> selectedAlbums;
    private Collection<Track> selectedTracks;

    private float lastX;

    @AfterViews
    void init() {

        selectedArtists = new ArrayList<>();
        selectedAlbums = new ArrayList<>();
        selectedTracks = new ArrayList<>();

        verifyStoragePermissions(this);
        initTabHost();
        extractDB();
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
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

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                btnConvert.setVisibility(View.GONE);

                selectedArtists.clear();
                selectedAlbums.clear();
                selectedTracks.clear();
            }
        });
    }

    /**
     * Extraction de la BDD de GPM
     */
    void extractDB() {
        GpmDbService_.intent(getApplication()).extractDb().start();
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.extraction_start")
    void onStartingExtraction() {
        animateView(progressOverlay, View.VISIBLE, 0.4f);
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.extraction_error")
    void onErrorExtraction() {
        Toast.makeText(getApplicationContext(), "Erreur !", Toast.LENGTH_LONG).show();
        animateView(progressOverlay, View.GONE, 0);
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.extraction_finished")
    void onFinishedExtraction() {
        animateView(progressOverlay, View.GONE, 0);

        try {
            final List<Artist> artists = artistDao.queryForAll();
            final List<Album> albums = albumDao.queryForAll();
            final List<Track> tracks = trackDao.queryForAll();

            //AlbumArtCoverDownloadService_.intent(getApplication()).downloadAlbumArtCoverFromUrl(albums).start();

            lvArtists.setAdapter(new ArtistAdapter(this, artists));
            lvAlbums.setAdapter(new AlbumAdapter(this, albums));
            lvTracks.setAdapter(new TrackAdapter(this, tracks));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.file_decryption_started")
    void onStartingDecryption() {
        animateView(progressOverlay, View.VISIBLE, 0.4f);
        btnConvert.setEnabled(false);
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.file_decryption_error")
    void onErrorDecryption() {
        animateView(progressOverlay, View.GONE, 0);
        Toast.makeText(getApplicationContext(), "Erreur à la conversion !", Toast.LENGTH_LONG).show();
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.file_decryption_success")
    void onFinishedDecryption() {
        animateView(progressOverlay, View.GONE, 0);
        btnConvert.setEnabled(true);
    }

    @ItemClick(R.id.lvArtists)
    public void onArtistClick(int position) {

        final Artist artist = (Artist) lvArtists.getItemAtPosition(position);
        artist.setChecked(!artist.isChecked());

        final ArtistItemView artistItemView = (ArtistItemView) lvArtists.getChildAt(position - lvArtists.getFirstVisiblePosition());
        artistItemView.bind(artist);

        if (artist.isChecked()) {
            selectedArtists.add(artist);
        } else {
            selectedArtists.remove(artist);
        }

        if (selectedArtists.isEmpty()) {
            btnConvert.setVisibility(View.GONE);
        } else {
            btnConvert.setVisibility(View.VISIBLE);
            if (selectedArtists.size() == 1) {
                btnConvert.setText("Convertir l'artiste sélectionné");
            } else {
                btnConvert.setText("Convertir les " + selectedArtists.size() + " artistes sélectionnés");
            }

        }
    }

    @ItemClick(R.id.lvAlbums)
    public void onAlbumClick(int position) {

        final Album album = (Album) lvAlbums.getItemAtPosition(position);
        album.setChecked(!album.isChecked());

        final AlbumItemView albumItemView = (AlbumItemView) lvAlbums.getChildAt(position - lvAlbums.getFirstVisiblePosition());
        albumItemView.bind(album);

        if (album.isChecked()) {
            selectedAlbums.add(album);
        } else {
            selectedAlbums.remove(album);
        }

        if (selectedAlbums.isEmpty()) {
            btnConvert.setVisibility(View.GONE);
        } else {
            btnConvert.setVisibility(View.VISIBLE);
            if (selectedAlbums.size() == 1) {
                btnConvert.setText("Convertir l'album sélectionné");
            } else {
                btnConvert.setText("Convertir les " + selectedAlbums.size() + " albums sélectionnés");
            }

        }
    }

    @ItemClick(R.id.lvTracks)
    public void onTrackClick(int position) {

        final Track track = (Track) lvTracks.getItemAtPosition(position);
        track.setChecked(!track.isChecked());

        final TrackItemView trackItemView = (TrackItemView) lvTracks.getChildAt(position - lvTracks.getFirstVisiblePosition());
        trackItemView.bind(track);

        if (track.isChecked()) {
            selectedTracks.add(track);
        } else {
            selectedTracks.remove(track);
        }

        if (selectedTracks.isEmpty()) {
            btnConvert.setVisibility(View.GONE);
        } else {
            btnConvert.setVisibility(View.VISIBLE);
            if (selectedTracks.size() == 1) {
                btnConvert.setText("Convertir la chanson sélectionnée");
            } else {
                btnConvert.setText("Convertir les " + selectedTracks.size() + " chansons sélectionnées");
            }

        }
    }

    @Click(R.id.btnConvert)
    public void onBtnConvertClick() {

        Collection<Long> tracksToConvertIds = new ArrayList<>();

        switch (tabHost.getCurrentTab()) {
            case ARTISTS_TAB:
                for (Artist artist : selectedArtists) {
                    tracksToConvertIds.addAll(getTrackIds(artist.getTrackList()));
                }
                break;
            case ALBUMS_TAB:
                for (Album album : selectedAlbums) {
                    tracksToConvertIds.addAll(getTrackIds(album.getTrackList()));
                }
                break;
            case TRACKS_TAB:
                tracksToConvertIds.addAll(getTrackIds(selectedTracks));
                break;
        }

        DecryptionService_.intent(getApplicationContext()).decrypt(tracksToConvertIds).start();
    }

    private Collection<? extends Long> getTrackIds(Collection<Track> trackList) {

        Collection<Long> trackIds = new ArrayList<>();

        for (Track track : trackList) {
            trackIds.add(track.getId());
        }
        return trackIds;
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
            if (tabHost.getCurrentTab() == ARTISTS_TAB)
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
                tabHost.setCurrentTab(ARTISTS_TAB);
        }
    }

    /**
     * @param view         View to animate
     * @param toVisibility Visibility at the end of animation
     * @param toAlpha      Alpha at the end of animation
     */
    private static void animateView(final View view, final int toVisibility, float toAlpha) {
        boolean show = toVisibility == View.VISIBLE;
        if (show) {
            view.setAlpha(0);
        }
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(show ? toAlpha : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(toVisibility);
                    }
                });
    }
}
