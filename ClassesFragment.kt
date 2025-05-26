package com.example.cnn_app

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ClassesFragment : Fragment() {

    private lateinit var btnClasses: Button
    private lateinit var btnHistory: Button
    private lateinit var editButton: Button
    private lateinit var classesTableLayout: LinearLayout
    private var isEditMode = false

    private val classes = mutableListOf(
        ClassData("0", "default", "default note")
    )

    data class ClassData(var id: String, var name: String, var notes: String)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_classes, container, false)

        btnClasses = view.findViewById(R.id.btn_classes)
        btnHistory = view.findViewById(R.id.btn_history)
        editButton = view.findViewById(R.id.edit_button)
        classesTableLayout = view.findViewById(R.id.classes_table_layout)

        btnClasses.isEnabled = false
        btnHistory.isEnabled = true

        btnClasses.setOnClickListener {
            // Do nothing because this is the dummy button
        }

        btnHistory.setOnClickListener {
            findNavController().navigate(R.id.action_classesFragment_to_statisticsFragment)
        }

        editButton.setOnClickListener {
            if (isEditMode) {
                // Save data from EditTexts back into the classes list
                for (i in classes.indices) {
                    val idField = classesTableLayout.findViewWithTag<EditText>("id_$i")
                    val nameField = classesTableLayout.findViewWithTag<EditText>("name_$i")
                    val notesField = classesTableLayout.findViewWithTag<EditText>("notes_$i")

                    classes[i].id = idField.text.toString()
                    classes[i].name = nameField.text.toString()
                    classes[i].notes = notesField.text.toString()
                }

                Toast.makeText(requireContext(), "Changes saved", Toast.LENGTH_SHORT).show()
            }

            isEditMode = !isEditMode
            editButton.text = if (isEditMode) "Done" else "Edit"
            showTable()
        }

        showTable()
        return view
    }

    private fun showTable() {
        classesTableLayout.removeAllViews()

        val headerRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFFE0E0E0.toInt())
            setPadding(8, 8, 8, 8)
        }

        val headers = listOf("ID", "Name", "Notes")
        headers.forEach { header ->
            headerRow.addView(TextView(requireContext()).apply {
                text = header
                setPadding(8, 0, 8, 0)
                layoutParams =
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
        }

        if (isEditMode) {
            headerRow.addView(TextView(requireContext()).apply {
                text = "Delete"
                setPadding(8, 0, 8, 0)
            })
        }

        classesTableLayout.addView(headerRow)

        classes.forEachIndexed { index, classData ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            if (isEditMode) {
                row.addView(EditText(requireContext()).apply {
                    setText(classData.id)
                    layoutParams =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    tag = "id_$index"
                })
                row.addView(EditText(requireContext()).apply {
                    setText(classData.name)
                    layoutParams =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    tag = "name_$index"
                })
                row.addView(EditText(requireContext()).apply {
                    setText(classData.notes)
                    layoutParams =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    tag = "notes_$index"
                })
                row.addView(Button(requireContext()).apply {
                    text = "Delete"
                    setOnClickListener {
                        classes.removeAt(index)
                        showTable()
                    }
                })
            } else {
                listOf(classData.id, classData.name, classData.notes).forEach {
                    row.addView(TextView(requireContext()).apply {
                        text = it
                        layoutParams =
                            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        setPadding(8, 0, 8, 0)
                    })
                }
            }

            classesTableLayout.addView(row)
        }

        if (isEditMode) {
            classesTableLayout.addView(Button(requireContext()).apply {
                text = "Add new row"
                setOnClickListener {
                    classes.add(ClassData("", "", ""))
                    showTable()
                }
            })
        }
    }
}
