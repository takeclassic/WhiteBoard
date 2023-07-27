package com.thinkers.whiteboard

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.*
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
    var isFavorite: Boolean = false
    var isMoved = false

    private var menuItemCache: MenuItem? = null
    private val navigationViewListener = NavigationView.OnNavigationItemSelectedListener { menuItem ->

        when(menuItem.itemId) {
            R.id.nav_total -> {
                restoreCustomMenuItemColor()
                binding.appbarFavoritesButton.visibility = View.VISIBLE
                binding.appbarTotalButton.visibility = View.GONE
                viewModel.setMemoBelongNote("my_memo")
                navController.navigate(R.id.nav_total)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            R.id.nav_favorites -> {
                restoreCustomMenuItemColor()
                binding.appbarFavoritesButton.visibility = View.GONE
                binding.appbarTotalButton.visibility = View.VISIBLE
                navController.navigate(R.id.nav_favorites)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            R.id.nav_custom_note -> {
                restoreCustomMenuItemColor()
                binding.appbarFavoritesButton.visibility = View.VISIBLE
                binding.appbarTotalButton.visibility = View.GONE
                viewModel.setMemoBelongNote(menuItem.title.toString())
                menuItemCache?.setCheckable(false)
                menuItem.setCheckable(true)
                val title = menuItem.title
                val s = SpannableString(title)
                s.setSpan(ForegroundColorSpan(resources.getColor(R.color.app_main_color, null)), 0, s.length, 0)
                menuItem.title = s
                menuItemCache = menuItem

                viewModel.setCustomNoteName(menuItem.title.toString())
                val bundle = bundleOf("noteName" to menuItem.title.toString())
                navController.navigate(R.id.nav_custom_note, bundle)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            R.id.nav_add_note -> {
                val navOptions = NavOptions
                    .Builder()
                    //.setEnterAnim(R.anim.fade_in)
                    //.setExitAnim(R.anim.fade_out)
                    .setPopExitAnim(R.anim.fade_out)
                    //.setPopEnterAnim(R.anim.fade_in)
                    .build()

                navController.navigate(R.id.nav_add_note, null, navOptions)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            R.id.nav_edit_note -> {
                val navOptions = NavOptions
                    .Builder()
                    //.setEnterAnim(R.anim.fade_in)
                    //.setExitAnim(R.anim.fade_out)
                    .setPopExitAnim(R.anim.fade_out)
                    //.setPopEnterAnim(R.anim.fade_in)
                    .build()

                val args = bundleOf("isActionMode" to false)
                navController.navigate(R.id.nav_edit_note, args, navOptions)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            R.id.nav_waste_bin -> {
                restoreCustomMenuItemColor()
                menuItemCache?.setCheckable(false)
                menuItem.setCheckable(true)
                menuItem.title = "휴지통"
                menuItemCache = menuItem
                navController.navigate(R.id.nav_waste_bin)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            R.id.nav_settings -> {
                val navOptions = NavOptions
                    .Builder()
                    //.setExitAnim(R.anim.fade_out)
                    .setPopExitAnim(R.anim.fade_out)
                    .build()

                restoreCustomMenuItemColor()
                menuItemCache?.setCheckable(false)
                menuItem.setCheckable(true)
                menuItem.title = "설정"
                menuItemCache = menuItem
                navController.navigate(R.id.nav_settings, null, navOptions)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
        }
        false
    }
    private val appBarMenuButtonClickListener = View.OnClickListener {
        if (!binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private val appBarSearchButtonClickListener = View.OnClickListener {
        val navOptions = NavOptions
            .Builder()
            .setEnterAnim(R.anim.bottom_up_appear)
            .setExitAnim(R.anim.bottom_down_disapper)
            .setPopExitAnim(R.anim.bottom_down_disapper)
            .setPopEnterAnim(R.anim.bottom_up_appear)
            .build()

        navController.navigate(R.id.nav_search, null, navOptions)
    }

    private val appBarFavoriteButtonClickListener = View.OnClickListener {
        restoreCustomMenuItemColor()

        binding.appbarFavoritesButton.visibility = View.GONE
        binding.appbarTotalButton.visibility = View.VISIBLE
        binding.navView.menu.findItem(R.id.nav_favorites).let {
            menuItemCache?.setCheckable(false)
            it.setCheckable(true)
        }

        val navOptions = NavOptions
            .Builder()
            .setEnterAnim(R.anim.bottom_up_appear)
            .setExitAnim(R.anim.bottom_down_disapper)
            .setPopExitAnim(R.anim.bottom_down_disapper)
            .setPopEnterAnim(R.anim.bottom_up_appear)
            .build()
        navController.navigate(R.id.nav_favorites, null, navOptions)
    }

    private val appBarTotalButtonClickListener = View.OnClickListener {
        restoreCustomMenuItemColor()

        binding.appbarFavoritesButton.visibility = View.VISIBLE
        binding.appbarTotalButton.visibility = View.GONE
        binding.navView.menu.findItem(R.id.nav_total).let {
            menuItemCache?.setCheckable(false)
            it.setCheckable(true)
        }

        val navOptions = NavOptions
            .Builder()
            .setEnterAnim(R.anim.bottom_up_appear)
            .setExitAnim(R.anim.bottom_down_disapper)
            .setPopExitAnim(R.anim.bottom_down_disapper)
            .setPopEnterAnim(R.anim.bottom_up_appear)
            .build()
        navController.navigate(R.id.nav_total, null, navOptions)
    }

    private val appBarWriteButtonClickListener = View.OnClickListener {
        val navOptions = NavOptions
            .Builder()
            .setEnterAnim(R.anim.bottom_up_appear)
            .setExitAnim(R.anim.bottom_down_disapper)
            .setPopExitAnim(R.anim.bottom_down_disapper)
            .setPopEnterAnim(R.anim.bottom_up_appear)
            .build()
        navController.navigate(R.id.nav_memo, null, navOptions)
    }

    private val mainDestinationChangedListener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            when(controller.currentDestination?.id) {
                R.id.nav_lock,
                R.id.nav_search,
                R.id.nav_edit_note,
                R.id.nav_add_note,
                R.id.nav_memo,
                R.id.nav_settings -> {
                    binding.appBar.visibility = View.GONE
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
                else -> {
                    binding.appBar.visibility = View.VISIBLE
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
            }
        }

//    fun tintMenuIcon(item: MenuItem, @ColorRes color: Int) {
//        val normalDrawable: Drawable = item.getIcon()!!
//        val wrapDrawable = DrawableCompat.wrap(normalDrawable)
//        DrawableCompat.setTint(wrapDrawable, color)
//        item.setIcon(wrapDrawable)
//    }

    private var processLifeCycleObserver: ProcessLifeCycleObserver? = null

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
        navView.itemIconTintList = null
        navController = findNavController(R.id.nav_host_fragment_content_main)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(navigationViewListener)
        navController.addOnDestinationChangedListener(mainDestinationChangedListener)

        val fileName = getString(R.string.file_name_shared_preference)
        val lockKey = getString(R.string.key_lock)
        val isLockModeOn = viewModel.getSwtichStatus(fileName, lockKey)

        if (processLifeCycleObserver == null && isLockModeOn) {
            processLifeCycleObserver = ProcessLifeCycleObserver(navController)
            ProcessLifecycleOwner.get().lifecycle.addObserver(processLifeCycleObserver!!)
        }

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
                            s.setSpan(ForegroundColorSpan(resources.getColor(R.color.black, null)), 0, s.length, 0)
                            this.title = s
                        }
                }
            }
        }

        binding.appbarMenuButton.setOnClickListener(appBarMenuButtonClickListener)
        binding.appbarSearchButton.setOnClickListener(appBarSearchButtonClickListener)
        binding.appbarFavoritesButton.setOnClickListener(appBarFavoriteButtonClickListener)
        binding.appbarTotalButton.setOnClickListener(appBarTotalButtonClickListener)
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
        if (binding.drawerLayout.isDrawerVisible(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            when(navController.currentDestination?.id) {
                R.id.nav_add_note, R.id.nav_edit_note, R.id.nav_memo, R.id.nav_search, R.id.nav_settings, R.id.nav_lock -> {
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

    override fun onResume() {
        super.onResume()
        val fileName = getString(R.string.file_name_shared_preference)
        val lockKey = getString(R.string.key_lock)
        val isLockModeOn = viewModel.getSwtichStatus(fileName, lockKey)

        Log.i(TAG, "isLockModeOn: $isLockModeOn, processLifeCycleObserver is null: ${processLifeCycleObserver == null}")

        if (!isLockModeOn && processLifeCycleObserver != null) {
            ProcessLifecycleOwner.get().lifecycle.removeObserver(processLifeCycleObserver!!)
        }

        if (isLockModeOn) {
            if (processLifeCycleObserver == null) {
                processLifeCycleObserver = ProcessLifeCycleObserver(navController)
            }
            ProcessLifecycleOwner.get().lifecycle.addObserver(processLifeCycleObserver!!)
        }
    }

    private fun restoreCustomMenuItemColor() {
        menuItemCache?.let {
            val title = it.title
            val s = SpannableString(title)
            s.setSpan(ForegroundColorSpan(resources.getColor(R.color.black, null)), 0, s.length, 0)
            it.title = s
        }
    }

    fun init() {
        isFavorite = false
        isMoved = false
    }

    companion object {
        val TAG = "MainActivity"
    }
}

class ProcessLifeCycleObserver(private val navController: NavController): LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            if(navController.currentDestination?.id != R.id.nav_lock) {
                navController.navigate(R.id.nav_lock)
            }
        }
    }
}
