package com.example.ibmtexttospeech;

//import android.app.Activity;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.text_to_speech.v1.model.SynthesizeOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.ibm.watson.text_to_speech.v1.util.WaveUtils.reWriteWaveHeader;

public class MainActivity extends AppCompatActivity
{
    private static final String API_KEY = "";
    private static final String URL = "";
    private static  final int BUFFER_SIZE = 1024;

    private EditText textEditText;
    private Button textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        textEditText = textEditText.findViewById(R.id.ettext);

        textToSpeech = textToSpeech.findViewById(R.id.btnaction);
        textToSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String text = textEditText.getText().toString();
                        if(text.length() > 0)
                        {
                            String voice = "en-GB_JamesV3Voice";
                            try {
                                createSoundFile(text, voice);
                                playSoundFile(text + voice);
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                thread.start();
            }
        });
    }

    public void createSoundFile(String text, String voice) throws IOException {
        com.ibm.cloud.sdk.core.security.IamAuthenticator authenticator = new IamAuthenticator(API_KEY);
        com.ibm.watson.text_to_speech.v1.TextToSpeech textToSpeech = new TextToSpeech(authenticator);
        textToSpeech.setServiceUrl(URL);

        SynthesizeOptions synthesizeOptions = new SynthesizeOptions.Builder()
                .text(text)
                .accept("audio/mp3")
                .voice(voice)
                .build();

        InputStream inputStream = textToSpeech.synthesize(synthesizeOptions).execute().getResult();
        InputStream in = reWriteWaveHeader(inputStream);

        String fileName = text + voice;
        FileOutputStream fos = getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);

        byte[] buffer = new byte[BUFFER_SIZE];

        int length;
        while ((length = in.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }

        fos.close();

        in.close();
        inputStream.close();
    }

    public void playSoundFile(String fileName) throws IOException {
        File file = new File(getApplicationContext().getFilesDir(), fileName);
        Uri fileUri = Uri.parse(file.getPath());
        MediaPlayer mediaPlayer = new MediaPlayer();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                            .build()
            );
        }
        else
        {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        mediaPlayer.setDataSource(getApplicationContext(), fileUri);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mediaPlayer.prepareAsync();
    }
}