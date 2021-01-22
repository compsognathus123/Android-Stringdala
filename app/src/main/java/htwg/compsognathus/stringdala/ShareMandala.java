package htwg.compsognathus.stringdala;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class ShareMandala {


    Mandala mandala;
    private int radius;
    private int circleX;
    private int circleY;
    MainActivity main;

    public ShareMandala(Mandala mandala, MainActivity main)
    {
        this.mandala = mandala;
        this.main = main;

        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if(main.checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_GRANTED)
        {
            performSave();
        }
        else {

            main.requestPermissions(permissions,10);
        }
    }

    private void share()
    {
        File mandaladir = new File(main.getFilesDir().getAbsolutePath() + "/mandalas");
        mandaladir.mkdir();

        File sharefile = new File(mandaladir.getAbsolutePath(), "mandala" + mandala.getModulus() + "-" + mandala.getTimes() + ".png");

        try {
            FileOutputStream out = new FileOutputStream(sharefile);
            mandala.getBitmap(2000,2000).compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            Log.e("ERROR", String.valueOf(e.getMessage()));

        }

        // Now send it out to share
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/png");

        Uri uri = FileProvider.getUriForFile(main, "htwg.compsognathus.fileprovider", sharefile);

        share.putExtra(Intent.EXTRA_STREAM, uri);

        main.startActivity(Intent.createChooser(share,  "Share Image"));
    }

    public void performSave()
    {
        share();
        //saveBitmap("mandala" + mandala.getModulus() + "_" + mandala.getTimes());
    }

    public void saveBitmap(String name)
    {
        long time = Calendar.getInstance().getTime().getTime();
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + name + "2.png");
        //File file = new File(main.getApplicationInfo().dataDir + "/" + name + "1.png");
        Bitmap bitmap = mandala.getBitmap(2000,2000);



        try {
            boolean worked = bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
            Log.d("DIRECCTORY", name + " saved: " + file.getAbsolutePath());
            Log.d("Success", ":" + worked);
        } catch (Exception e) {
            Log.d("DIRECCTORY", "error");
            e.printStackTrace();
        }


    }

    private void drawStrings(MandalaString[] strings, int numberOfStrings, Canvas canvas, Paint paint)
    {
        for(int i = 1; i < numberOfStrings; i++)
        {
            //if(i > strings.length/2) linePaint.setColor(Color.GREEN);

            canvas.drawLine((float)strings[i].getxStart() * radius + circleX,
                    (float)strings[i].getyStart() * radius + circleY,
                    (float)strings[i].getxEnd() * radius + circleX,
                    (float)strings[i].getyEnd() * radius + circleY, paint);
        }
    }

    private void drawCircle(Canvas canvas, Paint paint)
    {
        canvas.drawCircle(circleX ,circleY, radius, paint);
    }
}
