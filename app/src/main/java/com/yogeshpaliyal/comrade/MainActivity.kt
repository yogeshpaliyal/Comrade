package com.yogeshpaliyal.comrade

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yogeshpaliyal.comrade.ui.screen.homepage.HomeViewModel
import com.yogeshpaliyal.comrade.ui.screen.homepage.Homepage
import com.yogeshpaliyal.comrade.ui.screen.settings.SettingsPage
import com.yogeshpaliyal.comrade.ui.theme.ComradeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable


data class TopLevelRoute<T : Any>(val name: String, val route: T, val icon: ImageVector)

@Serializable
object Home

@Serializable
object Settings

val topLevelRoutes = listOf(
    TopLevelRoute("Home", Home, Icons.Outlined.Home),
    TopLevelRoute("Settings", Settings, Icons.Outlined.Settings)
)

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComradeTheme() {
                ComradeApp()
            }
        }
    }
}

@Composable
fun ComradeApp(viewModel: HomeViewModel = hiltViewModel()) {

        val navController = rememberNavController()
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    topLevelRoutes.forEach { topLevelRoute ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    topLevelRoute.icon,
                                    contentDescription = topLevelRoute.name
                                )
                            },
                            label = { Text(topLevelRoute.name) },
                            selected = currentDestination?.hierarchy?.any {
                                it.hasRoute(
                                    topLevelRoute.route::class
                                )
                            } == true,
                            onClick = {
                                navController.navigate(topLevelRoute.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }) { innerPadding ->
            NavHost(
                navController, startDestination = Home, Modifier.padding(innerPadding)
            ) {
                composable<Home> { Homepage(viewModel = viewModel) }
                composable<Settings> {
                    SettingsPage(
                        viewModel = viewModel
                    )
                }
            }
        }

}
