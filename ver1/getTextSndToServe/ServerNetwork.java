package com.example.eunsong.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class ServerNetwork extends AsyncTask<String, Void, String> {
    ProgressDialog progressDialog;

    private static final int BUFFER_SIZE = 1 * 1024 * 1024;
    public static String IP_ADDRESS = "http://ec2-13-209-47-170.ap-northeast-2.compute.amazonaws.com/prac2.php"; //서버주소입력
    static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static String TAG = "joassprac";
    int TIMEOUT = 100000;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d(TAG, "POST response  - " + result);
    }

    @Override
    protected String doInBackground(String... param) {
        try {
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";

            String filename = param[0];
            Log.d("Filename: ",filename);

            String fullFilename = FILE_PATH + "/" +filename;
            Log.d("full Filename: ",filename);


            //InputStream wavfile = getResources().openRawResource(R.raw.sound);
            File sourcefile = new File(fullFilename );
            FileInputStream wavfile = new FileInputStream(sourcefile);

            int available = wavfile.available();
            Log.d("size: ", Integer.toString(available));

            URL url = new URL(IP_ADDRESS);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            Log.d("connect", "connect "+url);

            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setReadTimeout(TIMEOUT);
            httpURLConnection.setConnectTimeout(TIMEOUT);
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            //httpURLConnection.setRequestProperty("Content-Type", "audio/wav");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            httpURLConnection.connect();


            //write data
            DataOutputStream outputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + filename+lineEnd);
            outputStream.writeBytes(lineEnd);

            // Log.d("outputStream size: ", Integer.toString(outputStream.));

            int bytesAvailable = wavfile.available();
            int maxBufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            byte[] buffer = new byte[bufferSize];
            int length;
            while((length = wavfile.read(buffer)) != -1){
                outputStream.write(buffer, 0, length);
            }

            Log.d("outputStream", "adsfasdf");
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens+boundary+twoHyphens+lineEnd);


            outputStream.flush();
            outputStream.close();
            wavfile.close();


            int responseStatusCode = httpURLConnection.getResponseCode();
            Log.d(TAG, "POST response code - " + responseStatusCode);

            InputStream rinputStream;
            if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                rinputStream = httpURLConnection.getInputStream();
                Log.d(TAG, "POST response code - inputstream");
            }
            else{
                rinputStream = httpURLConnection.getErrorStream();
            }

            InputStreamReader inputStreamReader = new InputStreamReader(rinputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line = null;

            while((line = bufferedReader.readLine()) != null){
                sb.append(line);
            }
            bufferedReader.close();
            return sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "InputStream: Error!!!! ", e);
            return new String("Error: " + e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, "InsertData: Error!!!! ", e);
            return new String("Error: " + e.getMessage());
        }
    }
}
