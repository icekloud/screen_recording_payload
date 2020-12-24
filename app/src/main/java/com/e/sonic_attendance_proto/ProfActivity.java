package com.e.sonic_attendance_proto;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.e.Network.RequestHttpURLConnection;
import com.e.codec.SoundReceiver;
import com.e.codec.SoundSender;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class ProfActivity extends AppCompatActivity implements MessageDialogFragment.Listener {
    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private DrawerLayout mDrawerLayout;


    private TextView textview2, textview3;
    private boolean repeating = false;
    private boolean attendance_check = false;
    private Button bt_action;
    private CheckBox box;

    private String trial_num;
    private String server_key;
    private int status = 0;
    private int repeat_time = 5;
    private String action;
    private Timer timer;

    private boolean timer_made = false, task1_made = false;

    private EditText text_repeat;

    int serverResponseCode = 0;

    ProgressDialog dialog = null;



    String upLoadServerUri = null;
    final String uploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();//경로를 모르겠으면, 갤러리 어플리케이션 가서 메뉴->상세 정보

    final String uploadFileName = "/sound.wav"; //전송하고자하는 파일 이름






    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(timer_made == true)
        timer.cancel();

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prof);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        final SoundSender sender = new SoundSender();
        setSupportActionBar(toolbar);

        textview2 = (TextView)findViewById(R.id.textView2);
        textview3 = (TextView)findViewById(R.id.textView3);


        text_repeat = (EditText)findViewById(R.id.text_repeat);
       // box = (CheckBox)findViewById(R.id.checkBox);


        String url = "http://cswj123.cafe24.com/index.php";
        NetworkTask networkTask = new NetworkTask(url, null);
        networkTask.execute();

        timer = new Timer();
        timer_made = true;

        TimerTask TT = new TimerTask() {
            @Override
            public void run() {
                // 반복실행할 구문
                String url = "http://cswj123.cafe24.com/update_check.php?trial_num="+trial_num;
                ProfActivity.NetworkTask networkTask = new ProfActivity.NetworkTask(url, null);
                networkTask.execute();

            }
        };

        timer.schedule(TT, 0, 1000); //Timer 실행




        bt_action = (Button) findViewById(R.id.bt_action);
        bt_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//status 0 : 초기상태 1 : 재생 중 2 : 정지

                switch (status) {


                    case 0:
                        StartAttendance(server_key);
                        textview2.setText(textview2.getText().toString() + "\n Starting attendace ... " + "status : " + status);
                        bt_action.setText("일시중지");

                        break;

                    //0->1
                    case 1:
                        StopAttendance();
                        textview2.setText(textview2.getText().toString() + "\n Stopped Attendance  " + "status : " + status);
                        bt_action.setText("재개");

                        break;
                    //1->2

                    case 2:
                        ResumeAttendance();
                        textview2.setText(textview2.getText().toString() + "\n Resuming Attendance  " + "status : " + status);
                        bt_action.setText("일시중지");

                        break;

                    //2->1
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
        protected void onCancelled()
        {
            super.onCancelled();
        }


        @Override
        protected String doInBackground(Void... params) {

            String result; // 요청 결과를 저장할 변수.
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.
            return result;


        }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);

            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
            //textview2.setext(s);

            if(url == "http://cswj123.cafe24.com/index.php")//status == 0)
            {

                Log.d("v", "received from webserver : " + s);
                String[] arr = doJSONParser(s).split("-");
                trial_num = arr[0];
                server_key = arr[1];
                action = arr[2];
                textview2.setText("출석 회차 : " + trial_num + "\n" + "서버 키 : " + server_key);

                if (action.substring(0,1).equals("1")) {
                    status = 1;
                    bt_action.setText("일시정지");


                }
            }

            else if(url.equals("http://cswj123.cafe24.com/update_check.php?trial_num="+trial_num))
            {
                textview3.setText("출석 현황\n" + s + "명 출석완료");

                Log.d("v","Looped");
            }

         //  this.cancel(true);

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




    private ListenHandler mServiceHandler;







    @Override
    protected void onResume() {
        super.onResume();
        // Start listening
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
         //   subscribeSound();
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

    public void StartAttendance(String key)
    {
        final SoundSender sender = new SoundSender();

        repeat_time = Integer.valueOf(text_repeat.getText().toString());

        for(int i=0; i<repeat_time; i++)
        {
        sender.sendString(key);
        }

        upLoadServerUri = "http://cswj123.cafe24.com/UploadToServer.php";//서버컴퓨터의 ip주소





        new Thread(new Runnable() {



            public void run() {


                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                uploadFile(uploadFilePath + "" + uploadFileName);
                Log.v("v","File uploaded");



            }

        }).start();
      //  textview2.setText(textview2.getText().toString() + "\n Attendace check end !");

        status = 1;

    }

    public void StopAttendance()
    {

        String url = "http://cswj123.cafe24.com/stop_checking.php";
        NetworkTask networkTask = new NetworkTask(url, null);
        networkTask.execute();

        status = 2;
    }


    public void ResumeAttendance()
    {
        String url = "http://cswj123.cafe24.com/resume_checking.php";
        NetworkTask networkTask = new NetworkTask(url, null);
        networkTask.execute();

        status = 1;
    }


    public int uploadFile(String sourceFileUri) {



        String fileName = sourceFileUri;



        HttpURLConnection conn = null;

        DataOutputStream dos = null;

        String lineEnd = "\r\n";

        String twoHyphens = "--";

        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;

        byte[] buffer;

        int maxBufferSize = 1 * 1024 * 1024;

        File sourceFile = new File(sourceFileUri);



        if (!sourceFile.isFile()) {



            //  dialog.dismiss();



            Log.e("uploadFile", "Source File not exist :"

                    +uploadFilePath + "" + uploadFileName);



            runOnUiThread(new Runnable() {

                public void run() {

//                    messageText.setText("Source File not exist :"
//
//                            +uploadFilePath + "" + uploadFileName);

                }

            });



            return 0;



        }

        else

        {

            try {



                // open a URL connection to the Servlet

                FileInputStream fileInputStream = new FileInputStream(sourceFile);

                URL url = new URL(upLoadServerUri);



                // Open a HTTP  connection to  the URL

                conn = (HttpURLConnection) url.openConnection();

                conn.setDoInput(true); // Allow Inputs

                conn.setDoOutput(true); // Allow Outputs

                conn.setUseCaches(false); // Don't use a Cached Copy

                conn.setRequestMethod("POST");

                conn.setRequestProperty("Connection", "Keep-Alive");

                conn.setRequestProperty("ENCTYPE", "multipart/form-data");

                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                conn.setRequestProperty("uploaded_file", fileName);



                dos = new DataOutputStream(conn.getOutputStream());



                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""

                        + fileName + "\"" + lineEnd);



                dos.writeBytes(lineEnd);



                // create a buffer of  maximum size

                bytesAvailable = fileInputStream.available();



                bufferSize = Math.min(bytesAvailable, maxBufferSize);

                buffer = new byte[bufferSize];



                // read file and write it into form...

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);



                while (bytesRead > 0) {



                    dos.write(buffer, 0, bufferSize);

                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);

                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);



                }



                // send multipart form data necesssary after file data...

                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);



                // Responses from the server (code and message)

                serverResponseCode = conn.getResponseCode();

                String serverResponseMessage = conn.getResponseMessage();



                Log.i("uploadFile", "HTTP Response is : "

                        + serverResponseMessage + ": " + serverResponseCode);



                if(serverResponseCode == 200){



                    runOnUiThread(new Runnable() {

                        public void run() {



                            String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"

                                    +uploadFileName;


                        }

                    });

                }

                fileInputStream.close();

                dos.flush();

                dos.close();



            } catch (MalformedURLException ex) {



                dialog.dismiss();

                ex.printStackTrace();



                runOnUiThread(new Runnable() {

                    public void run() {

                        //messageText.setText("MalformedURLException Exception : check script url.");

                        Toast.makeText(ProfActivity.this, "MalformedURLException",

                                Toast.LENGTH_SHORT).show();

                    }

                });



                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);

            } catch (Exception e) {



                dialog.dismiss();

                e.printStackTrace();



                runOnUiThread(new Runnable() {

                    public void run() {

                        //messageText.setText("Got Exception : see logcat ");

                        Toast.makeText(ProfActivity.this, "Got Exception : see logcat ",

                                Toast.LENGTH_SHORT).show();

                    }

                });

                // Log.e("Upload file to server Exception", "Exception : "

//                        + e.getMessage(), e);

            }

            //    dialog.dismiss();

            return serverResponseCode;



        } // End else block

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
                //subscribeSound();
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
                Toast.makeText(ProfActivity.this, "Received:" + receivedString, Toast.LENGTH_LONG).show();

                int distance = getDistance(server_key,receivedString.substring(0,receivedString.length()-1));


                textview2.setText(textview2.getText() + "\n"+ "Received:" + receivedString.substring(0,receivedString.length()) + "|  " +
                        " Distance : " + distance );




                if(distance < 4) {

                    attendance_check = true;
                    textview2.setText(textview2.getText() + "\n"+ "attendance check success!  Stop listening");
                   // unSubscribeSound();

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
