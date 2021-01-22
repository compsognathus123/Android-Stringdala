package htwg.compsognathus.stringdala;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    EditText editModulus;
    EditText editTimes;
    TextView textViewStringLength;
    TextView textViewStringNumber;
    SeekBar seekSteps;
    TextView textViewSteps;

    ArrayList<Mandala> favorites;

    double diameter;
    int modulus;
    float times;
    boolean optimized;

    float stepsize;

    boolean timesAnimation;
    boolean timesDirection;
    Timer timesAnimationTimer;

    Mandala mandala;

    MandalaString[] strings;

    private MyViewPager mPager;
    private PagerAdapter pagerAdapter;

    private boolean updateBoth;

    /*
        TODO
                #@Weaving possibilty to choose current String




     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("String Art Mandala Maker");

        favorites = new ArrayList<Mandala>();
        /*favorites.add(new Mandala(272, 50));
        favorites.add(new Mandala(272, 55));
        favorites.add(new Mandala(272, 60));*/
        loadSharedPreferences();

        mandala = new Mandala(modulus, times, optimized);


        pagerAdapter = new MandalaPageAdapter(getSupportFragmentManager(), this);

        //updateStrings();

        setContentView(R.layout.activity_main);


        mPager = (MyViewPager) findViewById(R.id.pager);
        mPager.setAdapter(pagerAdapter);
        mPager.setCurrentItem(pagerAdapter.getCount()-1);

        addViewPageListener();
        initViews();


        /*Intent myIntent = new Intent(this, NCFActivity.class);
        startActivity(myIntent);*/
        //randomMainMandala();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        saveSharedPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {

        }

        MyNFCService service = getSystemService(MyNFCService.class);
        service.*/


    }

    private void loadSharedPreferences()
    {
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        String gsonList = sharedPref.getString("fav", null);

        if(gsonList != null) {
            Gson gson = new Gson();

            Type type = new TypeToken<ArrayList<Mandala>>() {
            }.getType();
            favorites = gson.fromJson(gsonList, type);
        }
        optimized = sharedPref.getBoolean("optimized", true);
        diameter = sharedPref.getFloat("diameter", 1);

        //sharedPref.edit().remove("times").commit() ;
        modulus = sharedPref.getInt("modulus", 320);
        times = sharedPref.getFloat("times", 111);

        if(sharedPref.getBoolean("firsttime", true))
        {
            firstTimeDialog();
        }
    }

    public void saveSharedPreferences()
    {
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);

        Gson gson = new Gson();
        String jsonConfig = gson.toJson(favorites);
        if(!jsonConfig.isEmpty())
        {
            sharedPref.edit().putString("fav", jsonConfig).commit();
        }

        sharedPref.edit().putBoolean("optimized", optimized).commit();
        sharedPref.edit().putFloat("diameter", (float)diameter).commit();
        sharedPref.edit().putInt("stepprogress", seekSteps.getProgress()).commit();

        sharedPref.edit().putInt("modulus", modulus).commit();
        sharedPref.edit().putFloat("times", times).commit();

    }


    private void randomMainMandala()
    {
        Random r = new Random();
        modulus =  r.nextInt(500) + 200;
        times = r.nextInt(690) + 2;

        updateBoth = true;

        editModulus.setText("" + modulus);
        editTimes.setText("" + times);
    }

    private void addViewPageListener()
    {
        mPager.setOnSwipeOutListener(new MyViewPager.OnSwipeOutListener() {
            @Override
            public void onSwipeOutAtStart() {}

            @Override
            public void onSwipeOutAtEnd()
            {
                randomMainMandala();
            }
        });

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {}

            @Override
            public void onPageSelected(int i)
            {
                showNumberOfStrings();
                showStringLength();
            }

            @Override
            public void onPageScrollStateChanged(int i) {}
        });

    }

    public void showStringLength()
    {
        int length = (int)(((MandalaPageAdapter)pagerAdapter).getMandalaFragment(mPager.getCurrentItem()).getMandala().getStringLength() * diameter);
        textViewStringLength.setText("String length: " + length);
    }

    public void showNumberOfStrings()
    {
        if(((MandalaPageAdapter)pagerAdapter).getMandalaFragment(mPager.getCurrentItem()).getMandala().isOptimized())
        {
            textViewStringNumber.setText("Strings(optimized): " + (((MandalaPageAdapter)pagerAdapter).getMandalaFragment(mPager.getCurrentItem()).getMandala().getStrings().length-1));
        }
        else
        {
            textViewStringNumber.setText("Strings: " + (((MandalaPageAdapter)pagerAdapter).getMandalaFragment(mPager.getCurrentItem()).getMandala().getStrings().length-1));
        }
    }

    public Mandala getMandala()
    {
        return mandala;
    }

    public void mainMandalaChanged()
    {
        MandalaFragment currentFragment = ((MandalaPageAdapter)pagerAdapter).getMandalaFragment(mPager.getCurrentItem());

        mandala = new Mandala(modulus, times, optimized);

       /* for(Mandala favs:favorites)
        {
            if(mandala.getTimes() == favs.getTimes() && mandala.getModulus() == favs.getModulus())
            {
                mandala.setFavorite(true);
            }
        }*/

        if(((MandalaPageAdapter)pagerAdapter).getMainMandalaFragment().isFavoriteFragment())
        {
            ((MandalaPageAdapter)pagerAdapter).addMandalaFragment(mandala);
        }
        else
        {
            ((MandalaPageAdapter)pagerAdapter).getMainMandalaFragment().setMandala(mandala);
        }

        mPager.setCurrentItem(pagerAdapter.getCount()-1);
        pagerAdapter.notifyDataSetChanged();

        showStringLength();
        showNumberOfStrings();
    }

    public ArrayList<Mandala> getFavorites()
    {
        return favorites;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        if(optimized)
        {
            menu.getItem(3).setTitle("Disable optimization");
        }
        else
        {
            menu.getItem(3).setTitle("Enable optimization");
        }
        return super.onPrepareOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.shareMandala:
                shareMandala();
                return true;
            case R.id.changeDiameter:
                promptForDiameter();
                return true;
            case R.id.weaveMandala:
                Mandala current_mandala = ((MandalaPageAdapter)pagerAdapter).getMandalaFragment(mPager.getCurrentItem()).getMandala();
                Intent myIntent = new Intent(this, WeavingActivity.class);
                myIntent.putExtra("modulus", current_mandala.getModulus());
                myIntent.putExtra("times", current_mandala.getTimes());
                myIntent.putExtra("diameter", diameter);
                myIntent.putExtra("optimized", current_mandala.isOptimized());
                startActivity(myIntent);
                return true;
            case R.id.changeOptimization:
                optimized = !optimized;
                mainMandalaChanged();
                if(optimized)
                {
                    item.setTitle("Disable optimization");
                    Toast.makeText(getApplicationContext(), "Optimization enabled.", Toast.LENGTH_SHORT).show();

                }
                else
                {
                    item.setTitle("Enable optimization");
                    Toast.makeText(getApplicationContext(), "Optimization disabled.", Toast.LENGTH_SHORT).show();
                }
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void shareMandala()
    {
        Mandala shareMandala = ((MandalaPageAdapter)pagerAdapter).getMandalaFragment(mPager.getCurrentItem()).getMandala();
        File mandaladir = new File(getFilesDir().getAbsolutePath() + "/mandalas");
        mandaladir.mkdir();

        File sharefile = new File(mandaladir.getAbsolutePath(), "mandala" + shareMandala.getModulus() + "-" + shareMandala.getTimes() + ".png");

        try {
            FileOutputStream out = new FileOutputStream(sharefile);
            shareMandala.getBitmap(2000,2000).compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            Log.e("ERROR", String.valueOf(e.getMessage()));

        }

        // Now send it out to share
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/png");

        Uri uri = FileProvider.getUriForFile(this, "htwg.compsognathus.fileprovidernew", sharefile);

        share.putExtra(Intent.EXTRA_STREAM, uri);

        startActivity(Intent.createChooser(share,  "Share Image"));
    }

    private void firstTimeDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Welcome, fellow human <3");

        final TextView tv = new TextView(this);
        tv.setText("-Change nodes or factor to create a new pattern.\n-Swipe left to create a random pattern.\n-Long click the mandala to add it to your favorites.\n-Swipe right to browse through ya favorites.\n-Click the needle icon to show weaving instructions.");

        FrameLayout linlay = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50,50,20,10);
        tv.setLayoutParams(params);
        linlay.addView(tv);


        builder.setView(linlay);
        // Set up the buttons
        builder.setPositiveButton("Let's go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
                sharedPref.edit().putBoolean("firsttime", false).commit();
            }
        });
        builder.show();
    }

    private void promptForDiameter()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set diameter for string length calculation:");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        FrameLayout linlay = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50,25,60,10);
        input.setLayoutParams(params);
        linlay.addView(input);

        builder.setView(linlay);

        // Set up the buttons
        builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String number_text = input.getText().toString();
                if(!number_text.isEmpty())
                {
                    diameter = Double.parseDouble(number_text);
                    Toast.makeText(getApplicationContext(), "New mandala diameter is " + diameter, Toast.LENGTH_SHORT).show();
                }
                showStringLength();
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
        textViewStringNumber = (TextView) findViewById(R.id.textViewStringNumberMain) ;
        textViewStringLength = (TextView)findViewById(R.id.textViewStringLength);
        textViewSteps = (TextView) findViewById(R.id.textViewSteps);
        showStringLength();
        showNumberOfStrings();

        seekSteps = (SeekBar) findViewById(R.id.seekBarStep);
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        seekSteps.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                switch(progress)
                {
                    case 0: stepsize = 0.001F;
                        break;
                    case 1: stepsize = 0.01F;
                        break;
                    case 2: stepsize = 0.1F;
                        break;
                    case 3: stepsize = 1F;
                        break;
                    case 4: stepsize = 10F;
                        break;
                }
                Log.d("stepsize", progress + " " + stepsize);

                textViewSteps.setText("Step size: " + stepsize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekSteps.setProgress(0);
        seekSteps.setProgress(1);
        seekSteps.setProgress(sharedPreferences.getInt("stepprogress", 3));

        editModulus = ((EditText) findViewById(R.id.editTextModulus));
        editModulus.setText("" + modulus);
        editModulus.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable s)
            {
                String number_text = editModulus.getText().toString();
                if(!number_text.isEmpty() )
                {
                    int temp_mod = Integer.parseInt(number_text);
                    if(temp_mod >= 5)
                    {
                        modulus = temp_mod;
                        mainMandalaChanged();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Number of Nodes has to be >= 5.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        editTimes = (EditText)findViewById(R.id.editTextTimes);
        editTimes.setText("" + Math.round(times * 1000F) / 1000F);
        editTimes.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable s)
            {
                String number_text = editTimes.getText().toString();
                if(!number_text.isEmpty())
                {
                    float temp_times = Float.parseFloat(number_text);
                    if(temp_times > 1)
                    {
                        //Prevent from creating mandala twice when both values changed at once.
                        if(!updateBoth)
                        {
                            times = temp_times;
                            mainMandalaChanged();
                        }else{
                            updateBoth = false;
                        }
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Factor has to be > 1.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        ((Button)findViewById(R.id.buttonModulusPlus)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modulus++;
                editModulus.setText("" + modulus);
            }
        });
        ((Button)findViewById(R.id.buttonModulusMinus)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(modulus > 5) modulus--;
                editModulus.setText("" + modulus);
            }
        });
        ((Button)findViewById(R.id.buttonTimesPlus)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                times = stepsize + times;
                editTimes.setText("" + Math.round(times * 1000F) / 1000F);
            }
        });
        ((Button)findViewById(R.id.buttonTimesMinus)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(times - stepsize > 2) times -= stepsize;
                editTimes.setText("" + Math.round(times * 1000F) / 1000F);

            }
        });

        //Handle Times animation button press/release
        ((Button)findViewById(R.id.buttonTimesPlus)).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                timesAnimation = true;
                timesDirection = true;
                setTimesAnimation(true);
                return false;
            }
        });
        ((Button)findViewById(R.id.buttonTimesPlus)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(timesAnimation && event.getAction() == MotionEvent.ACTION_UP)
                {
                    setTimesAnimation(false);
                    timesAnimation = false;
                }
                return false;
            }
        });
        ((Button)findViewById(R.id.buttonTimesMinus)).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                timesAnimation = true;
                timesDirection = false;
                setTimesAnimation(true);
                return false;
            }
        });
        ((Button)findViewById(R.id.buttonTimesMinus)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(timesAnimation && event.getAction() == MotionEvent.ACTION_UP)
                {
                    setTimesAnimation(false);
                    timesAnimation = false;
                }
                return false;
            }
        });

    }

    private void setTimesAnimation(boolean enable)
    {
        if(enable)
        {
            timesAnimationTimer = new Timer();

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (timesDirection)
                    {
                        times += stepsize;
                    }
                    else if(times - stepsize > 2)
                    {
                        times -= stepsize;
                    }

                    Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run()
                        {
                            editTimes.setText(Math.round(times * 1000F) / 1000F + "");
                        }
                    });
                }
            };

            switch(seekSteps.getProgress())
            {
                case 0:
                    timesAnimationTimer.scheduleAtFixedRate(task, 0, 30);
                    break;
                case 1:
                    timesAnimationTimer.scheduleAtFixedRate(task, 0, 50);
                    break;
                case 2:
                    timesAnimationTimer.scheduleAtFixedRate(task, 0, 100);
                    break;
                case 3:
                    timesAnimationTimer.scheduleAtFixedRate(task, 0, 333);
                    break;
                case 4:
                    timesAnimationTimer.scheduleAtFixedRate(task, 0, 333);
                    break;
            }
        }
        else
        {
            timesAnimationTimer.cancel();
        }
    }



}
