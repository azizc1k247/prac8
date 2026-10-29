package uz.ttpu.permissionslab

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

enum class PermissionStatus {
    GRANTED,
    RATIONALE,
    DENIED
}

@Composable
fun Task3CameraPermissionScreen() {
    val context = LocalContext.current
    val activity = context as? Activity

    var permissionStatus by remember { mutableStateOf(PermissionStatus.DENIED) }
    var requestAttempted by remember { mutableStateOf(false) }

    fun updateCameraPermissionStatus() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Android 6.0 (API 23) dan past versiyalarda ruxsat o'rnatishda beriladi
            permissionStatus = PermissionStatus.GRANTED
            return
        }

        val isGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            permissionStatus = PermissionStatus.GRANTED
        } else {
            val showRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
            } ?: false

            permissionStatus = if (showRationale) {
                PermissionStatus.RATIONALE
            } else {
                PermissionStatus.DENIED
            }
        }
    }

    // Direct permission launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        requestAttempted = true
        updateCameraPermissionStatus()
    }

    // Refresh when screen resumes (e.g. returning from Settings)
    DisposableEffect(context) {
        updateCameraPermissionStatus()
        val lifecycleOwner = context as? androidx.lifecycle.LifecycleOwner
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                updateCameraPermissionStatus()
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
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
            text = "3-topshiriq: Kamera Ekrani (3 Holat)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (permissionStatus) {
            PermissionStatus.GRANTED -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "1-HOLAT: RUXSAT BERILGAN",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Kamera ruxsati muvaffaqiyatli olingan! Kamera ishga tayyor.",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            PermissionStatus.RATIONALE -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "2-HOLAT: RATIONALE (TUSHUNTIRISH)",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Rasmga olish funksiyasidan foydalanish uchun ilovaga kamera ruxsati zarur.",
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                launcher.launch(Manifest.permission.CAMERA)
                            }
                        }) {
                            Text(text = "Ruxsat so'rash")
                        }
                    }
                }
            }

            PermissionStatus.DENIED -> {
                val isPermanentlyDenied = requestAttempted && !(activity?.let {
                    ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
                } ?: false)

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "3-HOLAT: RUXSAT YO'Q",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (isPermanentlyDenied) {
                            Text(
                                text = "Ruxsat ikki marta rad etildi yoki 'Qayta so'ralmasin' belgilandi. Sozlamalardan qo'lda ruxsat bering.",
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null)
                                )
                                context.startActivity(intent)
                            }) {
                                Text(text = "Sozlamalarni ochish")
                            }
                        } else {
                            Text(
                                text = "Kameradan foydalanish uchun ruxsat berilmagan.",
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    launcher.launch(Manifest.permission.CAMERA)
                                }
                            }) {
                                Text(text = "Ruxsat so'rash")
                            }
                        }
                    }
                }
            }
        }
    }
}
