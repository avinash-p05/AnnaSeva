import android.os.Parcelable
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
    val idealfor: String,
    val availableAt: String,
    val transportation: String,
    val uploadPhoto: String?,
    val requests: List<String>,
    val contactPerson: String,
    val donationStatus: String,
    val pickupInstructions: String,
    val hotel: HotelNgo,
    val isUsable: Boolean,
    val reports: List<String>,
    val autoAssignStatus: String,
    val shipmentStatus: String,
    val hotelCoversTransport: Boolean,
    val platformManagesTransport: Boolean,
    val createdAt: String,
    val updatedAt: String
):Parcelable

@Parcelize
data class HotelNgo(
    val location: LocationNgo,
    val id: String,
    val name: String,
    val email: String,
    val address: String,
    val city: String,
    val state: String,
    val pincode: String,
    val contactPerson: String,
    val contactNumber: String,
    val isDeleted: Boolean,
    val createdAt: String,
    val updatedAt: String
): Parcelable

@Parcelize
data class LocationNgo(
    val type: String,
    val coordinates: List<Double>
): Parcelable


