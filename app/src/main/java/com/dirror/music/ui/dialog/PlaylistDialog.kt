package com.dirror.music.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.dirror.music.App
import com.dirror.music.R
import com.dirror.music.adapter.PlaylistDialogAdapter
import com.dirror.music.databinding.DialogPlayListBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PlaylistDialog: BottomSheetDialogFragment() {

    private var _binding: DialogPlayListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPlayListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // binding.root.setBackgroundColor(resources.getColor(android.R.color.transparent))
        App.musicController.value?.getPlaylist()?.let {
            binding.rvPlaylist.adapter = PlaylistDialogAdapter(it)
            binding.tvPlaylist.text = this.context?.getString(R.string.playlist_number, it.size)
            binding.rvPlaylist.scrollToPosition(App.musicController.value?.getNowPosition() ?: 0)
        }
        binding.rvPlaylist.layoutManager = LinearLayoutManager(context)
        App.musicController.value?.getPlayingSongData()?.observe(this, {
            App.musicController.value?.getPlaylist()?.let {
                binding.rvPlaylist.adapter = PlaylistDialogAdapter(it)
                binding.tvPlaylist.text = this.context?.getString(R.string.playlist_number, it.size)
                binding.rvPlaylist.scrollToPosition(App.musicController.value?.getNowPosition() ?: 0)
            }
        })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return FixLandBottomDialog(requireContext(), theme)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class FixLandBottomDialog(ctx: Context, theme: Int): BottomSheetDialog(ctx, theme) {
    override fun onStart() {
        super.onStart()
        //如果是横屏状态
        if (context.resources.configuration.orientation == 2) {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
}