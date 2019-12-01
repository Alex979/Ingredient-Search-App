package com.example.ingredientsearch

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var cameraButton: Button
    private lateinit var imageView: ImageView
    private lateinit var storage: FirebaseStorage
    var currentPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up references to our UI elements
        cameraButton = findViewById(R.id.cameraButton)
        imageView = findViewById(R.id.imageView)

        // Create an instance of firebase storage (for uploading the image)
        storage = FirebaseStorage.getInstance()

        cameraButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takePicture()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    200
                )
            }

        }
    }

    private fun takePicture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.ingredientsearch.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, 1)
                }
            }
        }
    }

    private fun uploadToFirebaseStorage(filepath: String) {
        // Create a storage reference from our app
        val storageRef = storage.reference

        var file = Uri.fromFile(File(filepath))
        val photoRef = storageRef.child("images/${file.lastPathSegment}")
        var uploadTask = photoRef.putFile(file)

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            photoRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                Log.d("URL", downloadUri.toString())
            } else {
                // Handle failures
                // ...
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 200) {
            // We only requested one permission, so its result is the first element
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("DEBUG", "Permission granted");
                takePicture()
            } else {
                // User denied the permission :(
                Log.d("DEBUG", "Permission denied");
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Picture has been taken with the file path stored in currentPhotoPath
        if (requestCode == 1 && resultCode == RESULT_OK) {
            imageView.setImageURI(Uri.parse(currentPhotoPath))
            uploadToFirebaseStorage(currentPhotoPath!!)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        Log.d("DEBUG", "creating image")
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }
}
