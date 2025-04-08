import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hrishikeshbooks.bookapp.databinding.ActivityPurchaseBinding
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class PurchaseActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var binding: ActivityPurchaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Checkout.preload(applicationContext)

        binding.btnPurchase.setOnClickListener {
            startRazorpayPayment()
        }
    }

    private fun startRazorpayPayment() {
        val checkout = Checkout()
        checkout.setKeyID("YOUR_RAZORPAY_KEY_ID") // Replace with your Razorpay key

        try {
            val options = JSONObject()
            options.put("name", "Hrishikesh Books")
            options.put("description", "Book Purchase")
            options.put("currency", "INR")
            options.put("amount", 9) // amount in paise (₹9.00)

            val prefill = JSONObject()
            prefill.put("email", "user@example.com")
            prefill.put("contact", "9876543210")

            options.put("prefill", prefill)

            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in starting Razorpay Checkout", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        Toast.makeText(this, "✅ Payment Successful: $razorpayPaymentID", Toast.LENGTH_LONG).show()
        // Save to SharedPreferences
        getSharedPreferences("PurchasePrefs", MODE_PRIVATE)
            .edit().putBoolean("isPurchased", true).apply()
        // Finish or redirect
        finish()
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "❌ Payment Failed: $response", Toast.LENGTH_SHORT).show()
        binding.retryButton.visibility = View.VISIBLE
    }
}
