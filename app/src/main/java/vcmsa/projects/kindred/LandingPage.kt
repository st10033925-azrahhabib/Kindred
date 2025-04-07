package vcmsa.projects.kindred

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button

class LandingPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landing_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val loginButton: Button = findViewById(R.id.login_btn)
        val registerButton: Button = findViewById(R.id.register_btn)

        // --- Set OnClickListener for Login Button ---
        loginButton.setOnClickListener {
            // Create an Intent to start LoginActivity
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        // --- Set OnClickListener for Register (Sign Up) Button ---
        registerButton.setOnClickListener {
            // Create an Intent to start RegistrationActivity
            val intent = Intent(this, Registration::class.java)
            startActivity(intent)
        }

    }
}