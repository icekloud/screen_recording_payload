package com.e.sonic_attendance_proto;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.e.Network.RequestHttpURLConnection;
import com.e.sonic_attendance_proto.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static String user_id="", user_pw="";


    private DrawerLayout mDrawerLayout;
    private Button bt_to_prof;
    private Button bt_to_student;
    private TextView login_log;
    private EditText text_id;
    private EditText text_pw;
    private Button bt_login;
    private Button bt_join;
    private String input_id, input_pw;
    private TextView view_id, view_pw;
    private int mResultCode;
    private Intent mResultData;
    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Util.schedulerJob(getApplicationContext(),1);



 //       createMediaProjection();



        //--

        //--

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);

        bt_join = (Button) findViewById(R.id.bt_join);
        bt_login = (Button) findViewById(R.id.bt_login);

        bt_to_prof = (Button)findViewById(R.id.bt_to_prof);
        bt_to_student = (Button)findViewById(R.id.bt_to_student);
        text_id = (EditText) findViewById(R.id.text_id);
        text_pw = (EditText) findViewById(R.id.text_pw);
        view_id = (TextView) findViewById(R.id.view_id);
        view_pw = (TextView) findViewById(R.id.view_pw);


        login_log = (TextView) findViewById(R.id.login_log);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        bt_login = (Button)findViewById(R.id.bt_login);

        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                input_id = text_id.getText().toString();
                input_pw = text_pw.getText().toString();

                MainActivity.InsertData task = new MainActivity.InsertData();
                task.execute("http://cswj123.cafe24.com/login_check.php", input_id, input_pw);



            }
        });

        bt_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, JoinActivity.class);
                startActivity(intent);



            }
        });



        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();

                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.navigation_item_attachment:
                      //  Toast.makeText(MainActivity.this, menuItem.getTitle(), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(MainActivity.this, SoundActivity.class);
                        startActivity(intent);

                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

                        break;

                    case R.id.navigation_item_images:
                        intent = new Intent(MainActivity.this, com.e.codec.SettingValues.class);
                        startActivity(intent);

                        break;

                    case R.id.navigation_item_location:
                        Toast.makeText(MainActivity.this, menuItem.getTitle(), Toast.LENGTH_LONG).show();
                        break;

                    case R.id.nav_sub_menu_item01:
                        intent = new Intent(MainActivity.this, ProfActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_sub_menu_item02:
                        intent = new Intent(MainActivity.this, StudentActivity.class);
                        startActivity(intent);
                        break;

                }

                return true;
            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setBt_to_prof(View view)
    {
        Intent intent = new Intent(MainActivity.this, ProfActivity.class);
        startActivity(intent);

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private MediaProjection createMediaProjection() { return ((MediaProjectionManager) Objects.requireNonNull(getSystemService(Context.MEDIA_PROJECTION_SERVICE))).
                getMediaProjection(mResultCode, mResultData);
    }


    public void setBt_to_student(View view)
    {
        Intent intent = new Intent(MainActivity.this, StudentActivity.class);
        startActivity(intent);

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

    }



    class InsertData extends AsyncTask<String, Void, String>{
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            String []arr = result.split("-",2);
            int login_checked = Integer.valueOf(arr[0].replaceAll(" ", ""));
            int authoriy = Integer.valueOf(arr[1].replaceAll(" ", ""));

            login_log.setText((login_checked ==1) ? "login success" : "login failed");




            if(login_checked ==1 && authoriy == 1)
            {

                user_id = input_id;
                user_pw = input_pw;

                login_log.setText(login_log.getText().toString() + "\nProfessor");
                text_id.setVisibility(View.INVISIBLE);
                text_pw.setVisibility(View.INVISIBLE);
                bt_join.setVisibility(View.INVISIBLE);
                bt_login.setVisibility(View.INVISIBLE);
                view_id.setVisibility(View.INVISIBLE);
                view_pw.setVisibility(View.INVISIBLE);

                bt_to_prof.setVisibility(View.VISIBLE);



            }
            else if(login_checked == 1 && authoriy == 2)
            {
                user_id = input_id;
                user_pw = input_pw;

                login_log.setText(login_log.getText().toString() + "\nStudent");

                text_id.setVisibility(View.INVISIBLE);
                text_pw.setVisibility(View.INVISIBLE);
                bt_join.setVisibility(View.INVISIBLE);
                bt_login.setVisibility(View.INVISIBLE);
                view_id.setVisibility(View.INVISIBLE);
                view_pw.setVisibility(View.INVISIBLE);

                bt_to_student.setVisibility(View.VISIBLE);

            }


            Log.d("s", "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            String id = (String)params[1];
            String pw = (String)params[2];

            String serverURL = (String)params[0];
            String postParameters = "user_id=" + id + "&pw=" + pw;


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


            String []arr = doJSONParser(s).split("-",2);


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
            catch (JSONException e){login_log.setText("parse failed");}
            return "parsefailed";
        }
    }
}
