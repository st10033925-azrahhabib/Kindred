package vcmsa.projects.kindred

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Authentication and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        usernameEditText = findViewById(R.id.Username) // EditText for Username input
        passwordEditText = findViewById(R.id.Password)
        loginButton = findViewById(R.id.login_btn)

        loginButton.setOnClickListener {
            loginUserWithUsername() // Call the new login function
        }
    }

    private fun loginUserWithUsername() { // Renamed function to reflect username login
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter Username and Password", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Query Firestore to get the user's email based on the entered username
        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Username found, get the email from the document
                    val document = querySnapshot.documents[0] // Assuming username is unique
                    val email = document.getString("email")

                    if (email != null) {
                        // 2. Sign in with Firebase Authentication using the retrieved email and entered password
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this) { authTask ->
                                if (authTask.isSuccessful) {
                                    // Login successful, navigate to Home Activity
                                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish() // Close the Login Activity
                                } else {
                                    // Firebase Authentication sign-in failed (likely incorrect password)
                                    Toast.makeText(this, "Login failed: Invalid password",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        // Email not found in Firestore document (should not happen if signup is correct)
                        Toast.makeText(this, "Login failed: Email not found for username", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    // Username not found in Firestore
                    Toast.makeText(this, "Login failed: Username not registered", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // Firestore query failed
                Toast.makeText(this, "Login failed: Error fetching user data", Toast.LENGTH_SHORT).show()
                println("Firestore query error: ${e.message}") // Log the error for debugging
            }
    }
}