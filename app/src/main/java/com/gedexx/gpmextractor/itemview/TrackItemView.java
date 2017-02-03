package com.gedexx.gpmextractor.itemview;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gedexx.gpmextractor.R;
import com.gedexx.gpmextractor.domain.Track;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;

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
    public CheckBox cbTrack;

    public TrackItemView(Context context) {
        super(context);
    }

    public void bind(Track track) {

        /*try {
            Bitmap photo = BitmapFactory.decodeStream(getContext().openFileInput(track.getAlbum().getCoverArtLocalPath()));
            ivTrackAlbumCoverArt.setImageDrawable(new BitmapDrawable(getContext().getResources(), photo));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/

        Picasso.with(getContext()).load(track.getAlbum().getCoverArtUrl()).placeholder(android.R.drawable.ic_menu_camera).error(android.R.drawable.ic_delete).into(ivTrackAlbumCoverArt);

        tvTrackTitle.setText(track.getTitle().replace("\"", ""));
        tvTrackArtistName.setText(track.getArtist().getName().replace("\"", ""));
        tvTrackAlbumName.setText(track.getAlbum().getName().replace("\"", ""));
        tvTrackDuration.setText(String.format("%1$tM:%1$tS", track.getDuration()));
        cbTrack.setChecked(track.isChecked());
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.download_finished", registerAt = Receiver.RegisterAt.OnAttachOnDetach)
    void onFinishedDownload() {
        ivTrackAlbumCoverArt.invalidate();
    }
}
