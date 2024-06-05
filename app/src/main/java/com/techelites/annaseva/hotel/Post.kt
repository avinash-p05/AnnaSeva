package com.techelites.annaseva.hotel

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.techelites.annaseva.R
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Post : Fragment() {

    private lateinit var name: EditText
    private lateinit var quantity: EditText
    private lateinit var availableDate: EditText
    private lateinit var availDateBtn: TextView
    private lateinit var expDateBtn: TextView
    private lateinit var expiryDate: EditText
    private lateinit var description: EditText
    private lateinit var instruction: EditText
    private lateinit var chooseImage: TextView
    private lateinit var foodImage: ImageView
    private lateinit var post: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var successAnimation: LottieAnimationView
    private lateinit var spinnerType: Spinner
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerIdeal: Spinner
    private lateinit var spinnerTrans: Spinner
    private lateinit var contactPerson: EditText
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private var flag: Boolean = true
    private lateinit var userId: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post, container, false)

        name = view.findViewById(R.id.name)
        quantity = view.findViewById(R.id.quantity)
        availDateBtn = view.findViewById(R.id.ChooseAvailDate)
        expiryDate = view.findViewById(R.id.ExpDate)
        description = view.findViewById(R.id.steps)
        contactPerson = view.findViewById(R.id.tagsU)
        instruction = view.findViewById(R.id.pickup)
        foodImage = view.findViewById(R.id.recipeImage)
        post = view.findViewById(R.id.postBtn)
        chooseImage = view.findViewById(R.id.ChooseImage)
        expDateBtn = view.findViewById(R.id.ChooseExpDate)
        availableDate = view.findViewById(R.id.AvailDate)
        spinnerType = view.findViewById(R.id.spinnerType)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        spinnerIdeal = view.findViewById(R.id.spinnerIdeal)
        spinnerTrans = view.findViewById(R.id.spinnerTrans)
        progressBar = view.findViewById(R.id.progressBar)
        successAnimation = view.findViewById(R.id.uploadAnimationView)

        // Set up spinners
        setupSpinner(spinnerType, R.array.Type_spinner_items, R.layout.type_custom_spinner_item)
        setupSpinner(spinnerCategory,
            R.array.Category_spinner_items,
            R.layout.category_custom_spinner_item
        )
        setupSpinner(spinnerIdeal, R.array.Ideal_spinner_items, R.layout.ideal_custom_spinner_item)
        setupSpinner(spinnerTrans, R.array.Trans_spinner_items, R.layout.trans_custom_spinner_item)

        // Set up date pickers
        setupDatePicker(availDateBtn, availableDate)
        setupDatePicker(expDateBtn, expiryDate)

        // Handle image selection
        chooseImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Handle post button click
        post.setOnClickListener {
            showConfirmationDialog()
        }

        return view
    }

    private fun setupSpinner(spinner: Spinner, itemsArrayId: Int, customItemLayoutId: Int) {
        val items = resources.getStringArray(itemsArrayId)
        val adapter = ArrayAdapter(requireContext(), customItemLayoutId, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupDatePicker(button: TextView, editText: EditText) {
        button.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    val date = "$year-${monthOfYear + 1}-$dayOfMonth"
                    editText.setText(date)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            foodImage.setImageURI(imageUri)
        }
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Post")
            .setMessage("Are you sure you want to post this donation?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                postDonation()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun postDonation() {
        val pref: SharedPreferences = requireActivity().getSharedPreferences("login", Context.MODE_PRIVATE)
        userId = pref.getString("userid", "").toString()
        val url = "http://10.0.2.2:5000/api/donation/donations?id=$userId"
        val jsonBody = JSONObject()

        try {
            // Donation details
            jsonBody.put("type", spinnerType.selectedItem.toString())
            jsonBody.put("name", name.text.toString())
            jsonBody.put("description", description.text.toString())
            jsonBody.put("category", spinnerCategory.selectedItem.toString())
            jsonBody.put("quantity", quantity.text.toString().toInt())

            // Date format
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val expiryDateString = expiryDate.text.toString() + "T00:00:00.000Z"
            val availableDateString = availableDate.text.toString() + "T09:00:00.000Z"

            // Format the dates
            val expiryDateFormatted = dateFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(expiryDateString)!!)
            val availableDateFormatted = dateFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(availableDateString)!!)

            // Convert image URI to base64 string
            val imageBase64 = convertImageUriToBase64(imageUri)

            jsonBody.put("uploadPhoto", imageBase64)
            jsonBody.put("expiry", expiryDateFormatted)
            jsonBody.put("idealfor", spinnerIdeal.selectedItem.toString())
            jsonBody.put("availableAt", availableDateFormatted)
            jsonBody.put("transportation", spinnerTrans.selectedItem.toString())
            jsonBody.put("contactPerson", contactPerson.text.toString())
            jsonBody.put("pickupInstructions", instruction.text.toString())
            jsonBody.put("locationType", "Point")

            // Correct coordinates
            val latitude = 15.8392
            val longitude = 74.5557

            jsonBody.put("locationCoordinates[0]", latitude)
            jsonBody.put("locationCoordinates[1]", longitude)

            Log.d("PostDonation", "JSON Body: $jsonBody")
            // Show progress bar
            progressBar.visibility = View.VISIBLE

            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST, url, jsonBody,
                { response ->
                    // Handle response
                    progressBar.visibility = View.GONE
                    successAnimation.visibility = View.VISIBLE
                    successAnimation.playAnimation()
                    Handler(Looper.getMainLooper()).postDelayed({
                        successAnimation.visibility = View.GONE
                        Toast.makeText(requireContext(), "Donation posted successfully!", Toast.LENGTH_SHORT).show()
                        Log.d("PostDonation", "Response: $response")
                        clearFields()
                    }, 3000) // Delay for 3 seconds before hiding the animation
                },
                { error ->
                    // Handle error
                    progressBar.visibility = View.GONE
                    val responseBody = error.networkResponse?.data?.let { String(it) }
                    val statusCode = error.networkResponse?.statusCode
                    Toast.makeText(requireContext(), "Failed to post donation: ${error.message}", Toast.LENGTH_SHORT).show()
                    Log.d("PostDonation", "Error: ${error.message}")
                    Log.d("PostDonation", "Status Code: $statusCode")
                    Log.d("PostDonation", "Response Body: $responseBody")
                }
            )

            val requestQueue = Volley.newRequestQueue(requireContext())
            requestQueue.add(jsonObjectRequest)
        } catch (e: Exception) {
            e.printStackTrace()
            progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "An error occurred while constructing the JSON body: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.d("PostDonation", "JSON Error: ${e.message}")
        }
    }

    private fun convertImageUriToBase64(imageUri: Uri?): String {
        if (imageUri == null) {
            Log.e("ConvertImage", "Image URI is null")
            return ""
        }

        try {
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null && bytes.isNotEmpty()) {
                return Base64.encodeToString(bytes, Base64.DEFAULT)
            } else {
                Log.e("ConvertImage", "Byte array is null or empty")
            }
        } catch (e: Exception) {
            Log.e("ConvertImage", "Error converting image to base64: ${e.message}")
        }

        return ""
    }


    private fun clearFields() {
        name.text.clear()
        quantity.text.clear()
        availableDate.text.clear()
        expiryDate.text.clear()
        description.text.clear()
        instruction.text.clear()
        contactPerson.text.clear()
        foodImage.setImageURI(null)
        spinnerType.setSelection(0)
        spinnerCategory.setSelection(0)
        spinnerIdeal.setSelection(0)
        spinnerTrans.setSelection(0)
        imageUri = null
    }

    companion object {
        const val RESULT_OK = -1
    }
}
