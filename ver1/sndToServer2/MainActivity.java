package com.example.eunsong.sndtoserver2;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    //녹음기 선언
    public MediaPlayer player;
    public MediaRecorder recorder;

    //녹음한 사운드
    String FILE_NAME = "sound.wav";
    String RECORDED_FILE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FILE_NAME;

    //서버에서 받는 response
    InputStream responseInput;
    InputStreamReader responseInputStream;
    BufferedReader bufferedReader;
    StringBuilder stringBuilder;
    String line;
    public static String resultMessage;
    public static String responseMessage;

    public Button uploadButton, recordButton, playButton, saveButton;

    static String uploadServerUrl = "http://13.125.251.29/upload";
    public static HttpURLConnection conn = null;

    static String boundary = "*****";
    final String fileName = "sound.wav";

    //서버와 연결
    Network connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recordButton = (Button) findViewById(R.id.recordButton);
        saveButton = (Button) findViewById(R.id.saveButton);
        playButton = (Button) findViewById(R.id.playButton);
        uploadButton = (Button) findViewById(R.id.uploadButton);


        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connection = new Network();

                //파일이 있는지 없는지 확인
                File inputFile = new File(RECORDED_FILE);
                if (inputFile.exists() == true) {
                    Log.e("File_Name", FILE_NAME);

                    new Thread(new Runnable() {
                        // 결과값
                        String result = null;
                        String response = null;


                        @Override
                        public void run() {
                            try {

                                System.out.println("시작전");
                                //사바와 연결
                                //connection.setHttpURLConnection();
                                //서버와 url connection
                                // FileInputStream fileInputStream = new FileInputStream(sourceFile);
                                URL url = new URL(uploadServerUrl);

                                //open a HTTP connection to the URL
                                conn = (HttpURLConnection) url.openConnection();
                                conn.setDoInput(true); //Allow inputs
                                conn.setDoOutput(true); // Allow outputs
                                conn.setUseCaches(false); //Don't use a Caches Copy

                                conn.setRequestMethod("POST");
                                //conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                                conn.setRequestProperty("Connection", "Keep-Alive");
                                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                                conn.setRequestProperty("Cache-Control", "no-cache");
                                conn.setRequestProperty("uploaded_file", fileName);
                                conn.setReadTimeout(100 * 100 * 100);
                                conn.setConnectTimeout(100 * 100 * 100);

                                System.out.println("연결됐어~");


                                //서버에 파일 전송
                                response = connection.upload(FILE_NAME);
                                responseMessage = response;

                                responseInput = conn.getInputStream();
                                responseInputStream = new InputStreamReader(responseInput, "UTF-8");
                                bufferedReader = new BufferedReader(responseInputStream);
                                stringBuilder = new StringBuilder();
                                line = null;


                                //서버로부터 결과값 받아오기
                                while ((line = bufferedReader.readLine()) != null) {
                                    stringBuilder.append(line);
                                }
                                result = stringBuilder.toString();
                                String[] splitmesg = result.split(",");
                                resultMessage = splitmesg[1];


                                if (resultMessage != null) {
                                    Log.e("result", resultMessage);
                                    Bundle data = new Bundle();
                                    Message msg = new Message();
                                    data.putString("data", resultMessage);
                                    msg.setData(data);
                                    //handler.sendMessage(msg);

                                }
                                Intent intent = new Intent(getApplicationContext(), TTS.class);
                                startActivity(intent);

                                responseInputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.out.println("연결안됐어");
                                Log.e("error",e.toString());
                            }


                        }
                    }).start();


                } else {
                    Toast.makeText(getApplicationContext(), "전송할 파일이 없습니다.녹음을 먼저 해주세요",
                            Toast.LENGTH_SHORT).show();
                }

                inputFile.delete();

            }

        });


        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                    recorder = null;
                }

                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                recorder.setOutputFile(RECORDED_FILE);
                try {
                    Toast.makeText(getApplicationContext(),
                            "녹음을 시작합니다.", Toast.LENGTH_SHORT).show();
                    recorder.prepare();
                    recorder.start();
                } catch (Exception ex) {
                    Log.e("SampleAudioRecorder", "Exception : ", ex);
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recorder == null)
                    return;

                recorder.stop();
                recorder.release();
                recorder = null;

                Toast.makeText(getApplicationContext(),
                        "녹음 파일을 저장합니다", Toast.LENGTH_SHORT).show();
                // TODO Auto-generated method stub
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    playAudio(RECORDED_FILE);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "녹음파일 재생", Toast.LENGTH_SHORT).show();

            }
        });
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String stringMsg = msg.getData().getString("data");
           Log.e("handler message", stringMsg);
        }
    };

    private void playAudio(String url) throws Exception {
        killMediaPlayer();

        player = new MediaPlayer();
        player.setDataSource(url);
        player.prepare();
        player.start();
    }

    protected void onDestroy() {
        super.onDestroy();
        killMediaPlayer();
    }

    private void killMediaPlayer() {
        if (player != null) {
            try {
                player.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void onPause() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
        super.onPause();
    }
}
