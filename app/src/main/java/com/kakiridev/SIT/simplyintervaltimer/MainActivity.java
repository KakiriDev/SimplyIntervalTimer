package com.kakiridev.SIT.simplyintervaltimer;

import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
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

public class MainActivity extends AppCompatActivity implements View.OnTouchListener{

    private boolean Loc_addValue;
    private int Loc_etVersion;

    long clickDownTime;

    private AdView mAdView;
    private Handler mHandler;


    LinearLayout LL_Timer;
    LinearLayout LL_Menu;

    /** menu **/
    EditText ET_intervalMin;
    EditText ET_intervalSec;
    EditText ET_intervalCount;
    EditText ET_breaklMin;
    EditText ET_breakSec;
    EditText ET_intervalSeries;

    /** timer **/
    Button btn_start;
    Button btn_pause;
    Button btn_reset;

    int accStatusMode; //1 - work, 2 - break

    ImageButton IB_intervalTime_minus;
    ImageButton IB_intervalTime_plus;
    ImageButton IB_intervalCount_minus;
    ImageButton IB_intervalCount_plus;
    ImageButton IB_intervalRest_minus;
    ImageButton IB_intervalRest_plus;
    ImageButton IB_Series_minus;
    ImageButton IB_Series_plus;

    int intervalMin;
    int intervalSec;
    int intervalSeries;
    int intervalCount;
    int breaklMin;
    int breakSec;

    int mWorkTime;
    int accWorkTime;
    int mWorkCount;
    int accWorkCount;
    int mRestTime;
    int accRestTime;
    int mSeriesCount;
    int mAccSeries;
    int totalTime;

    MediaPlayer mp_time_sound;
    MediaPlayer mp_finish_5s;
    MediaPlayer mp_finish_sound;

    Timer t;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addAddMob();
        initButton();
        initMP();
        initET_LL();


        btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimer();
            }
        });

        btn_pause = findViewById(R.id.btn_pause);
        btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTimer();
            }
        });

        btn_reset = findViewById(R.id.btn_reset);
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTimer();
            }
        });


        //final MediaPlayer mp = MediaPlayer.create(this, R.raw.beep);

        btn_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                intervalMin = checkEmptyField(ET_intervalMin.getText().toString());
                intervalSec = checkEmptyField(ET_intervalSec.getText().toString());
                intervalCount = checkEmptyField(ET_intervalCount.getText().toString());
                breaklMin = checkEmptyField(ET_breaklMin.getText().toString());
                breakSec = checkEmptyField(ET_breakSec.getText().toString());
                intervalSeries = checkEmptyField(ET_intervalSeries.getText().toString());

                int timeInterval = intervalMin * 60 + intervalSec; //whole time of one cycle in [s]
                int timeBreak = breaklMin * 60 + breakSec; //whole time of one break in [s]

                /** play start sound
                 *  show timer menu + stop button and hide config menu and start button
                 *  start Timer
                 */
                if (timeInterval > 0) {
                    //mp.start();
                    mp_time_sound.start();
                    btn_Start.setVisibility(View.GONE);
                    LL_Menu.setVisibility(View.GONE);
                    LL_Timer.setVisibility(View.VISIBLE);
                    btn_stop.setVisibility(View.VISIBLE);
                    startTimer(timeInterval, intervalCount, timeBreak, intervalSeries);
                } else {
                    Toast.makeText(getBaseContext(),"Work time must be > 0s",Toast.LENGTH_LONG).show();
                }
            }
        });

        /** repeat to main menu and stop Timer **/
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

    /** add AdMob Ads **/
    private void addAddMob(){
        MobileAds.initialize(this, "ca-app-pub-8343407965657663~3060933681");
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    /** Initialize Media Player **/
    private void initMP(){
        mp_time_sound = MediaPlayer.create(this, R.raw.beep);
        mp_finish_5s = MediaPlayer.create(this, R.raw.drum);
        mp_finish_sound = MediaPlayer.create(this, R.raw.fixbell);
    }

    /** Initialize Edit Text and Linear Layout **/
    private void initET_LL(){
        ET_intervalMin = findViewById(R.id.ET_intervalMin);
        ET_intervalSec = findViewById(R.id.ET_intervalSec);
        ET_intervalSeries = findViewById(R.id.ET_intervalSeries);
        ET_intervalCount = findViewById(R.id.ET_intervalCount);
        ET_breaklMin = findViewById(R.id.ET_breaklMin);
        ET_breakSec = findViewById(R.id.ET_breakSec);
        LL_Timer = findViewById(R.id.LL_Timer);
        LL_Menu = findViewById(R.id.LL_Menu);
    }

    /** Initialize +/- buttons and add listeners **/
    public void initButton(){
        btn_Start = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);

        IB_intervalTime_minus = (ImageButton) findViewById(R.id.IB_intervalTime_minus);
        IB_intervalTime_minus.setOnTouchListener(this);

        IB_intervalTime_plus = (ImageButton) findViewById(R.id.IB_intervalTime_plus);
        IB_intervalTime_plus.setOnTouchListener(this);

        IB_intervalCount_minus = (ImageButton) findViewById(R.id.IB_intervalCount_minus);
        IB_intervalCount_minus.setOnTouchListener(this);

        IB_intervalCount_plus = (ImageButton) findViewById(R.id.IB_intervalCount_plus);
        IB_intervalCount_plus.setOnTouchListener(this);

        IB_intervalRest_minus = (ImageButton) findViewById(R.id.IB_intervalRest_minus);
        IB_intervalRest_minus.setOnTouchListener(this);

        IB_intervalRest_plus = (ImageButton) findViewById(R.id.IB_intervalRest_plus);
        IB_intervalRest_plus.setOnTouchListener(this);

        IB_Series_minus = (ImageButton) findViewById(R.id.IB_Series_minus);
        IB_Series_minus.setOnTouchListener(this);

        IB_Series_plus = (ImageButton) findViewById(R.id.IB_Series_plus);
        IB_Series_plus.setOnTouchListener(this);
    }

    @Override protected void onStop() {
        super.onStop();
        t.cancel();
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        t.cancel();
    }
    @Override protected void onPause() {
        super.onPause();
        t.cancel();
    }

    /** set interval/count/break/series Edit Text **/
    public void setEditTextValue(boolean addValue, int etVersion){

        if (etVersion == 1){ //interval time

            int min = Integer.parseInt(ET_intervalMin.getText().toString());
            int sec = Integer.parseInt(ET_intervalSec.getText().toString());

            if (addValue) {
                //powiekszanie
                if (sec == 60) {
                    ET_intervalSec.setText("00");
                    min++;
                    if (min < 10){
                        ET_intervalMin.setText("0" + String.valueOf(min));
                    } else {
                        ET_intervalMin.setText(String.valueOf(min));
                    }
                } else {
                    sec++;
                    if (sec < 10){
                        ET_intervalSec.setText("0" + String.valueOf(sec));
                    } else {
                        ET_intervalSec.setText(String.valueOf(sec));
                    }
                }
            } else {
                //pomniejszanie
                if (sec == 0) {
                    if(min > 0) {
                        ET_intervalSec.setText("60");
                        min--;
                        if (min < 10){
                            ET_intervalMin.setText("0" + String.valueOf(min));
                        } else {
                            ET_intervalMin.setText(String.valueOf(min));
                        }
                    }
                } else {
                    sec--;
                    if (sec < 10){
                        ET_intervalSec.setText("0" + String.valueOf(sec));
                    } else {
                        ET_intervalSec.setText(String.valueOf(sec));
                    }
                }
            }

        } else if (etVersion == 2){ // interval count
            int count = Integer.parseInt(ET_intervalCount.getText().toString());

            if (addValue) {
                count++;
                ET_intervalCount.setText(String.valueOf(count));
            } else {
                if(count > 0) {
                    count--;
                    ET_intervalCount.setText(String.valueOf(count));
                }
            }

        } else if (etVersion == 3) { // rest time


            int min = Integer.parseInt(ET_breaklMin.getText().toString());
            int sec = Integer.parseInt(ET_breakSec.getText().toString());

            if (addValue) {
                //powiekszanie
                if (sec == 60) {
                    ET_breakSec.setText("00");
                    min++;
                    if (min < 10){
                        ET_breaklMin.setText("0" + String.valueOf(min));
                    } else {
                        ET_breaklMin.setText(String.valueOf(min));
                    }
                } else {
                    sec++;
                    if (sec < 10){
                        ET_breakSec.setText("0" + String.valueOf(sec));
                    } else {
                        ET_breakSec.setText(String.valueOf(sec));
                    }
                }
            } else {
                //pomniejszanie
                if (sec == 0) {
                    if(min > 0) {
                        ET_breakSec.setText("60");
                        min--;
                        if (min < 10){
                            ET_breaklMin.setText("0" + String.valueOf(min));
                        } else {
                            ET_breaklMin.setText(String.valueOf(min));
                        }
                    }
                } else {
                    sec--;
                    if (sec < 10){
                        ET_breakSec.setText("0" + String.valueOf(sec));
                    } else {
                        ET_breakSec.setText(String.valueOf(sec));
                    }
                }
            }

        } else if (etVersion == 4) { // series
            int series = Integer.parseInt(ET_intervalSeries.getText().toString());

            if (addValue) {
                series++;
                ET_intervalSeries.setText(String.valueOf(series));
            } else {
                if(series > 0) {
                    series--;
                    ET_intervalSeries.setText(String.valueOf(series));
                }
            }
        }

    }


    /** set touch iteration process, when u hold on button time will be increase or decrease **/
    public boolean setTouchEvent(boolean av, int et, MotionEvent motionEvent){
        Loc_addValue = av;
        Loc_etVersion = et;

        switch(motionEvent.getAction()) {

            case MotionEvent.ACTION_DOWN:

                if (mHandler != null) return true;
                clickDownTime = System.currentTimeMillis();
                mHandler = new Handler();
                mHandler.postDelayed(mAction, 300); // delay before start iteration
                break;

            case MotionEvent.ACTION_UP:
                long diff = System.currentTimeMillis() - clickDownTime;
                if(diff < 300 ){

                    setEditTextValue(Loc_addValue, Loc_etVersion);
                    Log.d("EVEE","curr: " +System.currentTimeMillis()+" , clickDownTime: " + clickDownTime + ", diff " + diff);
                }

                if (mHandler == null) return true;
                mHandler.removeCallbacks(mAction);
                mHandler = null;
                break;

        }

        return false;
    }

    /** runnable iteration 100ms cycle **/
    Runnable mAction = new Runnable() {
        @Override public void run() {
            setEditTextValue(Loc_addValue, Loc_etVersion);
            mHandler.postDelayed(this, 100); // delay iteration time
        }
    };


    /** set touch event on all buttons **/
    @Override
    public boolean onTouch(View v, MotionEvent motionEvent) {
        switch (v.getId()) {

            case R.id.IB_intervalTime_minus:
                setTouchEvent(false, 1, motionEvent);
                //setEditTextValue(false, 1);
                break;

            case R.id.IB_intervalTime_plus:
                setTouchEvent(true, 1, motionEvent);
                //setEditTextValue(true, 1);
                break;

            case R.id.IB_intervalCount_minus:
                setTouchEvent(false, 2, motionEvent);
                //setEditTextValue(false, 2);
                break;

            case R.id.IB_intervalCount_plus:
                setTouchEvent(true, 2, motionEvent);
                //setEditTextValue(true, 2);
                break;

            case R.id.IB_intervalRest_minus:
                setTouchEvent(false, 3, motionEvent);
                //setEditTextValue(false, 3);
                break;

            case R.id.IB_intervalRest_plus:
                setTouchEvent(true, 3, motionEvent);
                //setEditTextValue(true, 3);
                break;

            case R.id.IB_Series_minus:
                setTouchEvent(false, 4, motionEvent);
                //setEditTextValue(false, 4);
                break;

            case R.id.IB_Series_plus:
                setTouchEvent(true, 4, motionEvent);
                //setEditTextValue(true, 4);
                break;

            default:
                break;
        }
        return false;
    }

    /** convert string to number or to 0 if is empty**/
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

        /** if series > 0 then show total series and total time else hide it**/
        if(mSeriesCount > 0){
            showTotalSeries();
            showTimeTotal();
        } else {
            hideTotalSeries();
            hideTimeTotal();
        }

        /** change work text size to bigger **/
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


    /** change work text size **/
    private void startWork(){
        TextView TV_work_time_total = (TextView) findViewById(R.id.TV_work_time_total);
        TextView TV_rest_time_total = (TextView) findViewById(R.id.TV_rest_time_total);
        TV_work_time_total.setTextSize(25);
        TV_rest_time_total.setTextSize(15);
    }

    /** change rest text size **/
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

    /** get minutes from whole int time **/
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

    /** get seconds from whole int time **/
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

    /** play sound **/
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





private void startTimer(){


/**

    final Handler handler = new Handler();
    Timer timer = new Timer(false);
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("EVEE","start");
                    playSound(1);
                }
            });
        }
    };
    t.scheduleAtFixedRate(timerTask, 0, 1000); // every 5 seconds.
    //timer.scheduleAtFixedRate(timerTask, 0, 1000); // every 5 seconds.


**/





    t = new Timer();
    t.scheduleAtFixedRate(new TimerTask() {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Log.d("EVEE","start");
                    playSound(1);
                }
                });
            }
        }, 0, 1000);




//    Handler handler = new Handler();
//    handler.postDelayed(new Runnable() {
//        @Override
//        public void run() {
//            Log.d("EVEE","start");
//            playSound(1);
//        }
//    }, 1000);


//    handler = new Handler();
//    myRunnable = new Runnable() {
//        @Override
//        public void run() {
//            Log.d("EVEE","start");
//            playSound(1);
//        }
//    };
//    handler.postDelayed(myRunnable, 1000);
}
private void stopTimer(){

        //t.cancel();

        if(t != null){
            t.cancel();
        //handler.removeCallbacks(myRunnable);
        Log.d("EVEE","stop");
    }

//    if(handler != null){
//        handler.removeCallbacks(myRunnable);
//        Log.d("EVEE","stop");
//    }
}
}

