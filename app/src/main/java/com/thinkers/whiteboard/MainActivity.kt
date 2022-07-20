package com.thinkers.whiteboard

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.thinkers.whiteboard.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val navigationViewListener = NavigationView.OnNavigationItemSelectedListener { menuItem ->
        when(menuItem.itemId) {
            R.id.nav_total -> {
                navController.navigate(R.id.nav_total)
                binding.drawerLayout.closeDrawer(Gravity.START)
                true
            }
            R.id.nav_favorites -> {
                navController.navigate(R.id.nav_favorites)
                binding.drawerLayout.closeDrawer(Gravity.START)
                true
            }
        }
        false
    }
    private val appBarMenuButtonClickListener = View.OnClickListener {
        if (!binding.drawerLayout.isDrawerOpen(Gravity.START)) {
            binding.drawerLayout.openDrawer(Gravity.START)
        }
    }

    private val appBarSearchButtonClickListener = View.OnClickListener {

    }

    private val appBarFavoriteButtonClickListener = View.OnClickListener {
        navController.navigate(R.id.nav_favorites)
    }

    private val appBarWriteButtonClickListener = View.OnClickListener {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(navigationViewListener)

        binding.appbarMenuButton.setOnClickListener(appBarMenuButtonClickListener)
        binding.appbarSearchButton.setOnClickListener(appBarSearchButtonClickListener)
        binding.appbarFavoritesButton.setOnClickListener(appBarFavoriteButtonClickListener)
        binding.appbarWriteButton.setOnClickListener(appBarWriteButtonClickListener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        //val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerVisible(Gravity.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (navController.currentDestination == null) {
                Log.i(TAG, "current destination is null")
            }

            Log.i(TAG, "id: ${navController.currentDestination?.id}")

            if (navController.currentDestination?.id == R.id.nav_favorites) {
                Log.i(TAG, "it is favorites")
                finish()
            }
            super.onBackPressed()
        }
    }

    companion object {
        val TAG = "MainActivity"
    }
}