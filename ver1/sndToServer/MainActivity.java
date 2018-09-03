package com.example.eunsong.sndtoserver;


import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {
    MediaPlayer player;
    MediaRecorder recorder;
    int playbackPosition = 0;
    String FILE_NAME = "sound.wav";
    String RECORDED_FILE = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+FILE_NAME;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button recordBtn = (Button) findViewById(R.id.recordBtn);
        Button recordStopBtn = (Button) findViewById(R.id.recordStopBtn);
        Button playBtn = (Button) findViewById(R.id.playBtn);
        Button sendToServer = (Button) findViewById(R.id.sendToServerBtn);
        final TextView responseView = (TextView)findViewById(R.id.responseTxt);

        sendToServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File inputFile = new File(RECORDED_FILE);

                //파일이 있는지 없는지 확인
                if (inputFile.exists() == true) {
                    Log.e("File_Name", FILE_NAME);
                    ServerNetwork send = new ServerNetwork();
                    String result = null;
                    try {
                        result = send.execute(FILE_NAME).get();
                        Toast.makeText(getApplicationContext(), "서버로 파일 보냄",
                                Toast.LENGTH_SHORT).show();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    responseView.setText(result);

                    //서버에 보낸 후 삭제
                    inputFile.delete();

                    //tts 화면으로 전환 getTextAndTts class로 이동
                    Intent intent = new Intent(getApplicationContext(), getTextAndTts.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(), "전송할 파일이 없습니다.녹음을 먼저 해주세요",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recorder != null){
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

        recordStopBtn.setOnClickListener(new View.OnClickListener() {
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

        playBtn.setOnClickListener(new View.OnClickListener() {
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
