package com.trends.testwebcloak.view.utils

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

open class BindingFragment<Binding : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> Binding
) : Fragment(), BindingCallbacks {

    private var _binding: Binding? = null
    protected val binding: Binding get() = _binding!!

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = inflate(inflater, container, false)
        afterInflated()
        return binding.root
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        afterCreated()
    }

    final override fun onDestroyView() {
        beforeDestroy()
        _binding = null
        super.onDestroyView()
    }

    protected fun <T> collectOnStarted(
        flow: Flow<T>,
        job: Job? = null,
        collector: suspend (T) -> Unit
    ): Job {
        return lifecycleScope.launch(job ?: Job()) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect(collector)
            }
        }
    }

    protected fun actionView(url: String?) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            requireActivity().startActivity(intent)
        } catch (_: Exception) {

        }
    }
}

interface BindingCallbacks {
    fun afterInflated() {}

    fun afterCreated() {}

    fun beforeDestroy() {}
}