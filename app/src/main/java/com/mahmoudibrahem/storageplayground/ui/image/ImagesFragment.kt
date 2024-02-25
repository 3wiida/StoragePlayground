package com.mahmoudibrahem.storageplayground.ui.image

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.mahmoudibrahem.storageplayground.databinding.FragmentImagesLayoutBinding
import com.mahmoudibrahem.storageplayground.ui.image.adapters.ExternalImageAdapter
import com.mahmoudibrahem.storageplayground.ui.image.adapters.InternalImageAdapter
import com.mahmoudibrahem.storageplayground.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID


@AndroidEntryPoint
class ImagesFragment : Fragment() {

    private lateinit var binding: FragmentImagesLayoutBinding
    private val viewModel: ImagesViewModel by viewModels()
    private val internalImagesAdapter = InternalImageAdapter()
    private val externalImagesAdapter = ExternalImageAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImagesLayoutBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imagesRv.layoutManager = GridLayoutManager(requireContext(),3,GridLayoutManager.VERTICAL,false)
        initTabView()
        initCallbacks()
        initObservers()
        initClicks()
        onTabChanged()
    }

    override fun onResume() {
        super.onResume()
        val isReadPermissionGranted = viewModel.checkReadPermission(requireContext())
        viewModel.loadImagesFromInternalStorage(requireContext())
        if (isReadPermissionGranted) {
            binding.dataSection.isVisible = true
            binding.permissionSection.isVisible = false
            viewModel.loadImagesFromExternalStorage(requireContext())
        } else {
            binding.dataSection.isVisible = false
            binding.permissionSection.isVisible = true
        }
    }

    private fun initTabView() {
        binding.storageTypeTabLayout.apply {
            addTab(this.newTab().setText("App Internal Storage"))
            addTab(this.newTab().setText("External Storage"))
            addOnTabSelectedListener(
                object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        tab?.let { viewModel.onTabChanged(tabPosition = it.position) }
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {}
                    override fun onTabReselected(tab: TabLayout.Tab?) {}
                }
            )
        }
        val tab = binding.storageTypeTabLayout.getTabAt(viewModel.selectedTab.value)
        binding.storageTypeTabLayout.selectTab(tab)
    }

    private fun initClicks() {
        binding.grantPermissionBtn.setOnClickListener {
            (activity as MainActivity).permissionLauncher.launch(
                arrayOf(
                    viewModel.getReadPermission()
                )
            )
        }

        binding.takePhotoBtn.setOnClickListener {
            (requireActivity() as MainActivity).takePhotoLauncher.launch(null)
        }

        internalImagesAdapter.onImageLongClick = { image ->
            viewModel.deleteImageFromInternalStorage(requireContext(), image.name)
        }

        externalImagesAdapter.onImageLongClick = { image ->
            viewModel.deleteImageFromExternalStorage(
                requireContext(),
                image.uri,
                (requireActivity() as MainActivity).intentSenderLauncher
            )
        }
    }

    private fun initCallbacks() {
        (requireActivity() as MainActivity).onImageTaken = { image ->
            image?.let {
                viewModel.saveImage(requireContext(), it, "${UUID.randomUUID()}.png")
            }
        }
    }

    private fun initObservers() {
        lifecycleScope.launch {
            viewModel.isInternalImageDeleted.collectLatest { isDeleted ->
                if (isDeleted) {
                    viewModel.loadImagesFromInternalStorage(requireContext())
                }
            }
        }
    }

    private fun onTabChanged() {
        lifecycleScope.launch {
            viewModel.selectedTab.collectLatest { pos ->
                when (pos) {
                    0 -> {
                        binding.imagesRv.adapter = internalImagesAdapter
                        viewModel.internalImage.collectLatest {
                            internalImagesAdapter.submitList(it)
                        }
                    }

                    1 -> {
                        binding.imagesRv.adapter = externalImagesAdapter
                        viewModel.externalImages.collectLatest {
                            externalImagesAdapter.submitList(it)
                        }
                    }
                }
            }
        }
    }

}