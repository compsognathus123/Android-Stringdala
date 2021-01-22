package htwg.compsognathus.stringdala;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class DrawView extends View {

    private float circleX;
    private float circleY;
    private float radius;

    private Mandala mandala;

    private int weavingCurrentString;
    boolean weaving;

    boolean draw_string_number;

    public void drawMandala(Mandala mandala)
    {
        this.mandala = mandala;
        this.weaving = false;
        invalidate();
    }

    public void drawWeavingMandala(Mandala mandala, int currentString)
    {
        this.mandala = mandala;
        this.weaving = true;
        this.weavingCurrentString = currentString;
        invalidate();
    }


    private float getRadius()
    {
        return radius;
    }

    public DrawView(Context context) {
        super(context);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void setDrawStringNumber(boolean draw_nr)
    {
        this.draw_string_number = draw_nr;
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        if(radius == 0)
        {
            if(canvas.getWidth() < canvas.getHeight())
            {
                radius = canvas.getWidth()/2 - canvas.getWidth()/20;
            }
            else
            {
                radius = canvas.getHeight()/2 - canvas.getHeight()/20;
            }
            circleX = canvas.getClipBounds().left + canvas.getWidth()/2;
            circleY = canvas.getClipBounds().top + canvas.getHeight()/2;
        }


        if(mandala != null)
        {
            if(weaving)
            {
                //Draw background mandala
                Paint linePaint = new Paint();
                linePaint.setColor(Color.LTGRAY);
                linePaint.setStrokeWidth(1F);
                linePaint.setAntiAlias(true);
                drawStrings(mandala.getStrings(), mandala.getStrings().length, canvas, linePaint);

                //Draw already woven strings
                linePaint.setColor(Color.BLACK);
                linePaint.setStrokeWidth(2F);
                drawStrings(mandala.getStrings(), weavingCurrentString, canvas, linePaint);

                //Draw current string
                linePaint.setStrokeWidth(4F);
                linePaint.setColor(getResources().getColor(R.color.colorAccent));
                drawStrings(new MandalaString[]{null, mandala.getStrings()[weavingCurrentString]}, 2, canvas, linePaint);

                //Draw circle
                linePaint.setStyle(Paint.Style.STROKE);
                linePaint.setColor(Color.GRAY);
                canvas.drawCircle(circleX ,circleY, radius, linePaint);

            }
            else
            {
                //float thickness = 0.75F * (200F/mandala.getModulus()) + 1F;
                float thickness = 1.25F * (200F/mandala.getModulus()) + 0.5F;

                Paint linePaint = new Paint();
                linePaint.setAntiAlias(true);
                linePaint.setColor(Color.BLACK);
                linePaint.setStrokeWidth(1F);
                linePaint.setStrokeWidth(thickness);

                drawStrings(mandala.getStrings(), mandala.getStrings().length, canvas, linePaint);

                if(mandala.isFavorite())
                {
                    Bitmap heart = BitmapFactory.decodeResource(getResources(), R.drawable.hearticon);
                    canvas.drawBitmap(heart, null, new Rect(canvas.getClipBounds().left + 25, canvas.getClipBounds().top + 25, 100, 100), null);

                    Paint textPaint = new Paint();
                    textPaint.setColor(Color.BLACK);
                    textPaint.setStrokeWidth(1F);
                    textPaint.setTextSize(36);

                    canvas.drawText(mandala.getModulus() + "|" + Math.round(mandala.getTimes() * 1000F) / 1000F, canvas.getClipBounds().left + 25, canvas.getClipBounds().bottom - 50, textPaint);
                }

                //Draw circle
                linePaint.setStyle(Paint.Style.STROKE);
               // linePaint.setStrokeWidth(radius * 2 * (3F/50F));
                linePaint.setStrokeWidth(thickness * 1.5F);
                canvas.drawCircle(circleX ,circleY, radius, linePaint);
            }

            //For NFC Activity
            if(draw_string_number)
            {
                Paint textPaint = new Paint();
                textPaint.setColor(Color.BLACK);
                textPaint.setStrokeWidth(1F);
                textPaint.setTextSize(36);

                canvas.drawText("[n]" + mandala.getStrings().length + " [l]" + Math.round(mandala.getStringLength() * 10F) / 10F + " [o]" + Math.round(mandala.getOptimization() * 1000F) / 1000F , canvas.getClipBounds().left + 25, canvas.getClipBounds().bottom - 50, textPaint);

            }
        }


    }


    private void drawStrings(MandalaString[] strings, int numberOfStrings, Canvas canvas, Paint paint)
    {
        for(int i = 1; i < numberOfStrings; i++)
        {
            /*if(!weaving)
            {
                //float hue = i/(float)numberOfStrings * 255F;
                float hue = 255 -(-145 + (float)strings[i].getLength()/1F * 400);

                paint.setColor(Color.HSVToColor(new float[]{hue, 255, 255}));
            }*/

            canvas.drawLine((float)strings[i].getxStart() * radius + circleX,
                    (float)strings[i].getyStart() * radius + circleY,
                    (float)strings[i].getxEnd() * radius + circleX,
                    (float)strings[i].getyEnd() * radius + circleY, paint);
        }
    }

    private void drawCircle(Canvas canvas)
    {
        Paint blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
        //blackPaint.setStrokeWidth(20F/0.4F);
        blackPaint.setStrokeWidth(3F);
        blackPaint.setStyle(Paint.Style.STROKE);

        canvas.drawCircle(circleX ,circleY, radius, blackPaint);
    }

}