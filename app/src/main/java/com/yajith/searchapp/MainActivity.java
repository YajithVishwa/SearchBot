package com.yajith.searchapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private EditText editText;
    private String searchtext,responseMessage,result,finaltext="";
    private Integer responseCode;
    private TextToSpeech textToSpeech;
    private TextView textView;
    private MaterialButton button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        editText=findViewById(R.id.text);
        button=findViewById(R.id.search);
        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });
        textView=findViewById(R.id.searchtext);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                searchtext=editText.getText().toString().trim().toLowerCase();
                if(searchtext.equals(""))
                {
                    Toast.makeText(MainActivity.this, "Search Text is Empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    Asyncs asyncs = new Asyncs();
                    searchtext=searchtext.replaceAll(" ","+");
                    String url="https://www.googleapis.com/customsearch/v1?q="+searchtext+"&key=AIzaSyDUkHycKC8v14QveA6pGasF9I82I6BCWRU"+"&cx=e768af4fbfa3e77d5"+"&alt=json";
                    URL url1=null;
                    try {
                        url1=new URL(url);
                    }
                    catch (MalformedURLException e)
                    {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                    }
                    asyncs.execute(url1);
                }
            }
        });
    }
    class Asyncs extends AsyncTask<URL,String,String>
    {
        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                responseCode = conn.getResponseCode();
                responseMessage = conn.getResponseMessage();
            } catch (IOException e) {
               e.printStackTrace();

            }
            try {

                if(responseCode != null && responseCode == 200) {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line=null;
                    while ((line = rd.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    rd.close();
                    conn.disconnect();
                    result = sb.toString();
                    Log.d("Main", "result=" + result);
                    return result;

                }else{
                     Log.e("Main", "Http Error");
                    result = "Http Error";
                    return  result;

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            textView.setMovementMethod(new ScrollingMovementMethod());
            try {
                textToSpeech.speak("This came back from search",TextToSpeech.QUEUE_FLUSH,null);
                JSONObject jsonObject=new JSONObject(s);
                JSONArray jsonArray=jsonObject.getJSONArray("items");
                JSONObject jsonObject1=jsonArray.getJSONObject(0);
                finaltext+="Title- "+jsonObject1.getString("title")+"\n";
                finaltext+="Link- "+jsonObject1.getString("link")+"\n";
                finaltext+="Description- "+jsonObject1.getString("snippet")+"\n";
                progressDialog.dismiss();
                textView.setText(finaltext);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        if(textToSpeech!=null)
        {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onPause();
    }
}