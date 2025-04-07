package vcmsa.projects.kindred

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.util.Log // <-- Import Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    //Define a logging tag
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        usernameEditText = findViewById(R.id.Username) // Ensure this ID matches your XML
        passwordEditText = findViewById(R.id.Password) // Ensure this ID matches your XML
        loginButton = findViewById(R.id.login_btn)     // Ensure this ID matches your XML

        loginButton.setOnClickListener {
            loginUserWithUsername()
        }
    }

    private fun loginUserWithUsername() {
        //Get input and TRIM whitespace
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim() // Trim password too!

        //Validate Input
        var isValid = true
        if (username.isEmpty()) {
            usernameEditText.error = "Username is required"
            usernameEditText.requestFocus()
            isValid = false
        }
        // Clear previous error if field is now filled
        else {
            usernameEditText.error = null
        }

        if (password.isEmpty()) {
            // Set error only if username was valid or also empty, otherwise focus stays on username
            if (isValid) passwordEditText.requestFocus()
            passwordEditText.error = "Password is required"
            isValid = false
        }
        else {
            passwordEditText.error = null
        }

        if (!isValid) {
            return // Stop if input is invalid
        }

        Log.d(TAG, "Attempting login for username: '$username'")

        //Query Firestore for the username
        firestore.collection("users")
            .whereEqualTo("username", username)
            .limit(1) //Expect username to be unique
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    //Username Found
                    val document = querySnapshot.documents[0]
                    val email = document.getString("email")
                    Log.d(TAG, "Firestore found username. Document ID: ${document.id}, Email retrieved: '$email'")

                    if (email != null && email.isNotEmpty()) {
                        //4. Attempt Firebase Auth Sign In
                        Log.d(TAG, "Attempting Firebase Auth sign-in with email: '$email'")
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this) { authTask ->
                                if (authTask.isSuccessful) {
                                    //Login Success
                                    Log.d(TAG, "Firebase Auth successful for email: $email")
                                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                                    // Clear potential previous errors
                                    usernameEditText.error = null
                                    passwordEditText.error = null
                                    // Navigate to Main Activity
                                    val intent = Intent(this, MainActivity::class.java) // Or your main content activity
                                    startActivity(intent)
                                    finish() // Close the Login Activity
                                } else {
                                    //Firebase Auth Failed
                                    Log.w(TAG, "Firebase Auth failed for email: $email", authTask.exception)
                                    passwordEditText.error = "Incorrect password" // Specific feedback
                                    passwordEditText.requestFocus()
                                    Toast.makeText(this, "Login failed: Incorrect password.", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        //Email field missing or empty in Firestore document
                        Log.e(TAG, "Firestore document for username '$username' (ID: ${document.id}) is missing 'email' field or email is empty.")

                        Toast.makeText(this, "Login failed: User account data is incomplete. Please contact support.", Toast.LENGTH_LONG).show()

                        passwordEditText.text.clear()
                    }

                } else {
                    //Username Not Found in Firestore
                    Log.w(TAG, "Firestore query found no user with username: '$username'")
                    usernameEditText.error = "Username not found" // Specific feedback
                    usernameEditText.requestFocus()
                    passwordEditText.text.clear()
                    Toast.makeText(this, "Login failed: Username not registered.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                //Firestore Query Failed
                Log.e(TAG, "Firestore query failed for username: '$username'", e)
                Toast.makeText(this, "Login failed: Could not connect to database. Check network and try again.", Toast.LENGTH_LONG).show()
            }
    }
}