package at.apramendorfer.authenticator

import android.content.SharedPreferences
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import at.apramendorfer.authenticator.service.BiometricPromptManager

sealed class NavRoute(val path: String) {
    data object Home : NavRoute("home")

    data object Settings : NavRoute("setting")
}

@Composable
fun AuthenticatorNavHost(
    navController: NavHostController,
    promptManager: BiometricPromptManager,
    sharedPreferences: SharedPreferences
) {
    val vm = viewModel<AuthRequestViewModel>()

    NavHost(
        navController = navController,
        startDestination = NavRoute.Home.path
    ) {
        addHomeScreen(navController, this, promptManager, sharedPreferences, vm)
        addSettingsScreen(navController, this, sharedPreferences)
    }
}

private fun addHomeScreen(
    navController: NavHostController,
    navGraphBuilder: NavGraphBuilder,
    promptManager: BiometricPromptManager,
    sharedPreferences: SharedPreferences,
    vm: AuthRequestViewModel
) {
    navGraphBuilder.composable(route = NavRoute.Home.path, exitTransition = {
        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
    },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        }) {
        AuthenticatorHomeScreen(
            promptManager = promptManager,
            goToSettings = { navController.navigate(NavRoute.Settings.path) },
            sharedPreferences = sharedPreferences,
            vm
        )
    }
}

private fun addSettingsScreen(
    navController: NavHostController,
    navGraphBuilder: NavGraphBuilder,
    sharedPreferences: SharedPreferences
) {
    navGraphBuilder.composable(
        route = NavRoute.Settings.path,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(300)
            )
        }
    ) {
        SettingsView(
            goBack = { navController.popBackStack() },
            sharedPreferences = sharedPreferences
        )
    }
}
