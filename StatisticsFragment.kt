package com.example.cnn_app

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class StatisticsFragment : Fragment() {

    private lateinit var btnClasses: Button
    private lateinit var btnHistory: Button
    private lateinit var historyLayout: LinearLayout

    // Retrofit and API service
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.0.14:5000/") // CHANGE THIS
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private val apiService by lazy { retrofit.create(ApiService::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)

        btnClasses = view.findViewById(R.id.btn_classes)
        btnHistory = view.findViewById(R.id.btn_history)
        historyLayout = view.findViewById(R.id.history_layout)

        showHistory()
        btnHistory.isEnabled = false
        btnClasses.isEnabled = true

        btnClasses.setOnClickListener {
            findNavController().navigate(R.id.action_statisticsFragment_to_classesFragment)
        }

        btnHistory.setOnClickListener {
            showHistory()
            btnHistory.isEnabled = false
            btnClasses.isEnabled = true
        }

        return view
    }

    private fun showHistory() {
        historyLayout.visibility = View.VISIBLE
        loadDetections()
    }

    private fun loadDetections() {
        historyLayout.removeAllViews()

        val header = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFFE0E0E0.toInt()) // light gray
            setPadding(8, 8, 8, 8)
        }

        val headers = listOf("File", "Class", "Conf.", "BBox")
        headers.forEach { text ->
            header.addView(TextView(requireContext()).apply {
                this.text = text
                setPadding(8, 0, 8, 0)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
        }
        historyLayout.addView(header)

        // Load data from API
        apiService.getDetections().enqueue(object : Callback<List<Detection>> {
            override fun onResponse(call: Call<List<Detection>>, response: Response<List<Detection>>) {
                if (response.isSuccessful) {
                    val detections = response.body() ?: emptyList()
                    if (detections.isEmpty()) {
                        showMessage("No detection history available.")
                    } else {
                        detections.forEach { detection ->
                            val row = LinearLayout(requireContext()).apply {
                                orientation = LinearLayout.HORIZONTAL
                                setPadding(8, 8, 8, 8)
                            }

                            val values = listOf(
                                detection.filename,
                                detection.class_id.toString(),
                                "%.2f".format(detection.confidence),
                                detection.bbox.joinToString(", ")
                            )

                            values.forEach { value ->
                                row.addView(TextView(requireContext()).apply {
                                    text = value
                                    setPadding(8, 0, 8, 0)
                                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                                })
                            }
                            historyLayout.addView(row)
                        }
                    }
                } else {
                    showMessage("Failed to load history: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Detection>>, t: Throwable) {
                showMessage("Error loading history: ${t.message}")
            }
        })
    }

    private fun showMessage(message: String) {
        historyLayout.removeAllViews()
        val tv = TextView(requireContext())
        tv.text = message
        tv.setPadding(16, 16, 16, 16)
        historyLayout.addView(tv)
    }
}
