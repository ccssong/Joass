package com.example.eunsong.sndtoserver2;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Network {
    final String fileName = "sound.wav";
    InputStream sourceFile;
    //서버 주소
    //static String uploadServerUrl = "http://13.125.251.29/upload";
    //public static HttpURLConnection conn = null;
    public static DataOutputStream dos = null;

    static String attachmentName = "sound";
    static String lineEnd = "\r\n";
    static String twoHyphens = "--";
    static String boundary = "*****";

    //서버에서 보내는 responsecode
    int serverResponseCode = 0;


    static final String FILE_PATH = Environment.getExternalStorageDirectory().getPath();
    private static String TAG = "joassprac";




    public String upload(String param){
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        String message = null;

        try {
            String filename = param;
            Log.d("Filename: ", filename);

            String fullFilename = FILE_PATH + "/" + filename;
            Log.d("full Filename: ", filename);


            File sourcefile = new File(fullFilename);
            FileInputStream wavfile = new FileInputStream(sourcefile);

            int fileAvailable = wavfile.available();
            Log.d("size: ", Integer.toString(fileAvailable));


            if (fileAvailable == 0) {
                Log.e("uploadFile", " Source File not exist: ");
            } else {
                dos = new DataOutputStream(MainActivity.conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"" + attachmentName + "\";filename=\"" + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                //create a buffer of maximum size
                bytesAvailable = wavfile.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                //read file and write it into form...
                bytesRead = wavfile.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = wavfile.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = wavfile.read(buffer, 0, bufferSize);
                }

                //send multipart from data necessary after file data..
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


                //Response from the server(code and message);
                serverResponseCode = MainActivity.conn.getResponseCode();
                String serverResponseMessage = MainActivity.conn.getResponseMessage();

                Log.e("uploadFile", "HTTP Response is: " + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {
                    message = "File Upload Completed\n\n";

                }
                else{
                    message = "Something error\n\n";
                }


                //close the streams
                wavfile.close();
                dos.flush();
                dos.close();

            }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            message = "MalformedURLException error: check script url: " + e.getMessage();
            Log.e("Upload file to server", message, e);
            return(message);
        } catch (IOException e) {
            e.printStackTrace();
            message = "Got Exception : see logcat " + e.getMessage();
            Log.e("Upload file to server", message, e);
            return(message);
        }

        return(message);

    }

}
