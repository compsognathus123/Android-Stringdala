package htwg.compsognathus.stringdala;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.LinkedList;

public class Mandala {

    private int modulus;
    private float times;
    private double string_length;
    private boolean isFavorite;
    private boolean isOptimized;
    private int numberOfStrings;

    private float OPTIMIZATION;

    private MandalaString[] strings;


    public Mandala(int modulus, float times, boolean optimized)
    {
        this(modulus, times, optimized ? 9999:0);

    }

    public Mandala(int modulus, float times, float optimization)
    {

        this.modulus = modulus;
        this.times = times;
        this.numberOfStrings = modulus;

        if(optimization == 9999)
        {
            this.OPTIMIZATION = 0.28F;
            this.isOptimized = true;
        }
        else if(optimization == 0)
        {
            this.isOptimized = false;
        }
        else
        {
            this.OPTIMIZATION = optimization;
            this.isOptimized = true;
        }

        //Create strings
        strings = new MandalaString[modulus];

        for(int i = 1; i < modulus; i++)
        {
            strings[i] = new MandalaString(i, modulus, times);
        }

        if(times % modulus != 1)
        {
            //Remove very short Strings
            if(isOptimized)
            {
                optimize();
            }

            //Sort strings
            sortStringsForWeaving();

            //Calculate length of whole string needed, diameter 1
            this.string_length = calculateStringLength();
        }
    }


    public Bitmap getBitmap(int width, int height)
    {
        int radius = (int)(width * 0.45);
        float thickness = 2.5F * (250F/modulus) + 1F;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        int circleX = canvas.getClipBounds().left + width/2;
        int circleY = canvas.getClipBounds().top + height/2;

        canvas.drawColor(Color.WHITE);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(thickness);
        linePaint.setAntiAlias(true);

        for(int i = 1; i < numberOfStrings; i++)
        {
            canvas.drawLine((float)strings[i].getxStart() * radius + circleX,
                    (float)strings[i].getyStart() * radius + circleY,
                    (float)strings[i].getxEnd() * radius + circleX,
                    (float)strings[i].getyEnd() * radius + circleY, linePaint);
        }

        linePaint.setStrokeWidth(thickness*1.5F);
        linePaint.setStyle(Paint.Style.STROKE);

        canvas.drawCircle(canvas.getClipBounds().left + circleX ,circleY, radius, linePaint);

        return bitmap;
    }

    public boolean isOptimized() {
        return isOptimized;
    }

    public void setFavorite(boolean isFavorite)
    {
        this.isFavorite = isFavorite;
    }

    public boolean isFavorite()
    {
        return isFavorite;
    }

    public double getStringLength() {
        return string_length;
    }

    public int getModulus()
    {
        return modulus;
    }

    public float getTimes()
    {
        return times;
    }

    public float getOptimization()
    {
        return OPTIMIZATION;
    }

    public MandalaString[] getStrings()
    {
        return strings;
    }

    private void optimize()
    {
        for(int i = 1; i < strings.length; i++)
        {
            if(strings[i].getLength() < OPTIMIZATION)
            {
                strings[i] = null;
                numberOfStrings--;
            }
        }

        for(int i = 1; i <  strings.length; i++)
        {
                for(int j = 1; j < strings.length; j++)
                {
                    if(j != i && strings[j] != null && strings[i] != null && strings[i].getIndexEnd() == strings[j].getIndexStart() && strings[i].getIndexStart() == strings[j].getIndexEnd())
                    {
                        strings[i] = null;
                        numberOfStrings--;
                    }
                }
        }

    }

    private double calculateStringLength()
    {
        double length = 0;

        for(int i = 1; i < strings.length; i++)
        {
            length += strings[i].getLength();

            if(i < strings.length - 1)
            {
                int index1 = strings[i].getIndexEnd();

                int index2 = strings[i + 1].getIndexStart();

                double dist_straight = Math.sqrt(Math.pow(index1 - index2, 2));
                double dist_backwards;

                if(index1 < index2)
                {
                    dist_backwards = index1 + (modulus - index2);
                }
                else
                {
                    dist_backwards = index2 + (modulus - index1);
                }

                if(dist_backwards <= dist_straight)
                {
                    length += dist_backwards/modulus * Math.PI;
                }
                else
                {
                    length += dist_straight/modulus * Math.PI ;
                }
            }
        }

        return length;
    }

    private void sortStringsForWeaving()
    {
        LinkedList<MandalaString> strings_list = new LinkedList<MandalaString>();

        int firstString = -1;
        do
        {
            firstString++;
            if(strings[firstString] != null)
            {
                strings[firstString].setSorted(true);
                strings_list.add(strings[firstString]);
            }
            else
            {
            }
        }while(strings[firstString] == null);

        while(strings_list.size() < numberOfStrings - 1)
        {
            int endex = strings_list.getLast().getIndexEnd();

            boolean foundNext = false;
            int step = 1;

            while(!foundNext)
            {
                int check_index = endex + step;

                if(check_index < 0)
                {
                    check_index = strings.length + (endex + step);
                }
                else if(check_index >= strings.length)
                {
                    check_index = (endex + step) - (strings.length);
                }

                if(check_index == 0)
                {
                    if(step < 0)
                    {
                        check_index = strings.length-1;
                    }
                    else
                    {
                        check_index = 1;
                    }
                }

                if(strings[check_index] != null && !strings[check_index].isSorted())
                {
                    strings_list.add(strings[check_index]);
                    strings[check_index].setSorted(true);
                    foundNext = true;
                }
                else
                {
                    if(step > 0)
                    {
                        step = -step;
                    }
                    else
                    {
                        step = -step + 1;
                    }
                }
            }
        }

        strings = new MandalaString[strings_list.size() + 1];

        for(int i = 1; i < strings.length; i++)
        {
            strings[i] = strings_list.removeFirst();
        }
    }
}
