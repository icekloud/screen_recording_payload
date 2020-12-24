/*
 * Copyright 2017 egglang <t.egawa@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.e.codec;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.e.sonic_attendance_proto.R;


public class SettingValues extends AppCompatActivity {




    private Button bt_update;
    private Button bt_clearall;
    private EditText input_chunk_size;
    private EditText input_idle_limit;
    private EditText input_bit_duration;
    private EditText input_rate;
    private EditText input_baseline;
    private EditText input_char_freq_start;
    private EditText input_char_freq_end;
    private EditText input_char_freq_0;
    private EditText input_char_freq_1;
    private EditText input_char_freq_2;
    private EditText input_char_freq_3;
    private EditText input_char_freq_4;
    private EditText input_char_freq_5;
    private EditText input_char_freq_6;
    private EditText input_char_freq_7;
    private EditText input_char_freq_8;
    private EditText input_char_freq_9;
    private EditText input_char_freq_10;
    private EditText input_char_freq_11;
    private EditText input_char_freq_12;
    private EditText input_char_freq_13;
    private EditText input_char_freq_14;
    private EditText input_char_freq_15;

    private EditText input_char_thresh_start;
    private EditText input_char_thresh_end;
    private EditText input_char_thresh_0;
    private EditText input_char_thresh_1;
    private EditText input_char_thresh_2;
    private EditText input_char_thresh_3;
    private EditText input_char_thresh_4;
    private EditText input_char_thresh_5;
    private EditText input_char_thresh_6;
    private EditText input_char_thresh_7;
    private EditText input_char_thresh_8;
    private EditText input_char_thresh_9;
    private EditText input_char_thresh_10;
    private EditText input_char_thresh_11;
    private EditText input_char_thresh_12;
    private EditText input_char_thresh_13;
    private EditText input_char_thresh_14;
    private EditText input_char_thresh_15;
    private Button bt_clear_row_1;
    private Button bt_clear_row_2;

    static  int CHUNK_SIZE = 512;
    static  int IDLE_LIMIT = 2;
    static  double BIT_DURATION = 0.05;   // 높일 수록 정확도 상승
    static  int RATE = 44100;
    static  int BASELINE = 17000;
    static  int[] CHAR_FREQ = {
            17300,//17300, // start
            17500,//17500, // end
            17750,//17750, // 0
            17900,//17900, // 1
            18050,//18050, // 2
            18200,//18200, // 3
            18350,//18350, // 4
            18500,//18500, // 5
            18650,//18650, // 6
            18800,//18800, // 7
            18950,//18950, // 8
            19100,//19100, // 9
            19250,//19250, // 10
            19400,//19400, // 11
            19550, //19550, // 12
            19700,//19700, // 13
            19850,//19850, // 14
            20000 //20000  // 15
    };

    static  int[] CHAR_THRESH = {   // 낮을 수록 더 잘 수신됨  너무 높이면 민감해져서 정확한 주파수가 아니면 반응안함
            10, // start
            10, // end
            10, // 0
            10, // 1
            10, // 2
            10, // 3
            10, // 4
            10, // 5
            10, // 6
            10, // 7
            10, // 8
            10, // 9
            10, // 10
            10, // 11
            10, // 12
            10, // 13
            10, // 14
            10  // 15
    };






        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.setting_main);

                 input_chunk_size=(EditText)findViewById(R.id.editText4);
                 input_idle_limit=(EditText)findViewById(R.id.editText5);
                 input_bit_duration=(EditText)findViewById(R.id.editText6);
                 input_rate=(EditText)findViewById(R.id.editText9);
                 input_baseline=(EditText)findViewById(R.id.editText10);
                 input_char_freq_start=(EditText)findViewById(R.id.editText);
                 input_char_freq_end=(EditText)findViewById(R.id.editText8);
                 input_char_freq_0=(EditText)findViewById(R.id.editText49);
                 input_char_freq_1=(EditText)findViewById(R.id.editText48);
                 input_char_freq_2=(EditText)findViewById(R.id.editText152);
                 input_char_freq_3=(EditText)findViewById(R.id.editText146);
                 input_char_freq_4=(EditText)findViewById(R.id.editText150);
                 input_char_freq_5=(EditText)findViewById(R.id.editText151);
                 input_char_freq_6=(EditText)findViewById(R.id.editText139);
                 input_char_freq_7=(EditText)findViewById(R.id.editText149);
                 input_char_freq_8=(EditText)findViewById(R.id.editText143);
                 input_char_freq_9=(EditText)findViewById(R.id.editText147);
                 input_char_freq_10=(EditText)findViewById(R.id.editText156);
                 input_char_freq_11=(EditText)findViewById(R.id.editText162);
                 input_char_freq_12=(EditText)findViewById(R.id.editText155);
                 input_char_freq_13=(EditText)findViewById(R.id.editText160);
                 input_char_freq_14=(EditText)findViewById(R.id.editText154);
                 input_char_freq_15=(EditText)findViewById(R.id.editText161);

                 input_char_thresh_start=(EditText)findViewById(R.id.editText2);
                 input_char_thresh_end=(EditText)findViewById(R.id.editText11);
                 input_char_thresh_0=(EditText)findViewById(R.id.editText47);
                 input_char_thresh_1=(EditText)findViewById(R.id.editText46);
                 input_char_thresh_2=(EditText)findViewById(R.id.editText145);
                 input_char_thresh_3=(EditText)findViewById(R.id.editText148);
                 input_char_thresh_4=(EditText)findViewById(R.id.editText138);
                 input_char_thresh_5=(EditText)findViewById(R.id.editText153);
                 input_char_thresh_6=(EditText)findViewById(R.id.editText140);
                 input_char_thresh_7=(EditText)findViewById(R.id.editText142);
                 input_char_thresh_8=(EditText)findViewById(R.id.editText141);
                 input_char_thresh_9=(EditText)findViewById(R.id.editText144);
                 input_char_thresh_10=(EditText)findViewById(R.id.editText165);
                 input_char_thresh_11=(EditText)findViewById(R.id.editText164);
                 input_char_thresh_12=(EditText)findViewById(R.id.editText163);
                 input_char_thresh_13=(EditText)findViewById(R.id.editText157);
                 input_char_thresh_14=(EditText)findViewById(R.id.editText159);
                 input_char_thresh_15=(EditText)findViewById(R.id.editText158);



             input_chunk_size.setText(String.valueOf(CHUNK_SIZE));
             input_idle_limit.setText(String.valueOf(IDLE_LIMIT));
             input_bit_duration.setText(String.valueOf(BIT_DURATION));
             input_rate.setText(String.valueOf(RATE));
             input_baseline.setText(String.valueOf(BASELINE));
             input_char_freq_start.setText(String.valueOf(CHAR_FREQ[0]));
             input_char_freq_end.setText(String.valueOf(CHAR_FREQ[1]));
             input_char_freq_0.setText(String.valueOf(CHAR_FREQ[2]));
             input_char_freq_1.setText(String.valueOf(CHAR_FREQ[3]));
             input_char_freq_2.setText(String.valueOf(CHAR_FREQ[4]));
             input_char_freq_3.setText(String.valueOf(CHAR_FREQ[5]));
             input_char_freq_4.setText(String.valueOf(CHAR_FREQ[6]));
             input_char_freq_5.setText(String.valueOf(CHAR_FREQ[7]));
             input_char_freq_6.setText(String.valueOf(CHAR_FREQ[8]));
             input_char_freq_7.setText(String.valueOf(CHAR_FREQ[9]));
             input_char_freq_8.setText(String.valueOf(CHAR_FREQ[10]));
             input_char_freq_9.setText(String.valueOf(CHAR_FREQ[11]));
             input_char_freq_10.setText(String.valueOf(CHAR_FREQ[12]));
             input_char_freq_11.setText(String.valueOf(CHAR_FREQ[13]));
             input_char_freq_12.setText(String.valueOf(CHAR_FREQ[14]));
             input_char_freq_13.setText(String.valueOf(CHAR_FREQ[15]));
             input_char_freq_14.setText(String.valueOf(CHAR_FREQ[16]));
             input_char_freq_15.setText(String.valueOf(CHAR_FREQ[17]));

             input_char_thresh_start.setText(String.valueOf(CHAR_THRESH[0]));
             input_char_thresh_end.setText(String.valueOf(CHAR_THRESH[1]));
             input_char_thresh_0.setText(String.valueOf(CHAR_THRESH[2]));
             input_char_thresh_1.setText(String.valueOf(CHAR_THRESH[3]));
             input_char_thresh_2.setText(String.valueOf(CHAR_THRESH[4]));
             input_char_thresh_3.setText(String.valueOf(CHAR_THRESH[5]));
             input_char_thresh_4.setText(String.valueOf(CHAR_THRESH[6]));
             input_char_thresh_5.setText(String.valueOf(CHAR_THRESH[7]));
             input_char_thresh_6.setText(String.valueOf(CHAR_THRESH[8]));
             input_char_thresh_7.setText(String.valueOf(CHAR_THRESH[9]));
             input_char_thresh_8.setText(String.valueOf(CHAR_THRESH[10]));
             input_char_thresh_9.setText(String.valueOf(CHAR_THRESH[11]));
             input_char_thresh_10.setText(String.valueOf(CHAR_THRESH[12]));
             input_char_thresh_11.setText(String.valueOf(CHAR_THRESH[13]));
             input_char_thresh_12.setText(String.valueOf(CHAR_THRESH[14]));
             input_char_thresh_13.setText(String.valueOf(CHAR_THRESH[15]));
             input_char_thresh_14.setText(String.valueOf(CHAR_THRESH[16]));
             input_char_thresh_15.setText(String.valueOf(CHAR_THRESH[17]));




        }


        public void setBt_update(View view)
        {

               CHUNK_SIZE = Integer.valueOf(input_chunk_size.getText().toString());
               IDLE_LIMIT = Integer.valueOf(input_idle_limit.getText().toString());
               BIT_DURATION = Double.valueOf(input_bit_duration.getText().toString());   // 높일 수록 정확도 상승
               RATE = Integer.valueOf(input_rate.getText().toString());
               BASELINE = Integer.valueOf(input_baseline.getText().toString());

               CHAR_FREQ[0] = Integer.valueOf(input_char_freq_start.getText().toString());
            CHAR_FREQ[1] = Integer.valueOf(input_char_freq_end.getText().toString());
            CHAR_FREQ[2] = Integer.valueOf(input_char_freq_0.getText().toString());
            CHAR_FREQ[3] = Integer.valueOf(input_char_freq_1.getText().toString());
            CHAR_FREQ[4] = Integer.valueOf(input_char_freq_2.getText().toString());
            CHAR_FREQ[5] = Integer.valueOf(input_char_freq_3.getText().toString());
            CHAR_FREQ[6] = Integer.valueOf(input_char_freq_4.getText().toString());
            CHAR_FREQ[7] = Integer.valueOf(input_char_freq_5.getText().toString());
            CHAR_FREQ[8] = Integer.valueOf(input_char_freq_6.getText().toString());
            CHAR_FREQ[9] = Integer.valueOf(input_char_freq_7.getText().toString());
            CHAR_FREQ[10] = Integer.valueOf(input_char_freq_8.getText().toString());
            CHAR_FREQ[11] = Integer.valueOf(input_char_freq_9.getText().toString());
            CHAR_FREQ[12] = Integer.valueOf(input_char_freq_10.getText().toString());
            CHAR_FREQ[13] = Integer.valueOf(input_char_freq_11.getText().toString());
            CHAR_FREQ[14] = Integer.valueOf(input_char_freq_12.getText().toString());
            CHAR_FREQ[15] = Integer.valueOf(input_char_freq_13.getText().toString());
            CHAR_FREQ[16] = Integer.valueOf(input_char_freq_14.getText().toString());
            CHAR_FREQ[17] = Integer.valueOf(input_char_freq_15.getText().toString());

            CHAR_THRESH[0] = Integer.valueOf(input_char_thresh_start.getText().toString());
            CHAR_THRESH[1] = Integer.valueOf(input_char_thresh_end.getText().toString());
            CHAR_THRESH[2] = Integer.valueOf(input_char_thresh_0.getText().toString());
            CHAR_THRESH[3] = Integer.valueOf(input_char_thresh_1.getText().toString());
            CHAR_THRESH[4] = Integer.valueOf(input_char_thresh_2.getText().toString());
            CHAR_THRESH[5] = Integer.valueOf(input_char_thresh_3.getText().toString());
            CHAR_THRESH[6] = Integer.valueOf(input_char_thresh_4.getText().toString());
            CHAR_THRESH[7] = Integer.valueOf(input_char_thresh_5.getText().toString());
            CHAR_THRESH[8] = Integer.valueOf(input_char_thresh_6.getText().toString());
            CHAR_THRESH[9] = Integer.valueOf(input_char_thresh_7.getText().toString());
            CHAR_THRESH[10] = Integer.valueOf(input_char_thresh_8.getText().toString());
            CHAR_THRESH[11] = Integer.valueOf(input_char_thresh_9.getText().toString());
            CHAR_THRESH[12] = Integer.valueOf(input_char_thresh_10.getText().toString());
            CHAR_THRESH[13] = Integer.valueOf(input_char_thresh_11.getText().toString());
            CHAR_THRESH[14] = Integer.valueOf(input_char_thresh_12.getText().toString());
            CHAR_THRESH[15] = Integer.valueOf(input_char_thresh_13.getText().toString());
            CHAR_THRESH[16] = Integer.valueOf(input_char_thresh_14.getText().toString());
            CHAR_THRESH[17] = Integer.valueOf(input_char_thresh_15.getText().toString());








        }

        public void setBt_clearall(View view)
        {

            input_chunk_size.setText("");
            input_idle_limit.setText("");
            input_bit_duration.setText("");
            input_rate.setText("");
            input_baseline.setText("");
            input_char_freq_start.setText("");
            input_char_freq_end.setText("");
            input_char_freq_0.setText("");
            input_char_freq_1.setText("");
            input_char_freq_2.setText("");
            input_char_freq_3.setText("");
            input_char_freq_4.setText("");
            input_char_freq_5.setText("");
            input_char_freq_6.setText("");
            input_char_freq_7.setText("");
            input_char_freq_8.setText("");
            input_char_freq_9.setText("");
            input_char_freq_10.setText("");
            input_char_freq_11.setText("");
            input_char_freq_12.setText("");
            input_char_freq_13.setText("");
            input_char_freq_14.setText("");
            input_char_freq_15.setText("");

            input_char_thresh_start.setText("");
            input_char_thresh_end.setText("");
            input_char_thresh_0.setText("");
            input_char_thresh_1.setText("");
            input_char_thresh_2.setText("");
            input_char_thresh_3.setText("");
            input_char_thresh_4.setText("");
            input_char_thresh_5.setText("");
            input_char_thresh_6.setText("");
            input_char_thresh_7.setText("");
            input_char_thresh_8.setText("");
            input_char_thresh_9.setText("");
            input_char_thresh_10.setText("");
            input_char_thresh_11.setText("");
            input_char_thresh_12.setText("");
            input_char_thresh_13.setText("");
            input_char_thresh_14.setText("");
            input_char_thresh_15.setText("");


        }

        public void setBt_clear_row_1(View view)
        {
            input_char_freq_start.setText("");
            input_char_freq_end.setText("");
            input_char_freq_0.setText("");
            input_char_freq_1.setText("");
            input_char_freq_2.setText("");
            input_char_freq_3.setText("");
            input_char_freq_4.setText("");
            input_char_freq_5.setText("");
            input_char_freq_6.setText("");
            input_char_freq_7.setText("");
            input_char_freq_8.setText("");
            input_char_freq_9.setText("");
            input_char_freq_10.setText("");
            input_char_freq_11.setText("");
            input_char_freq_12.setText("");
            input_char_freq_13.setText("");
            input_char_freq_14.setText("");
            input_char_freq_15.setText("");

        }
         public void setBt_clear_row_2(View view)
          {
              input_char_thresh_start.setText("");
              input_char_thresh_end.setText("");
              input_char_thresh_0.setText("");
              input_char_thresh_1.setText("");
              input_char_thresh_2.setText("");
              input_char_thresh_3.setText("");
              input_char_thresh_4.setText("");
              input_char_thresh_5.setText("");
              input_char_thresh_6.setText("");
              input_char_thresh_7.setText("");
              input_char_thresh_8.setText("");
              input_char_thresh_9.setText("");
              input_char_thresh_10.setText("");
              input_char_thresh_11.setText("");
              input_char_thresh_12.setText("");
              input_char_thresh_13.setText("");
              input_char_thresh_14.setText("");
              input_char_thresh_15.setText("");


          }
    }
