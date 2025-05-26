package com.example.cnn_app

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import org.json.JSONObject

class ResultFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var infoTextView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_result, container, false)

        imageView = view.findViewById(R.id.result_image)
        infoTextView = view.findViewById(R.id.info_text)

        val btnBack: Button = view.findViewById(R.id.button_back)
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        arguments?.getString("resultJson")?.let { jsonStr ->
            try {
                val json = JSONObject(jsonStr)
                val filename = json.getString("filename")
                val classId = json.getInt("class_id")
                val confidence = json.getDouble("confidence")
                val bbox = json.getJSONArray("bbox")

                val bboxStr = "[${bbox.getDouble(0)}, ${bbox.getDouble(1)}, ${bbox.getDouble(2)}, ${bbox.getDouble(3)}]"
                infoTextView.text = "Class ID: $classId\nConfidence: ${"%.2f".format(confidence)}\nBBox: $bboxStr"

                // Load labeled image from server using Glide
                val imageUrl = "http://192.168.0.14:5000/uploads/$filename"  // CHANGE THIS
                Glide.with(this).load(imageUrl).into(imageView)
            } catch (e: Exception) {
                infoTextView.text = "Error parsing result"
            }
        }

        return view
    }
}
