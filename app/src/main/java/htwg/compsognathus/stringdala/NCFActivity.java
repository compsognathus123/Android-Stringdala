package htwg.compsognathus.stringdala;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

public class NCFActivity extends AppCompatActivity
{

/*
    loop: Status auslesen: Running/Paused/Error/Ready?

    Running
        anzeigen:  Fortschritt (n-ter Faden, Dauer), aktuelles Mandala


    Paused/Error
        anzeigen: Fortschritt (n-ter Faden, Dauer), akutelles Mandala, Fehler
        einstellen: n-ter Faden, Schrittweite
        aktion: upload

    Ready
        anzeigen: akutelles(letztes) Mandala, n-ter Faden
        anzeigen: neue ausgewähltes Mandala
        einstellen: Mandala, n-ter Faden, Schrittweite
        aktion: upload

 */


    static final byte STATUS_READY = 10;
    static final byte STATUS_WEAVING = 11;
    static final byte STATUS_PAUSED = 12;
    static final byte STATUS_CALIBRATING = 13;


    DrawView drawView;

    TextView tvStatus;
    TextView tvModulus;
    TextView tvTimes;
    TextView tvCurrentString;
    TextView tvDeltaPhoto;
    TextView tvDeltaTooth;
    TextView tvDuration;

    FloatingActionButton fab;

    boolean connected;
    public static boolean running;

    public static boolean mandala_transfer_request;
    public static Mandala mandala;

    public static boolean current_string_transfer_request;
    public static int current_string = 1;

    public static boolean deltaphoto_transfer_request;
    public static int deltaphoto;

    public static final int request_color = Color.argb(255,255,255,175);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ncf);

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION))
        {
            Log.d("NFC Mandala", "HCE available");
        }
        initViews();
        Log.d("NFC Mandala", "acitivity created");

        if(isMyServiceRunning(MyNFCService.class))
        {

            Log.d("NFC Service running", "yes");
        }else{

            Log.d("NFC Service running", "not");
        }

    }

    public static Mandala getMandala()
    {
        return mandala;
    }


    final BroadcastReceiver hceNotificationsReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.hasExtra("apdu"))
            {
                byte[] apdu = intent.getByteArrayExtra("apdu");

                switch (apdu[0])
                {
                    case MyNFCService.NFC_SEND_STATUS:

                        updateStatus(apdu);
                        String stat = "Status: ";

                        for(int i = 0;  i < apdu.length; i++)
                        {
                            stat += apdu[i] + " ";

                        }

                        break;

                    case MyNFCService.NFC_SUCCESS:
                        switch (apdu[1])
                        {
                            case MyNFCService.NFC_RECIEVE_MANDALA:
                                mandala_transfer_request = false;
                                drawView.setBackgroundColor(Color.TRANSPARENT);
                                tvTimes.setBackgroundColor(Color.TRANSPARENT);
                                tvModulus.setBackgroundColor(Color.TRANSPARENT);
                                break;

                            case MyNFCService.NFC_DELTA_PHOTO:
                                deltaphoto_transfer_request = false;
                                tvDeltaPhoto.setBackgroundColor(Color.TRANSPARENT);
                                break;

                            case MyNFCService.NFC_CURRENT_STRING:
                                current_string_transfer_request = false;
                                tvCurrentString.setBackgroundColor(Color.TRANSPARENT);
                                break;


                        }
                }
            }

            if(intent.hasExtra("connected"))
            {
                connected = intent.getBooleanExtra("connected", false);
                if(connected)
                {
                    tvStatus.setTypeface(null, Typeface.BOLD);
                }
                else
                {
                    tvStatus.setTypeface(null, Typeface.NORMAL);
                }
            }
        }
    };

    private void updateStatus(byte[] apdu)
    {
        switch (apdu[1])
        {
            case STATUS_READY: tvStatus.setText("Ready");
            break;
            case STATUS_PAUSED: tvStatus.setText("Paused");
                break;
            case STATUS_CALIBRATING: tvStatus.setText("Calibrating");
                break;
            case STATUS_WEAVING: tvStatus.setText("Weaving");
                break;
        }

        float times = M.bbbbInt(apdu[4], apdu[5], apdu[6], apdu[7]) / 1000F;
        int mod = M.bbInt(apdu[2], apdu[3]);
        current_string = M.bbInt(apdu[8], apdu[9]);
        if(current_string == 0) current_string = 1;

        tvModulus.setText("" + mod);
        tvTimes.setText("" + times);
        tvCurrentString.setText("" + current_string);
        tvDeltaTooth.setText("" + apdu[10]);
        tvDuration.setText("" + M.bbInt(apdu[11], apdu[12]));

        if(mandala.getModulus() != mod || mandala.getTimes() != times)
        {
            mandala = new Mandala(mod, times, true);
            mandala.setFavorite(true);
           // Log.d("new Mandala created!", times + " " + mandala.getTimes() + " mo: " + mod);
            drawView.drawWeavingMandala(mandala, current_string);
        }

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        running = true;

        final IntentFilter hceNotificationsFilter = new IntentFilter();
        hceNotificationsFilter.addAction("your.hce.app.action.NOTIFY_HCE_DATA");
        registerReceiver(hceNotificationsReceiver, hceNotificationsFilter);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        unregisterReceiver(hceNotificationsReceiver);

        running = false;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

    private void initViews()
    {
        mandala = new Mandala(10, 5, true);
        current_string = 1;

        drawView = (DrawView) findViewById(R.id.weaveDrawViewNFC);
        drawView.setDrawStringNumber(true);
        drawView.drawWeavingMandala(mandala, current_string);

        tvStatus = findViewById(R.id.tvNFCStatus);
        tvModulus = findViewById(R.id.tvNFCModulus);
        tvTimes = findViewById(R.id.tvNFCTimes);
        tvCurrentString = findViewById(R.id.tvNFCCurrentString);
        tvDeltaPhoto = findViewById(R.id.tvDeltaPhotoLaser);
        tvDeltaTooth = findViewById(R.id.tvDeltaTooth);
        tvDuration = findViewById(R.id.tvNFCDuration);

        tvModulus.setText(mandala.getModulus() + "");
        tvTimes.setText(mandala.getTimes() + "");

        fab = findViewById(R.id.floatingNFCSend);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                promptFab();
            }
        });
    }

    private void promptFab()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Send to Mandala Maker:");

        builder.setItems(new CharSequence[] {"Mandala", "Photo/laser adjustment", "Current string"},
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        switch (which)
                        {
                            case 0:
                                promptMandala();
                                break;
                            case 1:
                                promptValue("steps photo/laser");
                                break;
                            case 2:
                                promptValue("current string");
                                break;
                        }
                    }
                });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void promptValue(final String value)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter value " + value);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        FrameLayout linlay = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50,25,60,10);
        input.setLayoutParams(params);
        linlay.addView(input);

        builder.setView(linlay);

        // Set up the buttons
        builder.setPositiveButton("Okay Go!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String number_text = input.getText().toString();
                if(!number_text.isEmpty())
                {
                    switch (value)
                    {
                        case "steps photo/laser":
                            deltaphoto = Integer.parseInt(number_text);
                            deltaphoto_transfer_request = true;
                            tvDeltaPhoto.setBackgroundColor(request_color);
                            tvDeltaPhoto.setText(deltaphoto + "");
                            break;
                        case "current string":
                            current_string = Integer.parseInt(number_text);
                            current_string_transfer_request = true;
                            drawView.drawWeavingMandala(mandala, current_string);
                            tvCurrentString.setBackgroundColor(request_color);
                            tvCurrentString.setText("" + current_string);
                            break;

                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void setNewMandala(Mandala new_mandala)
    {
        mandala = new_mandala;

        mandala_transfer_request = true;
        drawView.setBackgroundColor(request_color);

        tvTimes.setText("" + mandala.getTimes());
        tvTimes.setBackgroundColor(request_color);
        tvModulus.setText("" + mandala.getModulus());
        tvModulus.setBackgroundColor(request_color);

        //Also reset current string
        current_string = 1;
        current_string_transfer_request = true;
        tvCurrentString.setBackgroundColor(request_color);
        tvCurrentString.setText("" + current_string);

        drawView.drawWeavingMandala(mandala, current_string);
    }


    private void promptMandala()
    {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.mandala_layout);

        Button dialogButton = (Button) dialog.findViewById(R.id.buttonDialogSend);

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                EditText editMod = (EditText) dialog.findViewById(R.id.editTextDialogMod);
                EditText editTimes = (EditText) dialog.findViewById(R.id.editTextDialogTimes);
                EditText editOpt = (EditText) dialog.findViewById(R.id.editTextDialogOptimization);

                //setNewMandala(new Mandala(100,50,0));
                setNewMandala(new Mandala(Integer.parseInt(editMod.getText().toString()), Float.parseFloat(editTimes.getText().toString()), Float.parseFloat(editOpt.getText().toString())));
                dialog.dismiss();
            }
        });

        dialog.show();
    }


}
