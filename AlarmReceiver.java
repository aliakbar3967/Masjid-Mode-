package com.app.masjidmode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    Context mContext;
    AudioManager audioManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        String id;
        mContext = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        id = intent.getStringExtra("ID");
        if (id != null) {
            if (id.equals("1")) {
                setSilentMode();
                Log.d("AlarmReceiver", "Set To Silent");
            } else if (id.equals("2")) {
                setNormalMode();
                Log.d("AlarmReceiver", "Set To Normal");
            } else if (id.equals("3")) {
                setSilentMode();
                Log.d("AlarmReceiver", "Set To Silent");

            } else if (id.equals("4")) {
                setNormalMode();
                Log.d("AlarmReceiver", "Set To Normal");

            } else if (id.equals("5")) {
                setSilentMode();
                Log.d("AlarmReceiver", "Set To Silent");

            } else if (id.equals("6")) {
                setNormalMode();
                Log.d("AlarmReceiver", "Set To Normal");

            } else if (id.equals("7")) {
                setSilentMode();
                Log.d("AlarmReceiver", "Set To Silent");

            } else if (id.equals("8")) {
                setNormalMode();
            } else if (id.equals("9")) {
                setSilentMode();
                Log.d("AlarmReceiver", "Set To Silent");

            } else if (id.equals("10")) {
                setNormalMode();
                Log.d("AlarmReceiver", "Set To Normal");

            } else if (id.equals("11")) {
                azanAlert();

            } else {
                Log.d("AlarmReceiver", "recieved : " + id + " something went wrong");

            }
            Log.d("AlarmReceiver", "Id is: " + id);
        }
    }

    public void setSilentMode() {
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    public void setNormalMode() {
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    public void azanAlert() {
        Intent svc=new Intent(mContext, BackgroundAlarmAlertSerivce.class);
        mContext.startService(svc);
    }


}
