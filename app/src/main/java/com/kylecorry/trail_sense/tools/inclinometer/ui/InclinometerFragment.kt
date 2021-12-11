package com.kylecorry.trail_sense.tools.inclinometer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kylecorry.andromeda.core.sensors.asLiveData
import com.kylecorry.andromeda.core.time.Throttle
import com.kylecorry.andromeda.fragments.BoundFragment
import com.kylecorry.andromeda.sense.orientation.DeviceOrientation
import com.kylecorry.sol.science.geology.AvalancheRisk
import com.kylecorry.sol.science.geology.GeologyService
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.databinding.FragmentInclinometerBinding
import com.kylecorry.trail_sense.shared.CustomUiUtils
import com.kylecorry.trail_sense.shared.colors.AppColor
import com.kylecorry.trail_sense.shared.sensors.SensorService

class InclinometerFragment : BoundFragment<FragmentInclinometerBinding>() {

    private val sensorService by lazy { SensorService(requireContext()) }
    private val inclinometer by lazy { sensorService.getInclinometer() }
    private val deviceOrientation by lazy { sensorService.getDeviceOrientationSensor() }
    private val geology = GeologyService()
    private val throttle = Throttle(20)

    private var slopeIncline: Float? = null
    private var slopeAngle: Float? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.setOnClickListener {
            if (slopeIncline == null && isOrientationValid()) {
                slopeAngle = inclinometer.angle
                slopeIncline = inclinometer.incline
            } else {
                slopeAngle = null
                slopeIncline = null
            }
        }

        CustomUiUtils.setButtonState(binding.inclinometerLeftQuickAction, false)
        CustomUiUtils.setButtonState(binding.inclinometerRightQuickAction, false)

        inclinometer.asLiveData().observe(viewLifecycleOwner, { onInclinometerUpdate() })
        deviceOrientation.asLiveData().observe(viewLifecycleOwner, { onDeviceOrientationUpdate() })

    }

    private fun updateUI() {

        if (throttle.isThrottled()) {
            return
        }

        if (!isOrientationValid() && slopeIncline == null) {
            binding.sideInclinometer.reset()
            binding.sideInclinometer.message = getString(R.string.inclinometer_rotate_device)
            return
        }

        binding.sideInclinometer.angle = slopeAngle ?: inclinometer.angle
        binding.unitAngle.angle = slopeAngle ?: inclinometer.angle
        binding.sideInclinometer.incline = slopeIncline ?: inclinometer.incline

        val avalancheRisk = geology.getAvalancheRisk(
            slopeIncline ?: inclinometer.incline
        )

        binding.sideInclinometer.color = when(avalancheRisk){
            AvalancheRisk.Low -> AppColor.Gray.color
            AvalancheRisk.High -> AppColor.Red.color
            AvalancheRisk.Moderate -> AppColor.Yellow.color
        }

        binding.sideInclinometer.locked = slopeAngle != null
        binding.sideInclinometer.message = getAvalancheRiskString(avalancheRisk)
    }


    private fun getAvalancheRiskString(risk: AvalancheRisk): String {
        return when (risk) {
            AvalancheRisk.Low -> getString(R.string.avalanche_risk_low)
            AvalancheRisk.Moderate -> getString(R.string.avalanche_risk_med)
            AvalancheRisk.High -> getString(R.string.avalanche_risk_high)
        }
    }

    private fun isOrientationValid(): Boolean {
        return deviceOrientation.orientation != DeviceOrientation.Orientation.Flat && deviceOrientation.orientation != DeviceOrientation.Orientation.FlatInverse
    }

    private fun onInclinometerUpdate(): Boolean {
        updateUI()
        return true
    }

    private fun onDeviceOrientationUpdate(): Boolean {
        updateUI()
        return true
    }

    override fun generateBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentInclinometerBinding {
        return FragmentInclinometerBinding.inflate(layoutInflater, container, false)
    }

}
