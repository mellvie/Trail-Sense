package com.kylecorry.trail_sense.tools.maps.ui

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.kylecorry.andromeda.alerts.Alerts
import com.kylecorry.andromeda.alerts.toast
import com.kylecorry.andromeda.core.bitmap.BitmapUtils.rotate
import com.kylecorry.andromeda.core.system.Resources
import com.kylecorry.andromeda.core.tryOrNothing
import com.kylecorry.andromeda.fragments.BoundFragment
import com.kylecorry.andromeda.list.ListView
import com.kylecorry.andromeda.pickers.Pickers
import com.kylecorry.andromeda.preferences.Preferences
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.databinding.FragmentMapListBinding
import com.kylecorry.trail_sense.databinding.ListItemMapBinding
import com.kylecorry.trail_sense.shared.FormatService
import com.kylecorry.trail_sense.shared.UserPreferences
import com.kylecorry.trail_sense.shared.alerts.AlertLoadingIndicator
import com.kylecorry.trail_sense.shared.extensions.inBackground
import com.kylecorry.trail_sense.shared.extensions.onBackPressed
import com.kylecorry.trail_sense.shared.extensions.onIO
import com.kylecorry.trail_sense.shared.extensions.onMain
import com.kylecorry.trail_sense.shared.io.FileSubsystem
import com.kylecorry.trail_sense.shared.io.FragmentUriPicker
import com.kylecorry.trail_sense.shared.lists.GroupListManager
import com.kylecorry.trail_sense.shared.observe
import com.kylecorry.trail_sense.shared.sensors.SensorService
import com.kylecorry.trail_sense.tools.guide.infrastructure.UserGuideUtils
import com.kylecorry.trail_sense.tools.maps.domain.IMap
import com.kylecorry.trail_sense.tools.maps.domain.Map
import com.kylecorry.trail_sense.tools.maps.domain.MapGroup
import com.kylecorry.trail_sense.tools.maps.infrastructure.MapGroupLoader
import com.kylecorry.trail_sense.tools.maps.infrastructure.MapRepo
import com.kylecorry.trail_sense.tools.maps.infrastructure.MapService
import com.kylecorry.trail_sense.tools.maps.infrastructure.create.CreateMapFromCameraCommand
import com.kylecorry.trail_sense.tools.maps.infrastructure.create.CreateMapFromFileCommand
import com.kylecorry.trail_sense.tools.maps.infrastructure.create.CreateMapFromUriCommand
import com.kylecorry.trail_sense.tools.maps.infrastructure.create.ICreateMapCommand
import com.kylecorry.trail_sense.tools.maps.infrastructure.reduce.HighQualityMapReducer
import com.kylecorry.trail_sense.tools.maps.infrastructure.reduce.LowQualityMapReducer
import com.kylecorry.trail_sense.tools.maps.infrastructure.reduce.MediumQualityMapReducer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MapListFragment : BoundFragment<FragmentMapListBinding>() {

    private val sensorService by lazy { SensorService(requireContext()) }
    private val gps by lazy { sensorService.getGPS() }
    private val mapRepo by lazy { MapRepo.getInstance(requireContext()) }
    private val formatService by lazy { FormatService(requireContext()) }
    private val cache by lazy { Preferences(requireContext()) }
    private val prefs by lazy { UserPreferences(requireContext()) }
    private val mapService by lazy { MapService(mapRepo) }
    private val mapLoader by lazy { MapGroupLoader(mapService.loader) }
    private lateinit var manager: GroupListManager<IMap>

    private lateinit var mapList: ListView<Map>
    private var lastRoot: IMap? = null

    private var mapName = ""

    private val uriPicker = FragmentUriPicker(this)
    private val mapImportingIndicator by lazy {
        AlertLoadingIndicator(
            requireContext(),
            getString(R.string.importing_map)
        )
    }
    private val exportService by lazy { FragmentMapExportService(this) }
    private val files by lazy { FileSubsystem.getInstance(requireContext()) }

    override fun generateBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMapListBinding {
        return FragmentMapListBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        manager = GroupListManager(
            lifecycleScope,
            mapLoader,
            lastRoot,
            this::sortMaps
        )

        val mapIntentUri: Uri? = arguments?.getParcelable("map_intent_uri")
        arguments?.remove("map_intent_uri")
        if (mapIntentUri != null) {
            Pickers.text(
                requireContext(),
                getString(R.string.create_map),
                getString(R.string.create_map_description),
                null,
                hint = getString(R.string.name)
            ) {
                if (it != null) {
                    mapName = it
                    createMap(
                        CreateMapFromUriCommand(
                            requireContext(),
                            mapRepo,
                            mapIntentUri,
                            mapImportingIndicator
                        )
                    )
                }
            }
        }

        if (cache.getBoolean("tool_maps_experimental_disclaimer_shown") != true) {
            Alerts.dialog(
                requireContext(),
                getString(R.string.experimental),
                "Photo Maps is an experimental feature, please only use this to test it out at this point. Feel free to share your feedback on this feature and note that there is still a lot to be done before this will be non-experimental.",
                okText = getString(R.string.tool_user_guide_title),
                cancelText = getString(android.R.string.ok)
            ) { cancelled ->
                cache.putBoolean("tool_maps_experimental_disclaimer_shown", true)
                if (!cancelled) {
                    UserGuideUtils.openGuide(this, R.raw.importing_maps)
                }
            }
        }

        binding.mapListTitle.rightButton.setOnClickListener {
            UserGuideUtils.showGuide(this, R.raw.importing_maps)
        }

        val showMapPreviews = prefs.navigation.showMapPreviews
        mapList = ListView(binding.mapList, R.layout.list_item_map) { itemView: View, map: Map ->
            val mapItemBinding = ListItemMapBinding.bind(itemView)
            val onMap = map.boundary()?.contains(gps.location) ?: false
            if (showMapPreviews) {
                tryOrNothing {
                    mapItemBinding.mapImg.isVisible = true
                    mapItemBinding.mapImg.setImageBitmap(viewLifecycleOwner) {
                        loadMapThumbnail(map)
                    }
                }
            } else {
                mapItemBinding.mapImg.isVisible = false
            }
            mapItemBinding.fileSize.text = formatService.formatFileSize(map.metadata.fileSize)
            mapItemBinding.name.text = map.name
            mapItemBinding.description.text = if (onMap) getString(R.string.on_map) else ""
            mapItemBinding.description.isVisible = onMap
            mapItemBinding.root.setOnClickListener {
                tryOrNothing {
                    findNavController().navigate(
                        R.id.action_mapList_to_maps,
                        bundleOf("mapId" to map.id)
                    )
                }
            }
            mapItemBinding.menuBtn.setOnClickListener {
                Pickers.menu(it, R.menu.map_list_item_menu) {
                    when (it) {
                        R.id.action_map_resize -> {
                            // TODO: Extract this to a command
                            // TODO: Let the user know this is permanent
                            // TODO: Don't say "Importing map"
                            Pickers.item(
                                requireContext(),
                                getString(R.string.change_resolution),
                                listOf(
                                    getString(R.string.low),
                                    getString(R.string.moderate),
                                    getString(R.string.high)
                                )
                            ) {
                                if (it != null) {
                                    inBackground {
                                        mapImportingIndicator.show()
                                        onIO {
                                            val reducer = when (it) {
                                                0 -> LowQualityMapReducer(requireContext())
                                                1 -> MediumQualityMapReducer(requireContext())
                                                else -> HighQualityMapReducer(requireContext())
                                            }
                                            reducer.reduce(map)
                                        }
                                        onMain {
                                            if (isBound) {
                                                mapImportingIndicator.hide()
                                            }
                                            manager.refresh()
                                        }
                                    }
                                }
                            }

                        }
                        R.id.action_map_export -> {
                            exportService.export(map)
                        }
                        R.id.action_map_delete -> {
                            Alerts.dialog(
                                requireContext(),
                                getString(R.string.delete_map),
                                map.name
                            ) { cancelled ->
                                if (!cancelled) {
                                    inBackground {
                                        withContext(Dispatchers.IO) {
                                            mapRepo.deleteMap(map)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    true
                }
            }
        }

        mapList.addLineSeparator()

        manager.onChange = { root, items, rootChanged ->
            if (isBound) {
                mapList.setData(items.filterIsInstance<Map>())
                if (rootChanged) {
                    mapList.scrollToPosition(0, false)
                }
                binding.mapListTitle.title.text =
                    (root as MapGroup?)?.name ?: getString(R.string.photo_maps)
            }
        }

        observe(mapRepo.getMapsLive()) {
            manager.refresh(false)
        }

        onBackPressed {
            if (!manager.up()) {
                remove()
                findNavController().navigateUp()
            }
        }

        setupMapCreateMenu()
    }

    private suspend fun sortMaps(maps: List<IMap>): List<IMap>{
        // TODO: Delegate to a sort strategy and handle groups
        return maps.sortedBy {
            val bounds = if (it is Map){
                it.boundary()
            } else {
                null
            } ?: return@sortedBy Float.MAX_VALUE
            val onMap = bounds.contains(gps.location)
            (if (onMap) 0f else 100000f) + gps.location.distanceTo(bounds.center)
        }
    }

    private fun setupMapCreateMenu() {
        binding.addMenu.setOverlay(binding.overlayMask)
        binding.addMenu.fab = binding.addBtn
        binding.addMenu.hideOnMenuOptionSelected = true
        binding.addMenu.setOnMenuItemClickListener { menuItem ->
            Pickers.text(
                requireContext(),
                getString(R.string.create_map),
                getString(R.string.create_map_description),
                null,
                hint = getString(R.string.name)
            ) {
                if (it != null) {
                    mapName = it
                    when (menuItem.itemId) {
                        R.id.action_import_map_file -> {
                            createMap(
                                CreateMapFromFileCommand(
                                    requireContext(),
                                    uriPicker,
                                    mapRepo,
                                    mapImportingIndicator
                                )
                            )
                        }
                        R.id.action_import_map_camera -> {
                            createMap(
                                CreateMapFromCameraCommand(
                                    this,
                                    mapRepo,
                                    mapImportingIndicator
                                )
                            )
                        }
                    }
                }
            }
            true
        }
    }

    private fun loadMapThumbnail(map: Map): Bitmap {
        val size = Resources.dp(requireContext(), 48f).toInt()
        val bitmap = try {
            files.bitmap(map.filename, Size(size, size))
        } catch (e: Exception) {
            Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        }

        if (map.calibration.rotation != 0) {
            val rotated = bitmap.rotate(map.calibration.rotation.toFloat())
            bitmap.recycle()
            return rotated
        }

        return bitmap
    }

    override fun onPause() {
        super.onPause()
        tryOrNothing {
            lastRoot = manager.root
        }
    }

    private fun createMap(command: ICreateMapCommand) {
        inBackground {
            binding.addBtn.isEnabled = false

            val map = command.execute()?.copy(name = mapName)

            if (map == null) {
                toast(getString(R.string.error_importing_map))
                binding.addBtn.isEnabled = true
                return@inBackground
            }

            if (mapName.isNotBlank()) {
                onIO {
                    mapRepo.addMap(map)
                }
            }

            if (prefs.navigation.autoReduceMaps) {
                mapImportingIndicator.show()
                val reducer = HighQualityMapReducer(requireContext())
                reducer.reduce(map)
                mapImportingIndicator.hide()
            }

            if (map.calibration.calibrationPoints.isNotEmpty()) {
                toast(getString(R.string.map_auto_calibrated))
            }

            binding.addBtn.isEnabled = true
            findNavController().navigate(
                R.id.action_mapList_to_maps,
                bundleOf("mapId" to map.id)
            )

        }
    }


}
