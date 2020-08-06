package com.example.pomodoro_clock.pomodoro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.pomodoro_clock.R
import com.example.pomodoro_clock.databinding.PomodoroFragmentBinding

class PomodoroFragment : Fragment() {
    private lateinit var viewModel : PomodoroViewModel
    private lateinit var binding : PomodoroFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding  = DataBindingUtil.inflate(
            inflater,
            R.layout.pomodoro_fragment,
            container,
            false
        )

        viewModel = ViewModelProvider(this).get(PomodoroViewModel::class.java)
        binding.pomodoroViewModel = viewModel
        binding.setLifecycleOwner(this)

        return binding.root
    }

}