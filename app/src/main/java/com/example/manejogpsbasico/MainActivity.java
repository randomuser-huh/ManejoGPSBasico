package com.example.manejogpsbasico;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.manejogpsbasico.R;

import java.util.List;

// NUEVO
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.graphics.Color;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private TextView txtEstadoGPS, txtLatitud, txtLongitud, txtAltitud, txtVelocidad;
    private LocationManager locationManager;
    private static final int REQUEST_CODE_GPS_PERMISSION = 100;

    // NUEVO
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private float luzActual = 0;
    private View layoutPrincipal; // Para poner la pantalla roja si te quemas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtEstadoGPS = findViewById(R.id.txtEstadoGPS);
        txtLatitud = findViewById(R.id.txtLatitud);
        txtLongitud = findViewById(R.id.txtLongitud);
        txtAltitud = findViewById(R.id.txtAltitud);
        txtVelocidad = findViewById(R.id.txtVelocidad);

        // NUEVO
        layoutPrincipal = findViewById(R.id.layoutMain);

        //NUEVO
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Comprobamos los permisos
        verificarPermisos();
    }

    private void verificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si el permiso no ha sido concedido, lo solicitamos
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_GPS_PERMISSION);
        } else {
            // Si ya tenemos el permiso, iniciamos las actualizaciones de ubicación
            iniciarActualizacionesDeUbicacion();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_GPS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El usuario concedió el permiso
                iniciarActualizacionesDeUbicacion();
            } else {
                // El usuario denegó el permiso
                txtEstadoGPS.setText("Estado: Permiso de GPS denegado.");
                Toast.makeText(this, "La función de GPS no puede usarse sin el permiso.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void iniciarActualizacionesDeUbicacion() {
        // Comprobamos de nuevo el permiso por si acaso.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; // No debería ocurrir, pero es una buena práctica de seguridad
        }

        // Comprobamos si el proveedor de GPS está habilitado
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            txtEstadoGPS.setText("Estado: Por favor, activa el GPS.");
            Toast.makeText(this, "El GPS está desactivado.", Toast.LENGTH_LONG).show();
            return;
        }

        txtEstadoGPS.setText("Estado: Buscando señal GPS...");
        // Solicitamos actualizaciones de ubicación del proveedor de GPS.
        // Parámetros: proveedor, tiempo mínimo entre actualizaciones (ms), distancia mínima (m), listener
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Detenemos las actualizaciones para ahorrar batería cuando la app no está visible
        locationManager.removeUpdates(this);

        // NUEVO
        sensorManager.unregisterListener(lightEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reanudamos las actualizaciones si tenemos los permisos
        verificarPermisos();

        // NUEVO
        if (lightSensor != null) {
            sensorManager.registerListener(lightEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    // --- Métodos del LocationListener ---

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Este método se llama cada vez que el GPS recibe una nueva ubicación
        txtEstadoGPS.setText("Estado: Ubicación encontrada.");

        double latitud = location.getLatitude();
        double longitud = location.getLongitude();
        double altitud = location.getAltitude();
        float velocidad = location.getSpeed(); // en metros/segundo

        // Convertimos la velocidad a km/h
        float velocidadKmh = velocidad * 3.6f;

        // NUEVO
        if (velocidadKmh > 2.0 && luzActual > 2000) { // CAMBIAR PARA MODO DEMO
            txtEstadoGPS.setText("¡TE ESTÁS QUEMANDO! 🔥 BUSCA SOMBRA");
            txtEstadoGPS.setTextColor(Color.RED);
            // layoutPrincipal.setBackgroundColor(Color.parseColor("#FFDDDD")); // Efecto visual
        } else if (velocidadKmh > 2.0) {    // CAMBIAR PARA MODO DEMO
            txtEstadoGPS.setText("Corriendo seguro en las sombras... 🥷");
            txtEstadoGPS.setTextColor(Color.GREEN);
            // layoutPrincipal.setBackgroundColor(Color.BLACK);
        } else {
            txtEstadoGPS.setText("Parado. A salvo.");
            txtEstadoGPS.setTextColor(Color.BLACK);
            // layoutPrincipal.setBackgroundColor(Color.WHITE);
        }

        txtLatitud.setText(String.format("Latitud: %.6f", latitud));
        txtLongitud.setText(String.format("Longitud: %.6f", longitud));
        txtAltitud.setText(String.format("Altitud: %.2f m", altitud));
        txtVelocidad.setText(String.format("Velocidad: %.2f km/h", velocidadKmh));
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        // Se llama cuando el usuario activa el proveedor de GPS
        txtEstadoGPS.setText("Estado: GPS activado. Buscando señal...");
        iniciarActualizacionesDeUbicacion();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        // Se llama cuando el usuario desactiva el proveedor de GPS
        txtEstadoGPS.setText("Estado: GPS desactivado.");
    }

    // Este método es antiguo y no suele ser necesario implementarlo con detalle
    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    // NUEVO
    private final SensorEventListener lightEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            luzActual = event.values[0]; // Guardamos la luz actual
            // Actualizamos la interfaz para que se vea
            // Puedes reutilizar uno de los TextViews que no uses, o concatenarlo en "Estado"
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
}
