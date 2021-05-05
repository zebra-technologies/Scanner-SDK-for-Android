package com.zebra.scannercontrol.app.helpers;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;

import com.zebra.scannercontrol.app.R;

import java.io.IOException;
/**
 * Service class to handle the audio alarm when virtual tether event occurs
 */

public class BackgroundSoundService extends Service {

    MediaPlayer audioAlarmPlayer = null;
    private final static int MAX_VOLUME = 100;
    Context context;
    AudioManager.OnAudioFocusChangeListener afChangeListener;
    private int length;


    public BackgroundSoundService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

            playMusic(R.raw.vt_alarm_tone);

        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (audioAlarmPlayer != null) {
            try {
                audioAlarmPlayer.stop();
                audioAlarmPlayer.release();
            } finally {
                audioAlarmPlayer = null;
            }
        }
    }

    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
    }


    /**
     * custom method to play alarm sounds for application
     * @param musicFile alarm sound raw audio file
     */
    public void playMusic(int musicFile) {
        if (audioAlarmPlayer != null) {
            if (audioAlarmPlayer.isPlaying()) {
                try {
                    audioAlarmPlayer.stop();
                    audioAlarmPlayer.release();
                    audioAlarmPlayer = MediaPlayer.create(this, musicFile);

                    AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    int result = am.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        // Start playback.
                        audioAlarmPlayer.setLooping(true);
                        final float volume = (float) (1 - (Math.log(MAX_VOLUME - 85) / Math.log(MAX_VOLUME)));
                        audioAlarmPlayer.setVolume(volume, volume);
                        audioAlarmPlayer.start();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    audioAlarmPlayer = MediaPlayer.create(this, musicFile);

                    AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    int result = am.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        // Start playback.
                        audioAlarmPlayer.setLooping(true);
                        final float volume = (float) (1 - (Math.log(MAX_VOLUME - 85) / Math.log(MAX_VOLUME)));
                        audioAlarmPlayer.setVolume(volume, volume);
                        audioAlarmPlayer.prepare();
                        audioAlarmPlayer.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } else {
            try {
                audioAlarmPlayer = MediaPlayer.create(this, musicFile);

                AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                int result = am.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    // Start playback.
                    audioAlarmPlayer.setLooping(true);
                    final float volume = (float) (1 - (Math.log(MAX_VOLUME - 85) / Math.log(MAX_VOLUME)));
                    audioAlarmPlayer.setVolume(volume, volume);
                    audioAlarmPlayer.start();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * This method is to pause alarm sound
     */
    public void pauseMusic() {
        if (audioAlarmPlayer.isPlaying()) {
            audioAlarmPlayer.pause();
            length = audioAlarmPlayer.getCurrentPosition();

        }
    }

    /**
     * This method is to resume alarm sound
     */
    public void resumeMusic() {
        if (audioAlarmPlayer.isPlaying() == false) {
            audioAlarmPlayer.seekTo(length);
            audioAlarmPlayer.start();
        }
    }

    /**
     * This method is to stop alarm sound
     */
    public void stopMusic() {
        audioAlarmPlayer.stop();
        audioAlarmPlayer.release();
        audioAlarmPlayer = null;
    }

    /**
     * Error handling for the sound play service
     * @param mediaPlayer media player object
     * @param errorCode error code
     * @return status of the media player
     */
    public boolean onError(MediaPlayer mediaPlayer, int errorCode) {
        if (audioAlarmPlayer != null) {
            try {
                audioAlarmPlayer.stop();
                audioAlarmPlayer.release();
            } finally {
                audioAlarmPlayer = null;
            }
        }
        return false;
    }
}