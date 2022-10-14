package com.dirror.music.service;

import static android.content.Context.BIND_AUTO_CREATE;

import static com.dirror.music.music.standard.data.StandardDataKt.SOURCE_NETEASE;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.dirror.music.App;
import com.dirror.music.music.standard.data.StandardSongData;
import com.dirror.music.util.Config;
import com.ecarx.dim_interaction.bussiness.bean.IAdapterDI;
import com.ecarx.dim_interaction.bussiness.bean.IMediaDI;
import com.ecarx.dim_interaction.bussiness.bean.Media;

import java.io.File;

public class LynkCo {

    private IAdapterDI mAdapterDI;
    private IMediaDI mMediaDI;
    private boolean connected = true;
    private Context ctx = App.context;


    private int MSG_UPDATE_PROGRESS = 1001;
    private int MSG_UPDATE_DELAY = 1000;
    private MusicService.MusicController mMusicController;
    private Handler mHadler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_UPDATE_PROGRESS && mMusicController != null && connected) {
                if (mMusicController.isPlaying().getValue() == true) {
                    try {
                        long progress = mMusicController.getProgress();
                        Log.d(TAG, "updateCurrentProgress:" + progress);
                        if (mMediaDI != null) {
                            mMediaDI.updateCurrentProgress(progress);
                        }

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    mHadler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, MSG_UPDATE_DELAY);
                }
            }
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAdapterDI = IAdapterDI.Stub.asInterface(service);
            try {
                mMediaDI = mAdapterDI.getIMediaDI();
                Toast.makeText(ctx, "bindService Lynko success mMediaDI:" + mMediaDI, Toast.LENGTH_LONG).show();
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(ctx, "getIMediaDI Lynko失败" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            connected = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connected = false;
        }
    };

    private String TAG = "Lynko";

    public void bindService() {
        if (!App.mmkv.decodeBool(Config.BIND_LYNKCO_SERVICE, false)) {
            return;
        }
        Log.d(TAG, "bindService");
        try {
            Intent intent2 = new Intent();
            intent2.setComponent(new ComponentName("com.ecarx.dim_interaction", "com.ecarx.dim_interaction.server.DiServer"));
            intent2.putExtra("app_type", "om.ecarx.di_server");
            ctx.bindService(intent2, serviceConnection, BIND_AUTO_CREATE);
        } catch (Exception e10) {
            e10.printStackTrace();
            Toast.makeText(ctx, "bindService Lynko失败" + e10.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void release() {
        if (!App.mmkv.decodeBool(Config.BIND_LYNKCO_SERVICE, false)) {
            return;
        }
        ctx.unbindService(serviceConnection);
        mHadler.removeCallbacksAndMessages(null);
        mMusicController = null;
    }

    public void updateMedia(@NonNull StandardSongData song, MusicService.MusicController musicController) {
        if (!App.mmkv.decodeBool(Config.BIND_LYNKCO_SERVICE, false)) {
            return;
        }
        Log.d("LynkoUtil", "updateMedia: " + song.getName());
        mMusicController = musicController;
        if  (connected) {
            Media media = new Media();
            try {
                media.setSongTitle10(song.getName());
                if (song.getArtists() != null && song.getArtists().size() > 0) {
                    media.setArtistName11(song.getArtists().get(0).getName());
                }
                media.setBaseTotalTime(musicController.getDuration());
                media.setBaseMediaType(Media.MEDIA_TYPE_ONLINE);
                boolean isPlaying = musicController.isPlaying().getValue() != null ? musicController.isPlaying().getValue() : false;
                media.setBasePlayState(isPlaying ? Media.MEDIA_PLAY_STATE_PLAY : Media.MEDIA_PLAY_STATE_PAUSE);
                File f = new File(ctx.getExternalCacheDir(), "cover.jpg");
                media.setAlbumCover2(Uri.fromFile(f));
                media.setCurrentSource8(song.getSource() == SOURCE_NETEASE ? "网易" : "酷我");
                mMediaDI.updateMediaInfo(media);

            } catch (Throwable th) {
                th.printStackTrace();
                Toast.makeText(ctx, "更新Lynko失败" + th.getMessage(), Toast.LENGTH_LONG).show();
            }
            mHadler.removeMessages(MSG_UPDATE_PROGRESS);
            mHadler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, MSG_UPDATE_DELAY);
        }
    }

}
