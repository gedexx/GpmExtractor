package com.gedexx.gpmextractor.itemview;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gedexx.gpmextractor.R;
import com.gedexx.gpmextractor.domain.Artist;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.row_artist)
public class ArtistItemView extends RelativeLayout {

    @ViewById
    public TextView tvArtistName;

    @ViewById
    public CheckBox cbArtist;

    public ArtistItemView(Context context) {
        super(context);
    }

    public void bind(Artist artist) {
        tvArtistName.setText(artist.getName().replace("\"", ""));
        cbArtist.setChecked(cbArtist.isChecked());
    }
}
