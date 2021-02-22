package com.arbind.nsestockprice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class StockList extends AppCompatActivity {
    ListView listView;
    ArrayList stockName, stockCode;
    String exchangeNameWithSpace;
    String exName;
    ArrayAdapter arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_list);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        final String jsonArray = i.getStringExtra("jsonarray");
        stockName = i.getStringArrayListExtra("stName");
        stockCode = i.getStringArrayListExtra("stCode");
        exchangeNameWithSpace = i.getStringExtra("exNameWithSpace");
        exName = i.getStringExtra("exName");
        listView  = findViewById(R.id.listView2);

        arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_expandable_list_item_1, stockName);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String scd = parent.getItemAtPosition(position).toString();
                int pos = stockName.indexOf(scd);
                String stCode = stockCode.get(pos).toString();
                try {
                    JSONArray jsonArr = new JSONArray(jsonArray);
                    JSONObject jsonObj = jsonArr.getJSONObject(position);
                    Intent intent = new Intent(StockList.this,DisplayPrice.class);
                    intent.putExtra("exName", exName);
                    intent.putExtra("stCode", stCode);
                    intent.putExtra("stName", stockName);
                    intent.putExtra("exNameWithSpace", exchangeNameWithSpace);
                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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
        if(item.getItemId()==R.id.about){
            Intent i = new Intent(StockList.this, About.class);
            startActivity(i);
        }
        return false;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}