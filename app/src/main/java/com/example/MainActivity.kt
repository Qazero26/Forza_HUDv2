package com.example

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

val LocalDashboardTheme = staticCompositionLocalOf<DashboardTheme> { error("No theme provided") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ForzaViewModel = viewModel()
            val currentTheme by viewModel.currentTheme.collectAsState()
            
            MyApplicationTheme(darkTheme = !currentTheme.isLight) {
                CompositionLocalProvider(LocalDashboardTheme provides currentTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = currentTheme.background
                    ) {
                        ForzaDashboardApp(viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForzaDashboardApp(viewModel: ForzaViewModel = viewModel()) {
    val context = LocalContext.current
    val telemetryState by viewModel.telemetryState.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val localIp by viewModel.localIpAddress.collectAsState()
    val isMph by viewModel.isMph.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val currentLayout by viewModel.currentLayout.collectAsState()

    var portInput by remember { mutableStateOf("5300") }
    var ipInput by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(localIp) {
        if (ipInput.isEmpty() && localIp.isNotEmpty()) {
            ipInput = localIp
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val theme = LocalDashboardTheme.current

    LaunchedEffect(Unit) {
        viewModel.updateLocalIp(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        // Radial Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(theme.glowColor.copy(alpha = 0.08f), Color.Transparent),
                        radius = 1000f
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Bar Simulation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "FORZALINK",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.textSecondary,
                        letterSpacing = 2.sp
                    )
                    if (isListening) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(theme.secondary)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                    }
                }
                
                IconButton(onClick = { showSettings = true }) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = theme.textSecondary)
                }
            }

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        DashboardCluster(telemetryState, isLandscape = true, isMph = isMph, layout = currentLayout)
                    }
                    
                    Box(
                        modifier = Modifier
                            .width(360.dp)
                            .fillMaxHeight()
                            .background(
                                color = theme.surface,
                                shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = theme.textPrimary.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)
                            )
                            .padding(24.dp)
                    ) {
                        ConnectionPanel(
                            localIp = ipInput,
                            onIpInputChange = { ipInput = it },
                            portInput = portInput,
                            onPortInputChange = { portInput = it },
                            isListening = isListening,
                            onToggleListen = { viewModel.toggleListening(portInput) },
                            isLandscape = true
                        )
                    }
                }
            } else {
                // Dashboard View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    DashboardCluster(telemetryState, isLandscape = false, isMph = isMph, layout = currentLayout)
                }

                // Bottom Connection Sheet
                Box(
                    modifier = Modifier
                        .background(
                            color = theme.surface,
                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = theme.textPrimary.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                        )
                        .padding(24.dp)
                        .padding(bottom = 16.dp)
                ) {
                    ConnectionPanel(
                        localIp = ipInput,
                        onIpInputChange = { ipInput = it },
                        portInput = portInput,
                        onPortInputChange = { portInput = it },
                        isListening = isListening,
                        onToggleListen = { viewModel.toggleListening(portInput) },
                        isLandscape = false
                    )
                }
            }
        }
    }
    
    if (showSettings) {
        SettingsDialog(
            currentTheme = currentTheme,
            currentLayout = currentLayout,
            isMph = isMph,
            onThemeSelected = { viewModel.setTheme(it) },
            onLayoutSelected = { viewModel.setLayout(it) },
            onMphToggle = { viewModel.setMph(it) },
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
fun ConnectionPanel(
    localIp: String,
    onIpInputChange: (String) -> Unit,
    portInput: String,
    onPortInputChange: (String) -> Unit,
    isListening: Boolean,
    onToggleListen: () -> Unit,
    isLandscape: Boolean
) {
    val theme = LocalDashboardTheme.current

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = if(isLandscape) Modifier.fillMaxHeight() else Modifier) {
        if(!isLandscape) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(4.dp)
                    .background(theme.textPrimary.copy(alpha = 0.2f), CircleShape)
            )
            Spacer(modifier = Modifier.height(24.dp))
        } else {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "CONNECTION SETUP",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textSecondary,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // IP Address Field
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "CONSOLE IP ADDRESS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textSecondary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(theme.background.copy(alpha=0.5f), RoundedCornerShape(16.dp))
                        .border(1.dp, theme.textPrimary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = localIp,
                        onValueChange = onIpInputChange,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        singleLine = true,
                        enabled = !isListening,
                        textStyle = LocalTextStyle.current.copy(
                            color = theme.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Port Field
            Column(modifier = Modifier.width(96.dp)) {
                Text(
                    "PORT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textSecondary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(theme.background.copy(alpha=0.5f), RoundedCornerShape(16.dp))
                        .border(1.dp, theme.textPrimary.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = portInput,
                        onValueChange = onPortInputChange,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isListening,
                        textStyle = LocalTextStyle.current.copy(
                            color = theme.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onToggleListen,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isListening) Color(0xFFEF4444) else theme.primary
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = if (isListening) "DISCONNECT" else "ESTABLISH LINK",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isListening) Icons.Default.Close else Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isListening) "Receiving Data Out from Horizon..." else "Waiting for Data Out from Horizon...",
            color = theme.textSecondary,
            fontSize = 11.sp,
            fontStyle = FontStyle.Italic
        )

        if(isLandscape) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun DashboardCluster(data: TelemetryData, isLandscape: Boolean, isMph: Boolean, layout: DashboardLayout) {
    if (isLandscape) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            when (layout) {
                DashboardLayout.CLASSIC_ARC -> ClassicArcLayout(data, isMph)
                DashboardLayout.F1_LINEAR -> F1LinearLayout(data, isMph)
                DashboardLayout.DIGITAL_CLEAN -> DigitalCleanLayout(data, isMph)
                DashboardLayout.NEON_RING -> NeonRingLayout(data, isMph)
                DashboardLayout.SPORT_TACH -> SportTachLayout(data, isMph)
                DashboardLayout.SUPERCAR_DASH -> SupercarDashLayout(data, isMph)
                DashboardLayout.RALLY_HUD -> RallyHudLayout(data, isMph)
            }
            
            Spacer(modifier = Modifier.width(48.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TelemetryBadge(
                    label = "RPM",
                    value = "${data.currentEngineRpm.toInt()}",
                    modifier = Modifier.width(110.dp)
                )
                TelemetryBadge(
                    label = "IDLE",
                    value = "${data.engineIdleRpm.toInt()}",
                    modifier = Modifier.width(110.dp)
                )
                TelemetryBadge(
                    label = "REDLINE",
                    value = "${data.engineMaxRpm.toInt()}",
                    modifier = Modifier.width(110.dp)
                )
            }
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when (layout) {
                DashboardLayout.CLASSIC_ARC -> ClassicArcLayout(data, isMph)
                DashboardLayout.F1_LINEAR -> F1LinearLayout(data, isMph)
                DashboardLayout.DIGITAL_CLEAN -> DigitalCleanLayout(data, isMph)
                DashboardLayout.NEON_RING -> NeonRingLayout(data, isMph)
                DashboardLayout.SPORT_TACH -> SportTachLayout(data, isMph)
                DashboardLayout.SUPERCAR_DASH -> SupercarDashLayout(data, isMph)
                DashboardLayout.RALLY_HUD -> RallyHudLayout(data, isMph)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Telemetry Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TelemetryBadge(
                    label = "RPM",
                    value = "${data.currentEngineRpm.toInt()}",
                    modifier = Modifier.weight(1f)
                )
                TelemetryBadge(
                    label = "IDLE",
                    value = "${data.engineIdleRpm.toInt()}",
                    modifier = Modifier.weight(1f)
                )
                TelemetryBadge(
                    label = "REDLINE",
                    value = "${data.engineMaxRpm.toInt()}",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

fun getGearDisplay(gear: Int): String {
    return when(gear) {
        0 -> "N"
        11 -> "R"
        else -> "G$gear"
    }
}

@Composable
fun ClassicArcLayout(data: TelemetryData, isMph: Boolean) {
    val theme = LocalDashboardTheme.current
    
    Box(
        modifier = Modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        val animatedRpm by animateFloatAsState(
            targetValue = data.currentEngineRpm,
            animationSpec = tween(durationMillis = 100),
            label = "rpm_anim"
        )
        val max = if (data.engineMaxRpm > 100) data.engineMaxRpm else 8000f
        val progress = (animatedRpm / max).coerceIn(0f, 1f)

        Canvas(modifier = Modifier
            .matchParentSize()
            .padding(16.dp)) {
            val startAngle = 135f
            val sweepAngle = 270f
            
            // Background arc
            drawArc(
                color = theme.textPrimary.copy(alpha = 0.1f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Active RPM arc
            drawArc(
                color = theme.primary, // Theme primary
                startAngle = startAngle,
                sweepAngle = sweepAngle * progress,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Speed & Gear Display in Center
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(y = (-4).dp)) {
            val displaySpeed = if (isMph) (data.speedKmh * 0.621371f).toInt() else data.speedKmh.toInt()
            Text(
                text = "$displaySpeed",
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = theme.textPrimary
            )
            Text(
                if (isMph) "MPH" else "KM/H", 
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = theme.secondary,
                letterSpacing = 2.sp,
                modifier = Modifier.offset(y = (-8).dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Box(
                modifier = Modifier
                    .background(theme.textPrimary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .border(1.dp, theme.textPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = getGearDisplay(data.gear),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    color = theme.textPrimary
                )
            }
        }
    }
}

@Composable
fun F1LinearLayout(data: TelemetryData, isMph: Boolean) {
    val theme = LocalDashboardTheme.current
    val animatedRpm by animateFloatAsState(
        targetValue = data.currentEngineRpm,
        animationSpec = tween(durationMillis = 100),
        label = "rpm_anim"
    )
    val max = if (data.engineMaxRpm > 100) data.engineMaxRpm else 8000f
    val progress = (animatedRpm / max).coerceIn(0f, 1f)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(260.dp)) {
        // RPM Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(theme.textPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(theme.secondary, theme.primary, Color(0xFFEF4444)),
                            endX = Float.POSITIVE_INFINITY
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Gear
            Text(
                text = getGearDisplay(data.gear),
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                color = theme.primary
            )
            Spacer(modifier = Modifier.width(32.dp))
            // Speed
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val displaySpeed = if (isMph) (data.speedKmh * 0.621371f).toInt() else data.speedKmh.toInt()
                Text(
                    text = "$displaySpeed",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Black,
                    color = theme.textPrimary
                )
                Text(
                    if (isMph) "MPH" else "KM/H", 
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.secondary,
                    letterSpacing = 2.sp,
                    modifier = Modifier.offset(y = (-8).dp)
                )
            }
        }
    }
}

@Composable
fun DigitalCleanLayout(data: TelemetryData, isMph: Boolean) {
    val theme = LocalDashboardTheme.current
    
    Box(
        modifier = Modifier
            .size(260.dp)
            .background(theme.surface.copy(alpha = 0.5f), CircleShape)
            .border(2.dp, theme.primary.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "GEAR",
                fontSize = 12.sp,
                color = theme.textSecondary,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = getGearDisplay(data.gear),
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = theme.textPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val displaySpeed = if (isMph) (data.speedKmh * 0.621371f).toInt() else data.speedKmh.toInt()
            Text(
                text = "$displaySpeed",
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                color = theme.textPrimary
            )
            Text(
                if (isMph) "MPH" else "KM/H", 
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = theme.primary,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun NeonRingLayout(data: TelemetryData, isMph: Boolean) {
    val theme = LocalDashboardTheme.current
    val animatedRpm by animateFloatAsState(
        targetValue = data.currentEngineRpm,
        animationSpec = tween(durationMillis = 100),
        label = "rpm_anim"
    )
    val max = if (data.engineMaxRpm > 100) data.engineMaxRpm else 8000f
    val progress = (animatedRpm / max).coerceIn(0f, 1f)

    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize().padding(16.dp)) {
            // Draw background track
            drawArc(
                color = theme.textPrimary.copy(alpha = 0.05f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx())
            )
            
            // Progress ring
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(theme.primary, theme.secondary, theme.glowColor, theme.primary)
                ),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Inner Content
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val displaySpeed = if (isMph) (data.speedKmh * 0.621371f).toInt() else data.speedKmh.toInt()
            Text(
                text = getGearDisplay(data.gear),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = theme.primary
            )
            Text(
                text = "$displaySpeed",
                fontSize = 80.sp,
                fontWeight = FontWeight.Black,
                color = theme.textPrimary,
                modifier = Modifier.offset(y = (-8).dp)
            )
            Text(
                text = if (isMph) "MPH" else "KM/H",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textSecondary,
                letterSpacing = 4.sp,
                modifier = Modifier.offset(y = (-16).dp)
            )
        }
    }
}

@Composable
fun SportTachLayout(data: TelemetryData, isMph: Boolean) {
    val theme = LocalDashboardTheme.current
    val animatedRpm by animateFloatAsState(
        targetValue = data.currentEngineRpm,
        animationSpec = tween(durationMillis = 100),
        label = "rpm_anim"
    )
    val max = if (data.engineMaxRpm > 100) data.engineMaxRpm else 8000f
    val progress = (animatedRpm / max).coerceIn(0f, 1f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(280.dp)
                .height(140.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                val startAngle = 180f
                val sweepAngle = 180f
                
                // Track
                drawArc(
                    color = theme.textPrimary.copy(alpha = 0.1f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Butt)
                )
                
                // Redline area
                drawArc(
                    color = Color(0xFFEF4444).copy(alpha = 0.3f),
                    startAngle = 180f + (180f * 0.85f),
                    sweepAngle = 180f * 0.15f,
                    useCenter = false,
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Butt)
                )

                // Fill
                drawArc(
                    color = if (progress > 0.85f) Color(0xFFEF4444) else theme.primary,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * progress,
                    useCenter = false,
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Butt)
                )
            }
            
            Text(
                text = "${(animatedRpm / 1000).toInt()}k RPM",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textSecondary,
                modifier = Modifier.offset(y = (-16).dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            val displaySpeed = if (isMph) (data.speedKmh * 0.621371f).toInt() else data.speedKmh.toInt()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$displaySpeed",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    color = theme.textPrimary
                )
                Text(
                    if (isMph) "MPH" else "KM/H", 
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.secondary,
                    letterSpacing = 2.sp
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = getGearDisplay(data.gear),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    color = theme.primary
                )
                Text(
                    "GEAR", 
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textSecondary,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
fun SupercarDashLayout(data: TelemetryData, isMph: Boolean) {
    val theme = LocalDashboardTheme.current
    val animatedRpm by animateFloatAsState(
        targetValue = data.currentEngineRpm,
        animationSpec = tween(durationMillis = 100),
        label = "rpm_anim"
    )
    val max = if (data.engineMaxRpm > 100) data.engineMaxRpm else 8000f
    val progress = (animatedRpm / max).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .width(280.dp)
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Angled RPM bar using Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val barHeight = 40.dp.toPx()
            
            // Draw background track (a chevron-like shape)
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, h/2 - barHeight/2)
                lineTo(w/2 - 20f, h/2 - barHeight/2 - 20f)
                lineTo(w, h/2 - barHeight/2)
                lineTo(w, h/2 + barHeight/2)
                lineTo(w/2 - 20f, h/2 + barHeight/2 - 20f)
                lineTo(0f, h/2 + barHeight/2)
                close()
            }
            
            drawPath(
                path = path,
                color = theme.textPrimary.copy(alpha = 0.1f)
            )

            // We clip to a scaled moving rectangle to reveal the shape.
            clipRect(right = w * progress) {
                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        colors = listOf(theme.secondary, theme.primary, Color(0xFFEF4444)),
                        startX = 0f,
                        endX = w
                    )
                )
            }
        }

        // Center Content
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(y = 30.dp)) {
            val displaySpeed = if (isMph) (data.speedKmh * 0.621371f).toInt() else data.speedKmh.toInt()
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$displaySpeed",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    color = theme.textPrimary
                )
                Text(
                    if (isMph) "MPH" else "KM/H", 
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.secondary,
                    modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
                )
            }
            
            Box(
                modifier = Modifier
                    .background(theme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 24.dp, vertical = 4.dp)
            ) {
                Text(
                    text = getGearDisplay(data.gear),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    color = theme.primary
                )
            }
        }
    }
}

@Composable
fun RallyHudLayout(data: TelemetryData, isMph: Boolean) {
    val theme = LocalDashboardTheme.current
    val animatedRpm by animateFloatAsState(
        targetValue = data.currentEngineRpm,
        animationSpec = tween(durationMillis = 100),
        label = "rpm_anim"
    )
    val max = if (data.engineMaxRpm > 100) data.engineMaxRpm else 8000f
    val progress = (animatedRpm / max).coerceIn(0f, 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .border(2.dp, theme.textPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(16.dp)
            .width(240.dp)
    ) {
        // Shift lights style RPM
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val numLights = 10
            for (i in 0 until numLights) {
                val threshold = i / numLights.toFloat()
                val color = when {
                    progress > threshold && i >= 8 -> Color(0xFFEF4444)
                    progress > threshold && i >= 5 -> theme.primary
                    progress > threshold -> theme.secondary
                    else -> theme.textPrimary.copy(alpha = 0.1f)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                        .padding(horizontal = 2.dp)
                        .background(color, RoundedCornerShape(2.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val displaySpeed = if (isMph) (data.speedKmh * 0.621371f).toInt() else data.speedKmh.toInt()
                Text(
                    text = "$displaySpeed",
                    fontSize = 48.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
                Text(
                    if (isMph) "MPH" else "KM/H", 
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = theme.textSecondary
                )
            }
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(theme.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .border(2.dp, theme.primary, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getGearDisplay(data.gear),
                    fontSize = 52.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = theme.primary
                )
            }
        }
    }
}

@Composable
fun TelemetryBadge(label: String, value: String, modifier: Modifier = Modifier) {
    val theme = LocalDashboardTheme.current
    Column(
        modifier = modifier
            .background(theme.textPrimary.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .border(1.dp, theme.textPrimary.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textSecondary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary
        )
    }
}

@Composable
fun SettingsDialog(
    currentTheme: DashboardTheme,
    currentLayout: DashboardLayout,
    isMph: Boolean,
    onThemeSelected: (DashboardTheme) -> Unit,
    onLayoutSelected: (DashboardLayout) -> Unit,
    onMphToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val theme = LocalDashboardTheme.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Dashboard Settings",
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // Speed Unit Setting
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Speed Unit", fontWeight = FontWeight.Medium, color = theme.textPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("KM/H", color = theme.textSecondary, fontSize = 12.sp)
                        Switch(
                            checked = isMph,
                            onCheckedChange = onMphToggle,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = theme.background,
                                checkedTrackColor = theme.primary,
                                uncheckedThumbColor = theme.secondary,
                                uncheckedTrackColor = theme.surface
                            )
                        )
                        Text("MPH", color = theme.textSecondary, fontSize = 12.sp)
                    }
                }
                
                Divider(color = theme.textPrimary.copy(alpha = 0.1f))
                
                // Layout Selection
                Text("Select Layout", fontWeight = FontWeight.Medium, color = theme.textPrimary)
                DashboardLayout.values().forEach { l ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onLayoutSelected(l) }
                            .padding(8.dp)
                    ) {
                        RadioButton(
                            selected = l == currentLayout,
                            onClick = { onLayoutSelected(l) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = theme.primary,
                                unselectedColor = theme.textSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(l.title, color = theme.textPrimary)
                    }
                }
                
                Divider(color = theme.textPrimary.copy(alpha = 0.1f))
                
                // Theme Selection
                Text("Select Theme", fontWeight = FontWeight.Medium, color = theme.textPrimary)
                DashboardTheme.values().forEach { t ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onThemeSelected(t) }
                            .padding(8.dp)
                    ) {
                        RadioButton(
                            selected = t == currentTheme,
                            onClick = { onThemeSelected(t) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = theme.primary,
                                unselectedColor = theme.textSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(t.title, color = theme.textPrimary)
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(t.primary)
                                .border(1.dp, t.textSecondary, CircleShape)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = theme.primary)
            }
        },
        containerColor = theme.surface,
        textContentColor = theme.textPrimary
    )
}

