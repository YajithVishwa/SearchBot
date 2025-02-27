package com.yajith.searchapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.lang.UCharacter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Scroller;
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
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private ProgressDialog progressDialog;
    private EditText editText;
    private String responseMessage,result,finaltext="",link="";
    private Integer responseCode;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private TextView textView;
    private ImageButton button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        editText=findViewById(R.id.text);
        speechRecognizer=SpeechRecognizer.createSpeechRecognizer(this);
        button=findViewById(R.id.search);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},200);
        textToSpeech=new TextToSpeech(this,this);
        textView=findViewById(R.id.searchtext);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Need to speak");
                try {
                    startActivityForResult(intent,100);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case 100:if(resultCode==RESULT_OK)
            {
                ArrayList dat=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String a=dat.get(0).toString();
                if(a.contains("search"))
                {
                    String[] b=a.split("search");
                    if(b.length==0)
                    {
                        textToSpeech.speak("Sorry I cant get it",TextToSpeech.QUEUE_FLUSH,null);
                        return;
                    }
                    b[1]=b[1].trim();
                    editText.setText(b[1]);
                    Asyncs asyncs = new Asyncs();
                    if(a.contains(" ")) {
                        a = b[1].replaceAll(" ", "+");
                    }
                    String url="https://www.googleapis.com/customsearch/v1?q="+a+"&key=AIzaSyDUkHycKC8v14QveA6pGasF9I82I6BCWRU"+"&cx=e768af4fbfa3e77d5"+"&alt=json";
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
                else if(a.contains("open"))
                {
                    String[] b=a.split("open");
                    if(b.length==0)
                    {
                        textToSpeech.speak("Sorry I cant get it",TextToSpeech.QUEUE_FLUSH,null);
                        return;
                    }
                    b[1]=b[1].trim();
                    Intent intent=new Intent(Intent.ACTION_WEB_SEARCH);
                    if(link.equals(""))
                    {
                        textToSpeech.speak("Not Found",TextToSpeech.QUEUE_FLUSH,null);
                    }
                    else {
                        intent.putExtra(SearchManager.QUERY, link);
                    }
                    startActivity(intent);
                }
                else
                {
                    textToSpeech.speak("Sorry I cant get it",TextToSpeech.QUEUE_FLUSH,null);
                    return;
                }
            //    editText.setText(dat.get(0).toString());
            }
        }
    }

    @Override
    public void onInit(int i) {
        if(i==TextToSpeech.SUCCESS)
        {
            textToSpeech.setLanguage(Locale.ENGLISH);
            textToSpeech.setPitch(1);
            textToSpeech.setSpeechRate(1);
        }
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
                textView.setText("");
                JSONObject jsonObject=new JSONObject(s);
                JSONArray jsonArray=jsonObject.getJSONArray("items");
                JSONObject jsonObject1=jsonArray.getJSONObject(0);
                finaltext="Title- "+jsonObject1.getString("title")+"\n";
                link=jsonObject1.getString("link");
                finaltext+="Link- "+jsonObject1.getString("link")+"\n";
                finaltext+="Description- "+jsonObject1.getString("snippet")+"\n";
                textToSpeech.speak("Title-"+jsonObject1.getString("title"),TextToSpeech.QUEUE_FLUSH,null);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                textToSpeech.speak("Link-"+jsonObject1.getString("link"),TextToSpeech.QUEUE_FLUSH,null);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                textToSpeech.speak("Description-"+jsonObject1.getString("snippet"),TextToSpeech.QUEUE_FLUSH,null);
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                textView.setText(finaltext);
            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
                textToSpeech.speak("No results Found",TextToSpeech.QUEUE_FLUSH,null);
                textView.setText("No Result Found");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(ContextCompat.checkSelfPermission(this,permissions[0])== PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }
        else
        {
            ActivityCompat.requestPermissions(this,permissions,200);
        }
    }

    @Override
    protected void onDestroy() {
        if(textToSpeech!=null)
        {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}