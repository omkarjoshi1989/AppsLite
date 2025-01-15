package com.example.appslite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val apps = remember { getInstalledApps(this) }
            var searchQuery by remember { mutableStateOf("") }
            val filteredApps = if (searchQuery.isEmpty()) apps else apps.filter {
                it.name.contains(
                    searchQuery,
                    ignoreCase = true
                )
            }
            var isGridView by remember { mutableStateOf(true) }

            Column(modifier = Modifier.fillMaxSize()) {
                SearchBox(searchQuery) { searchQuery = it }
                Button(modifier = Modifier.fillMaxWidth(), onClick = { isGridView = !isGridView }) {
                    Text(if (isGridView) "Switch to List View" else "Switch to Grid View")
                }
                if (isGridView) {
                    AppGrid(filteredApps)
                } else {
                    AppList(filteredApps)
                }
            }
        }
    }
}

@Composable
fun SearchBox(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Search Apps") },
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
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
fun AppGrid(apps: List<AppInfo>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(apps) { app ->
            GridItem(app)
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
            bitmap = app.icon.toBitmap().asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .padding(8.dp),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(text = app.name, fontSize = 18.sp)
            Text(text = "(${app.packageName})", fontSize = 12.sp)
        }
    }
}

@Composable
fun GridItem(app: AppInfo) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clickable { openApp(context, app.packageName) },
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = app.icon.toBitmap().asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            contentScale = ContentScale.Crop
        )
        Text(
            text = app.name,
            fontSize = 8.sp,
            maxLines = 2,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

fun openApp(context: Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "Cannot open the app", Toast.LENGTH_SHORT).show()
    }
}

data class AppInfo(val name: String, val packageName: String, val icon: Drawable)

fun getInstalledApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
    return apps.filter {
        val packageName = it.packageName
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        launchIntent != null
    }.map {
        AppInfo(
            name = pm.getApplicationLabel(it).toString(),
            packageName = it.packageName,
            icon = pm.getApplicationIcon(it)
        )
    }.sortedBy { it.name }
}

@Preview
@Composable
fun GridItemPreview() {
    val context = LocalContext.current
    val drawable: Drawable? = context.getDrawable(R.drawable.ic_launcher_background)
    drawable?.let { AppInfo("app", "com.package", it) }?.let { GridItem(it) }
}
