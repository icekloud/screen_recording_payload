package com.e.sonic_attendance_proto;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.e.Network.RequestHttpURLConnection;
import com.e.codec.SoundReceiver;
import com.e.codec.SoundSender;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.params.HttpConnectionParams;
import cz.msebera.android.httpclient.params.HttpParams;
import cz.msebera.android.httpclient.util.EntityUtils;

import static android.util.Patterns.IP_ADDRESS;
import static com.e.sonic_attendance_proto.MainActivity.user_id;

public class StudentActivity extends AppCompatActivity implements MessageDialogFragment.Listener {
    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private DrawerLayout mDrawerLayout;


    private TextView textview2,textview3;
    private boolean repeating = false;
    private boolean attendance_check = false;

    private boolean task1_made = false, task2_made = false, timer_made = false;
    private CheckBox box;

    private String trial_num;
    private String server_key;
    private String myResult;
    private int status = 0;
    private Timer timer;
    private InsertData task;
    private NetworkTask networkTask;


    @Override
    protected void onDestroy() {
        super.onDestroy();


        //timer = new Timer();
        if(timer_made == true)
        timer.cancel();


        if(task1_made == true)
        networkTask.cancel(true);

        if(task2_made == true)
        task.cancel(true);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        final SoundSender sender = new SoundSender();
        setSupportActionBar(toolbar);

        textview2 = (TextView)findViewById(R.id.textView2);
        // box = (CheckBox)findViewById(R.id.checkBox);
        textview3 = (TextView)findViewById(R.id.textView3);

//        String url = "http://cswj123.cafe24.com/getkey.php";
//        NetworkTask networkTask = new NetworkTask(url, null);
//        networkTask.execute();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(status == 0) {

                    status = 1;
                    textview3.setText(textview3.getText().toString() + "\nStart listening..." );
//
//                    String url = "http://cswj123.cafe24.com/getkey.php";
//                    final NetworkTask networkTask = new NetworkTask(url, null);
//                    networkTask.execute();


                    subscribeSound();

//                    //networkTask.cancel(true);
                    timer = new Timer();
                    timer_made = true;

                    TimerTask TT = new TimerTask() {
                        @Override
                        public void run() {
                            // 반복실행할 구문
                            String url = "http://cswj123.cafe24.com/getkey.php";
                            networkTask = new NetworkTask(url, null);
                            networkTask.execute();

                            task1_made = true;
                        }
                    };

                    timer.schedule(TT, 0, 2000); //Timer 실행





                }

                else if(status == 1)
                {
                    status = 2;
                    textview3.setText(textview3.getText().toString() + "\nStop listening" );
                    unSubscribeSound();

                }

                else if(status == 2)
                {
                    status = 1;
                    textview3.setText(textview3.getText().toString() + "\nResume listening..." );
                    subscribeSound();
                }
            }
        });





    }






    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;





        public NetworkTask(String url, ContentValues values) {

            this.url = url;
            this.values = values;


        }

        @Override
        protected String doInBackground(Void... params) {



                String result; // 요청 결과를 저장할 변수.
                RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
                result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.

                return result;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
            textview2.setText(s);

            String []arr = doJSONParser(s).split("-",2);
            trial_num = arr[0];
            server_key = arr[1];

            textview2.setText("출석 회차 : " + trial_num +"\n" + "서버 키 : "+ server_key);

            Log.d("v","Looping");


        }

        protected String doJSONParser(String str){
            try{
                String result = "";
                JSONObject order = new JSONObject(str);
                JSONArray index = order.getJSONArray("webnautes");
                for (int i = 0; i < index.length(); i++) {
                    JSONObject tt = index.getJSONObject(i);

                    result += tt.getString("data")+"\n";


                    return result;

                }
                // textview2.setText(result);

            }
            catch (JSONException e){textview2.setText("parse failed");}
            return "parsefailed";
        }
    }


    class InsertData extends AsyncTask<String, Void, String>{
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            progressDialog = ProgressDialog.show(StudentActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            textview3.setText("Attendance Check SUCCESS\n" + result);

            Log.d("d", "POST response  - " + result);

        }


        @Override
        protected String doInBackground(String... params) {

            String trial_num = (String)params[1];
            String id = (String)params[2];
            String approval = (String)params[3];

            String serverURL = (String)params[0];
            String postParameters = "trial_num=" + trial_num + "&id=" + id + "&approval=" + approval;


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d("s", "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();




                return sb.toString();


            } catch (Exception e) {

                Log.d("s", "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }
    }







    private ListenHandler mServiceHandler;







    @Override
    protected void onResume() {
        super.onResume();
        // Start listening
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
           // subscribeSound();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            showPermissionMessageDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unSubscribeSound();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

    public void AttendaceCheck(String key)
    {



    }



    public void repeat()
    {
        final SoundSender sender = new SoundSender();

        while(repeating == true)
        {
            if(repeating == false) break;
            sender.sendString("Hello world!");

            Handler delayHandler = new Handler();
            delayHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // TODO
                }
            }, 5000);
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               // subscribeSound();
            } else {
                showPermissionMessageDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance(getString(R.string.permission_message))
                .show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }

    private void subscribeSound() {
        unSubscribeSound();
        HandlerThread thread = new HandlerThread("ServiceHandleThread", Process.THREAD_PRIORITY_URGENT_AUDIO);
        thread.start();
        mServiceHandler = new ListenHandler(thread.getLooper());
        Message message = this.mServiceHandler.obtainMessage();
        message.arg1 = 1;
        this.mServiceHandler.sendMessage(message);
    }

    private void unSubscribeSound() {
        if (mServiceHandler != null) {
            mServiceHandler.quit();
            mServiceHandler = null;
        }
    }

    private int cnt = 0;

    private void onStringReceived(final String receivedString) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(StudentActivity.this, "Received:" + receivedString, Toast.LENGTH_LONG).show();

                int distance = getDistance(server_key,receivedString.substring(0,receivedString.length()-1));


                textview2.setText(textview2.getText() + "\n"+ "Received:" + receivedString + "|  " +
                        " Distance : " + distance );




                if(distance < 4) {

                    attendance_check = true;
                    textview2.setText(textview2.getText() + "\n"+ "attendance check success!  Stop listening");
                    unSubscribeSound();


                    if(user_id =="")
                    user_id = Build.MODEL;

                    task = new InsertData();
                    task.execute("http://cswj123.cafe24.com/attcheck.php", trial_num,user_id,"1");

                    task2_made = true;





                }


            }
        });
    }


    public static int getDistance(String s1, String s2) {
        int longStrLen = s1.length() + 1;
        int shortStrLen = s2.length() + 1; // 긴 단어 만큼 크기가 나올 것이므로, 가장 긴단어 에 맞춰 Cost를 계산
        int[] cost = new int[longStrLen];
        int[] newcost = new int[longStrLen]; // 초기 비용을 가장 긴 배열에 맞춰서 초기화 시킨다.
        for (int i = 0; i < longStrLen; i++) { cost[i] = i; } // 짧은 배열을 한바퀴 돈다.
        for (int j = 1; j < shortStrLen; j++) {
            // 초기 Cost는 1, 2, 3, 4...
            newcost[0] = j; // 긴 배열을 한바퀴 돈다.
            for (int i = 1; i < longStrLen; i++) {
                // 원소가 같으면 0, 아니면 1
                int match = 0;
                if (s1.charAt(i - 1) != s2.charAt(j - 1)) { match = 1; }
                // 대체, 삽입, 삭제의 비용을 계산한다.
                int replace = cost[i - 1] + match;
                int insert = cost[i] + 1;
                int delete = newcost[i - 1] + 1;
                // 가장 작은 값을 비용에 넣는다.
                newcost[i] = Math.min(Math.min(insert, delete), replace);
            } // 기존 코스트 & 새 코스트 스위칭
            int[] temp = cost;
            cost = newcost;
            newcost = temp;
        }
        // 가장 마지막값 리턴
        return cost[longStrLen - 1];
    }




    private final class ListenHandler extends Handler {

        private boolean isRunning = true;
        private SoundReceiver mSoundReceiver = new SoundReceiver();

        public ListenHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            while (isRunning) {
                String body;
                try {
                    body = mSoundReceiver.receiveAsString();

                    if (body == null) continue;
                    Log.d(this.getClass().getName(), body);
                    Log.d(this.getClass().getName(), "isRunning : " + "true");
                    onStringReceived(body);                                               // sdk 상위버전에서 두번째 실행이 안됨
                    //     quit();
                    //     subscribeSound();

                } catch (RuntimeException e) {
                    Log.d(this.getClass().getName(), "Parse failed.");
                    Log.d(this.getClass().getName(), e.getMessage(), e);
                    quit();
                }

            }
            // getLooper().quit();
        }

        public void quit() {
            this.isRunning = false;
            this.mSoundReceiver.quit();
        }



    }

}
