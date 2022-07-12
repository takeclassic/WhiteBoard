package com.thinkers.whiteboard.common.memo

import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.databinding.FragmentMemoBinding
import kotlinx.coroutines.launch

class MemoFragment : Fragment() {

    private var _binding: FragmentMemoBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MemoViewModel

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
        val bundle = requireArguments()
        val args = MemoFragmentArgs.fromBundle(bundle)
        val memoId = args.memoId

        if (memoId == -1) {
            return
        }

        val title = binding.fragmentMemoTitle
        val text = binding.fragmentMemoText
        viewModel.getMemo(memoId).observe(viewLifecycleOwner) { memo ->
            if (!memo.title.isNullOrBlank()) {
                title.text = Editable.Factory.getInstance().newEditable(memo.title)
            }
            text.text = Editable.Factory.getInstance().newEditable(memo.text)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}