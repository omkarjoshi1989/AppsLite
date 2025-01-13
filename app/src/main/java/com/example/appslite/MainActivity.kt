package com.example.appslite
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
                // Fetch installed apps
                val apps = remember { getInstalledApps(this) }

                // Display apps in a LazyColumn
                AppList(apps)
        }
    }
}

@Composable
fun AppList(apps: List<AppInfo>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(apps) { app ->
            AppItem(app)
        }
    }
}

@Composable
fun AppItem(app: AppInfo) {
    val context = LocalContext.current
    Row(modifier = Modifier
        .padding(start = 16.dp, bottom = 8.dp)
        .clickable {
            openApp(context, app.packageName)
        }) {
        Image(
            bitmap = app.icon.toBitmap(32, 32).asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(text = app.name)
            Text(text = "(${app.packageName})", style = MaterialTheme.typography.bodySmall)
        }
    }
}

fun openApp(context: Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) {
        context.startActivity(intent)
    } else {
        // Handle case where the app cannot be launched
        Toast.makeText(context, "Cannot open the app", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {

        AppList(
            listOf(
//                AppInfo("App Name", "com.example.sample", context.getDrawable(R.drawable.ic_launcher_foreground)!!),
//                AppInfo("Second App Name", "com.example.secondapppackage"),
            )
        )

}

data class AppInfo(val name: String, val packageName: String, val icon: Drawable)


fun getInstalledApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
    return packages.filter {
        (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                (it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
    }.map {
        AppInfo(
            name = pm.getApplicationLabel(it).toString(),
            packageName = it.packageName,
            icon = pm.getApplicationIcon(it)
        )
    }.sortedBy { it.name }
}