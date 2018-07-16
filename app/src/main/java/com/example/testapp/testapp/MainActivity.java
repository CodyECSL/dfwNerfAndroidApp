package com.example.testapp.testapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.json.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

class TeamObject {
    String teamName;
    boolean isActive;
    long timerStartedAt;
    int elapsedTimeInSeconds;

    TeamObject(String JsonData) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(JsonData);
            this.teamName = (!jsonObject.isNull("teamName")) ? jsonObject.getString("teamName") : "nullTeam";
            this.isActive = (!jsonObject.isNull("isActive")) ? jsonObject.getBoolean("isActive") : false;
            this.timerStartedAt = (!jsonObject.isNull("timerStartedAt")) ? jsonObject.getLong("timerStartedAt") : 0;
            this.elapsedTimeInSeconds = (!jsonObject.isNull("elapsedTimeInSeconds")) ? jsonObject.getInt("elapsedTimeInSeconds") : 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
};

public class MainActivity extends AppCompatActivity {

    private OkHttpClient httpClient = new OkHttpClient();
    private TextView mTextMessage;
    private TextView myTextView;
    private Timer timer = new Timer();
    private int randomCounter = 0;
    private String result = "";
    private TeamObject teamObject = null;

    private RecyclerView recyclerViewMain;
    private RecyclerView.LayoutManager layoutManager;
    private List<String> list;
    private RecyclerAdapter adapter;

    ExecutorService service = Executors.newFixedThreadPool(4);

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final Button btnStartRedTimer = findViewById(R.id.btnStartRedTimer);
        btnStartRedTimer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                service.submit(new Runnable() {
                    public void run() {
                        getHttpResponse("https://nerf-data-app-api.herokuapp.com/reset");
                        getHttpResponse("https://nerf-data-app-api.herokuapp.com/startTimer/Red");
                    }
                });
            }
        });

        recyclerViewMain = findViewById(R.id.recyclerViewMain);
        layoutManager = new LinearLayoutManager(this);
        recyclerViewMain.setLayoutManager(layoutManager);
        list = Arrays.asList(getResources().getStringArray(R.array.android_versions));
        adapter = new RecyclerAdapter(list);
        recyclerViewMain.setHasFixedSize(true);
        recyclerViewMain.setAdapter(adapter);

        mTextMessage = (TextView) findViewById(R.id.message);
        myTextView = (TextView) findViewById(R.id.redTeamText);
        myTextView.setText("Even more sample text");
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        List<String> thing = list;
        thing.clear();
        adapter = new RecyclerAdapter(list);
        recyclerViewMain.setHasFixedSize(true);
        recyclerViewMain.setAdapter(adapter);

        t.start();
    }

    Thread t=new Thread(){
        @Override
        public void run(){
            while(!isInterrupted()){
                try {
                    Thread.sleep(1000);
                    //final String data = (String) getHttpResponse();  //1000ms = 1 sec
                    getHttpResponseAsync("https://nerf-data-app-api.herokuapp.com/status/Red");  //1000ms = 1 sec
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            teamObject = new TeamObject(result);
                            myTextView.setText(String.format("%s Team - %s Seconds", teamObject.teamName, teamObject.elapsedTimeInSeconds));
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Object getHttpResponse(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            String val = response.body().string();
            
            return val;
        } catch (Exception e) {
            Log.e("ERROR", "error in getting response get request okhttp");
        }
        return null;
    }

    public void getHttpResponseAsync(String url) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    //teamObject = processJsonResponse(response.body().string());
                    result = response.body().string();
                }
            }
        });
    }
}
