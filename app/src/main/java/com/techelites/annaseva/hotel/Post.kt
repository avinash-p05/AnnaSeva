package com.techelites.annaseva.hotel

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.techelites.annaseva.R
import com.techelites.annaseva.auth.DonationRequest
import com.techelites.annaseva.auth.UploadResponse
import com.techelites.annaseva.services.ApiService
import com.techelites.annaseva.services.RetrofitClient
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
    private lateinit var mapView: MapView
    private lateinit var selectedLocation: GeoPoint
    private lateinit var locationTextView: TextView
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private lateinit var userId: String

    private lateinit var cropImage: ActivityResultLauncher<CropImageContractOptions>
    private lateinit var storageReference: StorageReference
    private lateinit var locationOverlay: MyLocationNewOverlay

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post, container, false)

        // Initialize Firebase Storage reference
        storageReference = FirebaseStorage.getInstance().reference

        // Initialize views
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
        locationTextView = view.findViewById(R.id.locationTextView)

        // Set up MapView
        mapView = view.findViewById(R.id.map)
        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(Context.MODE_PRIVATE))
        mapView.setMultiTouchControls(true)

        // Set up location overlay to show user's location
        setupLocationOverlay()

        // Handle map click to drop pin and show coordinates
        mapView.setOnClickListener { event ->
            val geoPoint = mapView.projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
            setSelectedLocation(geoPoint)
        }

        cropImage = registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                // Use the cropped image URI
                val croppedImageUri = result.uriContent
                imageUri = croppedImageUri
                foodImage.setImageURI(imageUri)
            } else {
                // An error occurred
            }
        }

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

    private fun setupLocationOverlay() {
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()
        mapView.overlays.add(locationOverlay)

        locationOverlay.runOnFirstFix {
            val currentLocation = locationOverlay.myLocation
            if (currentLocation != null) {
                requireActivity().runOnUiThread {
                    setSelectedLocation(currentLocation)
                    val mapController: IMapController = mapView.controller
                    mapController.setZoom(15.0)
                    mapController.setCenter(currentLocation)
                }
            }
        }
    }

    private fun setSelectedLocation(location: GeoPoint) {
        selectedLocation = location
        locationTextView.text = "Selected Location: ${location.latitude}, ${location.longitude}"
        mapView.overlays.clear()
        val marker = Marker(mapView)
        marker.position = selectedLocation
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)
        mapView.invalidate()
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
            val uri = data.data
            uri?.let {
                startCrop(uri)
            }
        }
    }

    private fun startCrop(uri: Uri) {
        val options = CropImageOptions(
            guidelines = CropImageView.Guidelines.ON_TOUCH, // Use the correct enum or constant from the library
            outputCompressFormat = Bitmap.CompressFormat.PNG,
            aspectRatioX = 3, // 3:2 aspect ratio
            aspectRatioY = 2
        )

        // Launch the crop activity with the URI and options
        cropImage.launch(
            CropImageContractOptions(
                uri = uri,
                cropImageOptions = options
            )
        )
    }




    private fun showConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Post")
            .setMessage("Are you sure you want to post this donation?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                dimBackgroundAndShowProgressBar()
                uploadImageAndPostDonation()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun uploadImageAndPostDonation() {
        if (imageUri != null) {
            val storageRef = storageReference.child("images/${UUID.randomUUID()}.jpg")
            val uploadTask = storageRef.putFile(imageUri!!)

            uploadTask.addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    postDonation(uri.toString())
                }
            }.addOnFailureListener {
                enableUI()
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            postDonation("")
        }
    }

    private fun postDonation(imageUrl: String) {
        val pref: SharedPreferences = requireActivity().getSharedPreferences("login", Context.MODE_PRIVATE)
        userId = pref.getString("userId", "").toString()
        val apiService = RetrofitClient.getClient("https://anna-seva-backend.onrender.com/")

        val location = com.techelites.annaseva.auth.Location(
            type = "Point",
            coordinates = doubleArrayOf(selectedLocation.latitude, selectedLocation.longitude)
        )

        val donationRequest = DonationRequest(
            name = name.text.toString(),
            quantity = quantity.text.toString().toInt(),
            availableAt = availableDate.text.toString(),
            expiry = expiryDate.text.toString(),
            description = description.text.toString(),
            pickupInstructions = instruction.text.toString(),
            contactPerson = contactPerson.text.toString(),
            type = spinnerType.selectedItem.toString(),
            category = spinnerCategory.selectedItem.toString(),
            idealFor = spinnerIdeal.selectedItem.toString(),
            transportation = spinnerTrans.selectedItem.toString(),
            imageUrl = imageUrl,
            location = location
        )

        apiService.postDonation(userId,donationRequest).enqueue(object : Callback<UploadResponse> {
            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                enableUI ()
                successAnimation.visibility = View.VISIBLE
                successAnimation.playAnimation()
                successAnimation.addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationEnd(animation: Animator) {
                        successAnimation.visibility = View.GONE
                        resetForm()
                    }

                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                })
                Toast.makeText(
                    requireContext(),
                    "Donation posted successfully!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                enableUI()
                Toast.makeText(requireContext(), "Failed to post donation", Toast.LENGTH_SHORT).show()
                Log.e("PostDonation", t.message.toString())
            }
        })
    }


    private fun dimBackgroundAndShowProgressBar() {
        progressBar.visibility = View.VISIBLE
        post.isEnabled = false
        // Add any additional UI changes for dimming
    }

    private fun enableUI() {
        progressBar.visibility = View.GONE
        post.isEnabled = true
        // Reset any UI changes for enabling
    }

    private fun resetForm() {
        name.text.clear()
        quantity.text.clear()
        availableDate.text.clear()
        expiryDate.text.clear()
        description.text.clear()
        instruction.text.clear()
        contactPerson.text.clear()
        spinnerType.setSelection(0)
        spinnerCategory.setSelection(0)
        spinnerIdeal.setSelection(0)
        spinnerTrans.setSelection(0)
        imageUri = null
        foodImage.setImageResource(R.drawable.choose)
        locationTextView.text = "No location selected"
        mapView.overlays.clear()
    }

}
