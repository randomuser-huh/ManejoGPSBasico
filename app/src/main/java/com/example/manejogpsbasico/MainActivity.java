package com.example.manejogpsbasico;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.manejogpsbasico.R;

import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private TextView txtEstadoGPS, txtLatitud, txtLongitud, txtAltitud, txtVelocidad;
    private LocationManager locationManager;
    private static final int REQUEST_CODE_GPS_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtEstadoGPS = findViewById(R.id.txtEstadoGPS);
        txtLatitud = findViewById(R.id.txtLatitud);
        txtLongitud = findViewById(R.id.txtLongitud);
        txtAltitud = findViewById(R.id.txtAltitud);
        txtVelocidad = findViewById(R.id.txtVelocidad);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reanudamos las actualizaciones si tenemos los permisos
        verificarPermisos();
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
}
