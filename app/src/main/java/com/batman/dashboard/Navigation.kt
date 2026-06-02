package com.batman.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.batman.dashboard.ui.comms.ChatScreen
import com.batman.dashboard.ui.comms.CommsScreen
import com.batman.dashboard.ui.comms.CommsViewModel
import com.batman.dashboard.ui.emergency.EmergencyScreen
import com.batman.dashboard.ui.emergency.EmergencyViewModel
import com.batman.dashboard.ui.equipment.EquipmentScreen
import com.batman.dashboard.ui.equipment.EquipmentViewModel
import com.batman.dashboard.ui.home.HomeScreen
import com.batman.dashboard.ui.map.GothamMapScreen
import com.batman.dashboard.ui.map.MapViewModel
import com.batman.dashboard.ui.missions.MissionsScreen
import com.batman.dashboard.ui.missions.MissionsViewModel
import com.batman.dashboard.ui.music.MusicPlayerScreen
import com.batman.dashboard.ui.wayne.WayneEnterprisesScreen

@Composable
fun BatmanNavigation() {
    val context = LocalContext.current
    val app = context.applicationContext as BatmanApp
    val container = app.container
    val backStack = rememberNavBackStack(HomeKey)

    val missionsVm: MissionsViewModel = viewModel(factory = MissionsViewModel.factory(container.missionDao))
    val commsVm: CommsViewModel = viewModel(factory = CommsViewModel.factory(container.messageDao))
    val equipmentVm: EquipmentViewModel = viewModel(factory = EquipmentViewModel.factory(container.equipmentDao))
    val mapVm: MapViewModel = viewModel(factory = MapViewModel.factory(container.crimePinDao))
    val emergencyVm: EmergencyViewModel = viewModel(factory = EmergencyViewModel.factory(container.emergencyDao))

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<HomeKey> {
                HomeScreen(
                    missionDao = container.missionDao,
                    crimePinDao = container.crimePinDao,
                    onNavigate = { key -> backStack.add(key) }
                )
            }
            entry<MissionsKey> {
                MissionsScreen(viewModel = missionsVm, onBack = { backStack.removeLastOrNull() })
            }
            entry<CommsKey> {
                CommsScreen(viewModel = commsVm, onAllyClick = { id, name -> backStack.add(ChatKey(id, name)) }, onBack = { backStack.removeLastOrNull() })
            }
            entry<ChatKey> { key ->
                ChatScreen(allyId = key.allyId, allyName = key.allyName, viewModel = commsVm, onBack = { backStack.removeLastOrNull() })
            }
            entry<EquipmentKey> {
                EquipmentScreen(viewModel = equipmentVm, onBack = { backStack.removeLastOrNull() })
            }
            entry<MapKey> {
                GothamMapScreen(viewModel = mapVm, onBack = { backStack.removeLastOrNull() })
            }
            entry<MusicKey> {
                MusicPlayerScreen(onBack = { backStack.removeLastOrNull() })
            }
            entry<WayneKey> {
                WayneEnterprisesScreen(onBack = { backStack.removeLastOrNull() })
            }
            entry<EmergencyKey> {
                EmergencyScreen(viewModel = emergencyVm, onBack = { backStack.removeLastOrNull() })
            }
        }
    )
}
