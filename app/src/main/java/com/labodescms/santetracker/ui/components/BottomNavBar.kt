package com.labodescms.santetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.labodescms.santetracker.R
import com.labodescms.santetracker.domain.Tab
import com.labodescms.santetracker.ui.theme.AppColors
import com.labodescms.santetracker.ui.theme.AppType

private data class NavItem(val tab: Tab, val label: String)

private val NAV_ITEMS = listOf(
    NavItem(Tab.ACCUEIL, "Accueil"),
    NavItem(Tab.POIDS, "Poids"),
    NavItem(Tab.JOURNAL, "Journal"),
    NavItem(Tab.REGLAGES, "Réglages"),
)

@Composable
fun BottomNavBar(activeTab: Tab, onTabSelected: (Tab) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().background(AppColors.Background)) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AppColors.Border))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 14.dp, start = 6.dp, end = 6.dp),
        ) {
            NAV_ITEMS.forEach { item ->
                val active = item.tab == activeTab
                val tint = if (active) AppColors.Gold else AppColors.TextDim
                // The settings icon bakes its own on/off colors (it "punches" background-colored
                // holes through the track lines), so it must not be recolored by Icon's tint.
                val iconTint = if (item.tab == Tab.REGLAGES) Color.Unspecified else tint
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(item.tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        painter = painterResource(iconFor(item.tab, active)),
                        contentDescription = item.label,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.size(5.dp))
                    BasicText(
                        text = item.label,
                        style = AppType.BodySmall.copy(
                            color = tint,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
            }
        }
    }
}

private fun iconFor(tab: Tab, active: Boolean): Int = when (tab) {
    Tab.ACCUEIL -> R.drawable.ic_nav_home
    Tab.POIDS -> R.drawable.ic_nav_weight
    Tab.JOURNAL -> R.drawable.ic_nav_journal
    Tab.REGLAGES -> if (active) R.drawable.ic_nav_settings_active else R.drawable.ic_nav_settings_inactive
}
