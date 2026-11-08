package dev.medriid.davit

import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dev.medriid.davit.ui.AppScreen

class MainActivity : FragmentActivity() {
    private val repo get() = (application as DavitApp).repo

    /** App lock: true until you unlock; set again after a minute in the background. */
    private var locked by mutableStateOf(true)
    private var stoppedAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        // Bar icons dark on paper, light on night print, from the first frame (AppScreen keeps them in step).
        val night = when (repo.state.value.theme) {
            1 -> false
            2 -> true
            else -> (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
        val bars = if (night) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
        super.onCreate(savedInstanceState)
        locked = lockWanted()
        setContent { AppScreen(repo, locked = locked && lockWanted(), onUnlock = ::unlock) }
    }

    override fun onStart() {
        super.onStart()
        if (stoppedAt != 0L && SystemClock.elapsedRealtime() - stoppedAt > 60_000 && lockWanted()) locked = true
        if (locked && lockWanted()) unlock()
    }

    override fun onStop() {
        super.onStop()
        stoppedAt = SystemClock.elapsedRealtime()
    }

    override fun onResume() {
        super.onResume()
        if (!(locked && lockWanted())) repo.onAppOpen()
    }

    private fun lockWanted() = repo.state.value.appLock && canLock(this)

    private fun unlock() {
        if (!canLock(this)) {
            locked = false
            return
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock daVIT")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    locked = false
                    repo.onAppOpen()
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }

    companion object {
        /** Checks if the device supports biometric or credential lock and has one enrolled. */
        fun canLock(activity: ComponentActivity): Boolean {
            val biometricManager = BiometricManager.from(activity)
            val authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL

            return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
        }
    }
}
