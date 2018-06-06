package cat.detemporada.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import cat.detemporada.R;
import cat.detemporada.helpers.Utils;

/*
 *  Activitat "Splash" que s'obre al iniciar l'aplicació i serveix per inicialitzar la base de dades el primer
 *  cop després de l'instalació i executar de manera asíncrona el càlcul de temporalitat de les receptes.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            /*
             *  La primera vegada que s'executa l'aplicació cal copiar les dades del fitxer JSON a la base de dades local. Un cop
             *  fet això emmagatzemem a les SharedPreferences que la base de dades ha estat inicialitzada per evitar fer-ho
             *  cada cop que s'obri l'aplicació
             */
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            boolean BBDDInicialitzada = sharedPref.getBoolean(getString(R.string.sharedpref_bbdd), false);
            if (!BBDDInicialitzada) {
                Utils.flushBBDD();
                Utils.llegeixJSON(getResources().openRawResource(R.raw.receptes));
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.sharedpref_bbdd), true);
                editor.apply();
            }

            new Utils.calculaTemporalitatAsincron().execute();
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        } catch (IOException e) {
            String TAG = "SplashActivity";
            Log.e(TAG, e.getLocalizedMessage());
            finish();
        }
    }
}
