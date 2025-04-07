package vcmsa.projects.kindred

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.util.Log // <-- Add Log import for debugging
import android.util.Patterns // <-- IMPORT THIS FOR EMAIL VALIDATION
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



class Registration : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        usernameEditText = findViewById(R.id.Username)
        registerButton = findViewById(R.id.register_btn)
        passwordEditText = findViewById(R.id.Password)
        emailEditText = findViewById(R.id.editTextTextEmailAddress)

        registerButton.setOnClickListener {
            signUpUser()
        }
    }

    private fun signUpUser() {
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim() // Trim password too

        if (username.isEmpty()) {
            usernameEditText.error = "Username is required"
            usernameEditText.requestFocus()
            return
        }
        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            emailEditText.requestFocus()
            return
        }
        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            passwordEditText.requestFocus()
            return // Stop execution
        }

        //Validate Email Format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Please enter a valid email address"
            emailEditText.requestFocus()
            return
        }

        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            passwordEditText.requestFocus()
            return
        }

        Log.d("RegistrationActivity", "Attempting Firebase registration with email: '$email'")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("RegistrationActivity", "createUserWithEmail:success")
                    val user = auth.currentUser
                    user?.let {
                        // Pass the trimmed email to Firestore
                        saveUsernameToFirestore(it.uid, username, email)
                    }

                    Toast.makeText(this, "Signup Successful!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, LandingPage::class.java) // Or LoginActivity if preferred after registration
                    startActivity(intent)
                    finish()

                } else {
                    Log.w("RegistrationActivity", "createUserWithEmail:failure", task.exception)

                    var errorMessage = task.exception?.message ?: "Authentication failed."
                    // Check for common Firebase errors
                    if (errorMessage.contains("email address is already in use")) {
                        emailEditText.error = "This email is already registered"
                        emailEditText.requestFocus()
                        errorMessage = "This email address is already in use by another account."
                    } else if (errorMessage.contains("WEAK_PASSWORD")) {
                        passwordEditText.error = "Password is too weak"
                        passwordEditText.requestFocus()
                        errorMessage = "Password is too weak."
                    }

                    Toast.makeText(this, "Signup failed: $errorMessage", Toast.LENGTH_LONG).show() // Use LONG duration for errors
                }
            }
    }

    // --- Modified to accept email ---
    private fun saveUsernameToFirestore(uid: String, username: String, email: String) {
        val userDocument = firestore.collection("users").document(uid)
        val userData = hashMapOf(
            "username" to username,
            "email" to email // Save the validated & trimmed email
        )

        userDocument.set(userData)
            .addOnSuccessListener {
                Log.d("RegistrationActivity", "Username and email saved to Firestore for UID: $uid")
            }
            .addOnFailureListener { e ->
                Log.e("RegistrationActivity", "Error saving user data to Firestore for UID: $uid", e)
            }
    }
}