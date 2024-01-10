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
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnIr.setOnClickListener(v -> {
            iniciaDescarga(binding.inputUrl.getText().toString());
        });
    }

    private void iniciaDescarga(String text) {
        if (!valida(text))
            return;

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        Network currentNetwork = connectivityManager.getActiveNetwork();
        NetworkCapabilities capacidades = connectivityManager.getNetworkCapabilities(currentNetwork);
        if (capacidades!=null && capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            descargar(text);
        } else {
            mostrarTextoError();

        }

    }

    private void mostrarTextoError() {
        mostrarTexto("No se ha podido descargar la ruta. Compruebe la ruta y su conexión a internet");
    }

    private void descargar(String textoUrl) {
        binding.viewTexto.setText("");
        binding.progressBar.setVisibility(View.VISIBLE);
        ExecutorService e = Executors.newSingleThreadExecutor();
        Handler h = new Handler(Looper.getMainLooper());
            e.execute(new Runnable() {
                @Override
                public void run() {
                            Runnable callback = null;
                    try {
                        URL url = new URL(textoUrl);
                        URLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        boolean esImagen=esImagen(textoUrl);


                            ByteArrayOutputStream bo = new ByteArrayOutputStream();
                            InputStream is = urlConnection.getInputStream();

                            if (esImagen) {
                                Bitmap bmp = BitmapFactory.decodeStream(is);
                                try {
                                    is.close();
                                } catch (IOException ex) {
                                    mostrarTextoError();
                                }
                                callback = () -> {
                                    mostrarImagen(bmp);
                                };
                            }else {
                            int i =is.read();
                                while (i != -1) {
                                    bo.write(i);
                                    i = is.read();
                                }


                                callback = () -> {
                                    mostrarTexto(bo.toString());
                                };
                            }
                    } catch (Exception e) {
                        callback = () -> {
                            mostrarTextoError();
                        };
                    }
                        h.post(callback);
                }
            });

    }

    private void mostrarTexto(String s) {
        binding.progressBar.setVisibility(View.INVISIBLE);
        binding.viewTexto.setVisibility(View.VISIBLE);
        binding.viewTexto.setText(s);
    }

    private void mostrarImagen(Bitmap bmp) {
        binding.progressBar.setVisibility(View.INVISIBLE);
        binding.imageView.setVisibility(View.VISIBLE);
        binding.imageView.setImageBitmap(bmp);
    }
    private boolean esImagen(String url){
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type!=null && type.startsWith("image");
    }


    private boolean valida(CharSequence text) {
        if (text==null || text.length()<1) {
            Toast.makeText(this, "Escribe una url", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}