package com.thinkers.whiteboard

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.thinkers.whiteboard.databinding.ActivityMainBinding
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var viewModel: MainActivityViewModel

    private var menuItemCache: MenuItem? = null
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
            R.id.nav_custom_note -> {
                menuItemCache?.setCheckable(false)
                menuItem.setCheckable(true)
                menuItemCache = menuItem

                val bundle = bundleOf("noteName" to menuItem.title)
                navController.navigate(R.id.nav_custom_note, bundle)
                binding.drawerLayout.closeDrawer(Gravity.START)
                true
            }
            R.id.nav_add_note -> {
                navController.navigate(R.id.nav_add_note)
                binding.drawerLayout.closeDrawer(Gravity.START)
                true
            }
            R.id.nav_edit_note -> {
                navController.navigate(R.id.nav_edit_note)
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
        binding.navView.menu.findItem(R.id.nav_favorites).let {
            menuItemCache?.setCheckable(false)
            it.setCheckable(true)
        }
        navController.navigate(R.id.nav_favorites)
    }

    private val appBarWriteButtonClickListener = View.OnClickListener {
        navController.navigate(R.id.nav_memo)
    }

    private val mainDestinationChangedListener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            when(controller.currentDestination?.id) {
                R.id.nav_edit_note,
                R.id.nav_add_note,
                R.id.nav_memo -> {
                    binding.appBar.visibility = View.GONE
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
                else -> {
                    binding.appBar.visibility = View.VISIBLE
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
            }
        }

    fun tintMenuIcon(item: MenuItem, @ColorRes color: Int) {
        val normalDrawable: Drawable = item.getIcon()
        val wrapDrawable = DrawableCompat.wrap(normalDrawable)
        DrawableCompat.setTint(wrapDrawable, color)
        item.setIcon(wrapDrawable)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this,
            MainActivityViewModelFactory(
                WhiteBoardApplication.instance!!.noteRepository
            )
        ).get(MainActivityViewModel::class.java)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: NavigationView = binding.navView
        navView.itemIconTintList = null;
        lifecycle.coroutineScope.launch {
            viewModel.getAllCustomNotes.collect { list ->
                navView.menu.removeGroup(R.id.nav_view_note_group)
                var order = 3
                for (note in list) {
                    navView.menu.add(
                        R.id.nav_view_note_group,
                        R.id.nav_custom_note,
                        order++,
                        note.noteName
                    ).apply {
                            val normalDrawable: Drawable = resources.getDrawable(R.drawable.ic_navview_circle, null)
                            val wrapDrawable = DrawableCompat.wrap(normalDrawable)
                            DrawableCompat.setTint(wrapDrawable, note.noteColor)
                            this.setIcon(wrapDrawable)
                        }
                }
                navController = findNavController(R.id.nav_host_fragment_content_main)
                navView.setupWithNavController(navController)
                navView.setNavigationItemSelectedListener(navigationViewListener)

                navController.addOnDestinationChangedListener(mainDestinationChangedListener)
            }
        }

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
            when(navController.currentDestination?.id) {
                R.id.nav_add_note, R.id.nav_edit_note, R.id.nav_memo -> {
                    super.onBackPressed()
                }
                else -> {
                    finish()
                }
            }
        }
    }

    companion object {
        val TAG = "MainActivity"
    }
}
