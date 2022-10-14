package com.thinkers.whiteboard

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.thinkers.whiteboard.databinding.ActivityMainBinding
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var viewModel: MainActivityViewModel
    private var time: Long = 0

    private var menuItemCache: MenuItem? = null
    private val navigationViewListener = NavigationView.OnNavigationItemSelectedListener { menuItem ->

        when(menuItem.itemId) {
            R.id.nav_total -> {
                viewModel.setMemoBelongNote("내 메모")
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
                viewModel.setMemoBelongNote(menuItem.title.toString())
                menuItemCache?.setCheckable(false)
                menuItem.setCheckable(true)
                menuItemCache = menuItem

                val bundle = bundleOf("noteName" to menuItem.title.toString())
                navController.navigate(R.id.nav_custom_note, bundle)
                binding.drawerLayout.closeDrawer(Gravity.START)
                true
            }
            R.id.nav_add_note -> {
                val navOptions = NavOptions
                    .Builder()
                    .setPopExitAnim(R.anim.bottom_down_disapper)
                    .build()

                navController.navigate(R.id.nav_add_note, null, navOptions)
                binding.drawerLayout.closeDrawer(Gravity.START)
                true
            }
            R.id.nav_edit_note -> {
                val navOptions = NavOptions
                    .Builder()
                    .setPopExitAnim(R.anim.bottom_down_disapper)
                    .build()

                val args = bundleOf("isActionMode" to false)
                navController.navigate(R.id.nav_edit_note, args, navOptions)
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
        val navOptions = NavOptions
                                .Builder()
                                .setEnterAnim(R.anim.bottom_up_appear)
                                .setPopExitAnim(R.anim.bottom_down_disapper)
                                .build()

        navController.navigate(R.id.nav_search, null, navOptions)
    }

    private val appBarFavoriteButtonClickListener = View.OnClickListener {
        if (binding.navView.menu.findItem(R.id.nav_favorites).isChecked) {
            return@OnClickListener
        }

        binding.navView.menu.findItem(R.id.nav_favorites).let {
            menuItemCache?.setCheckable(false)
            it.setCheckable(true)
        }

        val navOptions = NavOptions
            .Builder()
            .setEnterAnim(R.anim.bottom_up_appear)
            .build()
        navController.navigate(R.id.nav_favorites, null, navOptions)
    }

    private val appBarWriteButtonClickListener = View.OnClickListener {
        val navOptions = NavOptions
            .Builder()
            .setEnterAnim(R.anim.bottom_up_appear)
            .setPopExitAnim(R.anim.bottom_down_disapper)
            .build()
        navController.navigate(R.id.nav_memo, null, navOptions)
    }

    private val mainDestinationChangedListener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            when(controller.currentDestination?.id) {
                R.id.nav_search,
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
                WhiteBoardApplication.instance!!.noteRepository,
                WhiteBoardApplication.instance!!.memoRepository
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

                            val s = SpannableString(note.noteName)
                            s.setSpan(ForegroundColorSpan(Color.BLACK), 0, s.length, 0)
                            this.title = s
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
                R.id.nav_add_note, R.id.nav_edit_note, R.id.nav_memo, R.id.nav_search -> {
                    super.onBackPressed()
                }
                else -> {
                    if (System.currentTimeMillis() - time > 1000L) {
                        time = System.currentTimeMillis()
                        Toast.makeText(
                            applicationContext,
                            "뒤로 버튼을 한번 더 누르시면 종료됩니다",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    finish()
                }
            }
        }
    }

    companion object {
        val TAG = "MainActivity"
    }
}
