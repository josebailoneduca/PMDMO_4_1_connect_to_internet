package com.imagenprogramada.connecttotheinternet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.imagenprogramada.connecttotheinternet.databinding.ActivityMainBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnIr.setOnClickListener(v -> {
            ocultarTeclado();
            descargar(binding.inputUrl.getText().toString());
        });
    }


    /**
     * Comprueba y lanza la descarga de la url
     * @param textoUrl La url a descargar
     */
    private void descargar(String textoUrl) {
        //comprobar url no vacia
        if (!valida(textoUrl))
            return;

        //comprobar que hay internet
        if (!comprobarRed()) {
            mostrarTextoError();
            return;
        }

        //detectar si la URL es imagen
        boolean esImagen = esImagen(textoUrl);

        //vaciar caja de texto de salida
        binding.viewTexto.setText("");

        //mostrar spinner de cargando
        binding.progressBar.setVisibility(View.VISIBLE);

        //preparar executor para iniciar hilo
        ExecutorService executor = Executors.newSingleThreadExecutor();
        //handler para ejecutar resultado en hilo principal de la activity
        Handler handler = new Handler(Looper.getMainLooper());

        //ejecutar la descarga en otro hilo
        executor.execute(() -> {
            Runnable callback = null;
            try {
                //abrir la conexion
                URL url = new URL(textoUrl);
                URLConnection urlConnection = (HttpURLConnection) url.openConnection();
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                InputStream is = urlConnection.getInputStream();

                //si es imagen convierte directamente el stream de la conexion en un Bitmap
                if (esImagen) {
                    Bitmap bmp = BitmapFactory.decodeStream(is);
                    try {
                        is.close();
                    } catch (IOException ex) {
                        mostrarTextoError();
                    }
                    //prepara el callback a ejecutar por el handler
                    callback = () -> {
                        mostrarImagen(bmp);
                    };

                //en otro caso lee el stream entero y una vez leido lo convierte a String
                } else {
                    int i = is.read();
                    while (i != -1) {
                        bo.write(i);
                        i = is.read();
                    }
                    //prepara el callback a ejecutar por el handler
                    callback = () -> {
                        mostrarTexto(bo.toString());
                    };
                }

            //si hay algun error el callback para el handler es mostar el mensaje de error
            } catch (Exception e) {
                callback = () -> {
                    mostrarTextoError();
                };
            }
            handler.post(callback);
        });

    }

    /**
     * Comprueba la red retornando si hay o no conexion a internet
     *
     * @return True hay conexion. False no hay
     */
    private boolean comprobarRed() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network currentNetwork = connectivityManager.getActiveNetwork();
        NetworkCapabilities capacidades = connectivityManager.getNetworkCapabilities(currentNetwork);
        return (capacidades != null && capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET));
    }

    /**
     * Devuelve si la url se puede interpretar como de imagen
     * @param url Url a comprobar
     * @return True es imagen, False no.
     */
    private boolean esImagen(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type != null && type.startsWith("image");
    }


    /**
     * Devuelve si el texto de la caja de entrada es un texto valido. Tiene al menos 1 caracter y no es nulo
     * @param text El texto
     * @return True es valido, false no lo es
     */
    private boolean valida(CharSequence text) {
        if (text == null || text.length() < 1) {
            Toast.makeText(this, "Escribe una url", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * Muestra el mensaje de error
     */
    private void mostrarTextoError() {
        mostrarTexto("No se ha podido descargar la ruta. Compruebe la ruta y su conexión a internet");
    }

    /**
     * Muestra la caja de texto de salida
     * @param s El texto a mostrar
     */
    private void mostrarTexto(String s) {
        binding.progressBar.setVisibility(View.INVISIBLE);
        binding.imageView.setVisibility(View.INVISIBLE);
        binding.viewTexto.setVisibility(View.VISIBLE);
        binding.viewTexto.setText(s);
    }

    /**
     * Muestra un bitmap
     * @param bmp El bitmap a mostrar
     */
    private void mostrarImagen(Bitmap bmp) {
        binding.progressBar.setVisibility(View.INVISIBLE);
        binding.viewTexto.setVisibility(View.INVISIBLE);
        binding.imageView.setVisibility(View.VISIBLE);
        binding.imageView.setImageBitmap(bmp);
    }

    /**
     * Fuerza a que se oculte el teclado
     */
    private void ocultarTeclado(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}