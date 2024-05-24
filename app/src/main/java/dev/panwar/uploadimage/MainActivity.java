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
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import dev.panwar.uploadimage.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Uri mSelectedImageFileUri;
    private String mProfileImageUrl = "";
    private String currentPhotoPath;

    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 7;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.testImg.setOnClickListener(v -> showImageChooser());
        binding.testSelfie.setOnClickListener(v -> showCameraIntent());
        binding.btnSeeSavedImages.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, All_Images.class));
        });
    }

    private void showImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void showCameraIntent() {
        dispatchTakePictureIntent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
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
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                File f = new File(currentPhotoPath);
                binding.testSelfie.setImageURI(Uri.fromFile(f));
                try {
                    saveImageToLocalDirectory(Uri.fromFile(f));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyAppImages");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        File file = new File(directory, fileName);

        FileOutputStream outputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

        outputStream.flush();
        outputStream.close();

        Toast.makeText(this, "Image saved locally!", Toast.LENGTH_SHORT).show();
    }

//    private String saveImageToFile(Bitmap imageBitmap) {
//        // Create a file to save the image
//        File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyAppImages");
//        try {
//            if (!directory.exists()) {
//                directory.mkdirs();
//            }
//
//            String fileName = "IMG_" + System.currentTimeMillis() + ".png";
//            File file = new File(directory, fileName);
//
//            FileOutputStream outputStream = new FileOutputStream(file);
//
//            // Adjust compression quality here
//            imageBitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
//
//            outputStream.flush();
//            outputStream.close();
//
//            // Update media store
//            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
//
//            // Log the absolute path of the saved file
//            Log.d("Saved Image Path", file.getAbsolutePath());
//
//            // Return the directory path
//            return directory.getAbsolutePath();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null; // Handle the error appropriately
//        }
//    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Handle the error appropriately
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "dev.panwar.uploadimage.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    //    private void saveImageToLocalDirectory(Uri imageUri) throws IOException {
//        InputStream inputStream = getContentResolver().openInputStream(imageUri);
//        if (inputStream == null) {
//            throw new IOException("Unable to open input stream from URI");
//        }
//
//        File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyAppImages");
//        if (!directory.exists()) {
//            boolean dirCreated = directory.mkdirs();
//            Log.d("DirectoryCreation", "Directory created: " + dirCreated + " at path: " + directory.getAbsolutePath());
//        }
//
//        String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
//        File file = new File(directory, fileName);
//
//        OutputStream outputStream = new FileOutputStream(file);
//        byte[] buffer = new byte[1024];
//        int bytesRead;
//        while ((bytesRead = inputStream.read(buffer)) != -1) {
//            outputStream.write(buffer, 0, bytesRead);
//        }
//
//        outputStream.close();
//        inputStream.close();
//
//        Log.d("FileSave", "Image saved at: " + file.getAbsolutePath());
//        Toast.makeText(this, "Image saved locally in original quality!", Toast.LENGTH_SHORT).show();
//    }

}

