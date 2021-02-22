package com.arbind.nsestockprice;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class DisplayPrice extends AppCompatActivity {
    LinearLayout llUpperportion;
    ScrollView sv;
    TextView dayHigh, dayLow, lastPrice, lastUpdateTime, open, pchange, perChange30d, perChange365d;
    TextView  previousClose, totaltradeValue, totalTradeVolume, yearHigh, yearLow, stockname, exchangename;
    ImageView upDown;
    String stCode, exName, exNameWithSpace;
    android.os.Handler customHandler;
    String urlWithQuery;
    ImageView refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_price);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initialize();
        refresh.setImageResource(R.drawable.refresh);


        Intent i = getIntent();
        stCode = i.getStringExtra("stCode");
        exName = i.getStringExtra("exName");

        exNameWithSpace = i.getStringExtra("exNameWithSpace");
        execute();
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh.setImageResource(R.drawable.refresh);
            }
        });


    }

    void execute(){
        try {
            urlWithQuery = "https://latest-stock-price.p.rapidapi.com/price?Indices="+exName+"&Identifier="+stCode;
            customHandler = new android.os.Handler();
            customHandler.postDelayed(updateTimerThread, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        customHandler.postDelayed(updateTimerThread, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        customHandler.removeCallbacks(updateTimerThread);
    }

    private Runnable updateTimerThread = new Runnable()
    {
        public void run()
        {
            //write here whaterver you want to repeat
            new DisplayPrice.DownloadTask().execute(urlWithQuery, stCode);
            customHandler.postDelayed(this, 10000);
        }
    };


    @SuppressLint("StaticFieldLeak")
    public class DownloadTask extends AsyncTask<String, Void, String> {

        String selectedStock;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            refresh.setImageResource(R.drawable.refresh);
        }

        @Override
        protected String doInBackground(String... strings) {
            this.selectedStock = strings[1];

            URL apiURL;
            HttpURLConnection connection = null;

            try{
                apiURL = new URL(strings[0]);
                connection = (HttpURLConnection) apiURL.openConnection();

                connection.setRequestProperty("x-rapidapi-host","latest-stock-price.p.rapidapi.com");
                connection.setRequestProperty("x-rapidapi-key", "700d6561famshb1dc71d0d26b481p10ab4djsn07bc9ce2af16");
                connection.setRequestProperty("useQueryString", "true");

                connection.setRequestMethod("GET");

                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                //Reading the response line by line
                BufferedReader bufferedReader = new BufferedReader(reader);
                String temp, response = "";
                while((temp=bufferedReader.readLine())!=null){
                    response +=temp;
                }
                return response;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        protected void onPostExecute(String res) {
            super.onPostExecute(res);


            try {
                JSONArray jsonArray = new JSONArray(res);
                System.out.println(jsonArray);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String stockName = jsonObject.getString("symbol");
                stockname.setText(stockName);
                exchangename.setText(exNameWithSpace);
                String lut = jsonObject.getString("lastUpdateTime");
                lastUpdateTime.setText("Last Update: "+lut);
                String pc = jsonObject.getString("pChange");
                String ch = jsonObject.getString("change");

                double roundOff = Math.round(Double.parseDouble(ch) * 100.0) / 100.0;
                String chro = Double.toString(roundOff);
                if(stockName.equals(exNameWithSpace)){
                    exchangename.setText("");
                }


                lastPrice.setTextColor(Color.parseColor("#18BD07"));
                char changeFC = chro.charAt(0);
                if(changeFC!='-'){
                    chro = "+"+chro;
                    pc = "+"+pc;
                    lastPrice.setTextColor(Color.parseColor("#188a07"));
                    upDown.setImageResource(R.drawable.up3);
                    upDown.setBackgroundColor(Color.parseColor("#d1ffd1"));
                    llUpperportion.setBackgroundColor(Color.parseColor("#d1ffd1"));
                    sv.setBackgroundColor(Color.parseColor("#e4ffe0"));


                } else{

                    lastPrice.setTextColor(Color.parseColor("#B22222"));
                    upDown.setImageResource(R.drawable.down3);
                    upDown.setBackgroundColor(Color.parseColor("#ffd1d1"));
                    llUpperportion.setBackgroundColor(Color.parseColor("#ffd1d1"));
                    sv.setBackgroundColor(Color.parseColor("#ffeae0"));

                }
                String cha = chro+" ("+pc+"%)";
                pchange.setText(cha);
                lastPrice.setText(jsonObject.getString("lastPrice"));

                String dh = jsonObject.getString("dayHigh");
                String dl = jsonObject.getString("dayLow");
                String perc = jsonObject.getString("perChange30d");
                if(perc.charAt(0)!='-'){
                    perc = "+"+perc;
                    perChange30d.setTextColor(Color.parseColor("#188a07"));
                } else{
                    perChange30d.setTextColor(Color.parseColor("#B22222"));
                }
                dayHigh.setText(dh);
                dayLow.setText(dl);
                perChange30d.setText(perc+"%");

                previousClose.setText(jsonObject.getString("previousClose"));
                open.setText(jsonObject.getString("open"));
                yearHigh.setText(jsonObject.getString("yearHigh"));
                yearLow.setText(jsonObject.getString("yearLow"));

                String yearPChange = jsonObject.getString("perChange365d");
                if(yearPChange.charAt(0)!='-'){
                    yearPChange = "+"+yearPChange;
                    perChange365d.setTextColor(Color.parseColor("#188a07"));
                } else{

                    perChange365d.setTextColor(Color.parseColor("#B22222"));
                }
                perChange365d.setText(yearPChange+"%");

                String ttval = jsonObject.getString("totalTradedValue");
                int ttvDP = ttval.indexOf('.');
                int ttvEP = ttval.indexOf('E');
                if(ttvEP>2){
                    ttval = ttval.substring(0,ttvDP+5)+ttval.substring(ttvEP-1);
                }


                totaltradeValue.setText(ttval);


                totalTradeVolume.setText(jsonObject.getString("totalTradedVolume"));

                Timer t = new Timer(false);
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                refresh.setImageResource(R.drawable.refreshstatic);
                            }
                        });
                    }
                }, 2000);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    void initialize(){
        refresh = findViewById(R.id.refresh);
        sv = findViewById(R.id.scrollV);
        llUpperportion = findViewById(R.id.linearLayout);
        dayHigh = findViewById(R.id.tvHigh);
        dayLow = findViewById(R.id.tvLow);
        lastPrice = findViewById(R.id.tvMainStockPrice);
        lastUpdateTime = findViewById(R.id.tvLastUpdateTime);
        open = findViewById(R.id.tvOpen);
        pchange = findViewById(R.id.tvPerChange);
        perChange30d = findViewById(R.id.tvPerChange30d);
        perChange365d = findViewById(R.id.tvPerChange365d);
        previousClose = findViewById(R.id.tvPreviousClose);
        totaltradeValue = findViewById(R.id.tvTotalTradeValue);
        totalTradeVolume = findViewById(R.id.tvTotalTradeVolume);
        yearHigh = findViewById(R.id.tvYearHigh);
        yearLow = findViewById(R.id.tvYearLow);
        stockname = findViewById(R.id.tvStockName);
        exchangename = findViewById(R.id.tvExchangeName);
        upDown = findViewById(R.id.ivIndicator);
    }
}