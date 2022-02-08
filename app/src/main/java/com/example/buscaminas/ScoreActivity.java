package com.example.buscaminas;

import static android.text.TextUtils.concat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScoreActivity extends AppCompatActivity implements View.OnClickListener {
    private String username, theme;
    private ConstraintLayout consScor;
    private ImageButton papaLoc, photoBtn;
    private int score;
    private String ruta = null;

    //BD
    SQLiteDatabase database;

    //Musica de Fondo
    private MediaPlayer musica = new MediaPlayer();

    @SuppressLint({"ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        getSupportActionBar().hide();

        Bundle extras = getIntent().getExtras();

        if (ContextCompat.checkSelfPermission(ScoreActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ScoreActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ScoreActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1000);
        }

        try {
            musica.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/"+R.raw.win));
            musica.prepare();
            musica.start();
            musica.setLooping(true);
        } catch (IOException e) {
            e.printStackTrace();
        }


        username = extras.getString("username");
        score = extras.getInt("score");
        theme = extras.getString("theme");
        consScor = findViewById(R.id.consScor);
        papaLoc = (ImageButton) findViewById(R.id.papaLoc);
        photoBtn = (ImageButton) findViewById(R.id.photoBtn);
        photoBtn.setOnClickListener(this);


        papaLoc.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lat = "66.543";
                String lon = "25.847";
                Uri web = Uri.parse((String) concat("geo:"+lat+","+lon));
                Intent intent = new Intent(Intent.ACTION_VIEW,web);
                startActivity(intent);
            }
        } );

        if (theme.equals("the3kings")) {
            consScor.setBackground(getDrawable(R.drawable.reyes_magos));
        }
        if (theme.equals("santa")) {
            consScor.setBackground(getDrawable(R.drawable.santa_claus));
        } else {
            consScor.setBackground(getDrawable(R.drawable.santa));
        }
        //Titulo
        TextView titleTxt = findViewById(R.id.showFlagId);
        titleTxt.setText(getString(R.string.score));

        //Base de Datos
        database = openOrCreateDatabase("mydatabase", Context.MODE_PRIVATE, null);

        database.execSQL("CREATE TABLE IF NOT EXISTS scoretable(Username VARCHAR,Score INT,Photo VARCHAR);");

        database.execSQL("INSERT INTO scoretable VALUES(?,?,?);",new String[]{ username, String.valueOf(score), null});

        actualizarTabla();

        //Play Again Button
        Button playAgainBtn = findViewById(R.id.button);
        playAgainBtn.setText(getString(R.string.play_again));
        playAgainBtn.setOnClickListener(this);
    }

    static final int CAPTURA_IMAGEN = 1;
    @Override
    public void onClick(View v) {
            switch (v.getId()){
                case R.id.button:
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    musica.stop();
                    musica.release();
                    break;
                case R.id.photoBtn:
                    //Hecer la foto
                    Intent hacerFotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (hacerFotoIntent.resolveActivity(getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            Toast.makeText(getApplicationContext(), "Error while saving picture.", Toast.LENGTH_LONG).show();
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(ScoreActivity.this,
                                    "com.example.buscaminas.fileprovider",
                                    photoFile);
                            hacerFotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(hacerFotoIntent, CAPTURA_IMAGEN);
                        }
                        ruta = photoFile.getAbsolutePath();
                    }
                    break;
            }
    }

    private void actualizarTabla() {
        Cursor cursor = database.rawQuery("SELECT * FROM scoretable ORDER BY score", null);

        TableLayout table = findViewById(R.id.tableLayout);

        int count = table.getChildCount();
        for (int i = 1; i < count; i++) {
            View child = table.getChildAt(i);
            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
        }

        while (cursor.moveToNext()) {
            TableRow row = new TableRow(this);
            TextView position = new TextView(this);
            ImageView img = new ImageView(this);
            TextView username = new TextView(this);
            TextView score = new TextView(this);

            //Añadir posicion
            position.setText(String.valueOf(cursor.getPosition()+1));
            position.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            position.setTextSize(20);
            position.setBackground(getDrawable(R.drawable.border));
            position.setTypeface(null, Typeface.BOLD);
            position.setTextColor(Color.BLUE);

            //Añadir foto
            if(cursor.getString(2)!=null) {
                mostrarImagen(img, cursor.getString(2));
            } else {
                img.setImageResource(R.drawable.usuario);
            }
            img.setScaleType(ImageView.ScaleType.FIT_XY);
            img.setMaxHeight(75);
            img.setMaxWidth(75);
            img.setAdjustViewBounds(true);
            img.setBackground(getDrawable(R.drawable.border));

            //Añadir username
            username.setText(cursor.getString(0));
            username.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            username.setTextSize(20);
            username.setBackground(getDrawable(R.drawable.border));
            username.setTextColor(Color.BLUE);

            //Añadir score
            score.setText(String.valueOf(cursor.getInt(1)));
            score.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            score.setTextSize(20);
            score.setBackground(getDrawable(R.drawable.border));
            score.setTextColor(Color.BLUE);

            //Añadir a la fila
            row.addView(position);
            row.addView(img);
            row.addView(username);
            row.addView(score);
            table.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));

        }

        cursor.close();
    }

    //Metodo para crear nombre unico a cada imagen
    String fotoPath;
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        fotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    //Metodo para mostrar vista previa en el ImageButton
    static final int REQUEST_IMAGE_CAPTURE = 1;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ContentValues cv = new ContentValues();
            cv.put("photo",ruta);
            database.update("scoretable", cv, "username = ? AND score = ?", new String[]{username, String.valueOf(score)});
            actualizarTabla();
        }
    }

    private void mostrarImagen(ImageView img, String ruta) {
        File imgFile = new File(ruta);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(ruta);
            img.setImageBitmap(myBitmap);
        } else {
            img.setImageResource(R.drawable.usuario);
        }
    }

}
