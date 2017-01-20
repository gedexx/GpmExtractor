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
import com.gedexx.gpmextractor.domain.Album;
import com.gedexx.gpmextractor.service.DecryptionService_;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;

import java.io.FileNotFoundException;

@EViewGroup(R.layout.row_album)
public class AlbumItemView extends RelativeLayout {

    @ViewById
    public ImageView ivAlbumCoverArt;

    @ViewById
    public TextView tvAlbumName;

    @ViewById
    public TextView tvArtistAlbumName;

    @ViewById
    public ImageView ivAlbumDL;

    @ViewById
    public ProgressBar pgAlbum;

    private Album album;

    public AlbumItemView(Context context) {
        super(context);
    }

    public void bind(Album album) {

        this.album = album;

        try {
            Bitmap photo = BitmapFactory.decodeStream(getContext().openFileInput(album.getCoverArtLocalPath()));
            ivAlbumCoverArt.setImageDrawable(new BitmapDrawable(getContext().getResources(), photo));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        tvAlbumName.setText(album.getName().replace("\"", ""));
        tvArtistAlbumName.setText(album.getArtist().getName().replace("\"", ""));
    }

    @Click(R.id.ivAlbumDL)
    public void onClickDL() {
        DecryptionService_.intent(getContext()).decrypt(album.getTrackList()).start();
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.file_decryption_started", registerAt = Receiver.RegisterAt.OnAttachOnDetach)
    public void onStartingDecryption() {
        ivAlbumDL.setVisibility(View.GONE);
        pgAlbum.setVisibility(View.VISIBLE);
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.file_decryption_success", registerAt = Receiver.RegisterAt.OnAttachOnDetach)
    public void onFinishedDecryption() {
        ivAlbumDL.setVisibility(View.VISIBLE);
        pgAlbum.setVisibility(View.GONE);
    }

    @Receiver(actions = "com.gedexx.gpmextractor.service.download_finished", registerAt = Receiver.RegisterAt.OnAttachOnDetach)
    void onFinishedDownload() {
        ivAlbumCoverArt.invalidate();
    }
}
