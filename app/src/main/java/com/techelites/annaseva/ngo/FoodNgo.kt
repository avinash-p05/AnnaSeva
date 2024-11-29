import android.os.Parcelable
import com.techelites.annaseva.auth.Hotel
import com.techelites.annaseva.auth.Location
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class FoodNgo(
    val id: String,
    val type: String,
    val name: String,
    val description: String,
    val category: String,
    val quantity: Int,
    val expiry: String,
    val idealFor: String,
    val availableAt: String,
    val location : LocationNgo,
    val transportation: String,
    val imageUrl: String?,
    val requests: List<String>,
    val contactPerson: String,
    val donationStatus: String,
    val pickupInstructions: String,
    val hotel: HotelNgo,
    val autoAssignStatus: String,
    val shipmentStatus: String,
    val hotelCoversTransport: Boolean,
    val createdAt: String,
    val updatedAt: String
):Parcelable

@Parcelize
data class HotelNgo(
    val id: String,
    val name: String,
    val email: String,
    val password : String,
    val address: String,
    val city: String,
    val state: String,
    val pinCode: String,
    val location : LocationNgo,
    val contactPerson: String,
    val contactNumber: String,
    val donations : List<String>,
    val createdAt: String,
    val updatedAt: String,
    val verified:Boolean
): Parcelable

@Parcelize
data class LocationNgo(
    val type: String,
    val coordinates: DoubleArray = DoubleArray(2)
): Parcelable

@Parcelize
data class FoodHotel(
    val id: String,
    val type: String,
    val name: String,
    val description: String,
    val category: String,
    val quantity: Int,
    val expiry: String,
    val idealFor: String,
    val availableAt: String,
    val location: LocationNgo,
    val transportation: String,
    val imageUrl: String,
    val requests: Map<String,String>, // Change type as per actual data
    val contactPerson: String,
    val donationStatus: String,
    val pickupInstructions: String,
    val hotel: HotelNgo, // Change type as per actual data
    val autoAssignStatus: String,
    val shipmentStatus: String,
    val hotelCoversTransport: Boolean,
    val createdAt: String,
    val updatedAt: String,
) : Parcelable