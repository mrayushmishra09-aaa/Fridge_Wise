package com.example.fridgewise;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProductLookupManager {

    private static final String API_URL = "https://world.openfoodfacts.org/api/v2/product/";
    private final OkHttpClient client;
    private final Handler mainHandler;

    public interface ProductCallback {
        void onProductFound(String name, String category);
        void onNotFound();
        void onError(Exception e);
    }

    public ProductLookupManager() {
        this.client = new OkHttpClient();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void lookupProduct(String barcode, ProductCallback callback) {
        String url = API_URL + barcode + ".json?fields=product_name,categories";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "FridgeWise - Android - Version 1.0")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> callback.onError(e));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    mainHandler.post(callback::onNotFound);
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonData);
                    
                    if (jsonObject.getInt("status") == 1) {
                        JSONObject product = jsonObject.getJSONObject("product");
                        String name = product.optString("product_name", "Unknown Product");
                        String categories = product.optString("categories", "");
                        
                        // Pick the first category from the list
                        String category = "Other";
                        if (!categories.isEmpty()) {
                            category = categories.split(",")[0].trim();
                        }

                        final String finalName = name;
                        final String finalCategory = category;
                        mainHandler.post(() -> callback.onProductFound(finalName, finalCategory));
                    } else {
                        mainHandler.post(callback::onNotFound);
                    }
                } catch (JSONException e) {
                    mainHandler.post(() -> callback.onError(e));
                }
            }
        });
    }
}
