package com.example.bookcatalogapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.widget.Toolbar
import android.widget.TextView



class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        val toolbar =  findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("logged_username", "") ?: ""
        if (username.isNotEmpty()) {
            val headerView = navigationView.getHeaderView(0)
            val usernameTextView = headerView.findViewById<TextView>(R.id.nav_username)
            val emailTextView = headerView.findViewById<TextView>(R.id.nav_email)

            usernameTextView.text = username

            val db = DatabaseHelper(this)
            val email = db.getUserEmail(username) ?: "No email"
            emailTextView.text = email
        } else {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            navigationView.setCheckedItem(R.id.nav_home)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_home -> replaceFragment(HomeFragment())
            R.id.nav_my_lists -> replaceFragment(MyListsFragment())
            R.id.nav_settings -> replaceFragment(SettingsFragment())
            R.id.nav_share -> replaceFragment(ShareFragment())
            R.id.nav_about -> replaceFragment(AboutFragment())
            R.id.nav_logout -> {
                val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.clear()
                editor.apply()

                Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
