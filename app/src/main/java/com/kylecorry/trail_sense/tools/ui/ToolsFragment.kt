package com.kylecorry.trail_sense.tools.ui

import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.databinding.FragmentToolWhistleBinding
import com.kylecorry.trail_sense.databinding.FragmentToolsBinding
import com.kylecorry.trail_sense.tools.maps.infrastructure.TrailSenseMaps
import com.kylecorry.trail_sense.shared.UserPreferences
import com.kylecorry.trail_sense.shared.views.TileButton
import com.kylecorry.trail_sense.tools.health.infrastructure.HealthSense
import com.kylecorry.trail_sense.tools.nws.infrastructure.NWSWeather
import com.kylecorry.trailsensecore.infrastructure.audio.Whistle
import com.kylecorry.trailsensecore.infrastructure.flashlight.Flashlight
import com.kylecorry.trailsensecore.infrastructure.sensors.SensorChecker
import com.kylecorry.trailsensecore.infrastructure.system.UiUtils
import com.kylecorry.trailsensecore.infrastructure.view.ViewMeasurementUtils


class ToolsFragment : Fragment() {

    private var _binding: FragmentToolsBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    private val sensorChecker by lazy { SensorChecker(requireContext()) }
    private val prefs by lazy { UserPreferences(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        createToolTiles()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToolsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createToolTiles() {




        val content = listOf(
            Tool("Signaling", -1, -1),
            Tool("Flashlight", R.drawable.flashlight, R.id.action_action_experimental_tools_to_fragmentToolFlashlight),
            Tool("Whistle", R.drawable.ic_tool_whistle, R.id.action_action_experimental_tools_to_toolWhistleFragment),
            Tool("Distance", -1, -1),
            Tool("Ruler", R.drawable.ruler, R.id.action_action_experimental_tools_to_rulerFragment),
            Tool("Converter", R.drawable.ic_tool_distance_convert, R.id.action_action_experimental_tools_to_fragmentDistanceConverter),
            Tool("Cliff Height", R.drawable.ic_tool_cliff_height, R.id.action_action_experimental_tools_to_toolCliffHeightFragment),
            Tool("Lightning Strike", R.drawable.ic_tool_lightning, R.id.action_action_experimental_tools_to_fragmentToolLightning),
            Tool("Depth", R.drawable.ic_depth, R.id.action_action_experimental_tools_to_toolDepthFragment),
            Tool("Location", -1, -1),
            Tool("Maps", R.drawable.maps, -1),
            Tool("Backtrack", R.drawable.ic_tool_backtrack, R.id.action_action_experimental_tools_to_fragmentBacktrack),
            Tool("Triangulate", R.drawable.ic_tool_triangulate, R.id.action_action_experimental_tools_to_fragmentToolTriangulate),
            Tool("Convert", R.drawable.ic_tool_distance_convert, R.id.action_action_experimental_tools_to_fragmentToolCoordinateConvert),
            Tool("Angles", -1, -1),
            Tool("Inclinometer", R.drawable.inclinometer, R.id.action_toolsFragment_to_inclinometerFragment),
            Tool("Level", R.drawable.level, R.id.action_action_experimental_tools_to_levelFragment),
            Tool("Time", -1, -1),
            Tool("Clock", R.drawable.ic_tool_clock, R.id.action_action_experimental_tools_to_toolClockFragment),
            Tool("Boil", R.drawable.ic_tool_boil, R.id.action_action_experimental_tools_to_waterPurificationFragment),
            Tool("Power", -1, -1),
            Tool("Battery", R.drawable.ic_tool_battery, R.id.action_action_experimental_tools_to_fragmentToolBattery),
            Tool("Solar Panel", R.drawable.ic_tool_solar_panel, R.id.action_action_experimental_tools_to_fragmentToolSolarPanel),
            Tool("Other", -1, -1),
            Tool("Metal Detector", R.drawable.ic_tool_metal_detector, R.id.action_action_experimental_tools_to_fragmentToolMetalDetector),
            Tool("Weather", R.drawable.ic_weather, -1),
            Tool("Health", R.drawable.ic_tool_health, -1),
            Tool("White Noise", R.drawable.ic_tool_white_noise, R.id.action_action_experimental_tools_to_fragmentToolWhiteNoise),
            Tool("Notes", R.drawable.ic_tool_notes, R.id.action_action_experimental_tools_to_fragmentToolNotes),
            Tool("Inventory", R.drawable.ic_inventory, R.id.action_action_experimental_tools_to_action_inventory),
            Tool("User Guide", R.drawable.ic_user_guide, R.id.action_action_experimental_tools_to_guideListFragment),
            )

        for (tool in content){
            if (tool.icon == -1){
                val text = TextView(requireContext())
                text.text = tool.name
                text.setTextColor(UiUtils.color(requireContext(), R.color.colorPrimary))
                text.layoutParams = GridLayout.LayoutParams().also {
                    it.columnSpec = GridLayout.spec(0, 3)
                }
                binding.toolGrid.addView(text)
            } else {
                val toolTile = TileButton(requireContext(), null)
                toolTile.setImageResource(tool.icon)
                toolTile.setText(tool.name)
                toolTile.setTextSize(12f)
                toolTile.setOnClickListener {
                    if (tool.action == -1){
                        when (tool.icon) {
                            R.drawable.maps -> TrailSenseMaps.open(requireContext())
                            R.drawable.ic_tool_health -> HealthSense.open(requireContext())
                            R.drawable.ic_weather -> NWSWeather.open(requireContext())
                        }
                    } else {
                        toolTile.setState(true)

                        navController.navigate(tool.action)
                    }
                }
                toolTile.width = (ViewMeasurementUtils.density(requireContext()) * 110).toInt()
                toolTile.height = (ViewMeasurementUtils.density(requireContext()) * 110).toInt()
                binding.toolGrid.addView(toolTile)
            }
        }



//        navigateOnClick(
//            findPreference(getString(R.string.tool_user_guide)),
//            R.id.action_action_experimental_tools_to_guideListFragment
//        )
//        navigateOnClick(
//            findPreference(getString(R.string.tool_bubble_level)),
//            R.id.action_action_experimental_tools_to_levelFragment
//        )
//        navigateOnClick(
//            findPreference(getString(R.string.tool_inclinometer)),
//            R.id.action_toolsFragment_to_inclinometerFragment
//        )
//        navigateOnClick(
//            findPreference(getString(R.string.tool_inventory)),
//            R.id.action_action_experimental_tools_to_action_inventory
//        )
//
//        val maps = findPreference<Preference>(getString(R.string.tool_trail_sense_maps))
//        maps?.isVisible = TrailSenseMaps.isInstalled(requireContext())
//        onClick(maps) { TrailSenseMaps.open(requireContext()) }
//
//        val health = findPreference<Preference>(getString(R.string.tool_health_sense))
//        health?.isVisible = HealthSense.isInstalled(requireContext())
//        onClick(health) { HealthSense.open(requireContext()) }
//
//        val nws = findPreference<Preference>("tool_nws_weather")
//        nws?.isVisible = NWSWeather.isInstalled(requireContext())
//        onClick(nws) { NWSWeather.open(requireContext()) }
//
//        val depth = findPreference<Preference>(getString(R.string.tool_depth))
//        depth?.isVisible = sensorChecker.hasBarometer()
//        navigateOnClick(depth, R.id.action_action_experimental_tools_to_toolDepthFragment)
//
//        navigateOnClick(
//            findPreference(getString(R.string.tool_cliff_height)),
//            R.id.action_action_experimental_tools_to_toolCliffHeightFragment
//        )
//
//        navigateOnClick(
//            findPreference(getString(R.string.tool_whistle)),
//            R.id.action_action_experimental_tools_to_toolWhistleFragment
//        )
//
//        navigateOnClick(
//            findPreference(getString(R.string.tool_distance_convert)),
//            R.id.action_action_experimental_tools_to_fragmentDistanceConverter
//        )
//
//        navigateOnClick(
//            findPreference(getString(R.string.tool_solar_panel)),
//            R.id.action_action_experimental_tools_to_fragmentToolSolarPanel
//        )
//
//        navigateOnClick(
//            findPreference(getString(R.string.tool_boil)),
//            R.id.action_action_experimental_tools_to_waterPurificationFragment
//        )
//
//        navigateOnClick(
//            findPreference(getString(R.string.tool_clock)),
//            R.id.action_action_experimental_tools_to_toolClockFragment
//        )
//
//        navigateOnClick(
//            findPreference(getString(R.string.tool_lightning)),
//            R.id.action_action_experimental_tools_to_fragmentToolLightning
//        )
//
//        navigateOnClick(
//            findPreference(getString(R.string.tool_ruler)),
//            R.id.action_action_experimental_tools_to_rulerFragment
//        )
//
//        navigateOnClick(
//            findPreference(getString(R.string.tool_battery)),
//            R.id.action_action_experimental_tools_to_fragmentToolBattery
//        )
//
//        navigateOnClick(
//            findPreference(getString(R.string.tool_triangulate)),
//            R.id.action_action_experimental_tools_to_fragmentToolTriangulate
//        )
//
//        navigateOnClick(
//            findPreference(getString(R.string.tool_metal_detector)),
//            R.id.action_action_experimental_tools_to_fragmentToolMetalDetector
//        )
//
//        navigateOnClick(
//            findPreference(getString(R.string.tool_notes)),
//            R.id.action_action_experimental_tools_to_fragmentToolNotes
//        )
//
//        navigateOnClick(
//            findPreference(getString(R.string.tool_backtrack)),
//            R.id.action_action_experimental_tools_to_fragmentBacktrack
//        )
//
//        navigateOnClick(
//            findPreference(getString(R.string.tool_coordinate_convert)),
//            R.id.action_action_experimental_tools_to_fragmentToolCoordinateConvert
//        )
//
//        val thermometer = findPreference<Preference>(getString(R.string.tool_thermometer))
//        thermometer?.isVisible = !sensorChecker.hasBarometer()
//        navigateOnClick(
//            thermometer,
//            R.id.action_action_experimental_tools_to_thermometerFragment
//        )
//
//        val clouds = findPreference<Preference>(getString(R.string.tool_clouds))
//        clouds?.isVisible = !sensorChecker.hasBarometer()
//        navigateOnClick(
//            clouds,
//            R.id.action_action_experimental_tools_to_cloudFragment
//        )
//
//        val isExperimentalEnabled = prefs.experimentalEnabled
//        val whiteNoise = findPreference<Preference>(getString(R.string.tool_white_noise))
//        whiteNoise?.isVisible = isExperimentalEnabled
//        navigateOnClick(whiteNoise, R.id.action_action_experimental_tools_to_fragmentToolWhiteNoise)
//
//        val flashlight = findPreference<Preference>(getString(R.string.tool_flashlight))
//        flashlight?.isVisible = Flashlight.hasFlashlight(requireContext())
//        navigateOnClick(
//            flashlight,
//            R.id.action_action_experimental_tools_to_fragmentToolFlashlight
//        )
    }

    data class Tool(val name: String, val icon: Int, val action: Int)

}