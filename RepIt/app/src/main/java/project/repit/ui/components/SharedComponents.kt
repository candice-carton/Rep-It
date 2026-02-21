package project.repit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import project.repit.navigation.Screen
import project.repit.ui.theme.*

@Composable
fun FitBottomBar(navController: NavController) {
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    data class NavItem(val screen: Screen, val icon: ImageVector, val label: String)

    val items = listOf(
        NavItem(Screen.Home,          Icons.Filled.Home,          "Accueil"),
        NavItem(Screen.NewChallenge,  Icons.Filled.EmojiEvents,   "Défis"),
        NavItem(Screen.Notifications, Icons.Filled.Notifications, "Notifications"),
        NavItem(Screen.Statistics,    Icons.Filled.BarChart,      "Statistiques"),
        NavItem(Screen.Profile,       Icons.Filled.Person,        "Profil"),
    )

    NavigationBar(containerColor = Color.White) {
        items.forEach { item ->
            val selected = current == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick  = {
                    navController.navigate(item.screen.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon   = { Icon(item.icon, contentDescription = item.label) },
                label  = { Text(item.label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Purple,
                    selectedTextColor   = Purple,
                    indicatorColor      = PurpleLight,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                ),
            )
        }
    }
}

@Composable
fun AvatarCircle(
    initial: String,
    level: Int,
    modifier: Modifier = Modifier,
    size: Int = 56,
) {
    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = .25f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(initial, color = Color.White,
                fontSize = (size / 2.5).sp, fontWeight = FontWeight.Bold)
        }
        Surface(
            shape    = RoundedCornerShape(12.dp),
            color    = Orange,
            modifier = Modifier.offset(y = 10.dp),
        ) {
            Text("niv. $level", color = Color.White,
                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}

@Composable
fun StarsRow(filled: Int, total: Int = 5, starSize: Int = 18) {
    Row {
        repeat(total) { i ->
            Icon(
                // CORRECTED: Use Icons.Outlined.Star for the empty star icon.
                imageVector = if (i < filled) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (i < filled) StarYellow else Color(0xFFD1D5DB),
                modifier = Modifier.size(starSize.dp),
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        if (actionLabel != null) {
            TextButton(onClick = { onAction?.invoke() }) {
                Text(actionLabel, color = Purple, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun StatChip(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
        Text(label, fontSize = 11.sp, color = TextSecondary)
    }
}
