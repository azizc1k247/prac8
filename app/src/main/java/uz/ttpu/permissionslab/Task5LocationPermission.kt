package uz.ttpu.permissionslab

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

enum class LocationPermissionState {
    PRECISE, // Aniq (FINE_LOCATION)
    APPROXIMATE, // Taxminiy (COARSE_LOCATION)
    NONE // Ruxsat yo'q
}

@Composable
fun Task5LocationPermissionScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var locationState by remember { mutableStateOf(LocationPermissionState.NONE) }

    fun checkLocationPermissions() {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        locationState = when {
            fineGranted -> LocationPermissionState.PRECISE
            coarseGranted -> LocationPermissionState.APPROXIMATE
            else -> LocationPermissionState.NONE
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val fineGranted = permissionsMap[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissionsMap[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        locationState = when {
            fineGranted -> LocationPermissionState.PRECISE
            coarseGranted -> LocationPermissionState.APPROXIMATE
            else -> LocationPermissionState.NONE
        }
    }

    // ON_RESUME vaqtida ruxsat holatini har safar qayta tekshiramiz ("Only this time" ruxsat qaytarilganda aniqlash uchun)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkLocationPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "5-topshiriq: Bir Martalik Joylashuv Ruxsati",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (locationState) {
                    LocationPermissionState.PRECISE -> MaterialTheme.colorScheme.primaryContainer
                    LocationPermissionState.APPROXIMATE -> MaterialTheme.colorScheme.secondaryContainer
                    LocationPermissionState.NONE -> MaterialTheme.colorScheme.errorContainer
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Joylashuv Ruxsati Holati:",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                val statusText = when (locationState) {
                    LocationPermissionState.PRECISE -> "Aniq Joylashuv (Fine Location)"
                    LocationPermissionState.APPROXIMATE -> "Taxminiy Joylashuv (Coarse Location)"
                    LocationPermissionState.NONE -> "Ruxsat Berilmagan (Yo'q)"
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    launcher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }) {
                    Text(text = "Joylashuv Ruxsatini So'rash")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "'Faqat bu safar' (Only this time) tanlanganda: ilovani fonga olib o'tganda yoki ma'lum vaqtdan keyin ruxsat avtomatik bekor qilinadi. ON_RESUME orqali holat qayta tekshiriladi.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
