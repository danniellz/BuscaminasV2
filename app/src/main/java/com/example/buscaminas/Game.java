package com.example.buscaminas;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.util.Random;

public class Game extends AppCompatActivity implements MediaPlayer.OnCompletionListener {
    //ID Activities
    public static final int SCORE_ACTIVITY = 3;

    //Atributos
    private String username, mode, theme, mine, mute;
    private int cont, contTime, cellType, mineNum, row, column, timeStart, score;
    private TextView usernameText, flagText, timeText, scoreText, rate;
    private Button btnFlag;
    private ImageButton cell, restart;
    private GridLayout grid, parent;
    private ConstraintLayout consGame;
    private Handler handler;
    private Runnable runnable;
    private ImageButton[][] cells;
    private VideoView videoView;
    private Random rnd;
    private boolean gOver;
    private Uri uri;
    private ProgressBar timeLimitBar;

    //Musica
    private MediaPlayer musica = new MediaPlayer();
    //Efectos
    private MediaPlayer efectos = new MediaPlayer();

    //CODIGO
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        getSupportActionBar().hide();
        gOver = false;

        //Tiempo inicial barra de limite
        timeStart = 5000;
        contTime = 5;

        //Recoger el usuario del MainActivity
        Bundle extras = getIntent().getExtras();
        username = extras.getString("username");
        mode = extras.getString("mode");
        theme = extras.getString("theme");
        mute = extras.getString("mute");
        uri = getIntent().getData();

        //IDs
        usernameText = findViewById(R.id.showUsernameId);
        scoreText = findViewById(R.id.showScoreId);
        flagText = findViewById(R.id.showFlagId);
        timeText = findViewById(R.id.time);
        rate = findViewById(R.id.rateId);
        restart = findViewById(R.id.restart);
        btnFlag = findViewById(R.id.button2);
        grid = findViewById(R.id.grid);
        consGame = findViewById(R.id.consGame);
        videoView = (VideoView) findViewById(R.id.videoViewId);
        timeLimitBar = findViewById(R.id.timeLimitBar);

        usernameText.setText(username);
        mine = getString(R.string.mine);
        mineNum = 0;
        cells = new ImageButton[9][9];
        efectos.setOnCompletionListener(this);

        //MUSICA de FONDO
        if(mute.equals("mute")){
           musicStop();
        }else {
            musicaFondo();
        }
        //SI HAY UN VIDEO
        if(uri != null){
            videoView.setVisibility(0);
            videoView.setVideoURI(uri);
            videoView.start();

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.setLooping(true);
                }
            });

            //Se convierte en el boton de reinicio
            videoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(gOver){
                        recreate();
                    }else{
                        restartGame();
                    }
                }
            });
        }


        //Temas
        if (theme.equals("the3kings")) {
            consGame.setBackground(getDrawable(R.drawable.reyes_magos));
        }
        if (theme.equals("santa")) {
            consGame.setBackground(getDrawable(R.drawable.santa_claus));
        } else {
            consGame.setBackground(getDrawable(R.drawable.santa));

        }


        restart.setBackground(getDrawable(R.drawable.rudolph));

        /********************************************Contador de tiempo*************************************************/
        timeLimitBar.setMax(timeStart);
        timeLimitBar.setProgress(timeStart);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                //mostrar tiempo en pantalla
                timeText.setText(String.valueOf(contTime));
                scoreText.setText(String.valueOf(score));
                timeLimitBar.setProgress(timeStart);

                timeStart = timeStart - 1000;
                contTime--;

                //barra


                //cada vez que el contador llega a 0 quita puntos si los tiene
                if (contTime < 0) {
                    if (score >= 0) {
                        score = score - 5000;
                        timeStart = 3000;
                        contTime = 3;
                    }
                }

                //Si el score es menor a 0, GAMEOVER
                if (score < 0) {
                    gameOver();
                } else {
                    handler.postDelayed(runnable, 1000);
                }

            }
        };

        // Iniciar bucle de ejecucion
        handler.postDelayed(runnable, 1000);





        /********************************************Contador de tiempo*************************************************/

        //**Botones**
        //Reiniciar
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gOver){
                    recreate();
                }else{
                    restartGame();
                }
            }
        });

        //Bandera
        btnFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnFlag.getText().equals(getString(R.string.btnDesactivado))) {
                    btnFlag.setText(getString(R.string.btnActivado));

                } else {
                    btnFlag.setText(getString(R.string.btnDesactivado));
                }
            }
        });

        //Celdas
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cell = new ImageButton(this);
                rnd = new Random();
                cellType = rnd.nextInt(5);
                cell.setId(View.generateViewId());

                //Segun Dificultad
                if (mode.equals("hard")) {
                    if (cellType == 1 && mineNum != 10) {
                        cell.setTag(mine);

                        mineNum++;
                    } else {
                        cell.setTag(getString(R.string.cell));
                    }
                } else {
                    if (cellType == 1 && mineNum != 6) {
                        cell.setTag(mine);

                        mineNum++;
                    } else {
                        cell.setTag(getString(R.string.cell));
                    }
                }

                cells[i][j] = cell;

                //Fondo de la casilla
                cell.setBackgroundResource(R.drawable.casi_llena);

                //Al hacer click en alguna celda
                cell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cont = Integer.parseInt(flagText.getText().toString());
                        cell = (ImageButton) view;
                        Drawable.ConstantState cellState = cell.getBackground().getConstantState();
                        if (btnFlag.getText().equals(getString(R.string.btnActivado))) {
                            if (cellState != getResources().getDrawable(R.drawable.casi_vacia).getConstantState()) {
                                if (cellState == getResources().getDrawable(R.drawable.casi_llena).getConstantState()) {
                                    if (cont != 0) {
                                        cont--;
                                        flagText.setText(String.valueOf(cont));
                                        cell.setBackgroundResource(R.drawable.candy_flag);
                                    }
                                } else if (cellState == getResources().getDrawable(R.drawable.candy_flag).getConstantState()) {
                                    cell.setBackgroundResource(R.drawable.casi_llena);
                                    cont++;
                                    flagText.setText(String.valueOf(cont));
                                }
                            }
                        } else {
                            if (cellState != getResources().getDrawable(R.drawable.candy_flag).getConstantState()) {
                                if (!cell.getTag().equals(mine)) {
                                    parent = (GridLayout) cell.getParent();
                                    row = parent.indexOfChild(cell) / parent.getColumnCount();
                                    column = parent.indexOfChild(cell) % parent.getColumnCount();
                                    showCell(row, column);
                                } else {
                                    //GAME OVER
                                    gameOver();
                                }
                            }
                        }
                        if (!compGanar(cells)) {
                            //HA GANADO
                            handler.removeCallbacks(runnable);
                            Toast.makeText(getApplicationContext(), R.string.Win, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Game.this, ScoreActivity.class);
                            intent.putExtra("username", username);
                            intent.putExtra("score", score);
                            intent.putExtra("theme", theme);
                            startActivityForResult(intent, SCORE_ACTIVITY);
                            freeMedia();

                        }
                    }
                });

                grid.addView(cell);

            }
        }

        setMineCount();

    }

    private void showMines() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cells[i][j].getTag().equals(getString(R.string.mine))) {
                    cells[i][j].setBackgroundResource(R.drawable.carbon);
                }
            }
        }
    }

    private void showAdjacentCells(int row, int column) {

        if (!cells[row][column].getTag().equals(getString(R.string.mine))) {
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    if ((0 <= i + row && i + row < 9) && (0 <= j + column && j + column < 9) && (i != 0 || j != 0)) {
                        Log.d("MainActivity", String.valueOf(row + i) + "," + String.valueOf(column + j));
                        if (cells[row + i][column + j].getBackground().getConstantState() != getResources().getDrawable(R.drawable.casi_vacia).getConstantState()) {
                            showCell(row + i, column + j);

                        }
                    }
                }
            }
        }
    }

    //Mostrar lo que hay dentro de la celda
    public void showCell(int row, int column) {
        switch (cells[row][column].getTag().toString()) {
            case "0":
                cells[row][column].setBackgroundResource(R.drawable.casi_vacia);
                showAdjacentCells(row, column);
                break;
            case "1":
                cells[row][column].setBackgroundResource(R.drawable.choc_1);
                //PUNTOS OTORGADOS EN UN MARGEN DE TIEMPO EN EL QUE FUE PRESIONADO
                if (contTime > 0 && contTime < 2) {
                    score = score + 100;
                    restart.setBackground(getDrawable(R.drawable.rudolph_bad));
                    rate.setText(getString(R.string.bad));
                }
                if (contTime > 2 && contTime < 4) {
                    score = score + 150;
                    restart.setBackground(getDrawable(R.drawable.rudolph_good));
                    rate.setText(getString(R.string.good));

                }
                if (contTime >= 4 && contTime <= 5) {
                    score = score + 200;
                    restart.setBackground(getDrawable(R.drawable.rudolph_s));
                    rate.setText(getString(R.string.sick));
                }
                timeStart = 5000;
                contTime = 5;
                break;
            case "2":
                cells[row][column].setBackgroundResource(R.drawable.choc_2);
                if (contTime > 0 && contTime < 2) {
                    score = score + 250;
                    restart.setBackground(getDrawable(R.drawable.rudolph_bad));
                    rate.setText(getString(R.string.bad));

                }
                if (contTime > 2 && contTime < 4) {
                    score = score + 300;
                    restart.setBackground(getDrawable(R.drawable.rudolph_good));
                    rate.setText(getString(R.string.good));
                }
                if (contTime >= 4 && contTime <= 5) {
                    score = score + 350;
                    restart.setBackground(getDrawable(R.drawable.rudolph_s));
                    rate.setText(getString(R.string.sick));
                }
                timeStart = 5000;
                contTime = 5;
                break;
            case "3":
                cells[row][column].setBackgroundResource(R.drawable.choc_3);
                if (contTime > 0 && contTime < 2) {
                    score = score + 400;
                    restart.setBackground(getDrawable(R.drawable.rudolph_bad));
                    rate.setText(getString(R.string.bad));
                }
                if (contTime > 2 && contTime < 4) {
                    score = score + 450;
                    restart.setBackground(getDrawable(R.drawable.rudolph_good));
                    rate.setText(getString(R.string.good));
                }
                if (contTime >= 4 && contTime <= 5) {
                    score = score + 500;
                    restart.setBackground(getDrawable(R.drawable.rudolph_s));
                    rate.setText(getString(R.string.sick));
                }
                timeStart = 5000;
                contTime = 5;
                break;
            case "4":
                cells[row][column].setBackgroundResource(R.drawable.choc_4);
                if (contTime > 0 && contTime < 2) {
                    score = score + 550;
                    restart.setBackground(getDrawable(R.drawable.rudolph_bad));
                    rate.setText(getString(R.string.bad));
                }
                if (contTime > 2 && contTime < 4) {
                    score = score + 600;
                    restart.setBackground(getDrawable(R.drawable.rudolph_good));
                    rate.setText(getString(R.string.good));
                }
                if (contTime >= 4 && contTime <= 5) {
                    score = score + 650;
                    restart.setBackground(getDrawable(R.drawable.rudolph_s));
                    rate.setText(getString(R.string.sick));
                }
                timeStart = 5000;
                contTime = 5;
                break;
            case "5":
                cells[row][column].setBackgroundResource(R.drawable.choc_5);
                if (contTime > 0 && contTime <= 2) {
                    score = score + 700;
                    restart.setBackground(getDrawable(R.drawable.rudolph_bad));
                    rate.setText(getString(R.string.bad));
                }
                if (contTime > 2 && contTime < 4) {
                    score = score + 750;
                    restart.setBackground(getDrawable(R.drawable.rudolph_good));
                    rate.setText(getString(R.string.good));
                }
                if (contTime >= 4 && contTime <= 5) {
                    score = score + 800;
                    restart.setBackground(getDrawable(R.drawable.rudolph_s));
                    rate.setText(getString(R.string.sick));
                }
                timeStart = 5000;
                contTime = 5;
                break;
            case "6":
                cells[row][column].setBackgroundResource(R.drawable.choc_6);
                if (contTime > 0 && contTime < 2) {
                    score = score + 850;
                    restart.setBackground(getDrawable(R.drawable.rudolph_bad));
                    rate.setText(getString(R.string.bad));
                }
                if (contTime > 2 && contTime < 4) {
                    score = score + 900;
                    restart.setBackground(getDrawable(R.drawable.rudolph_good));
                    rate.setText(getString(R.string.good));
                }
                if (contTime >= 4 && contTime <= 5) {
                    score = score + 950;
                    restart.setBackground(getDrawable(R.drawable.rudolph_s));
                    rate.setText(getString(R.string.sick));
                }
                timeStart = 5000;
                contTime = 5;
                break;
            case "7":
                cells[row][column].setBackgroundResource(R.drawable.choc_7);
                if (contTime > 0 && contTime < 2) {
                    score = score + 1000;
                    restart.setBackground(getDrawable(R.drawable.rudolph_bad));
                    rate.setText(getString(R.string.bad));
                }
                if (contTime > 2 && contTime < 4) {
                    score = score + 1050;
                    restart.setBackground(getDrawable(R.drawable.rudolph_good));
                    rate.setText(getString(R.string.good));
                }
                if (contTime >= 4 && contTime <= 5) {
                    score = score + 1100;
                    restart.setBackground(getDrawable(R.drawable.rudolph_s));
                    rate.setText(getString(R.string.sick));
                }
                timeStart = 5000;
                contTime = 5;
                break;
            case "8":
                cells[row][column].setBackgroundResource(R.drawable.choc_8);
                if (contTime > 0 && contTime < 2) {
                    score = score + 1150;
                    restart.setBackground(getDrawable(R.drawable.rudolph_bad));
                    rate.setText(getString(R.string.bad));
                }
                if (contTime > 2 && contTime < 4) {
                    score = score + 1200;
                    restart.setBackground(getDrawable(R.drawable.rudolph_good));
                    rate.setText(getString(R.string.good));
                }
                if (contTime >= 4 && contTime <= 5) {
                    score = score + 1250;
                    restart.setBackground(getDrawable(R.drawable.rudolph_s));
                    rate.setText(getString(R.string.sick));
                }
                timeStart = 5000;
                contTime = 5;
                break;

        }


    }

    //cambiar para el modo de juego
    public void setMineCount() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cells[i][j].getTag() != getString(R.string.mine)) {
                    int mineCont = 0;

                    if (i > 0 && j > 0 && cells[i - 1][j - 1].getTag() == mine) {
                        mineCont++;
                    }
                    if (i > 0 && cells[i - 1][j].getTag() == mine) {
                        mineCont++;
                    }
                    if (i > 0 && j < 8 && cells[i - 1][j + 1].getTag() == mine) {
                        mineCont++;
                    }
                    if (j > 0 && cells[i][j - 1].getTag() == mine) {
                        mineCont++;
                    }
                    if (j < 8 && cells[i][j + 1].getTag() == mine) {
                        mineCont++;
                    }
                    if (i < 8 && j > 0 && cells[i + 1][j - 1].getTag() == mine) {
                        mineCont++;
                    }
                    if (i < 8 && cells[i + 1][j].getTag() == mine) {
                        mineCont++;
                    }
                    if (i < 8 && j < 8 && cells[i + 1][j + 1].getTag() == mine) {
                        mineCont++;
                    }
                    cells[i][j].setTag(String.valueOf(mineCont));

                }
            }
        }
    }

    //METODOS DEL JUEGO
    //Ganar
    public Boolean compGanar(ImageButton[][] cells) {
        Boolean seguJugando = false;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Drawable.ConstantState cellState = cells[i][j].getBackground().getConstantState();

                if (cellState == getResources().getDrawable(R.drawable.casi_llena).getConstantState()) {
                    if (!cells[i][j].getTag().equals(getString(R.string.mine))) {
                        seguJugando = true;
                    }
                }

            }
        }
        return seguJugando;
    }

    //Efecto de sonido al perder
    public void gameOverEfect() {
        try {
            efectos.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ho_ho_ho));
            efectos.prepare();
            efectos.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Musica de fondo
    public void musicaFondo() {
        try {
            musica.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.epic_christmas_game));
            musica.prepare();
            musica.start();
            musica.setLooping(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Al presionar el boton atras del movil
    @Override
    public void onBackPressed() {
        if (musica != null && musica.isPlaying()) {
            musica.stop();
            musica.release();
            handler.removeCallbacks(runnable);
            finish();
        } else {
            handler.removeCallbacks(runnable);
            finish();
        }
    }

    //
    public void musicStop() {
        if (musica != null && musica.isPlaying()) {
            musica.stop();
            efectos.stop();
        }
    }

    public void freeMedia() {
        if (musica != null && musica.isPlaying()) {
            musica.stop();
            musica.release();
        }
    }

    public void restartGame() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (musica != null && musica.isPlaying()) {
                musica.stop();
                try {
                    efectos.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bell_ring));
                    efectos.prepare();
                    efectos.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                handler.removeCallbacks(runnable);
                recreate();
            } else {
                recreate();
            }
        }
    }

    public void hintEffect() {
        try {
            efectos.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.hint));
            efectos.prepare();
            efectos.start();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void gameOver() {
        gOver = true;
        //Bloquear pantalla, no dejar que el jugador pueda interaccionar con nada
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        //mostrar todas las minas
        showMines();
        restart.setBackground(getDrawable(R.drawable.rudolph_over));
        Toast.makeText(getApplicationContext(), R.string.lose, Toast.LENGTH_SHORT).show();
        //detener la musica y reproducir un efecto de sonido al perder
        freeMedia();
        gameOverEfect();
        //parar tiempo
        handler.removeCallbacks(runnable);
        //Esperar x segundos y Regresar a la pantalla principal
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 3000);

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        efectos.release();
    }

}
