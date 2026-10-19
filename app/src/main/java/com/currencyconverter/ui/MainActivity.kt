package com.currencyconverter.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.glance.appwidget.updateAll
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.currencyconverter.data.model.CachedRates
import com.currencyconverter.data.repository.CurrencyRepository
import com.currencyconverter.ui.theme.CurrencyConverterTheme
import com.currencyconverter.widget.CurrencyWidget
import com.currencyconverter.worker.CurrencySyncWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class CurrencyInfo(val code: String, val name: String, val flag: String)

enum class Screen {
    Converter, Settings
}

enum class SyncState {
    Idle, Syncing, Success, Error
}

class MainActivity : ComponentActivity() {

    private val currencyMetadata = mapOf(
        "USD" to CurrencyInfo("USD", "US Dollar", "🇺🇸"),
        "INR" to CurrencyInfo("INR", "Indian Rupee", "🇮🇳"),
        "AED" to CurrencyInfo("AED", "UAE Dirham", "🇦🇪"),
        "EUR" to CurrencyInfo("EUR", "Euro", "🇪🇺"),
        "GBP" to CurrencyInfo("GBP", "British Pound", "🇬🇧"),
        "JPY" to CurrencyInfo("JPY", "Japanese Yen", "🇯🇵"),
        "CAD" to CurrencyInfo("CAD", "Canadian Dollar", "🇨🇦"),
        "AUD" to CurrencyInfo("AUD", "Australian Dollar", "🇦🇺"),
        "SGD" to CurrencyInfo("SGD", "Singapore Dollar", "🇸🇬"),
        "CHF" to CurrencyInfo("CHF", "Swiss Franc", "🇨🇭"),
        "CNY" to CurrencyInfo("CNY", "Chinese Yuan", "🇨🇳"),
        "SEK" to CurrencyInfo("SEK", "Swedish Krona", "🇸🇪"),
        "NZD" to CurrencyInfo("NZD", "New Zealand Dollar", "🇳🇿"),
        "MXN" to CurrencyInfo("MXN", "Mexican Peso", "🇲🇽"),
        "HKD" to CurrencyInfo("HKD", "Hong Kong Dollar", "🇭🇰"),
        "NOK" to CurrencyInfo("NOK", "Norwegian Krone", "🇳🇴"),
        "KRW" to CurrencyInfo("KRW", "South Korean Won", "🇰🇷"),
        "TRY" to CurrencyInfo("TRY", "Turkish Lira", "🇹🇷"),
        "RUB" to CurrencyInfo("RUB", "Russian Ruble", "🇷🇺"),
        "BRL" to CurrencyInfo("BRL", "Brazilian Real", "🇧🇷"),
        "ZAR" to CurrencyInfo("ZAR", "South African Rand", "🇿🇦"),
        "DKK" to CurrencyInfo("DKK", "Danish Krone", "🇩🇰"),
        "PLN" to CurrencyInfo("PLN", "Polish Zloty", "🇵🇱"),
        "HUF" to CurrencyInfo("HUF", "Hungarian Forint", "🇭🇺"),
        "CZK" to CurrencyInfo("CZK", "Czech Koruna", "🇨🇿"),
        "IDR" to CurrencyInfo("IDR", "Indonesian Rupiah", "🇮🇩"),
        "PHP" to CurrencyInfo("PHP", "Philippine Peso", "🇵🇭"),
        "THB" to CurrencyInfo("THB", "Thai Baht", "🇹🇭"),
        "MYR" to CurrencyInfo("MYR", "Malaysian Ringgit", "🇲🇾"),
        "ILS" to CurrencyInfo("ILS", "Israeli Shekel", "🇮🇱")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val repository = CurrencyRepository(this)
        scheduleSyncWorker()

        setContent {
            CurrencyConverterTheme(dynamicColor = true) {
                var currentScreen by remember { mutableStateOf(Screen.Converter) }
                
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == Screen.Converter,
                                onClick = { currentScreen = Screen.Converter },
                                label = { Text("Converter") },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.CurrencyExchange,
                                        contentDescription = "Converter"
                                    )
                                }
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.Settings,
                                onClick = { currentScreen = Screen.Settings },
                                label = { Text("Settings") },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings"
                                    )
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.background,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                    ) {
                        when (currentScreen) {
                            Screen.Converter -> ConverterScreen(repository)
                            Screen.Settings -> SettingsScreen(repository)
                        }
                    }
                }
            }
        }
    }

    private fun scheduleSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<CurrencySyncWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "CurrencySyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    @Composable
    fun ConverterScreen(repository: CurrencyRepository) {
        val scope = rememberCoroutineScope()
        
        var baseCurrency by remember { mutableStateOf("USD") }
        var targetCurrency by remember { mutableStateOf("INR") }
        var baseInput by remember { mutableStateOf("100") }
        
        val cachedRatesState by repository.cachedRates.collectAsState(initial = repository.getFallbackRates())
        
        val targetResult = remember(baseInput, baseCurrency, targetCurrency, cachedRatesState) {
            val amount = baseInput.toDoubleOrNull() ?: 0.0
            val rates = cachedRatesState?.rates ?: repository.getFallbackRates().rates
            val sourceRate = rates[baseCurrency] ?: 1.0
            val targetRate = rates[targetCurrency] ?: 1.0
            val converted = amount * (targetRate / sourceRate)
            formatCurrencyValue(converted, targetCurrency)
        }
        
        val historyList = remember { mutableStateListOf<String>() }
        
        var isSelectingBase by remember { mutableStateOf(false) }
        var isSelectingTarget by remember { mutableStateOf(false) }

        // Load Defaults
        LaunchedEffect(Unit) {
            baseCurrency = repository.homeCurrency.first()
            targetCurrency = repository.travelCurrency.first()
        }

        fun addToHistory(amount: String, from: String, result: String, to: String) {
            val format = NumberFormat.getNumberInstance(Locale.getDefault())
            val amtDouble = amount.toDoubleOrNull() ?: 0.0
            val formattedAmt = format.format(amtDouble)
            val historyItem = "$formattedAmt $from ➔ $result $to"
            if (!historyList.contains(historyItem)) {
                if (historyList.size >= 5) historyList.removeAt(historyList.lastIndex)
                historyList.add(0, historyItem)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Source Currency Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { isSelectingBase = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val baseMeta = currencyMetadata[baseCurrency]
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = baseMeta?.flag ?: "❓",
                            fontSize = 32.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = baseCurrency,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = baseMeta?.name ?: "Unknown Currency",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "${baseInput.ifEmpty { "0" }} $baseCurrency",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End
                    )
                }
            }

            // Swap Button (centered)
            IconButton(
                onClick = {
                    val temp = baseCurrency
                    baseCurrency = targetCurrency
                    targetCurrency = temp
                },
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Swap Currencies",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Target Currency Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { isSelectingTarget = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val targetMeta = currencyMetadata[targetCurrency]
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = targetMeta?.flag ?: "❓",
                            fontSize = 32.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = targetCurrency,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = targetMeta?.name ?: "Unknown Currency",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "$targetResult $targetCurrency",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End
                    )
                }
            }

            // Keypad and History Area
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // History List (Visible if not empty)
                AnimatedVisibility(
                    visible = historyList.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Recent Lookups",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        LazyColumn(
                            modifier = Modifier.height(60.dp)
                        ) {
                            items(historyList) { item ->
                                Text(
                                    text = item,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Custom Keypad
                val onKeyClick = { key: String ->
                    when (key) {
                        "C" -> {
                            baseInput = "0"
                        }
                        "⌫" -> {
                            baseInput = if (baseInput.length <= 1) "0" else baseInput.dropLast(1)
                        }
                        "." -> {
                            if (!baseInput.contains(".")) {
                                baseInput = "$baseInput."
                            }
                        }
                        "Save" -> {
                            addToHistory(baseInput, baseCurrency, targetResult, targetCurrency)
                        }
                        else -> {
                            baseInput = if (baseInput == "0") key else baseInput + key
                        }
                    }
                }

                val keypadRows = listOf(
                    listOf("1", "2", "3", "⌫"),
                    listOf("4", "5", "6", "C"),
                    listOf("7", "8", "9", "Save"),
                    listOf(".", "0")
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    keypadRows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { key ->
                                val isZeroDot = key == "0" || key == "."
                                val flex = if (isZeroDot && row.size == 2) 2f else 1f
                                val buttonColor = when (key) {
                                    "Save" -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    "⌫", "C" -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                                    else -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                
                                Button(
                                    onClick = { onKeyClick(key) },
                                    modifier = Modifier
                                        .weight(flex)
                                        .height(54.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = buttonColor,
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = if (key == "Save") 14.sp else 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selection Dialogs
        if (isSelectingBase) {
            CurrencySearchDialog(
                onDismiss = { isSelectingBase = false },
                onSelect = {
                    baseCurrency = it
                    isSelectingBase = false
                }
            )
        }
        if (isSelectingTarget) {
            CurrencySearchDialog(
                onDismiss = { isSelectingTarget = false },
                onSelect = {
                    targetCurrency = it
                    isSelectingTarget = false
                }
            )
        }
    }

    @Composable
    fun SettingsScreen(repository: CurrencyRepository) {
        val scope = rememberCoroutineScope()
        val ratesState by repository.cachedRates.collectAsState(initial = null)
        val defaultHome by repository.homeCurrency.collectAsState(initial = "INR")
        val defaultTravel by repository.travelCurrency.collectAsState(initial = "AED")
        
        var syncState by remember { mutableStateOf(SyncState.Idle) }
        var isSelectingHomeDefault by remember { mutableStateOf(false) }
        var isSelectingTravelDefault by remember { mutableStateOf(false) }

        val formatSyncTime = { timestamp: Long ->
            if (timestamp == 0L) {
                "Never Synced (Using Offline Fallbacks)"
            } else {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Offline Settings & Sync",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Cache Sync Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Data Cache Management",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Column {
                        Text(
                            text = "Last Synced Time",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatSyncTime(ratesState?.lastUpdated ?: 0L),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = if (ratesState?.lastUpdated ?: 0L == 0L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Force Sync Button
                    Button(
                        onClick = {
                            syncState = SyncState.Syncing
                            scope.launch {
                                val res = repository.syncRates()
                                if (res.isSuccess) {
                                    syncState = SyncState.Success
                                    // Update widget
                                    CurrencyWidget().updateAll(applicationContext)
                                } else {
                                    syncState = SyncState.Error
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        enabled = syncState != SyncState.Syncing
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (syncState == SyncState.Syncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Fetching latest rates...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Cached,
                                    contentDescription = "Sync"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Force Sync Now")
                            }
                        }
                    }

                    // Sync Status Indicators
                    AnimatedVisibility(visible = syncState == SyncState.Success) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Exchange rates updated!",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    AnimatedVisibility(visible = syncState == SyncState.Error) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.errorContainer,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sync failed. Check connection.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Defaults Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Defaults Configuration",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    // Default Home Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isSelectingHomeDefault = true }
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Default Home Currency",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$defaultHome (${currencyMetadata[defaultHome]?.name})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = currencyMetadata[defaultHome]?.flag ?: "",
                            fontSize = 24.sp
                        )
                    }

                    // Default Travel Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isSelectingTravelDefault = true }
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Default Travel Currency",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$defaultTravel (${currencyMetadata[defaultTravel]?.name})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = currencyMetadata[defaultTravel]?.flag ?: "",
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }

        if (isSelectingHomeDefault) {
            CurrencySearchDialog(
                onDismiss = { isSelectingHomeDefault = false },
                onSelect = {
                    scope.launch { repository.setHomeCurrency(it) }
                    isSelectingHomeDefault = false
                }
            )
        }
        if (isSelectingTravelDefault) {
            CurrencySearchDialog(
                onDismiss = { isSelectingTravelDefault = false },
                onSelect = {
                    scope.launch { repository.setTravelCurrency(it) }
                    isSelectingTravelDefault = false
                }
            )
        }
    }

    @Composable
    fun CurrencySearchDialog(onDismiss: () -> Unit, onSelect: (String) -> Unit) {
        var query by remember { mutableStateOf("") }
        val filteredList = currencyMetadata.values.filter {
            it.code.contains(query, ignoreCase = true) || it.name.contains(query, ignoreCase = true)
        }

        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Currency",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }

                    // Search input
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search currency...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Results
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredList) { currency ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelect(currency.code) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currency.flag,
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Column {
                                    Text(
                                        text = currency.code,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = currency.name,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun formatCurrencyValue(value: Double, currencyCode: String): String {
        return try {
            val format = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
            format.format(value)
        } catch (e: Exception) {
            String.format(Locale.getDefault(), "%.2f", value)
        }
    }
}
