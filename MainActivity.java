package com.app.masjidmode;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.rm.rmswitch.RMSwitch;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.app.masjidmode.R.drawable.button_shape_checked;
import static com.app.masjidmode.R.drawable.button_shape_un_checked;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {

    //toggle buttons
    com.rm.rmswitch.RMSwitch fajir, zohar, asar, magrib, esha;
    AudioManager audioManager;

    //buttons to get Time From time picker and set
    Button buttonFajirStart, buttonFajirEnd;
    Button buttonZoharStart, buttonZoharEnd;
    Button buttonAsarStart, buttonAsarEnd;
    Button buttonMagribStart, buttonMagribEnd;
    Button buttonEshaStart, buttonEshaEnd;

    Boolean booleanFajir = false, booleanZohar = false, booleanAsar = false, booleanMagrib = false, booleanEsha = false;

    //boolean to check if both startTime and endTime has been set in individual Alarm
    boolean fajirTimeStart = false, fajirTimeEnd = false;
    boolean zoharTimeStart = false, zoharTimeEnd = false;
    boolean asarTimeStart = false, asarTimeEnd = false;
    boolean magribTimeStart = false, magribTimeEnd = false;
    boolean eshaTimeStart = false, eshaTimeEnd = false;

    boolean isFajarAlarmExists, isZoharAlarmExists, isAsarAlarmExists, isMagribAlarmExists, isEshaAlarmExists;

    //Id for each Pending Intent
    int fajirTimeStartID = 1,
            fajirTimeEndID = 2,
            zoharTimeStartID = 3,
            zoharTimeEndID = 4,
            asarTimeStartID = 5,
            asarTimeEndID = 6,
            magribTimeStartID = 7,
            magribTimeEndID = 8,
            eshaTimeStartID = 9,
            eshaTimeEndID = 10;

    //for time storing in each Calender object
    public Calendar fajirTimeStartCalender,
            fajirTimeEndCalender,
            zoharTimeStarCalender,
            zoharTimeEndCalender,
            asarTimeStartCalender,
            asarTimeEndCalender,
            magribTimeStartCalender,
            magribTimeEndCalender,
            eshaTimeStartCalender,
            eshaTimeEndCalender;

    public Calendar tempCalenderAzanReminder;
    SharedPreferences prefs = null;


    String time;
    DialogFragment timePicker;

    Button tempButton;
    View tempView;

    //save States & Alarm Time on Application Close
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*  checkLockedStatus();*/

        audioManager = (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
        sharedPreferences = getApplicationContext().getSharedPreferences(String.valueOf(R.string.PREFERENC_TAG), Context.MODE_PRIVATE);
        prefs = getSharedPreferences("com.aafaq.salatapp", MODE_PRIVATE);

        editor = sharedPreferences.edit();


        tempCalenderAzanReminder = Calendar.getInstance();

        //Buttons to Pick Time
        buttonFajirStart = findViewById(R.id.buttonTimeStartFajar);
        buttonFajirEnd = findViewById(R.id.buttonTimeEndFajar);

        buttonZoharStart = findViewById(R.id.buttonTimeStartZohar);
        buttonZoharEnd = findViewById(R.id.buttonTimeEndZohar);

        buttonAsarStart = findViewById(R.id.buttonTimeStartAsar);
        buttonAsarEnd = findViewById(R.id.buttonTimeEndAsar);

        buttonMagribStart = findViewById(R.id.buttonTimeStartMagrib);
        buttonMagribEnd = findViewById(R.id.buttonTimeEndMagrib);

        buttonEshaStart = findViewById(R.id.buttonTimeStartEsha);
        buttonEshaEnd = findViewById(R.id.buttonTimeEndEsha);


        //Toggle Buttons
        fajir = findViewById(R.id.toggleSwitchFajar);
        zohar = findViewById(R.id.toggleSwitchZohar);
        asar = findViewById(R.id.toggleSwitchAsar);
        magrib = findViewById(R.id.toggleSwitchMagrib);
        esha = findViewById(R.id.toggleSwitchEsha);


        //setting On clickListener by Implementing OnClick in MainActivity "see Line 34"
        buttonFajirStart.setOnClickListener(this);
        buttonFajirEnd.setOnClickListener(this);
        buttonZoharStart.setOnClickListener(this);
        buttonZoharEnd.setOnClickListener(this);
        buttonAsarStart.setOnClickListener(this);
        buttonAsarEnd.setOnClickListener(this);
        buttonMagribStart.setOnClickListener(this);
        buttonMagribEnd.setOnClickListener(this);
        buttonEshaStart.setOnClickListener(this);
        buttonEshaEnd.setOnClickListener(this);


        timePicker = new TimePickerFragment();


        //ask permission for higher sdk and ignores permission lower then Marshmallow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                setFajirMode();
                setZoharMode();
                setAsarMode();
                setMagribMode();
                setEshaMode();
            } else {
                PermissionTask permissionTask = new PermissionTask();
                permissionTask.execute();
            }
        } else {
            setFajirMode();
            setZoharMode();
            setAsarMode();
            setMagribMode();
            setEshaMode();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean checkPermission() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert mNotificationManager != null;
        if (mNotificationManager.isNotificationPolicyAccessGranted()) {
            return true;
        } else {
            Toast.makeText(this, "Please grant Permission", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void setFajirMode() {
        fajir.addSwitchObserver(new RMSwitch.RMSwitchObserver() {
            @Override
            public void onCheckStateChange(RMSwitch switchView, boolean isChecked) {

                if (!isChecked) {
                    buttonFajirStart.setBackground(getResources().getDrawable(button_shape_un_checked));
                    buttonFajirEnd.setBackground(getResources().getDrawable(button_shape_un_checked));

                    buttonFajirStart.setEnabled(true);
                    buttonFajirEnd.setEnabled(true);

                    booleanFajir = false;
                    isFajarAlarmExists = false;

                    cancelAlarmManagerProcess(1, 2);

                } else {
                    buttonFajirStart.setEnabled(false);
                    buttonFajirEnd.setEnabled(false);

                    if (!buttonFajirStart.getText().toString().equals(getString(R.string.default_text)) && !buttonFajirEnd.getText().toString().equals(getString(R.string.default_text)) &&
                            fajirTimeStartCalender.getTimeInMillis() != 000 && fajirTimeEndCalender.getTimeInMillis() != 000) {

                        if (!isFajarAlarmExists) {
                            isFajarAlarmExists = true;
                            setAlarm(fajirTimeStartCalender, fajirTimeEndCalender);
                            Toast.makeText(MainActivity.this, "Alarm Set from: " + getTimeFromMills(fajirTimeStartCalender.getTimeInMillis()) + " to: " + getTimeFromMills(fajirTimeEndCalender.getTimeInMillis()), Toast.LENGTH_SHORT).show();
                        }

                        //Toast.makeText(MainActivity.this, "Fajir Alarm Added for: " + fajirTimeStartCalender.getTime(), Toast.LENGTH_LONG).show();
                        buttonFajirStart.setBackground(getResources().getDrawable(button_shape_checked));
                        buttonFajirEnd.setBackground(getResources().getDrawable(button_shape_checked));

                    } else {
                        Toast.makeText(MainActivity.this, "Please Set The Time First", Toast.LENGTH_SHORT).show();


                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                fajir.setChecked(false);
                            }
                        }, 800);

                        buttonFajirStart.setEnabled(true);
                        buttonFajirEnd.setEnabled(true);

                    }
                }

            }
        });

    }

    private void setZoharMode() {
        zohar.addSwitchObserver(new RMSwitch.RMSwitchObserver() {
            @Override
            public void onCheckStateChange(RMSwitch switchView, boolean isChecked) {
                if (!isChecked) {

                    cancelAlarmManagerProcess(3, 4);
                    buttonZoharStart.setBackground(getResources().getDrawable(button_shape_un_checked));
                    buttonZoharEnd.setBackground(getResources().getDrawable(button_shape_un_checked));

                    booleanZohar = false;
                    buttonZoharStart.setEnabled(true);
                    buttonZoharEnd.setEnabled(true);
                    isZoharAlarmExists = false;

                } else {

                    buttonZoharStart.setEnabled(false);
                    buttonZoharEnd.setEnabled(false);

                    if (!buttonZoharStart.getText().toString().equals(getString(R.string.default_text)) && !buttonZoharEnd.getText().toString().equals(getString(R.string.default_text)) &&
                            zoharTimeStarCalender.getTimeInMillis() != 000 && zoharTimeEndCalender.getTimeInMillis() != 000) {
                        if (!isZoharAlarmExists) {
                            isZoharAlarmExists = true;
                            setAlarm(zoharTimeStarCalender, zoharTimeEndCalender);
                            Toast.makeText(MainActivity.this, "Alarm Set from: " + getTimeFromMills(zoharTimeStarCalender.getTimeInMillis()) + " to: " + getTimeFromMills(zoharTimeEndCalender.getTimeInMillis()), Toast.LENGTH_SHORT).show();
                        }
                        //Toast.makeText(MainActivity.this, "Zohar Alarm Added for: " + zoharTimeStarCalender.getTime(), Toast.LENGTH_LONG).show();

                        buttonZoharStart.setBackground(getResources().getDrawable(button_shape_checked));
                        buttonZoharEnd.setBackground(getResources().getDrawable(button_shape_checked));

                    } else {
                        Toast.makeText(MainActivity.this, "Please Set The Time First", Toast.LENGTH_SHORT).show();

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                zohar.setChecked(false);

                            }
                        }, 800);

                        buttonZoharStart.setEnabled(true);
                        buttonZoharEnd.setEnabled(true);

                    }
                }
            }
        });
    }

    private void setAsarMode() {
        asar.addSwitchObserver(new RMSwitch.RMSwitchObserver() {
            @Override
            public void onCheckStateChange(RMSwitch switchView, boolean isChecked) {
                if (!isChecked) {
                    cancelAlarmManagerProcess(3, 4);
                    buttonAsarStart.setBackground(getResources().getDrawable(button_shape_un_checked));
                    buttonAsarEnd.setBackground(getResources().getDrawable(button_shape_un_checked));

                    booleanAsar = false;
                    buttonAsarStart.setEnabled(true);
                    buttonAsarEnd.setEnabled(true);
                    isAsarAlarmExists = false;

                } else {

                    buttonAsarStart.setEnabled(false);
                    buttonAsarEnd.setEnabled(false);

                    if (!buttonAsarStart.getText().toString().equals(getString(R.string.default_text)) && !buttonAsarEnd.getText().toString().equals(getString(R.string.default_text)) &&
                            asarTimeStartCalender.getTimeInMillis() != 000 && asarTimeEndCalender.getTimeInMillis() != 000) {
                        if (!isAsarAlarmExists) {
                            isAsarAlarmExists = true;
                            setAlarm(asarTimeStartCalender, asarTimeEndCalender);
                            Toast.makeText(MainActivity.this, "Alarm Set from: " + getTimeFromMills(asarTimeStartCalender.getTimeInMillis()) + " to: " + getTimeFromMills(asarTimeEndCalender.getTimeInMillis()), Toast.LENGTH_SHORT).show();
                        }
                        //Toast.makeText(MainActivity.this, "Zohar Alarm Added for: " + zoharTimeStarCalender.getTime(), Toast.LENGTH_LONG).show();

                        buttonAsarStart.setBackground(getResources().getDrawable(button_shape_checked));
                        buttonAsarEnd.setBackground(getResources().getDrawable(button_shape_checked));

                    } else {
                        Toast.makeText(MainActivity.this, "Please Set The Time First", Toast.LENGTH_SHORT).show();

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                asar.setChecked(false);

                            }
                        }, 800);

                        buttonAsarStart.setEnabled(true);
                        buttonAsarEnd.setEnabled(true);

                    }
                }
            }
        });
    }

    private void setMagribMode() {
        magrib.addSwitchObserver(new RMSwitch.RMSwitchObserver() {
            @Override
            public void onCheckStateChange(RMSwitch switchView, boolean isChecked) {
                if (!isChecked) {
                    cancelAlarmManagerProcess(7, 8);

                    buttonMagribStart.setBackground(getResources().getDrawable(button_shape_un_checked));
                    buttonMagribEnd.setBackground(getResources().getDrawable(button_shape_un_checked));

                    booleanMagrib = false;
                    buttonMagribStart.setEnabled(true);
                    buttonMagribEnd.setEnabled(true);
                    isMagribAlarmExists = false;
                } else {

                    buttonMagribStart.setEnabled(false);
                    buttonMagribEnd.setEnabled(false);

                    if (!buttonMagribStart.getText().toString().equals(getString(R.string.default_text)) && !buttonMagribEnd.getText().toString().equals(getString(R.string.default_text)) &&
                            magribTimeStartCalender.getTimeInMillis() != 000 && magribTimeEndCalender.getTimeInMillis() != 000) {
                        if (!isMagribAlarmExists) {
                            isMagribAlarmExists = true;
                            setAlarm(magribTimeStartCalender, magribTimeEndCalender);
                            Toast.makeText(MainActivity.this, "Alarm Set from: " + getTimeFromMills(magribTimeStartCalender.getTimeInMillis()) + " to: " + getTimeFromMills(magribTimeEndCalender.getTimeInMillis()), Toast.LENGTH_SHORT).show();
                        }

                        //Toast.makeText(MainActivity.this, "Magrib Alarm Added for: " + magribTimeStartCalender.getTime(), Toast.LENGTH_LONG).show();

                        buttonMagribStart.setBackground(getResources().getDrawable(button_shape_checked));
                        buttonMagribEnd.setBackground(getResources().getDrawable(button_shape_checked));

                    } else {
                        Toast.makeText(MainActivity.this, "Please Set Time First", Toast.LENGTH_SHORT).show();


                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                magrib.setChecked(false);
                            }
                        }, 800);

                        buttonMagribStart.setEnabled(true);
                        buttonMagribEnd.setEnabled(true);
                    }
                }
            }
        });

    }

    private void setEshaMode() {
        esha.addSwitchObserver(new RMSwitch.RMSwitchObserver() {
            @Override
            public void onCheckStateChange(RMSwitch switchView, boolean isChecked) {
                if (!isChecked) {
                    cancelAlarmManagerProcess(9, 10);
                    buttonEshaStart.setBackground(getResources().getDrawable(button_shape_un_checked));
                    buttonEshaEnd.setBackground(getResources().getDrawable(button_shape_un_checked));

                    booleanEsha = false;
                    buttonEshaStart.setEnabled(true);
                    buttonEshaEnd.setEnabled(true);
                    isEshaAlarmExists = false;
                } else {

                    buttonEshaStart.setEnabled(false);
                    buttonEshaEnd.setEnabled(false);

                    if (!buttonEshaStart.getText().toString().equals(getString(R.string.default_text)) && !buttonEshaEnd.getText().toString().equals(getString(R.string.default_text)) &&
                            eshaTimeStartCalender.getTimeInMillis() != 000 && eshaTimeEndCalender.getTimeInMillis() != 000) {
                        if (!isEshaAlarmExists) {
                            isEshaAlarmExists = true;
                            setAlarm(eshaTimeStartCalender, eshaTimeEndCalender);
                            Toast.makeText(MainActivity.this, "Alarm Set from: " + getTimeFromMills(eshaTimeStartCalender.getTimeInMillis()) + " to: " + getTimeFromMills(eshaTimeEndCalender.getTimeInMillis()), Toast.LENGTH_SHORT).show();
                        }

                        //Toast.makeText(MainActivity.this, "Esha Alarm Added for: " + eshaTimeStartCalender.getTime(), Toast.LENGTH_LONG).show();

                        buttonEshaStart.setBackground(getResources().getDrawable(button_shape_checked));
                        buttonEshaEnd.setBackground(getResources().getDrawable(button_shape_checked));

                    } else {
                        Toast.makeText(MainActivity.this, "Please Set Time First", Toast.LENGTH_SHORT).show();

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                esha.setChecked(false);
                            }
                        }, 800);

                        buttonEshaStart.setEnabled(true);
                        buttonEshaEnd.setEnabled(true);

                    }
                }

            }
        });
    }

    //actual process to setAlarm triggered from individual SetModes Functions
    public void setAlarm(Calendar cStart, Calendar cEnd) {
        if (fajirTimeStart && fajirTimeEnd || zoharTimeStart && zoharTimeEnd ||
                asarTimeStart && asarTimeEnd || magribTimeStart && magribTimeEnd || eshaTimeStart && eshaTimeEnd) {
            if (fajirTimeStart && fajirTimeEnd && fajir.isChecked()) {

                alarm(fajirTimeStartID, cStart);
                alarm(fajirTimeEndID, cEnd);

                azanAlert(cStart);
            }
            if (zoharTimeStart && zoharTimeEnd && zohar.isChecked()) {

                alarm(zoharTimeStartID, cStart);
                alarm(zoharTimeEndID, cEnd);

                azanAlert(cStart);
            }
            if (asarTimeStart && asarTimeEnd && asar.isChecked()) {

                alarm(asarTimeStartID, cStart);
                alarm(asarTimeEndID, cEnd);

                azanAlert(cStart);

            }
            if (magribTimeStart && magribTimeEnd && magrib.isChecked()) {

                alarm(magribTimeStartID, cStart);
                alarm(magribTimeEndID, cEnd);

                azanAlert(cStart);

            }
            if (eshaTimeStart && eshaTimeEnd && esha.isChecked()) {

                alarm(eshaTimeStartID, cStart);
                alarm(eshaTimeEndID, cEnd);

                azanAlert(cStart);

            }

        } else {
            Toast.makeText(MainActivity.this, "No Silent Time Set", Toast.LENGTH_SHORT).show();
        }
    }

    //this function will be called when toggle button will be unchecked
    public void cancelAlarmManagerProcess(int startID, int endID) {

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        final Intent intent = new Intent(this, AlarmReceiver.class);







        PendingIntent startPendingIntent = PendingIntent.getBroadcast(this, startID, intent, PendingIntent.FLAG_MUTABLE);
        PendingIntent endPendingIntent = PendingIntent.getBroadcast(this, endID, intent, PendingIntent.FLAG_MUTABLE);

        //for alert
        PendingIntent alarmAlertPendingIntent = PendingIntent.getBroadcast(this, endID, intent, PendingIntent.FLAG_MUTABLE);

        alarmManager.cancel(startPendingIntent);
        alarmManager.cancel(endPendingIntent);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        Toast.makeText(this, "Alarm Cancelled", Toast.LENGTH_SHORT).show();

    }

    public void alarm(int id, Calendar calendar) {
        String stringID = String.valueOf(id);
        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        intent.putExtra("ID", stringID);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(MainActivity.this, id, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }
        assert alarmManager != null;
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, (calendar.getTimeInMillis()), (SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_DAY), alarmIntent);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    //set The Alarm time text and state after resume / start
    public void setAlarmTextAndState() {
        if (fajirTimeStart && fajirTimeEnd && booleanFajir) {
            buttonFajirStart.setEnabled(false);
            buttonFajirEnd.setEnabled(false);

            fajir.setChecked(true);

            buttonFajirStart.setBackground(getResources().getDrawable(button_shape_checked));
            buttonFajirEnd.setBackground(getResources().getDrawable(button_shape_checked));

            buttonFajirStart.setText(getTimeFromMills(fajirTimeStartCalender.getTimeInMillis()));
            buttonFajirEnd.setText(getTimeFromMills(fajirTimeEndCalender.getTimeInMillis()));
        }
        if (!booleanFajir) {
            buttonFajirStart.setEnabled(true);
            buttonFajirEnd.setEnabled(true);

            fajir.setChecked(false);

            buttonFajirStart.setBackground(getResources().getDrawable(button_shape_un_checked));
            buttonFajirEnd.setBackground(getResources().getDrawable(button_shape_un_checked));

            if (fajirTimeStartCalender.getTimeInMillis() != 000 && fajirTimeEndCalender.getTimeInMillis() != 000) {
                buttonFajirStart.setText(getTimeFromMills(fajirTimeStartCalender.getTimeInMillis()));
                buttonFajirEnd.setText(getTimeFromMills(fajirTimeEndCalender.getTimeInMillis()));
            }

        }

        ///handles zohar if alarm exists
        if (zoharTimeStart && zoharTimeEnd && booleanZohar) {
            buttonZoharStart.setEnabled(false);
            buttonZoharEnd.setEnabled(false);

            zohar.setChecked(true);

            buttonZoharStart.setBackground(getResources().getDrawable(button_shape_checked));
            buttonZoharEnd.setBackground(getResources().getDrawable(button_shape_checked));


            buttonZoharStart.setText(getTimeFromMills(zoharTimeStarCalender.getTimeInMillis()));
            buttonZoharEnd.setText(getTimeFromMills(zoharTimeEndCalender.getTimeInMillis()));
        }
        //Handles Zohar if Alarm Exists but turned off
        if (!booleanZohar) {
            buttonZoharStart.setEnabled(true);
            buttonZoharEnd.setEnabled(true);

            zohar.setChecked(false);

            buttonZoharStart.setBackground(getResources().getDrawable(button_shape_un_checked));
            buttonZoharEnd.setBackground(getResources().getDrawable(button_shape_un_checked));

            if (zoharTimeStarCalender.getTimeInMillis() != 000 && zoharTimeEndCalender.getTimeInMillis() != 000) {
                buttonZoharStart.setText(getTimeFromMills(zoharTimeStarCalender.getTimeInMillis()));
                buttonZoharEnd.setText(getTimeFromMills(zoharTimeEndCalender.getTimeInMillis()));
            }
        }

        ///handles asar if Alarm Exists
        if (asarTimeStart && asarTimeEnd && booleanAsar) {
            buttonAsarStart.setEnabled(false);
            buttonAsarEnd.setEnabled(false);

            asar.setChecked(true);

            buttonAsarStart.setBackground(getResources().getDrawable(button_shape_checked));
            buttonAsarEnd.setBackground(getResources().getDrawable(button_shape_checked));


            buttonAsarStart.setText(getTimeFromMills(asarTimeStartCalender.getTimeInMillis()));
            buttonAsarEnd.setText(getTimeFromMills(asarTimeEndCalender.getTimeInMillis()));
        }
        //Handle Asar if Alarm Exists but turned off
        if (!booleanAsar) {
            buttonAsarStart.setEnabled(true);
            buttonAsarEnd.setEnabled(true);

            asar.setChecked(false);

            buttonAsarStart.setBackground(getResources().getDrawable(button_shape_un_checked));
            buttonAsarEnd.setBackground(getResources().getDrawable(button_shape_un_checked));

            if (asarTimeStartCalender.getTimeInMillis() != 000 && asarTimeEndCalender.getTimeInMillis() != 000) {
                buttonAsarStart.setText(getTimeFromMills(asarTimeStartCalender.getTimeInMillis()));
                buttonAsarEnd.setText(getTimeFromMills(asarTimeEndCalender.getTimeInMillis()));
            }
        }

        //handles magrib if alarm exists
        if (magribTimeStart && magribTimeEnd && booleanMagrib) {
            buttonMagribStart.setEnabled(false);
            buttonMagribEnd.setEnabled(false);

            magrib.setChecked(true);

            buttonMagribStart.setBackground(getResources().getDrawable(button_shape_checked));
            buttonMagribEnd.setBackground(getResources().getDrawable(button_shape_checked));


            buttonMagribStart.setText(getTimeFromMills(magribTimeStartCalender.getTimeInMillis()));
            buttonMagribEnd.setText(getTimeFromMills(magribTimeEndCalender.getTimeInMillis()));
        }
        //handles magrib if alarm exists but turned off
        if (!booleanMagrib) {
            buttonMagribStart.setEnabled(true);
            buttonMagribEnd.setEnabled(true);

            magrib.setChecked(false);

            buttonMagribStart.setBackground(getResources().getDrawable(button_shape_un_checked));
            buttonMagribEnd.setBackground(getResources().getDrawable(button_shape_un_checked));

            if (magribTimeStartCalender.getTimeInMillis() != 000 && magribTimeEndCalender.getTimeInMillis() != 000) {
                buttonMagribStart.setText(getTimeFromMills(magribTimeStartCalender.getTimeInMillis()));
                buttonMagribEnd.setText(getTimeFromMills(magribTimeEndCalender.getTimeInMillis()));
            }
        }


        //Handle Esha if alarm exists
        if (eshaTimeStart && eshaTimeEnd && booleanEsha) {
            buttonEshaStart.setEnabled(false);
            buttonEshaEnd.setEnabled(false);

            esha.setChecked(true);


            buttonEshaStart.setBackground(getResources().getDrawable(button_shape_checked));
            buttonEshaEnd.setBackground(getResources().getDrawable(button_shape_checked));


            buttonEshaStart.setText(getTimeFromMills(eshaTimeStartCalender.getTimeInMillis()));
            buttonEshaEnd.setText(getTimeFromMills(eshaTimeEndCalender.getTimeInMillis()));

        }
        //handle Esha if alarm exists but turned off
        if (!booleanEsha) {
            buttonEshaStart.setEnabled(true);
            buttonEshaEnd.setEnabled(true);
            esha.setChecked(false);


            buttonEshaStart.setBackground(getResources().getDrawable(button_shape_un_checked));
            buttonEshaEnd.setBackground(getResources().getDrawable(button_shape_un_checked));

            if (eshaTimeStartCalender.getTimeInMillis() != 000 && eshaTimeEndCalender.getTimeInMillis() != 000) {
                buttonEshaStart.setText(getTimeFromMills(eshaTimeStartCalender.getTimeInMillis()));
                buttonEshaEnd.setText(getTimeFromMills(eshaTimeEndCalender.getTimeInMillis()));
            }
        }

    }

    //data is stored in Shared Preference before application close/terminating
    public void storeData() {
        if (fajir.isChecked()) {
            editor.putString(getString(R.string.FAJIR_CALENDER_START_TAG), String.valueOf(fajirTimeStartCalender.getTimeInMillis()));
            editor.putString(getString(R.string.FAJIR_CALENDER_END_TAG), String.valueOf(fajirTimeEndCalender.getTimeInMillis()));

            editor.putBoolean(getString(R.string.FAJIR_BOOLEAN_TAG), true);

            editor.putBoolean(getString(R.string.FAJIR_TIME_START_TAG), true);
            editor.putBoolean(getString(R.string.FAJIR_TIME_END_TAG), true);
            this.editor.commit();
        }
        if (!fajir.isChecked()) {
            if (!String.valueOf(fajirTimeStartCalender.getTimeInMillis()).equals(getString(R.string.default_text)) && !String.valueOf(fajirTimeEndCalender.getTimeInMillis()).equals(getString(R.string.default_text))) {
                editor.putString(getString(R.string.FAJIR_CALENDER_START_TAG), String.valueOf(fajirTimeStartCalender.getTimeInMillis()));
                editor.putString(getString(R.string.FAJIR_CALENDER_END_TAG), String.valueOf(fajirTimeEndCalender.getTimeInMillis()));

                editor.putBoolean(getString(R.string.FAJIR_BOOLEAN_TAG), false);

                editor.putBoolean(getString(R.string.FAJIR_TIME_START_TAG), false);
                editor.putBoolean(getString(R.string.FAJIR_TIME_END_TAG), false);
                this.editor.commit();
            }
        }

        if (zohar.isChecked()) {
            editor.putString(getString(R.string.ZOHAR_CALENDER_START_TAG), String.valueOf(zoharTimeStarCalender.getTimeInMillis()));
            editor.putString(getString(R.string.ZOHAR_CALENDER_END_TAG), String.valueOf(zoharTimeEndCalender.getTimeInMillis()));

            editor.putBoolean(getString(R.string.ZOHAR_BOOLEAN_TAG), true);

            editor.putBoolean(getString(R.string.ZOHAR_TIME_START_TAG), true);
            editor.putBoolean(getString(R.string.ZOHAR_TIME_END_TAG), true);

            this.editor.commit();
        }
        if (!zohar.isChecked()) {
            if (!String.valueOf(zoharTimeStarCalender.getTimeInMillis()).equals(getString(R.string.default_text)) && !String.valueOf(zoharTimeEndCalender.getTimeInMillis()).equals(getString(R.string.default_text))) {
                editor.putString(getString(R.string.ZOHAR_CALENDER_START_TAG), String.valueOf(zoharTimeStarCalender.getTimeInMillis()));
                editor.putString(getString(R.string.ZOHAR_CALENDER_END_TAG), String.valueOf(zoharTimeEndCalender.getTimeInMillis()));

                editor.putBoolean(getString(R.string.ZOHAR_BOOLEAN_TAG), false);


                editor.putBoolean(getString(R.string.ZOHAR_TIME_START_TAG), false);
                editor.putBoolean(getString(R.string.ZOHAR_TIME_END_TAG), false);
                this.editor.commit();
            }
        }


        if (asar.isChecked()) {
            editor.putString(getString(R.string.ASAR_CALENDER_START_TAG), String.valueOf(asarTimeStartCalender.getTimeInMillis()));
            editor.putString(getString(R.string.ASAR_CALENDER_END_TAG), String.valueOf(asarTimeEndCalender.getTimeInMillis()));

            editor.putBoolean(getString(R.string.ASAR_BOOLEAN_TAG), true);

            editor.putBoolean(getString(R.string.ASAR_TIME_START_TAG), true);
            editor.putBoolean(getString(R.string.ASAR_TIME_END_TAG), true);

            this.editor.commit();
        }
        if (!asar.isChecked()) {
            if (!String.valueOf(asarTimeStartCalender.getTimeInMillis()).equals(getString(R.string.default_text)) && !String.valueOf(asarTimeEndCalender.getTimeInMillis()).equals(getString(R.string.default_text))) {
                editor.putString(getString(R.string.ASAR_CALENDER_START_TAG), String.valueOf(asarTimeStartCalender.getTimeInMillis()));
                editor.putString(getString(R.string.ASAR_CALENDER_END_TAG), String.valueOf(asarTimeEndCalender.getTimeInMillis()));

                editor.putBoolean(getString(R.string.ASAR_BOOLEAN_TAG), false);


                editor.putBoolean(getString(R.string.ASAR_TIME_START_TAG), false);
                editor.putBoolean(getString(R.string.ASAR_TIME_END_TAG), false);
                this.editor.commit();
            }
        }

        if (magrib.isChecked()) {
            editor.putString(getString(R.string.MAGRIB_CALENDER_START_TAG), String.valueOf(magribTimeStartCalender.getTimeInMillis()));
            editor.putString(getString(R.string.MAGRIB_CALENDER_END_TAG), String.valueOf(magribTimeEndCalender.getTimeInMillis()));

            editor.putBoolean(getString(R.string.MAGRIB_BOOLEAN_TAG), true);

            editor.putBoolean(getString(R.string.MAGRIB_TIME_START_TAG), true);
            editor.putBoolean(getString(R.string.MAGRIB_TIME_END_TAG), true);

            this.editor.commit();
        }
        if (!magrib.isChecked()) {
            if (!String.valueOf(magribTimeStartCalender.getTimeInMillis()).equals(getString(R.string.default_text)) && !String.valueOf(magribTimeEndCalender.getTimeInMillis()).equals(getString(R.string.default_text))) {
                editor.putString(getString(R.string.MAGRIB_CALENDER_START_TAG), String.valueOf(magribTimeStartCalender.getTimeInMillis()));
                editor.putString(getString(R.string.MAGRIB_CALENDER_END_TAG), String.valueOf(magribTimeEndCalender.getTimeInMillis()));

                editor.putBoolean(getString(R.string.MAGRIB_BOOLEAN_TAG), false);


                editor.putBoolean(getString(R.string.MAGRIB_TIME_START_TAG), false);
                editor.putBoolean(getString(R.string.MAGRIB_TIME_END_TAG), false);
                this.editor.commit();
            }
        }

        if (esha.isChecked()) {
            editor.putString(getString(R.string.ESHA_CALENDER_START_TAG), String.valueOf(eshaTimeStartCalender.getTimeInMillis()));
            editor.putString(getString(R.string.ESHA_CALENDER_END_TAG), String.valueOf(eshaTimeEndCalender.getTimeInMillis()));

            editor.putBoolean(getString(R.string.ESHA_BOOLEAN_TAG), true);

            editor.putBoolean(getString(R.string.ESHA_TIME_START_TAG), true);
            editor.putBoolean(getString(R.string.ESHA_TIME_END_TAG), true);
            this.editor.commit();
        }
        if (!esha.isChecked()) {
            if (!String.valueOf(eshaTimeStartCalender.getTimeInMillis()).equals(getString(R.string.default_text)) && !String.valueOf(eshaTimeEndCalender.getTimeInMillis()).equals(getString(R.string.default_text))) {
                editor.putString(getString(R.string.ESHA_CALENDER_START_TAG), String.valueOf(eshaTimeStartCalender.getTimeInMillis()));
                editor.putString(getString(R.string.ESHA_CALENDER_END_TAG), String.valueOf(eshaTimeEndCalender.getTimeInMillis()));

                editor.putBoolean(getString(R.string.ESHA_BOOLEAN_TAG), false);

                editor.putBoolean(getString(R.string.ESHA_TIME_START_TAG), true);
                editor.putBoolean(getString(R.string.ESHA_TIME_END_TAG), true);
                this.editor.commit();
            }
        }
    }

    //Preserved data is restored from shared preferences after resume/start/onCreate (if terminated forcefully)
    public void restoreData() {
        Long startTimeInMills, endTimeInMills;
        if (sharedPreferences.getBoolean((getString(R.string.FAJIR_BOOLEAN_TAG)), false)) {

            booleanFajir = sharedPreferences.getBoolean(getString(R.string.FAJIR_BOOLEAN_TAG), false);

            fajirTimeStart = sharedPreferences.getBoolean(getString(R.string.FAJIR_TIME_START_TAG), false);
            fajirTimeEnd = sharedPreferences.getBoolean(getString(R.string.FAJIR_TIME_END_TAG), false);

            startTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.FAJIR_CALENDER_START_TAG), null));
            endTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.FAJIR_CALENDER_END_TAG), null));

            Calendar calendarTimeStart = Calendar.getInstance();
            Calendar calendarTimeEnd = Calendar.getInstance();

            calendarTimeStart.setTimeInMillis(startTimeInMills);
            calendarTimeEnd.setTimeInMillis(endTimeInMills);

            fajirTimeStartCalender = calendarTimeStart;
            fajirTimeEndCalender = calendarTimeEnd;

        }
        if (!sharedPreferences.getBoolean((getString(R.string.FAJIR_BOOLEAN_TAG)), false)) {

            booleanFajir = sharedPreferences.getBoolean(getString(R.string.FAJIR_BOOLEAN_TAG), false);
            fajirTimeStart = sharedPreferences.getBoolean(getString(R.string.FAJIR_TIME_START_TAG), false);
            fajirTimeEnd = sharedPreferences.getBoolean(getString(R.string.FAJIR_TIME_END_TAG), false);


            startTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.FAJIR_CALENDER_START_TAG), "000"));
            endTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.FAJIR_CALENDER_END_TAG), "000"));


            Calendar calendarTimeStart = Calendar.getInstance();
            Calendar calendarTimeEnd = Calendar.getInstance();

            calendarTimeStart.setTimeInMillis(startTimeInMills);
            calendarTimeEnd.setTimeInMillis(endTimeInMills);

            fajirTimeStartCalender = calendarTimeStart;
            fajirTimeEndCalender = calendarTimeEnd;


        }

        if (sharedPreferences.getBoolean(getString(R.string.ZOHAR_BOOLEAN_TAG), false)) {

            booleanZohar = sharedPreferences.getBoolean(getString(R.string.ZOHAR_BOOLEAN_TAG), false);

            zoharTimeStart = sharedPreferences.getBoolean(getString(R.string.ZOHAR_TIME_START_TAG), false);
            zoharTimeEnd = sharedPreferences.getBoolean(getString(R.string.ZOHAR_TIME_END_TAG), false);

            startTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.ZOHAR_CALENDER_START_TAG), null));
            endTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.ZOHAR_CALENDER_END_TAG), null));


            Calendar calendarTimeStart = Calendar.getInstance();
            Calendar calendarTimeEnd = Calendar.getInstance();

            calendarTimeStart.setTimeInMillis(startTimeInMills);
            calendarTimeEnd.setTimeInMillis(endTimeInMills);

            zoharTimeStarCalender = calendarTimeStart;
            zoharTimeEndCalender = calendarTimeEnd;

        }
        if (!sharedPreferences.getBoolean(getString(R.string.ZOHAR_BOOLEAN_TAG), false)) {
            booleanZohar = sharedPreferences.getBoolean(getString(R.string.ZOHAR_BOOLEAN_TAG), false);


            zoharTimeStart = sharedPreferences.getBoolean(getString(R.string.ZOHAR_TIME_START_TAG), false);
            zoharTimeEnd = sharedPreferences.getBoolean(getString(R.string.ZOHAR_TIME_END_TAG), false);

            startTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.ZOHAR_CALENDER_START_TAG), "000"));
            endTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.ZOHAR_CALENDER_END_TAG), "000"));


            Calendar calendarTimeStart = Calendar.getInstance();
            Calendar calendarTimeEnd = Calendar.getInstance();

            calendarTimeStart.setTimeInMillis(startTimeInMills);
            calendarTimeEnd.setTimeInMillis(endTimeInMills);

            zoharTimeStarCalender = calendarTimeStart;
            zoharTimeEndCalender = calendarTimeEnd;
        }

        if (sharedPreferences.getBoolean(getString(R.string.ASAR_BOOLEAN_TAG), false)) {

            booleanAsar = sharedPreferences.getBoolean(getString(R.string.ASAR_BOOLEAN_TAG), false);

            asarTimeStart = sharedPreferences.getBoolean(getString(R.string.ASAR_TIME_START_TAG), false);
            asarTimeEnd = sharedPreferences.getBoolean(getString(R.string.ASAR_TIME_END_TAG), false);

            startTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.ASAR_CALENDER_START_TAG), null));
            endTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.ASAR_CALENDER_END_TAG), null));


            Calendar calendarTimeStart = Calendar.getInstance();
            Calendar calendarTimeEnd = Calendar.getInstance();

            calendarTimeStart.setTimeInMillis(startTimeInMills);
            calendarTimeEnd.setTimeInMillis(endTimeInMills);

            asarTimeStartCalender = calendarTimeStart;
            asarTimeEndCalender = calendarTimeEnd;

        }
        if (!sharedPreferences.getBoolean(getString(R.string.ASAR_BOOLEAN_TAG), false)) {
            booleanAsar = sharedPreferences.getBoolean(getString(R.string.ASAR_BOOLEAN_TAG), false);


            asarTimeStart = sharedPreferences.getBoolean(getString(R.string.ASAR_TIME_START_TAG), false);
            asarTimeEnd = sharedPreferences.getBoolean(getString(R.string.ASAR_TIME_END_TAG), false);

            startTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.ASAR_CALENDER_START_TAG), "000"));
            endTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.ASAR_CALENDER_END_TAG), "000"));


            Calendar calendarTimeStart = Calendar.getInstance();
            Calendar calendarTimeEnd = Calendar.getInstance();

            calendarTimeStart.setTimeInMillis(startTimeInMills);
            calendarTimeEnd.setTimeInMillis(endTimeInMills);

            asarTimeStartCalender = calendarTimeStart;
            asarTimeEndCalender = calendarTimeEnd;
        }


        if (sharedPreferences.getBoolean(getString(R.string.MAGRIB_BOOLEAN_TAG), false)) {

            booleanMagrib = sharedPreferences.getBoolean(getString(R.string.MAGRIB_BOOLEAN_TAG), false);


            magribTimeEnd = sharedPreferences.getBoolean(getString(R.string.MAGRIB_TIME_START_TAG), false);
            magribTimeStart = sharedPreferences.getBoolean(getString(R.string.MAGRIB_TIME_END_TAG), false);

            startTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.MAGRIB_CALENDER_START_TAG), null));
            endTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.MAGRIB_CALENDER_END_TAG), null));

            Calendar calendarTimeStart = Calendar.getInstance();
            Calendar calendarTimeEnd = Calendar.getInstance();

            calendarTimeStart.setTimeInMillis(startTimeInMills);
            calendarTimeEnd.setTimeInMillis(endTimeInMills);

            magribTimeStartCalender = calendarTimeStart;
            magribTimeEndCalender = calendarTimeEnd;

        }
        if (!sharedPreferences.getBoolean(getString(R.string.MAGRIB_BOOLEAN_TAG), false)) {
            booleanMagrib = sharedPreferences.getBoolean(getString(R.string.MAGRIB_BOOLEAN_TAG), false);


            magribTimeEnd = sharedPreferences.getBoolean(getString(R.string.MAGRIB_TIME_START_TAG), false);
            magribTimeStart = sharedPreferences.getBoolean(getString(R.string.MAGRIB_TIME_END_TAG), false);

            startTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.MAGRIB_CALENDER_START_TAG), "000"));
            endTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.MAGRIB_CALENDER_END_TAG), "000"));

            Calendar calendarTimeStart = Calendar.getInstance();
            Calendar calendarTimeEnd = Calendar.getInstance();

            calendarTimeStart.setTimeInMillis(startTimeInMills);
            calendarTimeEnd.setTimeInMillis(endTimeInMills);

            magribTimeStartCalender = calendarTimeStart;
            magribTimeEndCalender = calendarTimeEnd;
        }


        if (sharedPreferences.getBoolean(getString(R.string.ESHA_BOOLEAN_TAG), false)) {

            booleanEsha = sharedPreferences.getBoolean(getString(R.string.ESHA_BOOLEAN_TAG), false);


            eshaTimeStart = sharedPreferences.getBoolean(getString(R.string.ESHA_TIME_START_TAG), false);
            eshaTimeEnd = sharedPreferences.getBoolean(getString(R.string.ESHA_TIME_END_TAG), false);

            startTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.ESHA_CALENDER_START_TAG), null));
            endTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.ESHA_CALENDER_END_TAG), null));

            Calendar calendarTimeStart = Calendar.getInstance();
            Calendar calendarTimeEnd = Calendar.getInstance();

            calendarTimeStart.setTimeInMillis(startTimeInMills);
            calendarTimeEnd.setTimeInMillis(endTimeInMills);

            eshaTimeStartCalender = calendarTimeStart;
            eshaTimeEndCalender = calendarTimeEnd;

        }
        if (!sharedPreferences.getBoolean(getString(R.string.ESHA_BOOLEAN_TAG), false)) {
            booleanEsha = sharedPreferences.getBoolean(getString(R.string.ESHA_BOOLEAN_TAG), false);
            eshaTimeStart = sharedPreferences.getBoolean(getString(R.string.ESHA_TIME_START_TAG), false);
            eshaTimeEnd = sharedPreferences.getBoolean(getString(R.string.ESHA_TIME_END_TAG), false);

            startTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.ESHA_CALENDER_START_TAG), "000"));
            endTimeInMills = Long.parseLong(sharedPreferences.getString(getString(R.string.ESHA_CALENDER_END_TAG), "000"));

            Calendar calendarTimeStart = Calendar.getInstance();
            Calendar calendarTimeEnd = Calendar.getInstance();

            calendarTimeStart.setTimeInMillis(startTimeInMills);
            calendarTimeEnd.setTimeInMillis(endTimeInMills);

            eshaTimeStartCalender = calendarTimeStart;
            eshaTimeEndCalender = calendarTimeEnd;
        }
    }


    //handles the click of every button in MainActivity
    @Override
    public void onClick(View v) {
        tempView = v;
        tempButton = findViewById(v.getId());
        timePicker.show(getSupportFragmentManager(), "Time Picker");

    }

    //for Azan Alert
    public void azanAlert(Calendar azanAlertTime) {
        Calendar fcalendar = azanAlertTime;
        alarm(11, fcalendar);
    }


    //gets the time from time picker and sets
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String timeText;

        Calendar c = new GregorianCalendar();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

        //setText of the button
        timeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
        time = timeText;

        tempButton.setText(timeText);

        switch (tempView.getId()) {
            case R.id.buttonTimeStartFajar:
                fajirTimeStart = true;
                Toast.makeText(MainActivity.this, "Time set to: " + time, Toast.LENGTH_SHORT).show();
                fajirTimeStartCalender = c;
                break;

            case R.id.buttonTimeEndFajar:
                fajirTimeEnd = true;
                fajirTimeEndCalender = c;
                Toast.makeText(MainActivity.this, "Time set to: " + time, Toast.LENGTH_SHORT).show();

                break;
            case R.id.buttonTimeStartZohar:
                zoharTimeStarCalender = c;
                zoharTimeStart = true;
                Toast.makeText(MainActivity.this, "Time set to: " + time, Toast.LENGTH_SHORT).show();

                break;
            case R.id.buttonTimeEndZohar:
                zoharTimeEndCalender = c;
                zoharTimeEnd = true;
                Toast.makeText(MainActivity.this, "Time set to: " + time, Toast.LENGTH_SHORT).show();

                break;

            case R.id.buttonTimeStartAsar:
                asarTimeStartCalender = c;
                asarTimeStart = true;
                Toast.makeText(MainActivity.this, "Time set to: " + time, Toast.LENGTH_SHORT).show();

                break;
            case R.id.buttonTimeEndAsar:
                asarTimeEndCalender = c;
                asarTimeEnd = true;
                Toast.makeText(MainActivity.this, "Time set to: " + time, Toast.LENGTH_SHORT).show();

                break;

            case R.id.buttonTimeStartMagrib:
                magribTimeStartCalender = c;
                magribTimeStart = true;
                Toast.makeText(MainActivity.this, "Time set to: " + time, Toast.LENGTH_SHORT).show();

                break;
            case R.id.buttonTimeEndMagrib:
                magribTimeEndCalender = c;
                magribTimeEnd = true;
                Toast.makeText(MainActivity.this, "Time set to: " + time, Toast.LENGTH_SHORT).show();

                break;

            case R.id.buttonTimeStartEsha:
                eshaTimeStartCalender = c;
                eshaTimeStart = true;
                Toast.makeText(MainActivity.this, "Time set to: " + time, Toast.LENGTH_SHORT).show();

                break;
            case R.id.buttonTimeEndEsha:
                eshaTimeEndCalender = c;
                eshaTimeEnd = true;
                Toast.makeText(MainActivity.this, "Time set to: " + time, Toast.LENGTH_SHORT).show();

                break;
        }

    }

    //Function 'storeData() is called in onDestroy() before application closing/termination
    @Override
    protected void onDestroy() {
        super.onDestroy();
        storeData();

    }

    //Function 'restoreData() is called in onResume() after application is restored
    @Override
    protected void onResume() {
        super.onResume();

        restoreData();
        setAlarmTextAndState();

    }

    /* Function 'restoreData() is called in onStart() after application is restored to restore the states is available */
    @Override
    protected void onStart() {
        super.onStart();
        restoreData();
        setAlarmTextAndState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        storeData();
    }


    //for share and review button on toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_review:
                Toast.makeText(MainActivity.this, "Rate this App", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_share:
                Toast.makeText(MainActivity.this, "Share App", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    public String getTimeFromMills(Long mills) {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(mills);
        String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY) - 12);
        String minute = String.valueOf(c.get(Calendar.MINUTE));
        String AM_PM;
        if (c.get(Calendar.AM_PM) == 0) {
            AM_PM = "AM";
        } else {
            AM_PM = "PM";
        }

        String time = hour + ":" + minute + " " + AM_PM;
        return time;
    }


    public void getStateChangePermissionRequest() {
        NotificationManager notificationManager = (NotificationManager) MainActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !notificationManager.isNotificationPolicyAccessGranted()) {

            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

            startActivity(intent);
        }
    }

    public void getIgnoreBatteryOptimizationRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }


    private class PermissionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            getIgnoreBatteryOptimizationRequest();
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getStateChangePermissionRequest();
        }
    }
}