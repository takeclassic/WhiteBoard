package com.thinkers.whiteboard.settings

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.thinkers.whiteboard.R
import com.thinkers.whiteboard.WhiteBoardApplication
import com.thinkers.whiteboard.common.utils.Utils
import com.thinkers.whiteboard.databinding.FragmentBackupHomeBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

class BackupHomeFragment : Fragment() {
    private val viewModel: BackupHomeViewModel by viewModels()
    private var _binding: FragmentBackupHomeBinding? = null
    private val binding get() = _binding!!

    private val backPressListener = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            backPressListenerImpl()
        }
    }

    private val backPressListenerImpl: () -> Unit = {
        val auth = Firebase.auth
        auth.signOut()
        findNavController().navigate(R.id.action_nav_backup_home_to_nav_settings)
    }

    private val deleteButtonListener = OnClickListener {
        val instance = WhiteBoardApplication.instance?.database
        val originalPath = instance!!.openHelper.readableDatabase.path
        val originalFile = File(originalPath)
        Log.i(TAG, "original path: $originalPath, exist: ${originalFile.exists()}")

        val originalWalPath = instance!!.openHelper.readableDatabase.path + "-wal"
        val originalWalFile = File(originalWalPath)

        val originalShmPath = instance!!.openHelper.readableDatabase.path + "-shm"
        val originalShmFile = File(originalShmPath)

        instance!!.close()

        val path = requireContext().filesDir.absolutePath + "/whiteboard_db"
        val file = File(path)

        val pathWal = requireContext().filesDir.absolutePath + "/whiteboard_db-wal"
        val fileWal = File(pathWal)

        val pathShm = requireContext().filesDir.absolutePath + "/whiteboard_db-shm"
        val fileShm = File(pathShm)
        Log.i(TAG, "backup path: $path, exist: ${file.exists()}")

        val res = file.copyTo(originalFile, true)
        Log.i(TAG, "result: ${res.exists()}, ${res.absolutePath}")

        val res2 = fileWal.copyTo(originalWalFile, true)
        Log.i(TAG, "result: ${res2.exists()}, ${res2.absolutePath}")

        val res3 = fileShm.copyTo(originalShmFile, true)
        Log.i(TAG, "result: ${res3.exists()}, ${res3.absolutePath}")

        //System.exit(0)
    }

    private val restoreButtonListener = OnClickListener {
        //val downloadFolder = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val storage = Firebase.storage
        var storageRef = storage.reference
        val fileRefStr = "users/" + Firebase.auth.uid + "/db_file.db"
        val walFileRefStr = "users/" + Firebase.auth.uid + "/db_file.db-wal"
        val shmFileRefStr = "users/" + Firebase.auth.uid + "/db_file.db-shm"

        var fileRef: StorageReference = storageRef.child(fileRefStr)
        var walFileRef: StorageReference = storageRef.child(walFileRefStr)
        var shmFileRef: StorageReference = storageRef.child(shmFileRefStr)

        val path = requireContext().filesDir.absolutePath + "/whiteboard_db"
        val walPath = requireContext().filesDir.absolutePath + "/whiteboard_db-wal"
        val shmPath = requireContext().filesDir.absolutePath + "/whiteboard_db-shm"
        val file = File(path)
        val fileWal = File(walPath)
        val fileShm = File(shmPath)
        //requireContext().deleteFile(file.name)

        fileRef.getFile(file).addOnSuccessListener {
            Log.i(TAG, "1 number: ${it.bytesTransferred}")
            Log.i(TAG, "1 path: ${file.absolutePath}")
        }.addOnFailureListener {
            // Handle any errors
            Log.i(TAG, "1 failed!, reason: $it")
        }

        walFileRef.getFile(fileWal).addOnSuccessListener {
            Log.i(TAG, "2 number: ${it.bytesTransferred}")
            Log.i(TAG, "2 path: ${fileWal.absolutePath}")
        }.addOnFailureListener {
            // Handle any errors
            Log.i(TAG, "2 failed!, reason: $it")
        }

        shmFileRef.getFile(fileShm).addOnSuccessListener {
            Log.i(TAG, "3 number: ${it.bytesTransferred}")
            Log.i(TAG, "3 path: ${fileShm.absolutePath}")
        }.addOnFailureListener {
            // Handle any errors
            Log.i(TAG, "3 failed!, reason: $it")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBackupHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = binding.backupHomeToolbar
        toolbar.setNavigationOnClickListener {
            backPressListenerImpl()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.checkUpdates()
                launch {
                    viewModel.metaSize.collect {
                        if (it == 0L) {
                            binding.backupHomeEmptyText.visibility = View.VISIBLE
                            binding.backupHomeHistorySizeTitle.visibility = View.GONE
                            binding.backupHomeHistorySizeContent.visibility = View.GONE
                        } else {
                            binding.backupHomeEmptyText.visibility = View.GONE
                            binding.backupHomeHistorySizeTitle.visibility = View.VISIBLE
                            binding.backupHomeHistorySizeContent.visibility = View.VISIBLE
                            binding.backupHomeHistorySizeContent.text =
                                "${(it / 1000).toString()} KB"
                        }
                    }
                }
                launch {
                    viewModel.uploadDate.collect {
                        if (it == 0L) {
                            binding.backupHomeEmptyText.visibility = View.VISIBLE
                            binding.backupHomeHistoryDateTitle.visibility = View.GONE
                            binding.backupHomeHistoryDateContent.visibility = View.GONE
                        } else {
                            Log.i(TAG, "date: $it, res: ${Utils.showDate(it)}")
                            binding.backupHomeEmptyText.visibility = View.GONE
                            binding.backupHomeHistoryDateTitle.visibility = View.VISIBLE
                            binding.backupHomeHistoryDateContent.visibility = View.VISIBLE
                            binding.backupHomeHistoryDateContent.text = Utils.showDate(it)
                        }
                    }
                }
            }
        }

        binding.backupHomeBackupButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.dialogTitle = getString(R.string.horizontal_progress_upload_text)
                Log.i(TAG, "set title: ${viewModel.dialogTitle}, viewmodel: ${viewModel.hashCode()}")
                val instance = HorizontalProgressBarFragment()
                instance.show(childFragmentManager, HorizontalProgressBarFragment.TAG)
            }
        }
        binding.backupHomeRemoveButton.setOnClickListener(deleteButtonListener)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressListener)
    }

    override fun onDetach() {
        super.onDetach()
        backPressListener.remove()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "BackupHomeFragment"
    }
}
