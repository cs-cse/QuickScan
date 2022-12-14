package com.example.googlelensapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView captureIV;
    private Button getSearchResultsBtn;
    private ImageView snapBtn;
    private RecyclerView resultRV;

    private SearchRvAdapter searchRvAdapter;
    private ArrayList<SearchRVModal> searchRVModalArrayList;
    int REQUEST_CODE= 1;
    private ProgressBar loadingPB;
    private Bitmap imageBitmap;
    String title,link,displayed_link,snippet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureIV = findViewById(R.id.image);
        snapBtn = findViewById(R.id.idBtnSnap);
        getSearchResultsBtn = findViewById(R.id.idBtnResults);
        resultRV = findViewById(R.id.idRVSearchResults);
        loadingPB = findViewById(R.id.idPBLoading);

        searchRVModalArrayList = new ArrayList<>();
        searchRvAdapter = new SearchRvAdapter(MainActivity.this,searchRVModalArrayList);
        resultRV.setLayoutManager(new LinearLayoutManager(MainActivity.this,LinearLayoutManager.HORIZONTAL,false));
        resultRV.setAdapter(searchRvAdapter);


        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            searchRVModalArrayList.clear();
            searchRvAdapter.notifyDataSetChanged();
            takePictureIntent();
            }
        });

        getSearchResultsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchRVModalArrayList.clear();
                searchRvAdapter.notifyDataSetChanged();
                loadingPB.setVisibility(View.VISIBLE);
            getResults();
            }
        });
    }

    private void getResults(){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();
        labeler.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                String searchQuery = firebaseVisionImageLabels.get(0).getText();
                getSearchResults(searchQuery);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed To Detect Image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getSearchResults(String searchQuery) {
        String apikey = "5eab4fc86dabf5caa941184a3a813fd4b9d5569b8027cb9e0ebb75a509b3ed07";
    String url = "https://serpapi.com/search.json?q="+searchQuery.trim()+"&location=Delhi,India&hl=en&gl=us&google_domain=google.com&api_key="+apikey;
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                try {
                    JSONArray organicArray = response.getJSONArray("organic_results");
                    for(int i=0;i<organicArray.length();i++){
                        JSONObject organicObj = organicArray.getJSONObject(i);
                        if(organicObj.has("title")){
                            title = organicObj.getString("title");

                        }
                        if(organicObj.has("link")){
                            link = organicObj.getString("link");
                        }
                        if(organicObj.has("displayed_link")){
                            displayed_link = organicObj.getString("displayed_link");
                        }
                        if(organicObj.has("snippet")){
                            snippet = organicObj.getString("snippet");
                        }
                        searchRVModalArrayList.add(new SearchRVModal(title,link,displayed_link,snippet));


                    }
                    searchRvAdapter.notifyDataSetChanged();

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "No results found", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(jsonObjectRequest);
    }

    private void takePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent,REQUEST_CODE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap)extras.get("data");
            captureIV.setImageBitmap(imageBitmap);

        }
    }


}