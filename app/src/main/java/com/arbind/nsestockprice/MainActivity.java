package com.arbind.nsestockprice;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String stock = "NIFTY50";
    String apiKey = "700d6561famshb1dc71d0d26b481p10ab4djsn07bc9ce2af16";
    ProgressDialog p;
    HashMap<String, String> stockIdNameMap = new HashMap<>();


    public MainActivity(){
        stockIdNameMap.put("Nifty 50", "NIFTY%2050");
        stockIdNameMap.put("Nifty 100", "NIFTY%20100");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.stockList, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        this.stock = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void getMeasurement(View view){
        try {
            DownloadTask task = new DownloadTask();
            String urlWithQuery = "https://latest-stock-price.p.rapidapi.com/prices?Indices=NIFTY%2050&Identifier=INDUSINDBKEQN";
            task.execute(urlWithQuery, this.stock);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String>{
        String selectedStock;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(MainActivity.this);
            p.setMessage("Fetching Stock Information...");
            p.setIndeterminate(false);
            p.setCancelable(false);
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

        JSONObject createResultViewParameters(JSONObject data){
            JSONObject resultView = new JSONObject();

            try {
                resultView.put("stock", this.selectedStock);

                JSONObject today = new JSONObject();
                today.put("lastPrice",Integer.toString(data.getInt("lastPrice")));
                today.put("dayHigh",Integer.toString(data.getInt("dayHigh")));
                today.put("dayLow",Integer.toString(data.getInt("dayLow")));
                today.put("change",Integer.toString(data.getInt("change")));
                resultView.put("today", today);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultView;
        }

        @Override
        protected void onPostExecute(String res) {
            super.onPostExecute(res);
            p.hide();
            p.dismiss();
            try{
                System.out.println(res);
                JSONArray resultJson = new JSONArray(res);
                Intent i = new Intent(getApplicationContext(), DisplayPrice.class);
                i.putExtra("result", resultJson.toString());
                startActivity(i);
                finish();
            } catch (JSONException e) {
                System.out.println("Exceptation while parsing response");
                e.printStackTrace();
            }
        }
    }
}