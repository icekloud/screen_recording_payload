package com.e.sonic_attendance_proto;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class JoinActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private Button bt_to_prof;
    private Button bt_to_student;
    private TextView login_log;
    private EditText text_id,text_pw,text_auth,text_info;

    private Button bt_login;
    private Button bt_join;
    private String input_id, input_pw, input_auth, input_info;
    private TextView view_id, view_pw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        text_id = (EditText) findViewById(R.id.text_id);
        text_pw = (EditText) findViewById(R.id.text_pw);
        text_auth = (EditText) findViewById(R.id.text_auth);
        text_info = (EditText) findViewById(R.id.text_info);

        view_id = (TextView) findViewById(R.id.view_id);
        view_pw = (TextView) findViewById(R.id.view_pw);



        login_log = (TextView) findViewById(R.id.login_log);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        bt_join = (Button)findViewById(R.id.bt_join);

        bt_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                input_id = text_id.getText().toString();
                input_pw = text_pw.getText().toString();
                input_auth = text_auth.getText().toString();
                input_info = text_info.getText().toString();

                JoinActivity.InsertData task = new JoinActivity.InsertData();
                task.execute("http://cswj123.cafe24.com/join_check.php", input_id,input_pw,input_auth,input_info);





            }
        });

    }



    class InsertData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(JoinActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            login_log.setText(result);

            Log.d("s", "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            String id = (String)params[1];
            String pw = (String)params[2];
            String authority = (String)params[3];
            String info = (String)params[4];

            String serverURL = (String)params[0];
            String postParameters = "user_id=" + id + "&pw=" + pw + "&authority=" + authority + "&info="+info;


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
}
