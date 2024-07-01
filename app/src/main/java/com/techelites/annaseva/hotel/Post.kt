package com.techelites.annaseva.hotel

import android.animation.Animator
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.techelites.annaseva.R
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
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
        setupSpinner(spinnerCategory, R.array.Category_spinner_items, R.layout.category_custom_spinner_item)
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
                uploadImageAndPostDonation()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun uploadImageAndPostDonation() {
        imageUri?.let {
            progressBar.visibility = View.VISIBLE
            val url = "http://annaseva.ajinkyatechnologies.in/public/donations/"

            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
            val compressedBitmap = compressBitmap(bitmap, 500) // Compress to 500 KB
            val byteArrayOutputStream = ByteArrayOutputStream()
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)

            val imageUploadRequest = object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    // Assuming the response contains the image path
                    val imagePath = response
                    postDonation(imagePath)
                },
                Response.ErrorListener { error ->
                    progressBar.visibility = View.GONE
                    Log.e("ImageUpload", "Error: ${error.message}", error)
                    Toast.makeText(requireContext(), "Image Upload Failed. Please try again.", Toast.LENGTH_SHORT).show()
                }) {
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/x-www-form-urlencoded"
                    return headers
                }

                override fun getBody(): ByteArray {
                    val params = HashMap<String, String>()
                    params["image"] = base64Image
                    return encodeParameters(params, paramsEncoding)
                }
            }

            val requestQueue = Volley.newRequestQueue(requireContext())
            requestQueue.add(imageUploadRequest)
        }
    }

    private fun postDonation(imagePath: String) {
        val pref: SharedPreferences = requireActivity().getSharedPreferences("login", Context.MODE_PRIVATE)
        userId = pref.getString("userid", "").toString()
        val url = "http://annaseva.ajinkyatechnologies.in/api/donation/donations?id=$userId"
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
            val expiryDateFormatted = dateFormat.parse(expiryDateString)
            val availableDateFormatted = dateFormat.parse(availableDateString)

            // Use formatted dates in the request body
            jsonBody.put("availableDate", dateFormat.format(availableDateFormatted))
            jsonBody.put("expiryDate", dateFormat.format(expiryDateFormatted))

            // Instructions and contacts
            jsonBody.put("transportRequirements", spinnerTrans.selectedItem.toString())
            jsonBody.put("idealFor", spinnerIdeal.selectedItem.toString())
            jsonBody.put("image", imagePath)
            jsonBody.put("contactPerson", contactPerson.text.toString())
            jsonBody.put("instructions", instruction.text.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Post Successfully Uploaded!", Toast.LENGTH_SHORT).show()
                startSuccessAnimation()

                // Optionally update UI or handle success state here
                // For example, update a RecyclerView or other UI elements

            },
            { error ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to Upload Post!", Toast.LENGTH_SHORT).show()
                Log.e("Post", "Error: ${error.message}", error)
            }
        )


        val requestQueue = Volley.newRequestQueue(requireContext())
        requestQueue.add(jsonObjectRequest)
    }

    private fun startSuccessAnimation() {
        progressBar.visibility = View.GONE
        successAnimation.visibility = View.VISIBLE
        successAnimation.playAnimation()
        successAnimation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                successAnimation.visibility = View.GONE
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    private fun compressBitmap(bitmap: Bitmap, maxFileSizeKB: Int): Bitmap {
        var quality = 100
        var compressedBitmap: Bitmap = bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()

        do {
            byteArrayOutputStream.reset()
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            quality -= 5
        } while (byteArrayOutputStream.size() / 1024 > maxFileSizeKB)

        val byteArray = byteArrayOutputStream.toByteArray()
        compressedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        return compressedBitmap
    }

    private fun encodeParameters(params: Map<String, String>, encoding: String): ByteArray {
        val encodedParams = StringBuilder()
        for ((key, value) in params) {
            if (encodedParams.isNotEmpty()) {
                encodedParams.append('&')
            }
            try {
                encodedParams.append(URLEncoder.encode(key, encoding))
                encodedParams.append('=')
                encodedParams.append(URLEncoder.encode(value, encoding))
            } catch (e: UnsupportedEncodingException) {
                throw RuntimeException("Encoding not supported: $encoding", e)
            }
        }
        return encodedParams.toString().toByteArray(charset(encoding))
    }
}
