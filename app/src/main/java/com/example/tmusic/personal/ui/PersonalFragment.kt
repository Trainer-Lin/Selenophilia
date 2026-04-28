package com.example.tmusic.personal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.tmusic.MainActivity
import com.example.tmusic.personal.mvi.PersonalIntent
import com.example.tmusic.personal.mvi.PersonalViewModel

class PersonalFragment : Fragment() {
    private lateinit var viewModel: PersonalViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[PersonalViewModel::class.java]
        
        return ComposeView(requireContext()).apply {
            setContent {
                PersonalScreen(
                    viewModel = viewModel,
                    onBackClick = {
                        (activity as? MainActivity)?.navigateBack()
                    },
                    onPlaySong = { musicList, index ->
                        (activity as? MainActivity)?.playOrPause(musicList, index)
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.handleIntent(PersonalIntent.LoadData)
    }
}
