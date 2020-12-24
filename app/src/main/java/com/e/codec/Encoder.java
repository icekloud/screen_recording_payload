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

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.e.ecc.EccEncoder;
import com.e.ecc.EccInstanceProvider;
import com.e.sonic_attendance_proto.MainActivity;
import com.e.sonic_attendance_proto.ProfActivity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import static com.e.codec.SettingValues.RATE;





public class Encoder extends Activity{
    private EccEncoder mEccEncoder;
    private AudioTrack mAudioTrack;
    private ExecutorService mExecutorService;




    class PlaySoundTask implements Runnable {
        private final byte[] mArrayToSend;

        public PlaySoundTask(byte[] arrayToSend) {
            mArrayToSend = arrayToSend;
        }  //mArrayToSend가 소리로 바꾸기 전 최종 데이터배열

        @Override
        public void run() {

            int minBufferSizeInBytes = AudioTrack.getMinBufferSize(
                    RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() +"/record.pcm");   //최상단 경로에 record.pcm 으로 녹음파일 저장


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


            try {
                fos.write(mArrayToSend, 0, mArrayToSend.length);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mAudioTrack.play();
            mAudioTrack.write(mArrayToSend, 0, mArrayToSend.length);
            mAudioTrack.stop();

            try {
                fos.close();

                convertPcm2Wav(Environment.getExternalStorageDirectory().getAbsolutePath() +"/record.pcm", Environment.getExternalStorageDirectory().getAbsolutePath() +"/sound.wav",44100,1, 16);
                Log.v("v","File created");


            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    public Encoder(boolean eccEnabled) {
        init_(eccEnabled);
    }

    private void init_(boolean eccEnabled) {
        mEccEncoder = EccInstanceProvider.getEncoder(eccEnabled);
        int minBufferSizeInBytes = AudioTrack.getMinBufferSize(
                RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        // 44.1kHz mono 16bit
        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSizeInBytes,
                AudioTrack.MODE_STREAM);
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    public void encodePlay(byte[] byteData) {
        double[] soundList = string2sound(byteData);
        // convert double to byte
        short[] arrayToSend = new short[soundList.length];
        for (int i = 0; i < soundList.length; i++) {
            arrayToSend[i] = (short) soundList[i];
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (short s : arrayToSend) {
            stream.write(s & 0xff);
            stream.write((s >> 8) & 0xff);
        }
        byte[] byteArrayToSend = stream.toByteArray();
        mExecutorService.execute(new PlaySoundTask(byteArrayToSend));
    }

    public void quit() {
        mAudioTrack.release();
    }

    private double[] string2sound(byte[] byteData) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (byte b : byteData) {
            bytes.write(-1); // -001
            byte[] encodedBytes = resolveEncodedBytes(b);
            for (byte encoded : encodedBytes) {
                bytes.write(encoded);
            }
        }
        bytes.write(-2);
        byte[] multiple = bytes.toByteArray();
        List<Double> soundList = new ArrayList<>();
        for (int i = 0; i < multiple.length; i++) {
            double[] gotten = getBit(SettingValues.CHAR_FREQ[multiple[i] + 2]);
            for (double g : gotten) {
                soundList.add(g);
            }
        }
        double[] result = new double[soundList.size()];
        for (int i = 0; i < soundList.size(); i++) {
            result[i] = soundList.get(i);
        }
        return result;
    }

    private byte[] resolveEncodedBytes(byte b) {
        byte[] encodedBytes = mEccEncoder.getEncodedBytes(b);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (byte encodedByte : encodedBytes) {
            byte b1 = (byte) ((encodedByte & 0xF0) >>> 4 & 0x0F);
            byte b2 = (byte) (encodedByte & 0x0F);
            bytes.write(b1);
            bytes.write(b2);
        }
        return bytes.toByteArray();
    }

    private double[] linspace(double min, double max, double step) {
        List<Double> d = new ArrayList<>();
        double current = min + step;
        while (current < max) {
            d.add(current);
            current += step;
        }
        double[] result = new double[d.size()];
        for (int i = 0; i < d.size(); i++) {
            result[i] = d.get(i);
        }
        return result;
    }

    private double[] createSin(double freq, double[] time) {
        int length = time.length;
        double[] data = new double[length];
        for (int i = 0; i < length; i++) {
            data[i] = (int) ((double) 32000 * Math.sin(2 * Math.PI * freq * time[i]));
        }
        return data;
    }

    private double sigmoid(double x) {
        return ((double) 1 / ((double) 1 + Math.pow(Math.E, (-1 * x))));
    }

    private double[] getBit(double freq) {
        double[] t = linspace(0, SettingValues.BIT_DURATION, (double) 1 / (double) RATE);
        double[] x = createSin(freq, t);
        double[] b = linspace(-6, 6, 0.02);
        int length = b.length;
        double[] sigmoid = new double[length];
        for (int i = 0; i < length; i++) {
            sigmoid[i] = sigmoid(b[i]);
        }
        double[] sigmoidInv = new double[length];
        for (int i = 0; i < length; i++) {
            sigmoidInv[length - i - 1] = sigmoid[i];
        }
        int xstart = x.length - length;
        for (int i = 0; i < length; i++) {
            x[xstart + i] *= sigmoidInv[i];
            x[i] *= sigmoid[i];
        }
        return x;
    }

    public static void convertPcm2Wav(String inPcmFilePath, String outWavFilePath, int sampleRate,int channels, int bitNum) {

        FileInputStream in = null;
        FileOutputStream out = null;
        byte[] data = new byte[1024];

        try {
            //采样字节byte率
            long byteRate = sampleRate * channels * bitNum / 8;

            in = new FileInputStream(inPcmFilePath);
            out = new FileOutputStream(outWavFilePath);

            //PCM文件大小
            long totalAudioLen = in.getChannel().size();

            //总大小，由于不包括RIFF和WAV，所以是44 - 8 = 36，在加上PCM文件大小
            long totalDataLen = totalAudioLen + 36;

            writeWaveFileHeader(out, totalAudioLen, totalDataLen, sampleRate, channels, byteRate);

            int length = 0;
            while ((length = in.read(data)) > 0) {
                out.write(data, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    /**
     * 输出WAV文件
     *
     * @param out           WAV输出文件流
     * @param totalAudioLen 整个音频PCM数据大小
     * @param totalDataLen  整个数据大小
     * @param sampleRate    采样率
     * @param channels      声道数
     * @param byteRate      采样字节byte率
     * @throws IOException
     */
    private static void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,long totalDataLen, int sampleRate, int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (channels * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }



}





