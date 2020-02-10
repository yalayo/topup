
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
import com.amazonaws.mobile.client.results.SignUpResult
import kotlinx.android.synthetic.main.activity_confirm_signup.*

class ConfirmSignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_signup)

        confirmSignupUser.setOnClickListener {
            confirmSignupEmail.validate("Email should not be empty") { s -> s.isNotEmpty() }
            confirmSignupEmail.validate("Valid email address required") { s -> s.isValidEmail() }

            code.validate("Password should not be empty") { s -> s.isNotEmpty() }

            confirmSignUp(confirmSignupEmail.text.toString(), code.text.toString())
        }
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

    private fun confirmSignUp(email: String, code: String) {
        AWSMobileClient.getInstance().confirmSignUp(email, code, object : Callback<SignUpResult> {
            override fun onResult(signUpResult: SignUpResult) {
                runOnUiThread {
                    Log.d("SIGN_UP", "Sign-up callback state: " + signUpResult.confirmationState)
                    if (!signUpResult.confirmationState) {
                        val details = signUpResult.userCodeDeliveryDetails
                        Log.d("SIGN_UP", "Confirm sign-up with: " + details.destination)
                    } else {
                        val loginActivity = Intent(this@ConfirmSignupActivity, LoginActivity::class.java)
                        startActivity(loginActivity)
                    }
                }
            }

            override fun onError(e: Exception) {
                Log.e("SIGN_UP", "Confirm sign-up error", e)
            }
        })
    }
}