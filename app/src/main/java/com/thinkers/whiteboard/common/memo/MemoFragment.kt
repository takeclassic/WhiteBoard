package com.thinkers.whiteboard.common.memo

import android.content.res.ColorStateList
import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.databinding.FragmentMemoBinding

class MemoFragment : Fragment() {

    private var _binding: FragmentMemoBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MemoViewModel
    private lateinit var favoriteButton: ImageButton
    private lateinit var memoText: EditText

    private var memo: Memo? = null
    private var isFavorite: Boolean = false
    private var beforeTextChangeEditable: Editable? = null
    private var afterTextChangeEditable: Editable? = null

    private val textWatcher = object: TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            Log.i(TAG, "beforeTextChanged $p0")
            p0?.let {
                beforeTextChangeEditable = Editable.Factory.getInstance().newEditable(p0)
            }
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(p0: Editable?) {
            Log.i(TAG, "afterTextChanged $p0")
            p0?.let {
                afterTextChangeEditable = p0
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            this,
            MemoViewModelFactory(WhiteBoardApplication.instance!!.memoRepository)
        ).get(MemoViewModel::class.java)

        _binding = FragmentMemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val toolbar = binding.memoToolbar
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        favoriteButton = binding.memoFragmentFavoriteButton
        favoriteButton.setOnClickListener {
            favoriteButton.isSelected = !favoriteButton.isSelected
            changeFavoriteIcon(favoriteButton.isSelected)
            isFavorite = favoriteButton.isSelected
        }
        memoText = binding.fragmentMemoText
        memoText.addTextChangedListener(textWatcher)

        val bundle = requireArguments()
        val args = MemoFragmentArgs.fromBundle(bundle)

        when(val memoId = args.memoId) {
            -1 -> return
            else -> showExistMemo(memoId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()

        if (memoText.text.isNullOrBlank()) {
            Log.i(TAG, "text is empty")
            return
        }

        when (memo) {
            null -> saveNewMemo()
            else -> updateExistMemo()
        }
    }

    private fun saveNewMemo() {
        val memo = Memo(
            memoId = 0,
            text = memoText.text.toString(),
            createdTime = System.currentTimeMillis(),
            revisedTime = null,
            viewModel.getMemoBelongNoteName(),
            isFavorite = isFavorite
        )
        viewModel.saveMemo(memo)
        viewModel.setHasUpdate(true)
        Log.i(TAG, "try saveNewMemo, noteName: ${viewModel.getMemoBelongNoteName()}")
    }

    private fun updateExistMemo() {
        if (hasChanges()) {
            memo?.let {
                it.text = binding.fragmentMemoText.text.toString()
                it.isFavorite = isFavorite
                viewModel.updateMemo(it)
                viewModel.setHasUpdate(true)
                Log.i(TAG, "try updateExistMemo, noteName: ${viewModel.getMemoBelongNoteName()}")
            }
            return
        }
        viewModel.setHasUpdate(false)
    }

    private fun showExistMemo(memoId: Int) {
        viewModel.getMemo(memoId).observe(viewLifecycleOwner) { it ->
            memo = it
            memoText.text = Editable.Factory.getInstance().newEditable(it.text)
            favoriteButton.isSelected = it.isFavorite
            isFavorite = it.isFavorite
            changeFavoriteIcon(it.isFavorite)
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

    private fun hasChanges(): Boolean {
        if (!beforeTextChangeEditable.isNullOrBlank()
            && beforeTextChangeEditable != afterTextChangeEditable) {
            return true
        } else if(memo?.isFavorite != isFavorite) {
            return true
        }
        return false
    }

    companion object {
        val TAG = "MemoFragment"
    }
}