package vcmsa.projects.kindred

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
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

        // Initialize Firebase Auth and Firestore
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
        val username = usernameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign up success, now save the username to Firestore
                    val user = auth.currentUser
                    user?.let {
                        saveUsernameToFirestore(it.uid, username)
                    }

                    Toast.makeText(this, "Signup Successful!", Toast.LENGTH_SHORT).show()

                    // Navigate back to MainActivity (Login Activity)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Close the SignupActivity so the user can't go back to it with the back button

                } else {
                    // If sign up fails, display a message to the user.
                    Toast.makeText(this, "Signup failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUsernameToFirestore(uid: String, username: String) {
        val userDocument = firestore.collection("users").document(uid) // "users" is a collection name in Firestore
        val userData = hashMapOf(
            "username" to username,
            "email" to auth.currentUser?.email // save email in Firestore as well
        )

        userDocument.set(userData)
            .addOnSuccessListener {
                // Username saved to Firestore successfully
                println("Username saved to Firestore")
            }
            .addOnFailureListener { e ->
                // Handle errors saving username to Firestore
                println("Error saving username to Firestore: ${e.message}")
            }
    }
}