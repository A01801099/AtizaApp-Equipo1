package mx.aro.atizaapp_equipo1.utils;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import mx.aro.atizaapp_equipo1.R;
import mx.aro.atizaapp_equipo1.view.screens.MainActivity;

/**
 * Muestra una pantalla de bienvenida (splash screen) al iniciar la aplicación.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int DURACION_SPLASH = 250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, DURACION_SPLASH);

    }
}