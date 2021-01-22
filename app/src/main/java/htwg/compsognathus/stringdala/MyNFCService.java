package htwg.compsognathus.stringdala;

import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

public class MyNFCService extends HostApduService
{
    public static final byte NFC_HELLO = 0;
    public static final byte NFC_SEND_STATUS = 10;
    public static final byte NFC_RECIEVE_MANDALA = 11;
    public static final byte NFC_DELTA_PHOTO = 12;
    public static final byte NFC_SUCCESS = 13;
    public static final byte NFC_CURRENT_STRING = 14;


    byte[] response;
    byte mandala_data[];

    int delta_photo = -1;

    Intent broad = new Intent("your.hce.app.action.NOTIFY_HCE_DATA");

   /* @Override
    protected void onStart() {
        super.onStart();

        running = true;

        final IntentFilter hceNotificationsFilter = new IntentFilter();
        hceNotificationsFilter.addAction("your.hce.app.action.NOTIFY_HCE_DATA");
        registerReceiver(hceNotificationsReceiver, hceNotificationsFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(hceNotificationsReceiver);

        running = false;
    }


    final BroadcastReceiver hceNotificationsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.hasExtra("apdu"))
            {
                byte[] apdu = intent.getByteArrayExtra("apdu");

                switch (apdu[0])
                {
                    case NFC_SEND_STATUS:
                        updateStatus(apdu);

                        String stat = "Status: ";
                        for(int i = 0;  i < apdu.length; i++)
                        {
                            stat += apdu[i] + " ";

                        }
                        status.setText(stat);
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
    };*/

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras)
    {
        Log.d("Mandala NFC Service", "Process Command");


        if(!NCFActivity.running)
        {
            Intent dialogIntent = new Intent(this, NCFActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialogIntent);
            Log.d("Mandala", "start Activity!");
        }


        switch (apdu[0])
        {
            case NFC_HELLO:
                if(delta_photo < 0)
                {
                    if(NCFActivity.mandala_transfer_request)
                    {
                        createMandalaData();

                        int times = (int)(NCFActivity.getMandala().getTimes() * 1000F);
                        int modulo = NCFActivity.getMandala().getModulus();
                        int data_length = mandala_data.length;
                        Log.d("Send mandala", times + " " + modulo + " " + data_length);

                        response = new byte[]{NFC_RECIEVE_MANDALA, M.intMSB(data_length), M.intLSB(data_length), M.intMSB(modulo), M.intLSB(modulo), M.intToByte(times, 0), M.intToByte(times, 1), M.intToByte(times, 2), M.intToByte(times, 3)};

                    }
                    else if(NCFActivity.current_string_transfer_request)
                    {
                        response = new byte[]{NFC_CURRENT_STRING, M.intMSB(NCFActivity.current_string), M.intLSB(NCFActivity.current_string)};
                        Log.d("Send current string", "" + NCFActivity.current_string);
                    }
                    else if(NCFActivity.deltaphoto_transfer_request)
                    {
                        response = new byte[]{NFC_DELTA_PHOTO, (byte)NCFActivity.deltaphoto};
                        Log.d("Send steps photo/laser", "" + NCFActivity.deltaphoto);
                    }
                    else
                    {
                        response = new byte[]{NFC_SEND_STATUS};
                    }
                }
                else
                {
                    response = new byte[]{NFC_DELTA_PHOTO, (byte) delta_photo};
                }
                break;

            case NFC_SEND_STATUS:
                broad.putExtra("apdu", apdu);
                sendBroadcast(broad);
                break;

            case NFC_RECIEVE_MANDALA:
                Log.d("Mandala", "send string " + M.bbInt(apdu[1], apdu[2]));
                response = sendMandala(M.bbInt(apdu[1], apdu[2]));
                break;

            case NFC_DELTA_PHOTO:
                broad.putExtra("apdu", apdu);
                sendBroadcast(broad);
                break;

            case NFC_SUCCESS:
                broad.putExtra("apdu", apdu);
                Log.d("NFC Success", "" + apdu[1]);

                break;
        }

        return response;
    }

    private void createMandalaData()
    {
        int n_strings = NCFActivity.getMandala().getStrings().length;
        int data_length = n_strings * 4;

        mandala_data = new byte[data_length];

        for (int i = 4; i < data_length; i += 4)
        {
            mandala_data[i] = M.intMSB(NCFActivity.getMandala().getStrings()[i/4].getIndexStart());
            mandala_data[i+1] = M.intLSB(NCFActivity.getMandala().getStrings()[i/4].getIndexStart());

            mandala_data[i+2] = M.intMSB(NCFActivity.getMandala().getStrings()[i/4].getIndexEnd());
            mandala_data[i+3] = M.intLSB(NCFActivity.getMandala().getStrings()[i/4].getIndexEnd());
            Log.d("Mandala String", (int)(i/4) + ":\t" + mandala_data[i] + "\t" + mandala_data[i+2]);
        }

    }

    private byte[] sendMandala(int string_index)
    {
        byte[] mandalaapdu = new byte[7];

        mandalaapdu[0] = NFC_RECIEVE_MANDALA;
        mandalaapdu[1] = M.intMSB(string_index);
        mandalaapdu[2] = M.intLSB(string_index);

        String strData = "";
        for(int i = 3; i < 7; i++)
        {
            mandalaapdu[i] = mandala_data[string_index * 4 + (i - 3)];
            strData += mandalaapdu[i] + " ";
        }
        Log.d("send Mandala", strData);
        Log.d("new Paket", " ");

        return mandalaapdu;
        /*
         byte[] mandalaapdu = new byte[32];

        mandalaapdu[0] = NFC_RECIEVE_MANDALA;
        mandalaapdu[1] = M.intMSB(string_index);
        mandalaapdu[2] = M.intLSB(string_index);

        String strData = "";
        for(int i = 3; i < 31; i++)
        {
            mandalaapdu[i] = mandala_data[string_index + (i - 3)];
            strData += mandalaapdu[i] + " ";
        }
        Log.d("send Mandala", strData);
        Log.d("new Paket", " ");

        return mandalaapdu;
         */
    }

    @Override
    public void onDeactivated(int reason)
    {
        broad.putExtra("connected", false);
        sendBroadcast(broad);
        Log.d("Mandala NFC Service", "Deacitvated");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broad.putExtra("connected", true);
        sendBroadcast(broad);
        Log.d("Mandala NFC Service", "Created");

    }


}