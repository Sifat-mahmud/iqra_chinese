package com.iqra.chinese.ui

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.iqra.chinese.R
import com.iqra.chinese.firebase.FirebaseManager
import kotlinx.coroutines.launch

class AuthFragment : BaseFragment() {

    private val WEB_CLIENT_ID =
        "499424446310-ktmjfhrmktvcn1vk3b8fq6u1joc4s7l9.apps.googleusercontent.com"

    private var isSignUp = true

    // Views — held as fields so the launcher callback can access them
    private var tvError: TextView?    = null
    private var progress: ProgressBar? = null
    private var btnSubmit: Button?    = null
    private var btnGoogle: Button?    = null

    // Legacy Google Sign-In launcher (works on Xiaomi/MIUI)
    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        setLoading(false)
        lifecycleScope.launch {
            val signInResult = FirebaseManager.handleGoogleSignInResult(data)
            if (signInResult.isSuccess) {
                onSuccess()
            } else {
                val msg = signInResult.exceptionOrNull()?.message ?: ""
                if ("Cancel" !in msg && "12501" !in msg) {
                    showError(friendlyError(msg))
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_auth, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle        = view.findViewById<TextView>(R.id.tvAuthTitle)
        val tvToggle       = view.findViewById<TextView>(R.id.tvAuthToggle)
        val tvForgot       = view.findViewById<TextView>(R.id.tvForgotPassword)
        val etEmail        = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword     = view.findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirm      = view.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val tvConfirmLabel = view.findViewById<TextView>(R.id.tvConfirmLabel)
        val btnBack        = view.findViewById<View>(R.id.btnAuthBack)

        tvError   = view.findViewById(R.id.tvAuthError)
        progress  = view.findViewById(R.id.authProgress)
        btnSubmit = view.findViewById(R.id.btnAuthSubmit)
        btnGoogle = view.findViewById(R.id.btnGoogleSignIn)

        fun updateUi() {
            tvTitle.text     = if (isSignUp) "Create Account" else "Sign In"
            btnSubmit!!.text = if (isSignUp) "Create Account" else "Sign In"
            tvToggle.text    = if (isSignUp) "Already have an account? Sign in"
                               else          "New here? Create account"
            etConfirm.isVisible      = isSignUp
            tvConfirmLabel.isVisible = isSignUp
            tvForgot.isVisible       = !isSignUp
            tvError!!.isVisible      = false
        }
        updateUi()

        tvToggle.setOnClickListener { isSignUp = !isSignUp; updateUi() }
        btnBack.setOnClickListener  { findNavController().navigateUp() }

        // ── Forgot password ───────────────────────────────────────────────────
        tvForgot.setOnClickListener {
            val email = etEmail.text?.toString()?.trim() ?: ""
            if (email.isEmpty()) { showError("Enter your email address first"); return@setOnClickListener }
            lifecycleScope.launch {
                val result = FirebaseManager.sendPasswordReset(email)
                tvError!!.setTextColor(resources.getColor(
                    if (result.isSuccess) R.color.jade else R.color.ruby, null))
                tvError!!.text = if (result.isSuccess)
                    "Password reset email sent to $email"
                else "Error: ${result.exceptionOrNull()?.message}"
                tvError!!.isVisible = true
            }
        }

        // ── Email submit ──────────────────────────────────────────────────────
        btnSubmit!!.setOnClickListener {
            val email    = etEmail.text?.toString()?.trim() ?: ""
            val password = etPassword.text?.toString() ?: ""
            val confirm  = etConfirm.text?.toString() ?: ""
            tvError!!.isVisible = false

            if (email.isEmpty() || password.isEmpty()) { showError("Please fill in all fields"); return@setOnClickListener }
            if (isSignUp && password != confirm)        { showError("Passwords do not match");    return@setOnClickListener }
            if (password.length < 6)                    { showError("Password must be at least 6 characters"); return@setOnClickListener }

            setLoading(true)
            lifecycleScope.launch {
                val result = if (isSignUp)
                    FirebaseManager.signUp(email, password)
                else
                    FirebaseManager.signIn(email, password)
                setLoading(false)
                if (result.isSuccess) onSuccess()
                else showError(friendlyError(result.exceptionOrNull()?.message))
            }
        }

        // ── Google Sign-In ────────────────────────────────────────────────────
        btnGoogle!!.setOnClickListener {
            tvError!!.isVisible = false
            setLoading(true)
            val client = FirebaseManager.getGoogleSignInClient(requireContext(), WEB_CLIENT_ID)
            // Sign out first to always show account picker (not auto-select last account)
            client.signOut().addOnCompleteListener {
                googleLauncher.launch(client.signInIntent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tvError = null; progress = null; btnSubmit = null; btnGoogle = null
    }

    private fun setLoading(loading: Boolean) {
        progress?.isVisible  = loading
        btnSubmit?.isEnabled = !loading
        btnGoogle?.isEnabled = !loading
    }

    private fun showError(msg: String) {
        tvError?.setTextColor(resources.getColor(R.color.ruby, null))
        tvError?.text = msg
        tvError?.isVisible = true
    }

    private fun onSuccess() {
        Toast.makeText(context, "Welcome! 🎉", Toast.LENGTH_SHORT).show()
        // Pull cloud data back down — handles reinstall + relogin case
        vm.restoreFromCloud { restored ->
            if (restored && context != null) {
                Toast.makeText(context, "📥 Progress restored from cloud", Toast.LENGTH_SHORT).show()
            }
        }
        findNavController().navigateUp()
    }

    private fun friendlyError(msg: String?): String = when {
        msg == null                         -> "Something went wrong. Please try again."
        "email address is already" in msg  -> "This email is already registered. Try signing in."
        "no user record"           in msg  -> "No account found with this email."
        "password is invalid"      in msg  -> "Incorrect password."
        "badly formatted"          in msg  -> "Invalid email address."
        "network"                  in msg  -> "Network error — check your connection."
        "10:"                      in msg  -> "Google Sign-In not configured.\nEnsure SHA-1 is added in Firebase Console and Google sign-in is enabled."
        else                               -> "Error: $msg"
    }
}
