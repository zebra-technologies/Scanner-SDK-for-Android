package com.zebra.scannercontrol.app.work;

import static com.zebra.scannercontrol.app.helpers.Constants.VIRTUAL_TETHER_HOST_BACKGROUND_MODE_NOTIFICATION;
import static com.zebra.scannercontrol.app.helpers.Constants.VIRTUAL_TETHER_HOST_NOTIFICATION_CHANNEL_ID;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.zebra.scannercontrol.app.R;
import com.zebra.scannercontrol.app.activities.VirtualTetherSettings;
import com.zebra.scannercontrol.app.helpers.Constants;

/**
 * The BackgroundSoundWorker class is designed to run background tasks while playing a specified audio file in a loop.
 * It utilizes Android's WorkManager to handle background execution and manages audio playback alongside foreground service notifications.
 **/

public class BackgroundSoundWorker extends Worker {

    private MediaPlayer audioAlarmPlayer;
    private static final int MAX_VOLUME = 100;
    private AudioFocusRequest audioFocusRequest;
    int audioFocusRequestResult;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;

    public BackgroundSoundWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        createNotificationChannel();
        startNotification();

        playMusic(R.raw.vt_alarm_tone);

        // Loop and check for cancellation
        while (!isStopped()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Result.failure();
            }
        }

        stopMusic();

        return Result.success();
    }

    @Override
    public void onStopped() {
        super.onStopped();
        stopMusic();

        // Abandon the focus request when work stops
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        if(audioFocusRequest != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        }else{
            if(audioFocusChangeListener != null){
                audioManager.abandonAudioFocus(audioFocusChangeListener);
            }
        }

    }

    private void startNotification() {
        Context context = getApplicationContext();

        String channelId = context.getString(R.string.virtual_tether_notification_channel_id);
        // Define resultIntent with the target activity class to be launched when user click the notification.
        Intent resultIntent;
        resultIntent = new Intent(context, VirtualTetherSettings.class);
        resultIntent.putExtra(Constants.VIRTUAL_TETHER_EVENT_NOTIFY, true);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.app_icon_small)
                .setContentText(VIRTUAL_TETHER_HOST_BACKGROUND_MODE_NOTIFICATION);
        notificationBuilder.setContentIntent(resultPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context,R.string.virtual_tether_permission_not_granted,Toast.LENGTH_LONG);
            return;
        }
        notificationManager.notify(VIRTUAL_TETHER_HOST_NOTIFICATION_CHANNEL_ID, notificationBuilder.build());

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    getApplicationContext().getString(R.string.virtual_tether_notification_channel_id),
                    getApplicationContext().getString(R.string.virtual_tether_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void playMusic(int musicFile) {
        Context context = getApplicationContext();
        if (audioAlarmPlayer != null && audioAlarmPlayer.isPlaying()) {
            audioAlarmPlayer.stop();
            audioAlarmPlayer.release();
        }
        audioFocusChangeListener = focusChange -> {
            final float volume = (float) (1 - (Math.log(MAX_VOLUME - 85.0) / Math.log(MAX_VOLUME)));
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if(audioAlarmPlayer!=null && (audioAlarmPlayer.isPlaying() || audioAlarmPlayer.isLooping())){
                        audioAlarmPlayer.setVolume(volume / 2, volume / 2);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    if(audioAlarmPlayer!=null && (audioAlarmPlayer.isPlaying() || audioAlarmPlayer.isLooping())){
                        audioAlarmPlayer.setVolume(volume, volume);
                    }
                    break;
                default: //Do Nothing
                    break;
            }
        };

        audioAlarmPlayer = MediaPlayer.create(context, musicFile);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (getAudioFocusRequestResult(audioManager) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
          startAudioAlarmPlayer();
        }else{
           new Handler(Looper.getMainLooper()).postDelayed(this::startAudioAlarmPlayer,1000);
        }
    }

    private void startAudioAlarmPlayer(){
        audioAlarmPlayer.setLooping(true);
        final float volume = (float) (1 - (Math.log(MAX_VOLUME - 85.0) / Math.log(MAX_VOLUME)));
        audioAlarmPlayer.setVolume(volume, volume);
        audioAlarmPlayer.start();
    }

    private int getAudioFocusRequestResult(AudioManager audioManager){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .setFocusGain(AudioManager.AUDIOFOCUS_GAIN)
                    .setAcceptsDelayedFocusGain(false) //To get Immediate Gain
                    .setWillPauseWhenDucked(true) //Reduce sound while playing notification sound
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .build();
            audioFocusRequestResult = audioManager.requestAudioFocus(audioFocusRequest);
        }else{
            audioFocusRequestResult = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        return audioFocusRequestResult;
    }

    private void stopMusic(){
        if (audioAlarmPlayer != null) {
            audioAlarmPlayer.stop();
            audioAlarmPlayer.release();
            audioAlarmPlayer = null;
        }
        // Remove the notification when work is completed
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(VIRTUAL_TETHER_HOST_NOTIFICATION_CHANNEL_ID);
    }


}