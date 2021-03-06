package ru.levabala.sensors_recorder.Recorder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.location.LocationManager;
import android.media.tv.TvInputService;
import android.os.Environment;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import ru.levabala.sensors_recorder.Activities.MainActivity;
import ru.levabala.sensors_recorder.Other.CallbackInterface;
import ru.levabala.sensors_recorder.Other.FileMethods;
import ru.levabala.sensors_recorder.Other.Utils;
import ru.levabala.sensors_recorder.Services.SensorsService;

/**
 * Created by levabala on 05.05.2017.
 */

public class Recorder {
    public boolean serviceIsRunning = false;
    private Context context;
    private Intent serviceIntent;
    private Activity activity;
    private ArrayList<SensorType> sensorsToRecord;
    private ArrayList<Integer> sensorsListInteger;
    private long startTime;
    public static String startTimeString;

    private boolean mBound = false;
    private SensorsService mService;

    public Recorder(ArrayList<SensorType> sensors, Activity activity){
        this.context = activity.getApplicationContext();
        this.activity = activity;
        this.serviceIntent = new Intent(context, SensorsService.class);
        this.sensorsToRecord = sensors;
        this.sensorsListInteger = new ArrayList<>();
        for (SensorType sType : sensors)
            sensorsListInteger.add(sType.getType());

        context.bindService(this.serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void onDestroy(){
        if (mBound) {
            mService.stopSelf();
            context.unbindService(mConnection);
            mBound = false;
        }
    }

    public void start(boolean recordGPS, Context context){
        if (serviceIsRunning) return;

        Utils.logText("Start", context);

        serviceIsRunning = true;
        startTime = System.currentTimeMillis();
        startTimeString = new SimpleDateFormat("yyyy-MM-dd'T'HH'h'mm'm'ss").format(Calendar.getInstance().getTime());

        if (recordGPS)
            if (!((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)){ //yohoho
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setMessage("Please, turn on GPS");
                dialog.setPositiveButton("Turn on", (DialogInterface paramDialogInterface, int paramInt) -> {
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(myIntent);
                });
                dialog.setNegativeButton("Cancel", (DialogInterface paramDialogInterface, int paramInt) -> {

                });
                dialog.show();
            }

        serviceIntent.putIntegerArrayListExtra("sensorsToRecord", sensorsListInteger);
        serviceIntent.putExtra("recordGPS", recordGPS);
        context.startService(serviceIntent);
    }

    public void pause(){

    }

    public void resume(){

    }

    public void stop(){
        Utils.logText("End", context);
        if (mService != null) {
            mService.onDestroy();
            mService.stopSelf();
        }
        context.stopService(serviceIntent);

        serviceIsRunning = false;
    }

    private void checkPreviousBuffer(){
        if (!FileMethods.isFileEmpty(MainActivity.EXTERNAL_BUFFER_FILE, 3)){
            Utils.snackbarAlert(
                    "Here are some data from previous session to save",
                    activity.findViewById(MainActivity.FAB_ID),
                    (View v) -> {
                        saveBuffer(() -> {});
                    });
        }
    }

    private void saveBuffer(final CallbackInterface callback){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
        final String formattedDate = df.format(c.getTime());

        final EditText input = new EditText(context);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (input.getText().toString().equals(MainActivity.BUFFER_FILENAME))
                    Utils.logText("You mustn't save tracks as \"buffer.dat\"", context);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String filename = input.getText().toString() + ".dat";
                File file = FileMethods.getExternalFile(filename);
                //checking for app-reserved files
                if (filename.equals(MainActivity.BUFFER_FILENAME)){
                    Utils.logText("Not saved. You can't use the names:\n'buffer.dat'\n'listoftracks.config'", context);
                    return;
                }
                saveRecordedData(file);

                Utils.logText("Saved as " + filename + "\nSize: "
                        + String.valueOf(file.length()) + "B", context);

                serviceIsRunning = false;
                callback.run();
            }
        };
        Utils.requestStringInDialog("Route saving", "File name:", formattedDate, input, onClickListener, (Activity)context, context);
    }

    private void saveRecordedData(File file){

    }

    //binder
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SensorsService.LocalBinder binder = (SensorsService.LocalBinder)service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public Map<SensorType, String> getSensorsInfo(){
        if (!serviceIsRunning) return new HashMap<SensorType, String>();
        else return SensorsService.sensorsInfo;
    }
}
