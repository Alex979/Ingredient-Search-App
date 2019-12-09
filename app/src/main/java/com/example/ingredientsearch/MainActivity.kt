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
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.storage.FirebaseStorage
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.io.InputStream
import android.graphics.BitmapFactory
import android.graphics.Bitmap




class MainActivity : AppCompatActivity() {

    private lateinit var cameraButton: Button
    private lateinit var imageButton: Button
    private lateinit var storage: FirebaseStorage
    private lateinit var loadingDialog: AlertDialog
    private var currentPhotoPath: String? = null
    private val okHttpClient: OkHttpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up references to our UI elements
        cameraButton = findViewById(R.id.cameraButton)
        imageButton = findViewById(R.id.imageButton)

        loadingDialog = AlertDialog.Builder(this)
            .setMessage("Loading...")
            .create()

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

        imageButton.setOnClickListener {
            selectImage()
        }
    }

    private fun selectImage() {
        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
        getIntent.type = "image/*"

        val pickIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickIntent.type = "image/*"

        val chooserIntent = Intent.createChooser(getIntent, "Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

        startActivityForResult(chooserIntent, 2)
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

        val file = Uri.fromFile(File(filepath))
        val photoRef = storageRef.child("images/${file.lastPathSegment}")
        val uploadTask = photoRef.putFile(file)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            photoRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                makeFoodAPIRequest(downloadUri.toString())
            } else {
                // Handle failures
                // ...
            }
        }
    }

    private fun uploadToFirebaseStorage(stream: InputStream) {
        // Create a storage reference from our app
        val storageRef = storage.reference

        val imageRef = storageRef.child("images/${UUID.randomUUID()}")
        val uploadTask = imageRef.putStream(stream)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                makeFoodAPIRequest(downloadUri.toString())
            } else {
                // Handle failures
                // ...
            }
        }
    }

    private fun makeFoodAPIRequest(imageUrl: String) {

        doAsync {
            val requestBody =
                """
                {
                    "inputs": [
                        {
                            "data": {
                                "image": {
                                    "url": "$imageUrl"
                                }
                            }
                        }
                    ]
                }
            """.trimIndent().toRequestBody(null)

            var request = Request.Builder()
                .url("https://api.clarifai.com/v2/models/bd367be194cf45149e75f01d59f77ba7/outputs")
                .header("Authorization", "Key ${getString(R.string.clarifai_key)}")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            var response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string()

            val ingredientList = ArrayList<Ingredient>()

            if (response.isSuccessful && !responseString.isNullOrEmpty()) {

                val json = JSONObject(responseString)
                val ingredients = json.getJSONArray("outputs")
                    .getJSONObject(0)
                    .getJSONObject("data")
                    .getJSONArray("concepts")

                for(i in 0 until ingredients.length()) {
                    val ingredient = ingredients.getJSONObject(i)
                    val name = ingredient.getString("name")
                    val percentage = ingredient.getDouble("value")

                    ingredientList.add(
                        Ingredient(
                            name = name,
                            percentage = percentage
                        )
                    )
                }
            }

            // Download the image to send to the next intent
            request = Request.Builder()
                .url(imageUrl)
                .build()

            response = okHttpClient.newCall(request).execute()

            val inputStream = response.body?.byteStream()
            val bytes = inputStream?.readBytes()

            val intent = Intent(this@MainActivity, IngredientsActivity::class.java)
            intent.putParcelableArrayListExtra("ingredients", ingredientList)
            intent.putExtra("image", bytes)
            startActivity(intent)

            loadingDialog.hide()
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
                takePicture()
            } else {
                // User denied the permission :(
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Picture has been taken with the file path stored in currentPhotoPath
        if (requestCode == 1 && resultCode == RESULT_OK) {

            loadingDialog.show()
            uploadToFirebaseStorage(currentPhotoPath!!)
        } else if (requestCode == 2 && resultCode == RESULT_OK) {

            loadingDialog.show()
            val inputStream = contentResolver.openInputStream(data!!.data!!)
            uploadToFirebaseStorage(inputStream!!)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
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
