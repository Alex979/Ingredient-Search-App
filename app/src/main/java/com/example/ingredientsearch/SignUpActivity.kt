package com.example.ingredientsearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var signUp: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        firebaseAuth = FirebaseAuth.getInstance()

        username = findViewById(R.id.register_username)
        password = findViewById(R.id.register_password)
        confirmPassword = findViewById(R.id.confirm_password)
        signUp = findViewById(R.id.signUp)
        progressBar = findViewById(R.id.registerProgressBar)

        username.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)
        confirmPassword.addTextChangedListener(textWatcher)

        signUp.isEnabled = false

        signUp.setOnClickListener {
            val inputtedUsername: String = username.text.toString().trim()
            val inputtedPassword: String = password.text.toString().trim()

            progressBar.visibility = View.VISIBLE

            firebaseAuth
                .createUserWithEmailAndPassword(inputtedUsername, inputtedPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentUser: FirebaseUser? = firebaseAuth.currentUser
                        val email = currentUser?.email
                        Toast.makeText(this, "Registered as $email", Toast.LENGTH_SHORT).show()

                        finish()
                    } else {
                        val exception = task.exception
                        Toast.makeText(this, "Registration failed: $exception", Toast.LENGTH_SHORT).show()
                    }

                    progressBar.visibility = View.INVISIBLE
                }
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {}

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(newString: CharSequence, start: Int, before: Int, count: Int) {
            val inputtedUsername: String = username.text.toString().trim()
            val inputtedPassword: String = password.text.toString().trim()
            val inputtedConfirmPassword: String = confirmPassword.text.toString().trim()
            val enabled: Boolean = inputtedUsername.isNotEmpty() && inputtedPassword.isNotEmpty()
                    && inputtedConfirmPassword == inputtedPassword

            // Kotlin shorthand for login.setEnabled(enabled)
            signUp.isEnabled = enabled
        }
    }
}
