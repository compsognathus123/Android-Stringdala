package htwg.compsognathus.stringdala;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class WeavingActivity extends AppCompatActivity {

    private Mandala mandala;
    private double diameter;
    private  boolean optimized;

    private int currentString;

    private TextView textString1;
    private TextView textString2;
    private TextView textStringNumber;
    private Button buttonNext;
    private Button buttonPrev;
    private DrawView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_weave);

        Intent intent = getIntent();

        int modulus = intent.getIntExtra("modulus", 100);
        float times = intent.getFloatExtra("times", 33);
        this.diameter = intent.getDoubleExtra("diameter", 1);
        this.optimized = intent.getBooleanExtra("optimized", false);

        mandala = new Mandala(modulus, times, optimized);
        currentString = 1;

        initViews();
    }

    private void updateText()
    {
        textString1.setText("" + mandala.getStrings()[currentString].getIndexStart());
        textString2.setText("" + mandala.getStrings()[currentString].getIndexEnd());
        textStringNumber.setText("String number: " + currentString + "/" + (mandala.getStrings().length-1));
    }

    private void promptForStringNumber()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Jump to string number:");

        // Set up the input
        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        FrameLayout linlay = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50,25,60,10);
        input.setLayoutParams(params);
        linlay.addView(input);

        builder.setView(linlay);

        // Set up the buttons
        builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String number_text = input.getText().toString();
                if(!number_text.isEmpty())
                {
                    int number_tmp = Integer.parseInt(number_text);
                    if(number_tmp >= 0 && number_tmp < mandala.getStrings().length)
                    {
                       currentString = number_tmp;
                       drawView.drawWeavingMandala(mandala, currentString);
                       updateText();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "String number out of range.", Toast.LENGTH_SHORT).show();
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


    private void initViews()
    {
        drawView = (DrawView) findViewById(R.id.weaveDrawView);
        textString1 = (TextView) findViewById(R.id.textViewString1);
        textString2 = (TextView) findViewById(R.id.textViewString2);
        textStringNumber = (TextView) findViewById(R.id.textViewStringNumber);
        buttonNext = (Button) findViewById(R.id.buttonNextString);
        buttonPrev = (Button) findViewById(R.id.buttonPrevString);

        ((ImageView) findViewById(R.id.imageViewString)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptForStringNumber();
            }
        });

        updateText();

        textStringNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptForStringNumber();
            }
        });

        drawView.drawWeavingMandala(mandala, currentString);

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(currentString < mandala.getStrings().length - 1)
                {
                    currentString++;
                    if(!buttonPrev.isEnabled()) buttonPrev.setEnabled(true);
                    if(currentString == mandala.getStrings().length - 1) buttonNext.setEnabled(false);

                    drawView.drawWeavingMandala(mandala, currentString);
                    updateText();
                }
                else
                {
                    //Done yayy!!!
                }
            }
        });

        buttonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(currentString > 1)
                {
                    currentString--;
                    if(!buttonNext.isEnabled()) buttonNext.setEnabled(true);
                    if(currentString == 1) buttonPrev.setEnabled(false);

                    drawView.drawWeavingMandala(mandala, currentString);
                    updateText();
                }

            }
        });
    }

}
