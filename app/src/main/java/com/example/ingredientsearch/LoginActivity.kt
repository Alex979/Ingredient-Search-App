package com.example.ingredientsearch

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    /*
      We cannot assign our view-based variables during initialization, since we do not set up our
      UI until setContentView(...) in onCreate (otherwise, findViewById(...) will return null.
      Kotlin requires variables be given an initial value, so we have two options:
        1. Declare these variables as nullable (e.g. private var username: EditText? = null)
            - This is annoying, since you need to do a null check every time you access
        2. Declare these variables as lateinit var (and non-null)
            - lateinit is a "promise" to the compiler that they the variable will be set, just not right now
              and, after being set, will never be null in this case.
     */

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var login: Button
    private lateinit var signUp: Button
    private lateinit var rememberCreds: CheckBox
    private lateinit var progressBar: ProgressBar
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tells Android which XML layout file to use for this Activity
        // The "R" is short for "Resources" (e.g. accessing a layout resource in this case)
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signOut()

        val preferences: SharedPreferences = getSharedPreferences("ingredient-search", Context.MODE_PRIVATE)

        // The "id" used here is what we had set in XML in the "id" field
        username = findViewById(R.id.login_username)
        password = findViewById(R.id.login_password)
        login = findViewById(R.id.login)
        signUp = findViewById(R.id.signUpPageButton)
        rememberCreds = findViewById(R.id.rememberCreds)
        progressBar = findViewById(R.id.loginProgressBar)


        username.setText(preferences.getString("SAVED_USERNAME", ""))
        password.setText(preferences.getString("SAVED_PASSWORD", ""))

        if(username.text.isNotEmpty() && password.text.isNotEmpty()) {
            rememberCreds.isChecked = true
        } else {
            login.isEnabled = false
        }

        username.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)

        // An OnClickListener is an interface with a single function, so you can use lambda-shorthand
        // The lambda is called when the user pressed the button
        // https://developer.android.com/reference/android/view/View.OnClickListener
        login.setOnClickListener {
            val inputtedUsername: String = username.text.toString().trim()
            val inputtedPassword: String = password.text.toString().trim()

            progressBar.visibility = View.VISIBLE

            firebaseAuth
                .signInWithEmailAndPassword(inputtedUsername, inputtedPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentUser: FirebaseUser? = firebaseAuth.currentUser
                        val email = currentUser?.email
                        Toast.makeText(this, "Logged in as $email", Toast.LENGTH_SHORT).show()

                        // Save the inputted username and password to file
                        if(rememberCreds.isChecked) {
                            preferences
                                .edit()
                                .putString("SAVED_USERNAME", username.text.toString())
                                .putString("SAVED_PASSWORD", password.text.toString())
                                .apply()
                        } else {
                            preferences
                                .edit()
                                .remove("SAVED_USERNAME")
                                .remove("SAVED_PASSWORD")
                                .apply()
                        }

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        val exception = task.exception
                        Toast.makeText(this, "Login failed: $exception", Toast.LENGTH_SHORT).show()

                    }

                    progressBar.visibility = View.INVISIBLE
                }
        }

        signUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    // A TextWatcher is an interface with three functions, so we cannot use lambda-shorthand
    // The functions are called accordingly as the user types in the EditText
    // https://developer.android.com/reference/android/text/TextWatcher
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {}

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(newString: CharSequence, start: Int, before: Int, count: Int) {
            val inputtedUsername: String = username.text.toString().trim()
            val inputtedPassword: String = password.text.toString().trim()
            val enabled: Boolean = inputtedUsername.isNotEmpty() && inputtedPassword.isNotEmpty()

            // Kotlin shorthand for login.setEnabled(enabled)
            login.isEnabled = enabled
        }
    }
}