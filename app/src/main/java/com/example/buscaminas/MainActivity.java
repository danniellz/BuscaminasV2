package com.example.buscaminas;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.transition.Explode;
import android.transition.Transition;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.IOException;
import java.net.URI;

public class MainActivity extends AppCompatActivity{
    //Atributos
    private ImageButton btnConfig, btnPlay, btnExit,btnVideo, bInfo;
    private EditText inputUsername;
    private VideoView videoView;
    private TextView version;
    private String nullValue, username, exitConfirm, mode ="hard", theme= "default", mute="notMute";
    private Intent intent;
    private ConstraintLayout mainPane;
    private AlertDialog.Builder alertDialog;
    private Uri uri;
    private  MediaPlayer mediaPlayer;
    private MediaController mediaController;
    //ID de los intents
    public static final int SETTINGS_ACTIVITY = 2;
    public static final int GAME_ACTIVITY = 3;
    public static final int VIDEO_ACTIVITY = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        mainPane = (ConstraintLayout) findViewById(R.id.mainPaneId);
        version = (TextView) findViewById(R.id.appVersion);
        btnConfig = (ImageButton) findViewById(R.id.configButtonId);
        btnPlay = (ImageButton) findViewById(R.id.playButtonId);
        btnExit = (ImageButton) findViewById(R.id.exitButtonId);
        btnVideo = (ImageButton) findViewById(R.id.videoId);
        inputUsername = (EditText) findViewById(R.id.inputUsernameId);
        videoView = (VideoView) findViewById(R.id.videoView2);
        bInfo = (ImageButton) findViewById(R.id.infoButtonId);

        nullValue = getString(R.string.text_null);
        exitConfirm = getString(R.string.text_exit_confirm);
         mediaPlayer = new MediaPlayer();
        bInfo.setOnClickListener(this::onClickVideo);
        //SETTINGS
        btnConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //transicion (se supone)
                Transition transition = new Explode();
                transition.setDuration(1000);
                transition.setInterpolator(new DecelerateInterpolator());
                getWindow().setExitTransition(transition);

                intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, SETTINGS_ACTIVITY);

                //startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());

            }
        });
        //PLAY
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = inputUsername.getText().toString();
                //si el username esta vacio, error
                if(username.isEmpty()){
                    emptyUsername();
                }else{
                    if(uri != null){
                        //Si se ha hecho un video
                        intent = new Intent(MainActivity.this, Game.class);
                        intent.putExtra("username", username);
                        intent.putExtra("mode", mode);
                        intent.putExtra("theme", theme);
                        intent.putExtra("mute", mute);
                        intent.setData(uri);
                        mediaPlayer.stop();
                        startActivityForResult(intent, GAME_ACTIVITY);
                    }else{
                        //si no esta vacio, pasar a ventana de juego pasandole el usuario
                        intent = new Intent(MainActivity.this, Game.class);
                        intent.putExtra("username", username);
                        intent.putExtra("mode", mode);
                        intent.putExtra("theme", theme);
                        intent.putExtra("mute", mute);
                        mediaPlayer.stop();
                        startActivityForResult(intent, GAME_ACTIVITY);
                    }
                }
            }
        });
        //EXIT
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exit();
            }
        });
        //VIDEO
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecording(view);
            }
        });

        mainPane.setBackground(getDrawable(R.drawable.santa));
        version.setText("Version "+BuildConfig.VERSION_NAME);

    }

    private void sonidodeFondo() {
        try {
            mediaPlayer.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mainact_music));

            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(50, 50);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //para mostrar el Video tutorial en el dialog
    private void onClickVideo(View view) {

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.videoalert);
        dialog.show();


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        lp.copyFrom(dialog.getWindow().getAttributes());
        dialog.getWindow().setAttributes(lp);

        VideoView videoview = (VideoView) dialog.findViewById(R.id.videoview);

       //Aparece detras del Video por el Dialog
        /*mediaController = new MediaController(this);
        videoview.setMediaController(mediaController);
        mediaController.setMediaPlayer(videoview);
        mediaController.setAnchorView(videoView);
        mediaController.show();*/


        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videotutorial);
        videoview.setVideoURI(uri);
        videoview.start();
    }
    public void emptyUsername(){
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(nullValue);
        alertDialog.setPositiveButton(android.R.string.ok, null);
        alertDialog.show();
    }
    public void exit(){
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(exitConfirm);
        alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finishAffinity();
                System.exit(0);
            }
        });
        alertDialog.setNegativeButton(android.R.string.no, null);
        alertDialog.show();
    }

    public void startRecording(View view){
        // Creación del intent
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // El vídeo se grabará en calidad baja (0)
        videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        // Limitamos la duración de la grabación a 5 segundos
        videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
        // Nos aseguramos de que haya una aplicación que pueda manejar el intent
        if (videoIntent.resolveActivity(getPackageManager()) != null) {
            // Lanzamos el intent
            startActivityForResult(videoIntent, VIDEO_ACTIVITY);
        }
    }

    @SuppressLint("WrongConstant")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //de que activity lo recibe y el resultado que obtiene
        if(requestCode == SETTINGS_ACTIVITY){
            //si obtiene un resultado, se ha ejecutado con exito
            if(resultCode == RESULT_OK){
                theme = data.getStringExtra("theme");
                mode = data.getStringExtra("mode");
                mute = data.getStringExtra("mute");


                if(theme.equals("the3kings")){
                    mainPane.setBackground(getDrawable(R.drawable.reyes_magos));
                }
                if(theme.equals("santa")){
                    mainPane.setBackground(getDrawable(R.drawable.santa_claus));
                }else{
                    mainPane.setBackground(getDrawable(R.drawable.santa));
                }
                if (mute.equals("mute")) {
                    mediaPlayer.stop();
                }
                if (mute.equals("notMute")) {
                    sonidodeFondo();
                }

            }else{
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage("Ha ocurrido un error");
                alertDialog.setPositiveButton(android.R.string.ok, null);
                alertDialog.show();
            }

        }

        if (requestCode == VIDEO_ACTIVITY && resultCode == RESULT_OK) {
            uri = data.getData();
            videoView.setVisibility(0);
            videoView.setVideoURI(data.getData());
            videoView.start();

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.setLooping(true);
                }
            });

        }
    }

}