package com.wongwingchun.speakeryou

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lanrhyme.micyou.plugin.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SpeakerPlugin : Plugin, PluginUIProvider, PluginSettingsProvider {

    private var context: PluginContext? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override val manifest = PluginManifest(
        id = "com.wongwingchun.speakeryou",
        name = "SpeakerYou",
        version = "1.0.0",
        author = "WongWingChun",
        description = "Use your phone as a computer speaker - stream audio from mobile to desktop via WiFi, Bluetooth, or USB.",
        tags = listOf("audio", "streaming", "network"),
        platform = PluginPlatform.DESKTOP,
        minApiVersion = "1.0.0",
        mainClass = "com.wongwingchun.speakeryou.SpeakerPlugin"
    )

    // UI Provider settings
    override val hasMainWindow: Boolean = true
    override val hasDialog: Boolean = false
    override val windowWidth = 500.dp
    override val windowHeight = 500.dp
    override val windowTitle = "SpeakerYou"
    override val mobileUIMode = MobileUIMode.NewScreen

    // Connection settings
    private var serverIp: String = ""
    private var serverPort: Int = 9876
    private var connectionMode: ConnectionMode = ConnectionMode.Wifi
    private var isRunning: Boolean = false

    override fun onLoad(context: PluginContext) {
        this.context = context
        serverIp = context.getString("serverIp", "")
        serverPort = context.getInt("serverPort", 9876)
        isRunning = context.getBoolean("isRunning", false)
        connectionMode = when (context.getString("connectionMode", "Wifi")) {
            "Bluetooth" -> ConnectionMode.Bluetooth
            "Usb" -> ConnectionMode.Usb
            else -> ConnectionMode.Wifi
        }
        context.log("SpeakerYou loaded - Author: WongWingChun")
    }

    override fun onEnable() {
        context?.log("SpeakerYou enabled")
    }

    override fun onDisable() {
        context?.log("SpeakerYou disabled")
        stopStreaming()
    }

    override fun onUnload() {
        context?.log("SpeakerYou unloaded")
        stopStreaming()
        context = null
    }

    private fun stopStreaming() {
        scope.launch {
            try {
                context?.host?.stopStream()
                context?.log("Stopped streaming")
            } catch (e: Exception) {
                context?.logError("Failed to stop stream: ${e.message}")
            }
        }
        isRunning = false
        context?.putBoolean("isRunning", false)
    }

    private fun startStreaming(onResult: (Boolean, String) -> Unit) {
        scope.launch {
            try {
                context?.host?.startStream(
                    ip = serverIp,
                    port = serverPort,
                    mode = connectionMode,
                    isClient = true  // Mobile acts as client, Desktop as server
                )
                isRunning = true
                context?.putBoolean("isRunning", true)
                context?.log("Started streaming to $serverIp:$serverPort via $connectionMode")
                withContext(Dispatchers.Main) {
                    onResult(true, "Started streaming to $serverIp:$serverPort via $connectionMode")
                }
            } catch (e: Exception) {
                context?.logError("Failed to start stream: ${e.message}")
                withContext(Dispatchers.Main) {
                    onResult(false, "Failed to start: ${e.message}")
                }
            }
        }
    }

    @Composable
    override fun MainWindow(onClose: () -> Unit) {
        var ipState by remember { mutableStateOf(serverIp) }
        var portState by remember { mutableStateOf(serverPort.toString()) }
        var selectedMode by remember { mutableStateOf(connectionMode) }
        var runningState by remember { mutableStateOf(isRunning) }
        var statusMessage by remember { mutableStateOf("") }

        val host = context?.host
        val streamState by host?.streamState?.collectAsState()
            ?: remember { mutableStateOf(StreamState.Idle) }

        LaunchedEffect(streamState) {
            runningState = streamState == StreamState.Streaming
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                Text(
                    text = "SpeakerYou",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Phone as Computer Speaker",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Connection Mode Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Connection Mode",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Column(modifier = Modifier.selectableGroup()) {
                            ConnectionMode.values().forEach { mode ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = (mode == selectedMode),
                                            onClick = {
                                                selectedMode = mode
                                                connectionMode = mode
                                                context?.putString("connectionMode", mode.name)
                                            },
                                            role = Role.RadioButton
                                        )
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (mode == selectedMode),
                                        onClick = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (mode) {
                                            ConnectionMode.Wifi -> "WiFi"
                                            ConnectionMode.Bluetooth -> "Bluetooth"
                                            ConnectionMode.Usb -> "USB"
                                        },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }

                // Connection Settings Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Server Settings",
                            style = MaterialTheme.typography.titleMedium
                        )

                        OutlinedTextField(
                            value = ipState,
                            onValueChange = { newValue ->
                                ipState = newValue
                                serverIp = newValue
                                context?.putString("serverIp", newValue)
                            },
                            label = { Text("Desktop IP Address") },
                            placeholder = { Text("e.g., 192.168.1.100") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !runningState
                        )

                        OutlinedTextField(
                            value = portState,
                            onValueChange = { newValue ->
                                portState = newValue
                                val parsed = newValue.toIntOrNull()
                                if (parsed != null && parsed in 1..65535) {
                                    serverPort = parsed
                                    context?.putInt("serverPort", parsed)
                                }
                            },
                            label = { Text("Port") },
                            placeholder = { Text("9876") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !runningState
                        )
                    }
                }

                // Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (runningState)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = if (runningState)
                                    Icons.Filled.PlayCircle
                                else
                                    Icons.Filled.StopCircle,
                                contentDescription = null,
                                tint = if (runningState)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = when (streamState) {
                                    StreamState.Idle -> "Idle"
                                    StreamState.Connecting -> "Connecting..."
                                    StreamState.Streaming -> "Streaming"
                                    StreamState.Error -> "Error"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        if (runningState || streamState == StreamState.Streaming) {
                            Text(
                                text = "$serverIp:$serverPort (${selectedMode.name})",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (statusMessage.isNotEmpty()) {
                            Text(
                                text = statusMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (!runningState && serverIp.isNotEmpty()) {
                                statusMessage = "Connecting..."
                                startStreaming { success, message ->
                                    statusMessage = message
                                    if (success) {
                                        host?.showSnackbar(message)
                                    }
                                }
                            } else if (serverIp.isEmpty()) {
                                host?.showSnackbar("Please enter Desktop IP address")
                            }
                        },
                        enabled = !runningState && serverIp.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Start")
                    }

                    Button(
                        onClick = {
                            stopStreaming()
                            statusMessage = ""
                            host?.showSnackbar("Stopped streaming")
                        },
                        enabled = runningState,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Stop")
                    }
                }

                // Close Button
                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Window")
                }
            }
        }
    }

    @Composable
    override fun SettingsContent() {
        var ipState by remember { mutableStateOf(serverIp) }
        var portState by remember { mutableStateOf(serverPort.toString()) }
        var selectedMode by remember { mutableStateOf(connectionMode) }
        var autoStart by remember { mutableStateOf(context?.getBoolean("autoStart", false) ?: false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Plugin Info
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "SpeakerYou",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Version: ${manifest.version}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Author: ${manifest.author}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = manifest.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Connection Settings
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Default Connection",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = ipState,
                        onValueChange = { newValue ->
                            ipState = newValue
                            context?.putString("serverIp", newValue)
                        },
                        label = { Text("Default Desktop IP") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = portState,
                        onValueChange = { newValue ->
                            portState = newValue
                            val parsed = newValue.toIntOrNull()
                            if (parsed != null && parsed in 1..65535) {
                                context?.putInt("serverPort", parsed)
                            }
                        },
                        label = { Text("Default Port") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Connection Mode Selection in Settings
                    Text(
                        text = "Default Connection Mode",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ConnectionMode.values().forEach { mode ->
                            FilterChip(
                                selected = mode == selectedMode,
                                onClick = {
                                    selectedMode = mode
                                    context?.putString("connectionMode", mode.name)
                                },
                                label = {
                                    Text(when (mode) {
                                        ConnectionMode.Wifi -> "WiFi"
                                        ConnectionMode.Bluetooth -> "Bluetooth"
                                        ConnectionMode.Usb -> "USB"
                                    })
                                }
                            )
                        }
                    }
                }
            }

            // Auto Start Option
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Auto Start Streaming",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Automatically start when plugin is enabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoStart,
                        onCheckedChange = { newValue ->
                            autoStart = newValue
                            context?.putBoolean("autoStart", newValue)
                        }
                    )
                }
            }
        }
    }
}