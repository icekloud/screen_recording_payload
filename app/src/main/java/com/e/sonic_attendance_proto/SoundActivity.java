package com.e.sonic_attendance_proto;


import android.Manifest;
import android.content.pm.PackageManager;
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

import com.e.codec.SoundReceiver;
import com.e.codec.SoundSender;

public class SoundActivity extends AppCompatActivity implements MessageDialogFragment.Listener {
    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private DrawerLayout mDrawerLayout;


    private TextView textview2;
    private boolean repeating = false;
    private boolean attendance_check = false;

    private CheckBox box;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

//        //---
//
//        setSupportActionBar(toolbar);
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
//        actionBar.setDisplayHomeAsUpEnabled(true);
//
//        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//
//        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
//        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(MenuItem menuItem) {
//                menuItem.setChecked(true);
//                mDrawerLayout.closeDrawers();
//
//                int id = menuItem.getItemId();
//                switch (id) {
//                    case R.id.navigation_item_attachment:
//                        //  Toast.makeText(MainActivity.this, menuItem.getTitle(), Toast.LENGTH_LONG).show();
//                        Intent intent = new Intent(SoundActivity.this, SoundActivity.class);
//                        startActivity(intent);
//
//                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
//
//                        break;
//
//                    case R.id.navigation_item_images:
//                        intent = new Intent(SoundActivity.this, com.e.codec.SettingValues.class);
//                        startActivity(intent);
//
//                        break;
//
//                    case R.id.navigation_item_location:
//                        intent = new Intent(SoundActivity.this, MainActivity.class);
//                        startActivity(intent);
//
//                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);                        break;
//
//                    case R.id.nav_sub_menu_item01:
//                        Toast.makeText(SoundActivity.this, menuItem.getTitle(), Toast.LENGTH_LONG).show();
//                        break;
//
//                    case R.id.nav_sub_menu_item02:
//                        Toast.makeText(SoundActivity.this, menuItem.getTitle(), Toast.LENGTH_LONG).show();
//                        break;
//
//                }
//
//                return true;
//            }
//        });

        final SoundSender sender = new SoundSender();
        setSupportActionBar(toolbar);

        textview2 = (TextView)findViewById(R.id.textView2);
        box = (CheckBox)findViewById(R.id.checkBox);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (int i = 0; i < 1; i++)
                {

                    Snackbar.make(view, "Sending message...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    sender.sendString("Hello world!");
                }
            }
        });






        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int cnt = 5;

                Snackbar.make(view, "Sending messages " + cnt +" times", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


                for (int i = 0; i < cnt; i++)
                {
                    Handler delayHandler = new Handler();
                     delayHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // TODO
                }
            }, 700);


                    sender.sendString("Hello world!");
                }


            }
        });





    }

    private ListenHandler mServiceHandler;

    @Override
    protected void onResume() {
        super.onResume();
        // Start listening
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            subscribeSound();
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
                subscribeSound();
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
                Toast.makeText(SoundActivity.this, "Received:" + receivedString, Toast.LENGTH_LONG).show();

                int distance = getDistance("Hello world!",receivedString.substring(0,receivedString.length()-1));


                textview2.setText(textview2.getText() + "\n"+ "Received:" + receivedString.substring(0,receivedString.length()-1) + "|  " +
                        " Distance : " + distance );




                if(box.isChecked() && distance < 6) {

                    attendance_check = true;
                    textview2.setText(textview2.getText() + "\n"+ "attendance check success!  Stop listening");
                    unSubscribeSound();

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
