package com.example.buscaminas;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.transition.Explode;

import java.io.IOException;

import pl.droidsonroids.gif.GifImageView;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private TextView modeTxt;
    private TextView themeTxt;
    private ConstraintLayout settingsPane;
    private ImageButton santaTheme;
    private ImageButton thekingsTheme;
    private ToggleButton prueba;
    private RadioGroup modeChoice;
    private RadioButton ezModeRbtn;
    private RadioButton hardModeRBtn;
    private Button saveSettings;
    private MediaPlayer mediaPlayer;
    private GifImageView mgifImageView;
    boolean checked;
    boolean choice;
    String mute;
    String mod;
    String theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().hide();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, Uri.parse("android.resource://"+ getPackageName()+ "/"+ R.raw.bell_sound));

            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(50,50);
        } catch (IOException e) {
            e.printStackTrace();
        }

        settingsPane = (ConstraintLayout) findViewById(R.id.settingsPaneId);

        //titulos de las tablas
        modeTxt = (TextView) findViewById(R.id.modeTxt);
        themeTxt = (TextView) findViewById(R.id.themeTxt);


        // RadioGroup
        modeChoice = (RadioGroup) findViewById(R.id.modeChoiceRaGr);
        modeChoice.setOnCheckedChangeListener(this);
        //RadioButtons
        ezModeRbtn = (RadioButton) findViewById(R.id.ezMode);
        hardModeRBtn = (RadioButton) findViewById(R.id.hardMode);
        hardModeRBtn.setChecked(true);
        checked = false;

        //ImgButton
        santaTheme = (ImageButton) findViewById(R.id.santaThemImBtn);
        santaTheme.setOnClickListener(this);
        thekingsTheme = (ImageButton) findViewById(R.id.kingsThemImBtn);
        thekingsTheme.setOnClickListener(this);
        //Boton
        saveSettings = (Button) findViewById(R.id.saveBtn);
        prueba = (ToggleButton) findViewById(R.id.pruebaFlip);
        prueba.setBackgroundResource(R.drawable.notmute);
        mute ="notMute";
        prueba.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked){
                    prueba.setBackgroundResource(R.drawable.mute);
                    mute= "mute";
                }else {
                    prueba.setBackgroundResource(R.drawable.notmute);
                    mute = "notMute";
                }
            }
        });
        saveSettings.setOnClickListener(this);
        choice = false;
        saveSettings.setEnabled(false);
        //android:backgroundTint="@color/red"

        settingsPane.setBackground(getDrawable(R.drawable.santa));

    }

    public void onClick(View v) {


        switch(v.getId()){
            case R.id.santaThemImBtn:
                theme = "santa";
                //settingsPane.setBackground(getDrawable(R.drawable.santa_claus));
                santaTheme.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue_light)));
                thekingsTheme.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.alpha)));
                choice = true;
                saveSettings.setEnabled(true);
                saveSettings.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));

                break;
            case R.id.kingsThemImBtn:
                theme =  "the3kings";
                //settingsPane.setBackground(getDrawable(R.drawable.reyes_magos));
                santaTheme.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.alpha)));
                thekingsTheme.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue_light)));

                choice = true;
                saveSettings.setEnabled(true);
                saveSettings.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));

                break;
            case R.id.saveBtn:
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.putExtra("mode", mod);
                intent.putExtra("theme",theme);
                intent.putExtra("mute",mute);
                setResult(RESULT_OK, intent);

                mediaPlayer.stop();
                finish();

                break;

        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
        switch (checkId) {
            case R.id.ezMode:
                mod = "easy";
                checked = true;
                break;
            case R.id.hardMode:
                mod = "hard";
                checked = true;
                break;
        }
    }
    @Override
    public void onBackPressed(){
        if(mediaPlayer.isPlaying() || mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            finish();

        }else{
            finish();
        }

    }
}