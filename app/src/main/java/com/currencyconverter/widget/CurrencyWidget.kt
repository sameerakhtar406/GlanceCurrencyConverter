package com.currencyconverter.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.GlanceTheme
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.currencyconverter.data.repository.CurrencyRepository
import com.currencyconverter.ui.QuickConvertActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CurrencyWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = CurrencyRepository(context)
        val home = runBlocking { repository.homeCurrency.first() }
        val travel = runBlocking { repository.travelCurrency.first() }
        val cachedRates = runBlocking { repository.cachedRates.first() }
        val lastSyncTime = cachedRates?.lastUpdated ?: 0L
        
        val syncText = if (lastSyncTime == 0L) {
            "Rates: Offline Cached"
        } else {
            val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
            "Synced: ${sdf.format(Date(lastSyncTime))}"
        }

        val rate = cachedRates?.rates?.get(travel)?.let { travelRate ->
            cachedRates.rates[home]?.let { homeRate ->
                val calculated = travelRate / homeRate
                String.format(Locale.US, "1 %s = %.4f %s", home, calculated, travel)
            }
        } ?: "$home ➔ $travel"

        provideContent {
            GlanceTheme {
                GlanceWidgetContent(
                    home = home,
                    travel = travel,
                    rateText = rate,
                    syncText = syncText
                )
            }
        }
    }

    @Composable
    private fun GlanceWidgetContent(
        home: String,
        travel: String,
        rateText: String,
        syncText: String
    ) {
        val clickAction = actionStartActivity<QuickConvertActivity>()
        
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .padding(12.dp)
                .clickable(clickAction),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$home ➔ $travel",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = "Tap to Convert",
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                
                Spacer(modifier = GlanceModifier.height(6.dp))
                
                Text(
                    text = rateText,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Spacer(modifier = GlanceModifier.height(2.dp))
                
                Text(
                    text = syncText,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
