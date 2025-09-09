package com.niteshray.xapps.billingpro.features.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.niteshray.xapps.billingpro.ui.theme.*
import com.niteshray.xapps.billingpro.utils.ValidationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onProfileSetupComplete: () -> Unit = {},
    profileViewModel: ProfileViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var gstNumber by remember { mutableStateOf("") }

    val profileState by profileViewModel.profileState.collectAsState()

    // Handle profile setup success
    LaunchedEffect(profileState.isSuccess) {
        if (profileState.isSuccess) {
            profileViewModel.resetState()
            onProfileSetupComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryBlue.copy(alpha = 0.15f),
                        Color.White,
                        BackgroundLight
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Header Design
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App Icon
                    Card(
                        modifier = Modifier.size(80.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Store,
                                contentDescription = "Store",
                                modifier = Modifier.size(40.dp),
                                tint = PrimaryBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Setup Your Store Profile",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Complete your profile to start billing",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Personal Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Name Field
                    ProfileTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Full Name",
                        icon = Icons.Filled.Person,
                        keyboardType = KeyboardType.Text,
                        isError = name.isNotBlank() && !ValidationUtils.isValidName(name),
                        errorMessage = if (name.isNotBlank() && !ValidationUtils.isValidName(name)) "Enter a valid name" else null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mobile Number Field
                    ProfileTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = "Mobile Number",
                        icon = Icons.Filled.Phone,
                        keyboardType = KeyboardType.Phone,
                        isError = mobileNumber.isNotBlank() && !ValidationUtils.isValidMobileNumber(mobileNumber),
                        errorMessage = if (mobileNumber.isNotBlank() && !ValidationUtils.isValidMobileNumber(mobileNumber)) "Enter a valid 10-digit mobile number" else null
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Divider(color = Color.Gray.copy(alpha = 0.3f))

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Business Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Store Name Field
                    ProfileTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = "Store Name",
                        icon = Icons.Filled.Business,
                        keyboardType = KeyboardType.Text,
                        isError = storeName.isNotBlank() && !ValidationUtils.isValidStoreName(storeName),
                        errorMessage = if (storeName.isNotBlank() && !ValidationUtils.isValidStoreName(storeName)) "Enter a valid store name" else null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // GST Number Field
                    ProfileTextField(
                        value = gstNumber,
                        onValueChange = { gstNumber = it },
                        label = "GST Number (Optional)",
                        icon = Icons.Filled.Receipt,
                        keyboardType = KeyboardType.Text,
                        isError = gstNumber.isNotBlank() ,
                        errorMessage = if (gstNumber.isNotBlank()) "Enter a number" else null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error message display
                    profileState.errorMessage?.let { errorMessage ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                color = ErrorRed,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Setup Profile Button
                    Button(
                        onClick = {
                            profileViewModel.setupProfile(
                                name = name,
                                mobileNumber = mobileNumber,
                                storeName = storeName,
                                gstNumber = gstNumber
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue
                        ),
                        enabled = !profileState.isLoading && name.isNotBlank() && 
                                 mobileNumber.isNotBlank() && storeName.isNotBlank() &&
                                 ValidationUtils.isValidName(name) &&
                                 ValidationUtils.isValidMobileNumber(mobileNumber) &&
                                 ValidationUtils.isValidStoreName(storeName) &&
                                 (gstNumber.isBlank())
                    ) {
                        if (profileState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Complete Setup",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // Loading overlay
        if (profileState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Setting up your profile...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { 
                Text(
                    text = label,
                    fontSize = 14.sp
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isError) ErrorRed else PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) ErrorRed else PrimaryBlue,
                unfocusedBorderColor = if (isError) ErrorRed else Color.Gray.copy(alpha = 0.5f),
                focusedLabelColor = if (isError) ErrorRed else PrimaryBlue,
                cursorColor = PrimaryBlue,
                focusedLeadingIconColor = if (isError) ErrorRed else PrimaryBlue,
                unfocusedLeadingIconColor = if (isError) ErrorRed else Color.Gray
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            isError = isError
        )
        
        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = ErrorRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
