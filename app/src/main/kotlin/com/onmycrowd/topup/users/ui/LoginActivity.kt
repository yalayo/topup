
package com.onmycrowd.topup.users.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserState
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.client.results.SignInResult
import com.amazonaws.mobile.client.results.SignInState
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        AWSMobileClient.getInstance().initialize(applicationContext, object : Callback<UserStateDetails> {
            override fun onResult(userStateDetails: UserStateDetails) {
                when (userStateDetails.userState) {
                    UserState.SIGNED_IN -> runOnUiThread {
                        Log.i("INIT", "onResult: " + userStateDetails.userState)
                    }
                    UserState.SIGNED_OUT -> runOnUiThread {
                        Log.i("INIT", "onResult: " + userStateDetails.userState)
                    }
                    else -> AWSMobileClient.getInstance().signOut()
                }
            }

            override fun onError(e: Exception) {
                Log.e("INIT", "Initialization error.", e)
            }
        })

        signup.setOnClickListener {
            val signupActivity = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(signupActivity)
        }

        login.setOnClickListener {
            email.validate("Email should not be empty") { s -> s.isNotEmpty() }
            email.validate("Valid email address required") { s -> s.isValidEmail() }

            password.validate("Password should not be empty") { s -> s.isNotEmpty() }
            password.validate("Password should contain at least one upper case letter, numbers and a special character") { s -> s.isValidPassword() }

            signIn(email.text.toString(), password.text.toString())
        }
    }

    private fun signIn(email: String, password: String) {
        AWSMobileClient.getInstance().signIn(email, password, null, object : Callback<SignInResult> {
            override fun onResult(signInResult: SignInResult) {
                runOnUiThread {
                    Log.d("SIGN_IN", "Sign-in callback state: " + signInResult.signInState)
                    when (signInResult.signInState) {
                        SignInState.DONE -> {

                        }
                        else -> Log.d("SIGN_IN", "Unsupported sign-in confirmation: " + signInResult.signInState)
                    }
                }
            }

            override fun onError(e: Exception) {
                Log.e("SIGN_IN", "Sign-in error", e)
            }
        })
    }

    private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                afterTextChanged.invoke(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })
    }

    private fun EditText.validate(message: String, validator: (String) -> Boolean) {
        this.afterTextChanged {
            this.error = if (validator(it)) null else message
        }
        this.error = if (validator(this.text.toString())) null else message
    }

    private fun String.isValidEmail(): Boolean
            = this.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    private fun String.isValidPassword(): Boolean {
        val pattern = Pattern.compile("^(?=.*[a-zA-Z])(?=.*[@#$%^&+=.])(?=\\S+$).{8,}$")
        return this.isNotEmpty() && pattern.matcher(this).matches()
    }
}
