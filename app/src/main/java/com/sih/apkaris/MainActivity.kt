package com.sih.apkaris

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.sih.apkaris.fragements.*
import com.sih.apkaris.fragments.HomeFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val sessionToken = sharedPref.getString("token", null)

        if (sessionToken != null) {
            showHomeUI()
        } else {
            if (savedInstanceState == null) {
                showLoginFragment()
            }
        }
    }

    fun navigateTo(fragment: Fragment, addToBackStack: Boolean = true) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragmentContainer, fragment)
            if (addToBackStack) {
                addToBackStack(fragment::class.java.simpleName)
            }
        }
    }

    fun showLoginFragment() {
        navigateTo(LoginFragment(), addToBackStack = false)
    }

    fun showSignUpFragment(addToBackStack: Boolean = true) {
        navigateTo(SignUpFragment(), addToBackStack)
    }

    fun showHomeFragment(addToBackStack: Boolean = true) {
        navigateTo(HomeFragment(), addToBackStack)
    }

    fun showReportLostFragment() = navigateTo(ReportLostFragment())
    fun showFindMyDeviceFragment() = navigateTo(FindMyDeviceFragment())
    fun showHelpFindFragment() = navigateTo(HelpFindFragment())
    fun showReportFoundFragment() = navigateTo(ReportFoundFragment())
    fun showProfileFragment() = navigateTo(ProfileFragment())
    fun showForgotPasswordFragment() = navigateTo(ForgotPasswordFragment())
    fun showVerificationFragment() = navigateTo(VerificationFragment())
    fun showCreateNewPasswordFragment() = navigateTo(CreateNewPasswordFragment())

    fun showHomeUI() {
        showHomeFragment(addToBackStack = false)
    }

    fun logout() {
        getSharedPreferences("userPrefs", Context.MODE_PRIVATE).edit { clear() }
        showLoginFragment()
    }
}