package com.arbind.nsestockprice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class LaunchActivity extends AppCompatActivity {
    ListView lv;
    ProgressDialog p;
    String exchangeName;
    String newExchangename;
    ArrayList stName, stCode;
    ArrayAdapter arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        startExe();
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        startExe();
    }

    protected void startExe(){
        lv = findViewById(R.id.listView);
        arrayAdapter = ArrayAdapter.createFromResource(this, R.array.exchangeList, android.R.layout.simple_expandable_list_item_1);
        lv.setAdapter(arrayAdapter);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {



                stName = new ArrayList();
                stCode = new ArrayList();

                exchangeName = parent.getItemAtPosition(position).toString();
                newExchangename = "";
                for(int i=0; i<exchangeName.length(); i++){
                    char ch = exchangeName.charAt(i);
                    if(ch==' '){
                        newExchangename+="%20";
                    } else{
                        newExchangename+=ch;
                    }
                }

                try {
                    String urlWithQuery = "https://latest-stock-price.p.rapidapi.com/price?Indices="+newExchangename;
                    new LaunchActivity.DownloadTask().execute(urlWithQuery, exchangeName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }



    public class DownloadTask extends AsyncTask<String, Void, String> {
        String selectedStock;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(LaunchActivity.this);
            p.setMessage("Fetching Stock Information...");
            p.setIndeterminate(false);
            p.setCancelable(false);
            p.show();
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

                System.out.println("gvhwvedv"+response);
                return response;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        String createResultViewParameters(String data){
            JSONArray resultView = new JSONArray();

            try {
                for(int i = 0; i<data.length(); i++){
                    JSONArray jsonArr2 = new JSONArray(data);
                    JSONObject jsonObj = jsonArr2.getJSONObject(i);
                    JSONObject setdata = new JSONObject();
                    setdata.put("lastPrice", jsonObj.getString("lastPrice"));
                    setdata.put("dayHigh", jsonObj.getString("dayHigh"));
                    setdata.put("dayLow", jsonObj.getString("dayLow"));
                    setdata.put("change", jsonObj.getString("change"));
                    stName.add(jsonObj.getString("symbol"));
                    stCode.add(jsonObj.getString("identifier"));
                    resultView.put(setdata);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultView.toString();
        }

        @Override
        protected void onPostExecute(String res) {
            super.onPostExecute(res);

            p.hide();
            p.dismiss();
            String res2 =  createResultViewParameters(res);
            try {
                JSONArray jsona = new JSONArray(res2);
                System.out.println("JAJHHJhjsjhb"+jsona);
                System.out.println(stName.get(0));
                System.out.println(stCode.get(0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println(res);
            try{
                JSONArray resultJson = new JSONArray(res);

                for(int i=0; i<5; i++){
                    System.out.println(stName.get(i));
                }
                Intent intent = new Intent(LaunchActivity.this, StockList.class);
                intent.putExtra("jsonarray", res2);
                intent.putStringArrayListExtra("stName", stName);
                intent.putStringArrayListExtra("stCode", stCode);
                intent.putExtra("exName", newExchangename);
                intent.putExtra("exNameWithSpace",exchangeName);
                startActivity(intent);
                
            } catch (JSONException e) {
                System.out.println("Exceptation while parsing response");
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView sv = (SearchView) item.getActionView();
        sv.setQueryHint("enter");
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                arrayAdapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}