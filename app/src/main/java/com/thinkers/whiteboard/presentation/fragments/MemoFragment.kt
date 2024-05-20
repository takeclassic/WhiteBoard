package com.thinkers.whiteboard.presentation.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.thinkers.whiteboard.presentation.MainActivity
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.data.enums.MemoUpdateState
import com.thinkers.whiteboard.utils.notifications.NotificationHelper
import com.thinkers.whiteboard.utils.AlertDialogArguments
import com.thinkers.whiteboard.utils.Utils
import com.thinkers.whiteboard.data.database.entities.Memo
import com.thinkers.whiteboard.databinding.FragmentMemoBinding
import com.thinkers.whiteboard.presentation.viewmodels.MemoViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MemoFragment : Fragment() {
    companion object {
        val TAG = "MemoFragment"
    }

    private var _binding: FragmentMemoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MemoViewModel by viewModels()
    private lateinit var favoriteButton: ImageButton
    private lateinit var alarmButton: ImageButton
    private lateinit var memoText: EditText
    private lateinit var memoDate: TextView

    private var myCalendar: Calendar = Calendar.getInstance()

    private val textWatcher = object: TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            p0?.let {
                viewModel.beforeTextChangeEditable = Editable.Factory.getInstance().newEditable(p0)
            }
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(p0: Editable?) {
            p0?.let {
                viewModel.afterTextChangeEditable = p0
            }
        }
    }

    private val datePickListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
        Log.i(TAG, "year: $year, month: $month, dayOfMonth: $dayOfMonth")
        myCalendar.set(Calendar.YEAR, year)
        myCalendar.set(Calendar.MONTH, month)
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        TimePickerDialog(
            requireContext(),
            R.style.DialogTheme,
            timePickListener,
            myCalendar.get(Calendar.HOUR_OF_DAY),
            myCalendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private val timePickListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
        myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        myCalendar.set(Calendar.MINUTE, minute)

        if (System.currentTimeMillis() > myCalendar.timeInMillis) {
            Toast.makeText(requireContext(), "현재 시간 이후만 알람예약이 가능합니다.", Toast.LENGTH_SHORT).show()
            showAlarmText(false)
            return@OnTimeSetListener
        }

        viewModel.alarmTime = myCalendar.timeInMillis
        registerNotification(viewModel.alarmTime!!)

        changeAlarmIcon(true)
        showAlarmText(true)
        val formatter = SimpleDateFormat("yyyy//MM//dd hh:mm")
        Log.i(TAG, "timePickListener ${formatter.format(viewModel.alarmTime)}")
    }

    private val onAlarmDelete: () -> Unit = {
        viewModel.alarmTime = null
        changeAlarmIcon(false)
        showAlarmText(false)
        unRegisterNotification()
    }

    private val onMemoDelete: (AlertDialogArguments) -> Unit = {
        it.memo?.let {
            unRegisterNotification()
            viewModel.setHasUpdate(it, MemoUpdateState.DELETE)
            viewModel.deleteMemo(it)
            viewModel.isDeletion = true
            requireActivity().onBackPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val toolbar = binding.memoToolbar
        toolbar.setNavigationOnClickListener {
            val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
            findNavController().popBackStack()
        }
        toolbar.inflateMenu(R.menu.fragment_memo_options)
        toolbar.setOnMenuItemClickListener{ onOptionsItemSelected(it) }
        viewModel.isFavorite = (requireActivity() as MainActivity).isFavorite
        favoriteButton = binding.memoFragmentFavoriteButton
        if (viewModel.isFavorite) {
            favoriteButton.isSelected = true
            changeFavoriteIcon(favoriteButton.isSelected)
        }
        favoriteButton.setOnClickListener {
            favoriteButton.isSelected = !favoriteButton.isSelected
            changeFavoriteIcon(favoriteButton.isSelected)
            viewModel.isFavorite = favoriteButton.isSelected
        }

        alarmButton = binding.memoFragmentAlarmButton
        alarmButton.setOnClickListener {
            if (viewModel.alarmTime == null) {
                myCalendar = Calendar.getInstance()

                DatePickerDialog(
                    requireContext(),
                    R.style.DialogTheme,
                    datePickListener,
                    myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            } else {
                val alertTitle = requireContext().getString(R.string.alert_dialog_alarm_title)
                val alertText = requireContext().getString(R.string.alert_dialog_alarm_text)
                val alertPositiveText = requireContext().getString(R.string.alert_dialog_delete)
                val alertNegativeText = requireContext().getString(R.string.alert_dialog_cancel)
                Utils.showAlertDialog(
                    requireContext(),
                    alertTitle,
                    alertText,
                    alertPositiveText,
                    alertNegativeText,
                    onAlarmDelete,
                    null
                )
            }
        }

        memoText = binding.fragmentMemoText
        memoText.addTextChangedListener(textWatcher)

        memoDate = binding.fragmentMemoDate

        val bundle = requireArguments()
        val args = MemoFragmentArgs.fromBundle(bundle)
        viewModel.memoId = args.memoId
        when(viewModel.memoId) {
            -1 ->  {
                binding.memoFragmentAlarmButton.visibility = View.GONE
                return
            }
            else -> showExistMemo(viewModel.memoId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).isFavorite = false

        if (memoText.text.isNullOrBlank()) {
            Log.i(TAG, "text is empty")
            return
        }

        when (viewModel.memo) {
            null -> saveNewMemo()
            else -> updateExistMemo()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.memo_action_share -> {
                Log.i(TAG, "memo_action_share")
                shareMemo()
                return true
            }
            R.id.memo_action_delete -> {
                Log.i(TAG, "memo_action_delete")
                viewModel.memo?.let {
                    val title = requireContext().getString(R.string.alert_dialog_memo_delete_title)
                    val text = requireContext().getString(R.string.alert_dialog_memo_delete_text)
                    val positiveText = requireContext().getString(R.string.alert_dialog_delete)
                    val negativeText = requireContext().getString(R.string.alert_dialog_cancel)

                    Utils.showAlertDialogWithArguments(
                        requireContext(),
                        title,
                        text,
                        positiveText,
                        negativeText,
                        onMemoDelete,
                        null,
                        AlertDialogArguments(it)
                    )
                }
                return false
            }
            R.id.memo_action_move -> {
                Log.i(TAG, "memo_action_move")
                moveMemo()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun shareMemo() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, memoText.text.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        requireContext().startActivity(shareIntent)
    }

    private fun moveMemo() {
        if (viewModel.memo == null) {
            return
        }
        val list = mutableListOf<Memo>()
        list.add(viewModel.memo!!)
        val action = MemoFragmentDirections.actionNavMemoToNavEditNote(
            true,
            noteName = viewModel.memo?.noteName,
            memoList = list.toTypedArray()
        )
        findNavController().navigate(action)
    }

    private fun saveNewMemo() {
        val memo = Memo(
            memoId = 0,
            text = memoText.text.toString(),
            createdTime = System.currentTimeMillis(),
            alarmTime = viewModel.alarmTime,
            revisedTime = null,
            noteName = viewModel.getMemoBelongNoteName(),
            isFavorite = viewModel.isFavorite
        )
        viewModel.memo = memo
        viewModel.setHasUpdate(Memo(-1, "", 0,0,0, ""), MemoUpdateState.INSERT)
        viewModel.saveMemo(memo)
        Log.i(TAG, "try saveNewMemo, noteName: ${viewModel.getMemoBelongNoteName()}, hashcode: ${this.hashCode()}")
    }

    private fun updateExistMemo() {
        if (hasChanges()) {
            viewModel.memo?.let {
                it.text = binding.fragmentMemoText.text.toString()
                it.isFavorite = viewModel.isFavorite
                it.alarmTime = viewModel.alarmTime
                viewModel.updateMemo(it)
                viewModel.setHasUpdate(it, MemoUpdateState.UPDATE)
                Log.i(TAG, "try updateExistMemo, noteName: ${viewModel.getMemoBelongNoteName()}")
            }
            return
        } else {
            if (!viewModel.isDeletion) {
                viewModel.setHasUpdate(
                    Memo(-1, "", 0,0,0, ""),
                    MemoUpdateState.NONE
                )
            }
        }
    }

    private fun showExistMemo(memoId: Int) {
        viewModel.getMemo(memoId).observe(viewLifecycleOwner) { it ->
            viewModel.memo = it
            memoText.text = Editable.Factory.getInstance().newEditable(it.text)
            favoriteButton.isSelected = it.isFavorite
            viewModel.isFavorite = it.isFavorite
            viewModel.alarmTime = it.alarmTime
            viewModel.oldAlarmTime = viewModel.alarmTime
            changeFavoriteIcon(it.isFavorite)
            if (it.alarmTime == null) {
                changeAlarmIcon(false)
            } else {
                changeAlarmIcon(true)
            }
            showAlarmText(true)
            showMemoDate(it.createdTime)
        }
    }

    private fun changeFavoriteIcon(flag: Boolean) {
        if (flag) {
            binding.memoFragmentFavoriteButton.setImageResource(R.drawable.ic_favorite_clicked_24)
            binding.memoFragmentFavoriteButton.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.favorite_selected))
        } else {
            binding.memoFragmentFavoriteButton.setImageResource(R.drawable.ic_appbar_favorites)
            binding.memoFragmentFavoriteButton.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.default_icon))
        }
    }

    private fun changeAlarmIcon(flag: Boolean) {
        if (flag) {
            binding.memoFragmentAlarmButton.setImageResource(R.drawable.ic_alarm_on_24)
            binding.memoFragmentAlarmButton.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.app_main_color))
        } else {
            binding.memoFragmentAlarmButton.setImageResource(R.drawable.ic_add_alarm_24)
            binding.memoFragmentAlarmButton.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.default_icon))
        }
    }

    private fun hasChanges(): Boolean {
        if (viewModel.isDeletion) {
            return false
        }

        if (viewModel.beforeTextChangeEditable != viewModel.afterTextChangeEditable) {
            return true
        } else if(viewModel.memo?.isFavorite != viewModel.isFavorite) {
            return true
        } else if (viewModel.oldAlarmTime != viewModel.alarmTime) {
            return true
        }
        return false
    }

    private fun showAlarmText(show:Boolean) {
        if (viewModel.alarmTime == null) {
            binding.memoFragmentAlarmText.visibility = View.GONE
            return
        }

        if (show) {
            val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm")
            binding.memoFragmentAlarmText.text = formatter.format(viewModel.alarmTime)
            binding.memoFragmentAlarmText.visibility = View.VISIBLE

            if (viewModel.alarmTime!! < System.currentTimeMillis()) {
                binding.memoFragmentAlarmText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                binding.memoFragmentAlarmButton.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black))
            } else {
                binding.memoFragmentAlarmText.setTextColor(ContextCompat.getColor(requireContext(), R.color.app_main_color))
                binding.memoFragmentAlarmButton.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.app_main_color))
            }

        } else {
            binding.memoFragmentAlarmText.visibility = View.GONE
        }
    }

    private fun registerNotification(savedTime: Long) {
        NotificationHelper.startNotificationWorker(viewModel.memoId, getAlarmTime(savedTime))
    }

    private fun unRegisterNotification() {
        NotificationHelper.cancelNotificationWorker(viewModel.memoId)
    }

    private fun getAlarmTime(savedTime: Long): Long {
        return savedTime - System.currentTimeMillis()
    }

    private fun showMemoDate(dateTimeInMillis: Long) {
        val dateFormat = "yyyy/MM/dd HH:mm:ss"
        val formatter = SimpleDateFormat(dateFormat)
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = dateTimeInMillis

        memoDate.text = formatter.format(calendar.time)
        memoDate.visibility = View.VISIBLE
    }
}
