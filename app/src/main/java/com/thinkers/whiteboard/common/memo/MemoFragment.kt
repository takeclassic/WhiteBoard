package com.thinkers.whiteboard.common.memo

import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.database.entities.Memo
import com.thinkers.whiteboard.databinding.FragmentMemoBinding

class MemoFragment : Fragment() {

    private var _binding: FragmentMemoBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MemoViewModel
    private var memo: Memo? = null

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
        val toolbar = binding.toolbar
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

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

        when (memo) {
            null -> saveNewMemo()
            else -> updateExistMemo()
        }
    }

    private fun saveNewMemo() {
        val memo = Memo(
            memoId = 0,
            title = binding.fragmentMemoTitle.text.toString(),
            text = binding.fragmentMemoText.text.toString(),
            createdTime = System.currentTimeMillis(),
            revisedTime = null,
            "temp"
        )
        viewModel.saveMemo(memo)
    }

    private fun updateExistMemo() {

    }

    private fun showExistMemo(memoId: Int) {
        val title = binding.fragmentMemoTitle
        val text = binding.fragmentMemoText
        viewModel.getMemo(memoId).observe(viewLifecycleOwner) { it ->
            memo = it
            if (!it.title.isNullOrBlank()) {
                title.text = Editable.Factory.getInstance().newEditable(it.title)
            } else {
                title.visibility = View.GONE
            }
            text.text = Editable.Factory.getInstance().newEditable(it.text)
        }
    }
}