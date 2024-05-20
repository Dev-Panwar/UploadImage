package dev.panwar.uploadimage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.panwar.uploadimage.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Uri mSelectedImageFileUri;
    private String mProfileImageUrl = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.testImg.setOnClickListener(v -> showImageChooser(this));
        binding.btnSeeSavedImages.setOnClickListener(v-> {
            startActivity(new Intent(MainActivity.this, All_Images.class));
        });
    }

    private void showImageChooser(Activity activity) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(galleryIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 1 && data != null && data.getData() != null) {
            mSelectedImageFileUri = data.getData();
            try {
                Glide.with(this)
                        .load(mSelectedImageFileUri)
                        .centerCrop()
                        .placeholder(R.drawable.placeholder1)
                        .into(binding.testImg);

                // Obtain and display the size of the selected image
                long fileSize = getImageSize(mSelectedImageFileUri);
                String fileSizeString = formatFileSize(fileSize);

                binding.imgSize.setText(fileSizeString);
                saveImageToLocalDirectory(mSelectedImageFileUri);

                uploadUserImage();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadUserImage() {
        if (mSelectedImageFileUri != null) {
            StorageReference sref = FirebaseStorage.getInstance().getReference().child("USER_IMAGE" + System.currentTimeMillis() + "." + getFileExtension(this, mSelectedImageFileUri));
            sref.putFile(mSelectedImageFileUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        sref.getDownloadUrl().addOnSuccessListener(uri -> {
                            mProfileImageUrl = uri.toString();
                            Log.d("upload", "success");
                        }).addOnFailureListener(exception ->
                                Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(exception ->
                            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }


    private String getFileExtension(Activity activity, Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.getContentResolver().getType(uri));
    }

    // Method to get the size of the selected image
    private long getImageSize(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                return inputStream.available();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Method to format the file size into human-readable format
    private String formatFileSize(long size) {
        String hrSize;
        double fileSize = size;
        double kiloByte = fileSize / 1024.0;
        double megaByte = kiloByte / 1024.0;
        double gigaByte = megaByte / 1024.0;
        double teraByte = gigaByte / 1024.0;

        DecimalFormat dec = new DecimalFormat("0.00");

        if (teraByte > 1) {
            hrSize = dec.format(teraByte).concat(" TB");
        } else if (gigaByte > 1) {
            hrSize = dec.format(gigaByte).concat(" GB");
        } else if (megaByte > 1) {
            hrSize = dec.format(megaByte).concat(" MB");
        } else if (kiloByte > 1) {
            hrSize = dec.format(kiloByte).concat(" KB");
        } else {
            hrSize = dec.format(fileSize).concat(" B");
        }

        return hrSize;
    }

    private void saveImageToLocalDirectory(Uri selectedImageUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
        if (inputStream == null) {
            throw new IOException("Unable to open input stream from URI");
        }

        File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyAppImages");
        if (!directory.exists()) {
            boolean dirCreated = directory.mkdirs();
            Log.d("DirectoryCreation", "Directory created: " + dirCreated + " at path: " + directory.getAbsolutePath());
        }

        String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        File file = new File(directory, fileName);

        OutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        inputStream.close();

        Log.d("FileSave", "Image saved at: " + file.getAbsolutePath());
        Toast.makeText(this, "Image saved locally in original quality!", Toast.LENGTH_SHORT).show();
    }

//    private void saveImageToLocalDirectory(Uri selectedImageUri) throws IOException {
//        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
//        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//
//        File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyAppImages");
//        if (!directory.exists()) {
//            directory.mkdirs();
//        }
//
//        String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
//        File file = new File(directory, fileName);
//
//        FileOutputStream outputStream = new FileOutputStream(file);
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//
//        outputStream.flush();
//        outputStream.close();
//
//        Toast.makeText(this, "Image saved locally!", Toast.LENGTH_SHORT).show();
//    }



}
