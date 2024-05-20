package dev.panwar.uploadimage;

import android.os.Bundle;
import android.os.Environment;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class All_Images extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_images);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<File> images = getSavedImages();
        imageAdapter = new ImageAdapter(this, images);
        recyclerView.setAdapter(imageAdapter);


    }

    private List<File> getSavedImages() {
        File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyAppImages");
        File[] files = directory.listFiles();
        if (files != null) {
            return Arrays.asList(files);
        } else {
            return new ArrayList<>();
        }
    }

}
