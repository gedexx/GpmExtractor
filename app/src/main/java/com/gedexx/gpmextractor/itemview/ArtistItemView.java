package com.gedexx.gpmextractor.itemview;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gedexx.gpmextractor.R;
import com.gedexx.gpmextractor.domain.Artist;
import com.gedexx.gpmextractor.service.DecryptionService_;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.row_artist)
public class ArtistItemView extends RelativeLayout {

    @ViewById
    public TextView tvArtistName;

    @ViewById
    public ImageView ivArtistDL;

    @ViewById
    public ProgressBar pgArtist;

    private Artist artist;

    public ArtistItemView(Context context) {
        super(context);
    }

    public void bind(Artist artist) {

        this.artist = artist;

        tvArtistName.setText(artist.getName().replace("\"", ""));
    }

    @Click(R.id.ivArtistDL)
    public void onClickDL() {
        DecryptionService_.intent(getContext()).decrypt(artist.getTrackList()).start();
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.file_decryption_started", registerAt = Receiver.RegisterAt.OnAttachOnDetach)
    public void onStartingDecryption() {
        ivArtistDL.setVisibility(View.GONE);
        pgArtist.setVisibility(View.VISIBLE);
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.file_decryption_success", registerAt = Receiver.RegisterAt.OnAttachOnDetach)
    public void onFinishedDecryption() {
        ivArtistDL.setVisibility(View.VISIBLE);
        pgArtist.setVisibility(View.GONE);
    }
}
