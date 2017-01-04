package com.gedexx.gpmextractor.activity;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TabHost;

import com.gedexx.gpmextractor.R;
import com.gedexx.gpmextractor.helper.database.GpmDatabaseHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Touch;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @ViewById(R.id.tabhost)
    TabHost tabHost;

    private GpmDatabaseHelper gpmDatabaseHelper;

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
    }

    /**
     * Permet la gestion du swipe pour les tabs
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
