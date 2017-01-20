package com.gedexx.gpmextractor.itemview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gedexx.gpmextractor.R;
import com.gedexx.gpmextractor.domain.Track;
import com.gedexx.gpmextractor.service.DecryptionService_;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;

import java.io.FileNotFoundException;
import java.util.Collections;

@EViewGroup(R.layout.row_track)
public class TrackItemView extends RelativeLayout {

    @ViewById
    public ImageView ivTrackAlbumCoverArt;

    @ViewById
    public TextView tvTrackTitle;

    @ViewById
    public TextView tvTrackArtistName;

    @ViewById
    public TextView tvTrackAlbumName;

    @ViewById
    public TextView tvTrackDuration;

    @ViewById
    public ImageView ivTrackDL;

    @ViewById
    public ProgressBar pgTrack;

    private Track track;

    public TrackItemView(Context context) {
        super(context);
    }

    public void bind(Track track) {

        this.track = track;

        try {
            Bitmap photo = BitmapFactory.decodeStream(getContext().openFileInput(track.getAlbum().getCoverArtLocalPath()));
            ivTrackAlbumCoverArt.setImageDrawable(new BitmapDrawable(getContext().getResources(), photo));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        tvTrackTitle.setText(track.getTitle().replace("\"", ""));
        tvTrackArtistName.setText(track.getArtist().getName().replace("\"", ""));
        tvTrackAlbumName.setText(track.getAlbum().getName().replace("\"", ""));
        tvTrackDuration.setText(String.format("%1$tM:%1$tS", track.getDuration()));
    }

    @Click(R.id.ivTrackDL)
    public void onClickDL() {
        DecryptionService_.intent(getContext()).decrypt(Collections.singletonList(track)).start();
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.file_decryption_started", registerAt = Receiver.RegisterAt.OnAttachOnDetach)
    public void onStartingDecryption() {
        ivTrackDL.setVisibility(View.GONE);
        pgTrack.setVisibility(View.VISIBLE);
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.file_decryption_success", registerAt = Receiver.RegisterAt.OnAttachOnDetach)
    public void onFinishedDecryption() {
        ivTrackDL.setVisibility(View.VISIBLE);
        pgTrack.setVisibility(View.GONE);
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.download_finished", registerAt = Receiver.RegisterAt.OnAttachOnDetach)
    void onFinishedDownload() {
        ivTrackAlbumCoverArt.invalidate();
    }
}
