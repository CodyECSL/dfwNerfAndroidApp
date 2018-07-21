package com.example.testapp.testapp;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.json.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
    private TeamObject currentActiveTeam = new TeamObject("{\"teamName\":\"dude\"}");

    private String endpointBaseUrl = "https://nerf-data-api-dfw.herokuapp.com/";
    private String endpointRedTeamTimer = "koth/startTimer/Red";
    private String endpointBlueTeamTimer = "koth/startTimer/Blue";
    private String endpointStatus = "koth/status";
    private String endpointReset = "koth/reset";

    String navBarFirstButtonText;

    TextToSpeech t1;

    private RecyclerView recyclerViewMain;
    private RecyclerView.LayoutManager layoutManager;
    private List<String> list;
    private RecyclerAdapter adapter;

    ExecutorService service = Executors.newFixedThreadPool(4);

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(navBarFirstButtonText);
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
                        try {
                            getHttpResponseAsync(endpointBaseUrl + endpointRedTeamTimer);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        final Button btnStartBlueTimer = findViewById(R.id.btnStartBlueTimer);
        btnStartBlueTimer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                service.submit(new Runnable() {
                    public void run() {
                        try {
                            getHttpResponseAsync(endpointBaseUrl + endpointBlueTeamTimer);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        final Button btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                service.submit(new Runnable() {
                    public void run() {
                        try {
                            getHttpResponseAsync(endpointBaseUrl + endpointReset);
                            currentActiveTeam = new TeamObject("{\"teamName\":\"dude\"}");
                            t1.speak("TIMER RESET", TextToSpeech.QUEUE_FLUSH, null, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });

        recyclerViewMain = findViewById(R.id.recyclerViewMain);
        layoutManager = new LinearLayoutManager(this);
        recyclerViewMain.setLayoutManager(layoutManager);
        list = Arrays.asList();
        adapter = new RecyclerAdapter(list);
        recyclerViewMain.setHasFixedSize(true);
        recyclerViewMain.setAdapter(adapter);

        mTextMessage = (TextView) findViewById(R.id.message);
        navBarFirstButtonText = getResources().getString(R.string.title_home) + " - " + endpointBaseUrl;
        mTextMessage.setText(navBarFirstButtonText);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        t.start();
    }

    Thread t=new Thread(){
        @Override
        public void run(){
            while(!isInterrupted()){
                try {
                    Thread.sleep(1000);
                    //final String data = (String) getHttpResponse();  //1000ms = 1 sec
                    getHttpResponseAsync(endpointBaseUrl + endpointStatus);  //1000ms = 1 sec

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONArray jsonArray = new JSONArray(result);
                                ArrayList teamObjectArray = new ArrayList();
                                ArrayList arrayList = new ArrayList();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    TeamObject teamObject = new TeamObject(jsonObject.toString());
                                    teamObjectArray.add(teamObject);
                                    if (teamObject.isActive) {
                                        if (!currentActiveTeam.teamName.equals(teamObject.teamName)) {
                                            currentActiveTeam = teamObject;
                                            String tts = String.format("%s %s %s", teamObject.teamName, teamObject.teamName, teamObject.teamName);
                                            t1.speak(tts, TextToSpeech.QUEUE_FLUSH, null, null);
                                        }
                                    }
                                    arrayList.add(String.format("%s Team - %s Seconds", teamObject.teamName, teamObject.elapsedTimeInSeconds));
                                }
                                recyclerViewMain.setAdapter(new RecyclerAdapter(new ArrayList(arrayList)));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        try {
            switch (keyCode) {
            case KeyEvent.KEYCODE_0:
            case KeyEvent.KEYCODE_NUMPAD_0:
                getHttpResponseAsync(endpointBaseUrl + endpointRedTeamTimer);
                return true;
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_NUMPAD_1:
                getHttpResponseAsync(endpointBaseUrl + endpointBlueTeamTimer);
                return true;
            case KeyEvent.KEYCODE_2:
                return true;
            case KeyEvent.KEYCODE_3:
                return true;
            default:
                return super.onKeyUp(keyCode, event);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return super.onKeyUp(keyCode, event);
        }
    }
}
