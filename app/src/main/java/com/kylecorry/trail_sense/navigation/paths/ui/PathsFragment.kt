package com.kylecorry.trail_sense.navigation.paths.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.kylecorry.andromeda.alerts.toast
import com.kylecorry.andromeda.fragments.BoundFragment
import com.kylecorry.andromeda.pickers.Pickers
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.databinding.FragmentPathsBinding
import com.kylecorry.trail_sense.navigation.paths.domain.IPath
import com.kylecorry.trail_sense.navigation.paths.domain.Path
import com.kylecorry.trail_sense.navigation.paths.domain.PathGroup
import com.kylecorry.trail_sense.navigation.paths.domain.pathsort.*
import com.kylecorry.trail_sense.navigation.paths.infrastructure.BacktrackIsAvailable
import com.kylecorry.trail_sense.navigation.paths.infrastructure.BacktrackScheduler
import com.kylecorry.trail_sense.navigation.paths.infrastructure.PathGroupLoader
import com.kylecorry.trail_sense.navigation.paths.infrastructure.persistence.PathService
import com.kylecorry.trail_sense.navigation.paths.ui.commands.*
import com.kylecorry.trail_sense.shared.UserPreferences
import com.kylecorry.trail_sense.shared.alerts.ILoadingIndicator
import com.kylecorry.trail_sense.shared.alerts.NullLoadingIndicator
import com.kylecorry.trail_sense.shared.extensions.onBackPressed
import com.kylecorry.trail_sense.shared.io.IOFactory
import com.kylecorry.trail_sense.shared.lists.GroupListManager
import com.kylecorry.trail_sense.shared.permissions.RequestRemoveBatteryRestrictionCommand
import com.kylecorry.trail_sense.shared.sensors.SensorService

class PathsFragment : BoundFragment<FragmentPathsBinding>() {

    private val prefs by lazy { UserPreferences(requireContext()) }
    private val pathService by lazy {
        PathService.getInstance(requireContext())
    }

    private val gps by lazy {
        SensorService(requireContext()).getGPS(false)
    }

    private val gpxService by lazy {
        IOFactory().createGpxService(this)
    }

    private var paths = emptyList<Path>()
    private var sort = PathSortMethod.MostRecent

    private val listMapper by lazy {
        IPathListItemMapper(
            requireContext(),
            this::handleAction,
            this::handleGroupAction
        )
    }

    private val pathLoader by lazy { PathGroupLoader(pathService) }
    private lateinit var manager: GroupListManager<IPath>
    private lateinit var loadingIndicator: ILoadingIndicator


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingIndicator = NullLoadingIndicator()

        binding.pathsList.emptyView = binding.waypointsEmptyText
        manager = GroupListManager(
            lifecycleScope,
            loadingIndicator,
            pathLoader,
            this::sortPaths
        )

        manager.onChange = { root, items, rootChanged ->
            if (isBound) {
                binding.pathsList.setItems(items, listMapper)
                if (rootChanged) {
                    binding.pathsList.scrollToPosition(0, false)
                }
                binding.pathsTitle.title.text =
                    (root as PathGroup?)?.name ?: getString(R.string.paths)
            }
        }

        sort = prefs.navigation.pathSort

        // TODO: See if it is possible to get notified of changes without loading all paths
        pathService.getLivePaths().observe(viewLifecycleOwner) {
            manager.refresh()
        }

        onBackPressed {
            if (!manager.up()) {
                remove()
                findNavController().navigateUp()
            }
        }


        binding.pathsTitle.rightQuickAction.setOnClickListener {
            val defaultSort = prefs.navigation.pathSort
            Pickers.menu(
                it, listOf(
                    getString(R.string.sort_by, getSortString(defaultSort)),
                    getString(R.string.import_gpx),
                    getString(R.string.group)
                )
            ) { selected ->
                when (selected) {
                    0 -> changeSort()
                    1 -> importPaths()
                    2 -> createGroup()
                }
                true
            }
        }

        binding.backtrackPlayBar.setState(isBacktrackRunning, prefs.backtrackRecordFrequency)
        binding.backtrackPlayBar.setOnSubtitleClickListener {
            ChangeBacktrackFrequencyCommand(requireContext()) { onUpdate() }.execute()
        }

        binding.backtrackPlayBar.setOnPlayButtonClickListener {
            if (!BacktrackIsAvailable().isSatisfiedBy(requireContext())) {
                toast(getString(R.string.backtrack_disabled_low_power_toast))
            } else {
                val isOn = isBacktrackRunning
                prefs.backtrackEnabled = !isOn
                if (!isOn) {
                    BacktrackScheduler.start(requireContext(), true)
                    RequestRemoveBatteryRestrictionCommand(requireContext()).execute()
                } else {
                    BacktrackScheduler.stop(requireContext())
                }
            }
            onUpdate()
        }

        scheduleUpdates(INTERVAL_1_FPS)
    }

    override fun onUpdate() {
        binding.backtrackPlayBar.setState(isBacktrackRunning, prefs.backtrackRecordFrequency)
    }

    override fun generateBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPathsBinding {
        return FragmentPathsBinding.inflate(layoutInflater, container, false)
    }

    private val isBacktrackRunning: Boolean
        get() = BacktrackScheduler.isOn(requireContext())


    private fun changeSort() {
        val sortOptions = PathSortMethod.values()
        Pickers.item(
            requireContext(),
            getString(R.string.sort),
            sortOptions.map { getSortString(it) },
            sortOptions.indexOf(prefs.navigation.pathSort)
        ) { newSort ->
            if (newSort != null) {
                prefs.navigation.pathSort = sortOptions[newSort]
                sort = sortOptions[newSort]
                onSortChanged()
            }
        }
    }

    private fun getSortString(sortMethod: PathSortMethod): String {
        return when (sortMethod) {
            PathSortMethod.MostRecent -> getString(R.string.most_recent)
            PathSortMethod.Longest -> getString(R.string.longest)
            PathSortMethod.Shortest -> getString(R.string.shortest)
            PathSortMethod.Closest -> getString(R.string.closest)
            PathSortMethod.Name -> getString(R.string.name)
        }
    }

    private fun onSortChanged() {
        manager.refresh(true)
    }

    private fun handleGroupAction(group: PathGroup, action: PathGroupAction) {
        when (action) {
            PathGroupAction.Delete -> deleteGroup(group)
            PathGroupAction.Rename -> renameGroup(group)
            PathGroupAction.Open -> manager.open(group)
        }
    }

    private fun handleAction(path: Path, action: PathAction) {
        when (action) {
            PathAction.Export -> exportPath(path)
            PathAction.Delete -> deletePath(path)
            PathAction.Merge -> merge(path)
            PathAction.Show -> showPath(path)
            PathAction.Rename -> renamePath(path)
            PathAction.Keep -> keepPath(path)
            PathAction.ToggleVisibility -> togglePathVisibility(path)
            PathAction.Simplify -> simplifyPath(path)
            else -> {}
        }
    }

    private fun simplifyPath(path: Path) {
        val command = SimplifyPathCommand(requireContext(), lifecycleScope, pathService)
        command.execute(path)
    }

    private suspend fun sortPaths(paths: List<IPath>): List<IPath> {
        val strategy = when (sort) {
            PathSortMethod.MostRecent -> MostRecentPathSortStrategy()
            PathSortMethod.Longest -> LongestPathSortStrategy()
            PathSortMethod.Shortest -> ShortestPathSortStrategy()
            PathSortMethod.Closest -> ClosestPathSortStrategy(gps.location)
            PathSortMethod.Name -> NamePathSortStrategy()
        }
        return strategy.sort(paths)
    }

    private fun togglePathVisibility(path: Path) {
        val command = TogglePathVisibilityCommand(requireContext(), lifecycleScope, pathService)
        command.execute(path)
    }

    private fun renamePath(path: Path) {
        val command = RenamePathCommand(requireContext(), lifecycleScope, pathService)
        command.execute(path)
    }

    private fun keepPath(path: Path) {
        val command = KeepPathCommand(requireContext(), lifecycleScope, pathService)
        command.execute(path)
    }

    private fun showPath(path: Path) {
        val command = ViewPathCommand(findNavController())
        command.execute(path)
    }


    private fun importPaths() {
        val command = ImportPathsCommand(
            requireContext(),
            lifecycleScope,
            gpxService,
            pathService,
            prefs.navigation
        )
        command.execute()
    }

    private fun exportPath(path: Path) {
        val command = ExportPathCommand(requireContext(), lifecycleScope, gpxService, pathService)
        command.execute(path)
    }

    private fun deletePath(path: Path) {
        val command = DeletePathCommand(requireContext(), lifecycleScope, pathService)
        command.execute(path)
    }

    private fun merge(path: Path) {
        val command = MergePathCommand(requireContext(), lifecycleScope, paths, pathService)
        command.execute(path)
    }

    // Groups
    private fun deleteGroup(group: PathGroup) {
        val command = DeletePathGroupGroupCommand(requireContext(), pathService)
        runInBackground {
            command.execute(group)
            manager.refresh()
        }
    }

    private fun renameGroup(group: PathGroup) {
        val command = RenamePathGroupGroupCommand(requireContext(), pathService)
        runInBackground {
            command.execute(group)
            manager.refresh()
        }
    }

    private fun createGroup() {
        val command = CreatePathGroupGroupCommand(requireContext(), pathService)
        runInBackground {
            command.execute(manager.root?.id)
            manager.refresh()
        }
    }

}