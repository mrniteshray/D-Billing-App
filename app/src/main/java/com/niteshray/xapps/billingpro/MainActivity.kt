package com.niteshray.xapps.billingpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.niteshray.xapps.billingpro.ui.screens.MainScreen
import com.niteshray.xapps.billingpro.features.ProductManagement.ui.ProductManagementScreen
import com.niteshray.xapps.billingpro.features.auth.ui.SignInScreen
import com.niteshray.xapps.billingpro.features.auth.ui.SignUpScreen
import com.niteshray.xapps.billingpro.ui.theme.BillingProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BillingProTheme {
                BillingProApp()
            }
        }
    }
}

@Composable
fun BillingProApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "signin"
    ) {
        composable("signin") {
            SignInScreen(
                onSignInClick = {
                    navController.navigate("main") {
                        popUpTo("signin") { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navController.navigate("signup")
                }
            )
        }
        
        composable("signup") {
            SignUpScreen(
                onSignUpClick = {
                    navController.navigate("main") {
                        // Clear the back stack so user can't go back to signup
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onSignInClick = {
                    // Navigate back to sign in screen
                    navController.popBackStack()
                }
            )
        }
        
        composable("main") {
            MainScreen(
                onNavigateToProducts = {
                    navController.navigate("products")
                },
                onLogout = {
                    navController.navigate("signin") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
        
        composable("products") {
            ProductManagementScreen()
        }
    }
}