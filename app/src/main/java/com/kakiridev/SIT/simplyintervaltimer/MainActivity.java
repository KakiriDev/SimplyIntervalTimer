package com.kakiridev.SIT.simplyintervaltimer;

import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    private AdView mAdView;
    private Handler mHandler;
    LinearLayout LL_Timer, LL_Menu;
    /** menu **/
    Button btn_Start;
    EditText ET_intervalMin, ET_intervalSec, ET_intervalCount, ET_breaklMin, ET_breakSec, ET_intervalSeries;
    /** timer **/
    Button btn_stop;
    int accStatusMode; //1 - work, 2 - break


    int intervalMin, intervalSec, intervalSeries, intervalCount, breaklMin, breakSec;

    int mWorkTime, accWorkTime, mWorkCount, accWorkCount, mRestTime, accRestTime, mSeriesCount, mAccSeries, totalTime;

    MediaPlayer mp_time_sound, mp_finish_5s, mp_finish_sound;

    Timer t;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, "ca-app-pub-8343407965657663~3060933681");
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        initButton();

        mp_time_sound = MediaPlayer.create(this, R.raw.beep);
        mp_finish_5s = MediaPlayer.create(this, R.raw.drum);
        mp_finish_sound = MediaPlayer.create(this, R.raw.fixbell);

        btn_Start = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);

        ET_intervalMin = findViewById(R.id.ET_intervalMin);
        ET_intervalSec = findViewById(R.id.ET_intervalSec);
        ET_intervalSeries = findViewById(R.id.ET_intervalSeries);
        ET_intervalCount = findViewById(R.id.ET_intervalCount);
        ET_breaklMin = findViewById(R.id.ET_breaklMin);
        ET_breakSec = findViewById(R.id.ET_breakSec);
        LL_Timer = findViewById(R.id.LL_Timer);
        LL_Menu = findViewById(R.id.LL_Menu);
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.beep);

        btn_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                intervalMin = checkEmptyField(ET_intervalMin.getText().toString());
                intervalSec = checkEmptyField(ET_intervalSec.getText().toString());
                intervalCount = checkEmptyField(ET_intervalCount.getText().toString());
                breaklMin = checkEmptyField(ET_breaklMin.getText().toString());
                breakSec = checkEmptyField(ET_breakSec.getText().toString());
                intervalSeries = checkEmptyField(ET_intervalSeries.getText().toString());

                int timeInterval = intervalMin * 60 + intervalSec;
                int timeBreak = breaklMin * 60 + breakSec;

                if (timeInterval > 0) {
                    startTimer(timeInterval, intervalCount, timeBreak, intervalSeries);

                    mp.start();
                    btn_Start.setVisibility(View.GONE);
                    LL_Menu.setVisibility(View.GONE);
                    LL_Timer.setVisibility(View.VISIBLE);
                    btn_stop.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getBaseContext(),"Work time must be > 0s",Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LL_Timer.setVisibility(View.GONE);
                btn_stop.setVisibility(View.GONE);
                btn_Start.setVisibility(View.VISIBLE);
                LL_Menu.setVisibility(View.VISIBLE);
                t.cancel();
            }
        });
    }

    public void initButton(){
        ImageButton IB_intervalTime_minus = (ImageButton) findViewById(R.id.IB_intervalTime_minus);
        IB_intervalTime_minus.setOnClickListener(this);

        ImageButton IB_intervalTime_plus = (ImageButton) findViewById(R.id.IB_intervalTime_plus);
        IB_intervalTime_plus.setOnClickListener(this);

        IB_intervalTime_plus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 300); // delay before start iteration
                        break;

                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override public void run() {
                    setEditTextValue(true, 1);
                    mHandler.postDelayed(this, 100); // delay iteration time
                }
            };
        });




        ImageButton IB_intervalCount_minus = (ImageButton) findViewById(R.id.IB_intervalCount_minus);
        IB_intervalCount_minus.setOnClickListener(this);
        ImageButton IB_intervalCount_plus = (ImageButton) findViewById(R.id.IB_intervalCount_plus);
        IB_intervalCount_plus.setOnClickListener(this);
        ImageButton IB_intervalRest_minus = (ImageButton) findViewById(R.id.IB_intervalRest_minus);
        IB_intervalRest_minus.setOnClickListener(this);
        ImageButton IB_intervalRest_plus = (ImageButton) findViewById(R.id.IB_intervalRest_plus);
        IB_intervalRest_plus.setOnClickListener(this);
        ImageButton IB_Series_minus = (ImageButton) findViewById(R.id.IB_Series_minus);
        IB_Series_minus.setOnClickListener(this);
        ImageButton IB_Series_plus = (ImageButton) findViewById(R.id.IB_Series_plus);
        IB_Series_plus.setOnClickListener(this);

    }

    public void setEditTextValue(boolean addValue, int etVersion){

        if (etVersion == 1){ //interval time
            int min = Integer.parseInt(ET_intervalMin.getText().toString());
            int sec = Integer.parseInt(ET_intervalSec.getText().toString());
Log.d("aaaa", String.valueOf(sec));
            if (addValue) {

                if (sec == 60) {
                    ET_intervalSec.setText("00");
                    min++;
                    ET_intervalMin.setText(String.valueOf(min));
                    //ET_intervalSec.setText(00);
                    //ET_intervalMin.setText(min++);
                } else {
                    sec++;
                    ET_intervalSec.setText(String.valueOf(sec));
                }
            } else {
                if (sec == 0) {
                    if(min > 0) {
                        ET_intervalSec.setText("60");
                        min--;
                        ET_intervalMin.setText(min);
                    }
                } else {
                    ET_intervalSec.setText("00");
                }
            }

        } else if (etVersion == 2){ // interval count
            int count = Integer.parseInt(ET_intervalCount.getText().toString());

            if (addValue) {
                ET_intervalCount.setText(count++);
            } else {
                if(count > 0) {
                    ET_intervalCount.setText(count--);
                }
            }
        } else if (etVersion == 3) { // rest time

            int min = Integer.parseInt(ET_breaklMin.getText().toString());
            int sec = Integer.parseInt(ET_breakSec.getText().toString());

            if (addValue) {
                if (sec == 60) {
                    ET_breakSec.setText(00);
                    ET_breaklMin.setText(min++);
                } else {
                    ET_breakSec.setText(sec++);
                }
            } else {
                if (sec == 0) {
                    if(min > 0) {
                        ET_breakSec.setText("60");
                        ET_breaklMin.setText(min--);
                    }
                } else {
                    ET_breakSec.setText(00);
                }
            }
        } else if (etVersion == 4) { // series
            int series = Integer.parseInt(ET_intervalSeries.getText().toString());

            if (addValue) {
                ET_intervalSeries.setText(series++);
            } else {
                if(series > 0) {
                    ET_intervalSeries.setText(series--);
                }
            }
        }

    }





    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.IB_intervalTime_minus:
                setEditTextValue(false, 1);
                break;

            case R.id.IB_intervalTime_plus:
                setEditTextValue(true, 1);
                break;

            case R.id.IB_intervalCount_minus:
                setEditTextValue(false, 2);
                break;

            case R.id.IB_intervalCount_plus:
                setEditTextValue(true, 2);
                break;

            case R.id.IB_intervalRest_minus:
                setEditTextValue(false, 3);
                break;

            case R.id.IB_intervalRest_plus:
                setEditTextValue(true, 3);
                break;

            case R.id.IB_Series_minus:
                setEditTextValue(false, 4);
                break;

            case R.id.IB_Series_plus:
                setEditTextValue(true, 4);
                break;

            default:
                break;
        }
    }

    private int checkEmptyField(String str){

        if (TextUtils.isEmpty(str)){
            return 0;
        } else {
            return Integer.parseInt(str);
        }
    }



    private void startTimer(final int workTime, final int workCount, final int restTime, final int seriesCount){
        mWorkTime = workTime;
        accWorkTime = mWorkTime;

        mWorkCount = workCount;
        accWorkCount = 1;

        mRestTime = restTime;
        accRestTime = -1;

        mSeriesCount = seriesCount;
        mAccSeries = 1;

        accStatusMode = 1; //work mode

        totalTime = (workTime * workCount + restTime) * seriesCount - restTime;


        if(mSeriesCount > 0){
            showTotalSeries();
            showTimeTotal();
        } else {
            hideTotalSeries();
            hideTimeTotal();
        }
        startWork();

        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        setTimeTotal(totalTime);

                        setTotalSeries(mAccSeries, mSeriesCount);
                        setTotalRepeats(accWorkCount, mWorkCount);

                        setWorkTotalTime(mWorkTime);
                        setWorkTime(mWorkTime);

                        setRestTimeTotal(mRestTime);
                        setRestTime(mRestTime);


/**
 * zmien czas cwiczenia 1
 * zmien cykl cwiczenia 2
 * zmien czas przerwy 3
 * zmien serie 4
 */

                        if(accWorkTime > 0) {

//pierwszy cykl work
                            if(accWorkTime == mWorkTime){
                                //ustawia nowy cykl powtorzenia
                                setTotalRepeats(accWorkCount, mWorkCount);
                            }

//normalny tryb work
                            setWorkTime(accWorkTime);
                            accWorkTime--;

                            if(accWorkTime < 5){
                                playSound(2);
                            }

                        } else if (accWorkTime == 0){
//ostatni cykl work
                            setWorkTime(accWorkTime);
                            if(accRestTime < 0){
                                playSound(1);
                            }


                            if(accWorkCount < mWorkCount){
//zwiększenie powtorzenia / wyzerowanie czasu cwiczenia
                                accWorkCount++;
//                                setTotalRepeats(accWorkCount, mWorkCount);
//                                setWorkTime(accWorkTime);
                                accWorkTime = mWorkTime;
                            } else if (accWorkCount == mWorkCount){
                                //dodaj przerwe
//                                setWorkTime(accWorkTime);

                                if(mAccSeries < mSeriesCount){
                                    if (accRestTime < 0 ){
                                        accRestTime = mRestTime;
                                        //playSound(1);
                                    } else if(accRestTime > 0) {
//start rest series
                                        startRest();
                                        setRestTime(accRestTime);
                                        accRestTime--;
                                        if(accRestTime < 5){
                                            playSound(2);
                                        }
                                    } else if(accRestTime == 0) {
//ostatnia sekunda przerwy
                                        //accRestTime = mRestTime;
                                        //setRestTime(accRestTime);
                                        mAccSeries++;
                                        setTotalSeries(mAccSeries, mSeriesCount);
                                        accWorkTime = mWorkTime;
                                        accWorkCount = 1;
                                        setTotalRepeats(accWorkCount, mWorkCount);
                                        accRestTime--;
                                        startWork();
                                        playSound(3);
                                    }
                                } else if (mSeriesCount == mAccSeries){
                                    //ostatnia seria, nie potrzebuje odpoczynku
                                    t.cancel();
                                }
                            }
                        }
                        totalTime--;
                    }
                });
            }
        }, 0, 1000);
    }


    private void startWork(){
        TextView TV_work_time_total = (TextView) findViewById(R.id.TV_work_time_total);
        TextView TV_rest_time_total = (TextView) findViewById(R.id.TV_rest_time_total);
        TV_work_time_total.setTextSize(25);
        TV_rest_time_total.setTextSize(15);
    }

    private void startRest(){
        TextView TV_work_time_total = (TextView) findViewById(R.id.TV_work_time_total);
        TextView TV_rest_time_total = (TextView) findViewById(R.id.TV_rest_time_total);
        TV_work_time_total.setTextSize(15);
        TV_rest_time_total.setTextSize(25);
    }


    private void setTotalSeries(int acc_series ,int total_series){
        TextView TV_series = (TextView) findViewById(R.id.TV_series);
        TV_series.setText("SERIES " + acc_series + " / " + total_series);
    }

    private void hideTotalSeries(){
        TextView TV_series = (TextView) findViewById(R.id.TV_series);
        TV_series.setVisibility(View.GONE);
    }

    private void showTotalSeries(){
        TextView TV_series = (TextView) findViewById(R.id.TV_series);
        TV_series.setVisibility(View.VISIBLE);
    }

    private void setTotalRepeats(int acc_repeats ,int total_repeats){
        TextView TV_series = (TextView) findViewById(R.id.TV_repeats_count);
        TV_series.setText("REPEATS " + acc_repeats + " / " + total_repeats);
    }

    private void setTimeTotal(int TimeTotal){
        TextView TV_time_total = (TextView) findViewById(R.id.TV_time_total);
        TV_time_total.setText("Total Time: " + getMin(TimeTotal) + ":" + getSec(TimeTotal));
    }

    private void hideTimeTotal(){
        TextView TV_time_total = (TextView) findViewById(R.id.TV_time_total);
        TV_time_total.setVisibility(View.GONE);
    }

    private void showTimeTotal(){
        TextView TV_time_total = (TextView) findViewById(R.id.TV_time_total);
        TV_time_total.setVisibility(View.VISIBLE);
    }

    private void setWorkTotalTime(int WorkTotalTime){
        TextView TV_work_time_total = (TextView) findViewById(R.id.TV_work_time_total);
        TV_work_time_total.setText("WORK TIME: " + getMin(WorkTotalTime) + ":" + getSec(WorkTotalTime));

    }

    private void setWorkTime(int WorkTime){
        TextView TV_work_time = (TextView) findViewById(R.id.TV_work_time);

        TV_work_time.setText(getMin(WorkTime) + ":" + getSec(WorkTime));
        TV_work_time.setTypeface(Typeface.createFromAsset(getAssets(), "font.otf"));
    }

    private void setRestTimeTotal(int RestTimeTotal){
        TextView TV_rest_time_total = (TextView) findViewById(R.id.TV_rest_time_total);
        TV_rest_time_total.setText("REST TIME: " + getMin(RestTimeTotal) + ":" + getSec(RestTimeTotal));
    }

    private void setRestTime(int RestTime){
        TextView TV_rest_time = (TextView) findViewById(R.id.TV_work_time);
        TV_rest_time.setText(getMin(RestTime) + ":" + getSec(RestTime));
    }


    private String getMin(int time){
        int int_min = time / 60;
        String string_min;

        if(int_min<10){
            string_min = "0" + String.valueOf(int_min);
        } else{
            string_min = String.valueOf(int_min);
        }

        return string_min;
    }

    private String getSec(int time){
        int int_sec = time % 60;
        String string_sec;

        if(int_sec<10){
            string_sec = "0" + String.valueOf(int_sec);
        } else{
            string_sec = String.valueOf(int_sec);
        }

        return string_sec;
    }




    private void playSound(int type){

        switch (type) {
            case 1:
                mp_time_sound.start();
                break;
            case 2:
                mp_finish_5s.start();
                break;
            case 3:
                mp_finish_sound.start();
                break;
        }

    }



    private void setTvText(TextView ET, int min, int sec){
        if(min <10){
            if(sec<10){
                ET.setText("0" + min + ":0"+ sec);
            } else{
                ET.setText("0" + min + ":"+ sec);
            }
        }else{
            if(sec<10){
                ET.setText("" + min + ":0"+ sec);
            } else{
                ET.setText("" + min + ":"+ sec);
            }
        }

    }
}
