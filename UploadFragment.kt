package com.example.cnn_app

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class UploadFragment : Fragment() {

    private lateinit var imageView: ImageView
    private var imageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
        imageView.setImageURI(it)
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.0.14:5000/")  // CHANGE THIS
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val apiService by lazy { retrofit.create(ApiService::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_upload, container, false)

        imageView = view.findViewById(R.id.image_box)
        val btnUpload: Button = view.findViewById(R.id.btn_upload)

        imageView.setOnClickListener { pickImage.launch("image/*") }

        btnUpload.setOnClickListener {
            imageUri?.let {
                uploadImageToServer(it)
            } ?: Toast.makeText(requireContext(), "Select an image first", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun uploadImageToServer(uri: Uri) {
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
        val tempFile = File(requireContext().cacheDir, "upload_image")
        inputStream?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), tempFile)
        val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

        apiService.uploadImage(body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val jsonStr = response.body()!!.string()

                    // Pass the JSON string to ResultFragment
                    val bundle = Bundle()
                    bundle.putString("resultJson", jsonStr)
                    findNavController().navigate(R.id.action_uploadFragment_to_resultFragment, bundle)
                } else {
                    Toast.makeText(requireContext(), "Upload failed: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
