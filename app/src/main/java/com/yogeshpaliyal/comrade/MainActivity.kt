package com.yogeshpaliyal.comrade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yogeshpaliyal.comrade.ui.screen.Homepage
import com.yogeshpaliyal.comrade.ui.theme.ComradeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.yogeshpaliyal.comrade.utils.DriveServiceHelper
import javax.inject.Inject


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

    private var mDriveService: DriveServiceHelper? = null

    private fun initiateGoogleDrive() {
        val googleAccount = GoogleSignIn.getLastSignedInAccount(this)
        mDriveService = googleAccount?.account?.let {
            val credential =
                GoogleAccountCredential.usingOAuth2(
                    this, setOf(DriveScopes.DRIVE_FILE)
                )
            credential.setSelectedAccount(it)

            val googleDriveService =
                Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory(),
                    credential
                )
                    .setApplicationName("Drive API Migration")
                    .build()

            DriveServiceHelper(googleDriveService)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initiateGoogleDrive()
        if (mDriveService == null) {
            val dialog = GDriveLoginDialog()
            dialog.setListener() {
                if (it != null) {
                    mDriveService = it
                }
                dialog.dismiss()
            }
            dialog.show(supportFragmentManager, "GDriveLoginDialog")
        }

        setContent {
            ComradeTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        BottomNavigation {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            topLevelRoutes.forEach { topLevelRoute ->
                                BottomNavigationItem(
                                    icon = { Icon(topLevelRoute.icon, contentDescription = topLevelRoute.name) },
                                    label = { Text(topLevelRoute.name, color = Color.White) },
                                    selected = currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true,
                                    onClick = {
                                        navController.navigate(topLevelRoute.route) {
                                            // Pop up to the start destination of the graph to
                                            // avoid building up a large stack of destinations
                                            // on the back stack as users select items
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            // Avoid multiple copies of the same destination when
                                            // reselecting the same item
                                            launchSingleTop = true
                                            // Restore state when reselecting a previously selected item
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(navController, startDestination = Home, Modifier.padding(innerPadding)) {
                        composable<Home> { Homepage() }
                        composable<Settings> { Homepage() }
                    }
                }

            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComradeTheme {
        Greeting("Android")
    }
}