package com.migui.statistic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF080B10)
private val Panel = Color(0xFF141A22)
private val Muted = Color(0xFF8994A3)
private val Accent = Color(0xFF27D17F)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MiguiApp() }
    }
}

@Composable
fun MiguiApp() {
    var selected by remember { mutableStateOf("Hoje") }
    var tab by remember { mutableStateOf(0) }
    MaterialTheme(colorScheme = darkColorScheme(
        background = Bg, surface = Panel, primary = Accent,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Scaffold(containerColor = Bg, bottomBar = {
            NavigationBar(containerColor = Panel, contentColor = Color.White) {
                listOf(Icons.Default.SportsBasketball to "Jogos", Icons.Default.Analytics to "Análises", Icons.Default.Star to "Favoritos").forEachIndexed { i, item ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i },
                        icon = { Icon(item.first, contentDescription = item.second) },
                        label = { Text(item.second) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Accent, indicatorColor = Color(0xFF20372E), selectedTextColor = Accent, unselectedTextColor = Muted))
                }
            }
        }) { padding ->
            LazyColumn(modifier = Modifier.fillMaxSize().background(Bg).padding(padding).padding(horizontal = 18.dp)) {
                item {
                    Spacer(Modifier.height(22.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("MIGUI", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                            Text("STATISTIC", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                        }
                        Surface(color = Panel, shape = RoundedCornerShape(50), onClick = {}) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, null, tint = Accent, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(7.dp)); Text("07 NOV 2026", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                    Spacer(Modifier.height(26.dp))
                    Text("PAINEL DE JOGOS", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Hoje", "Ao vivo", "Próximos").forEach { label ->
                            FilterChip(selected = selected == label, onClick = { selected = label }, label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF20372E), selectedLabelColor = Accent, labelColor = Muted))
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.QueryStats, null, tint = Accent, modifier = Modifier.size(34.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Dados em preparação", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text("Os jogos e as estatísticas aparecerão aqui quando uma fonte de dados confiável estiver conectada.", color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
                            Spacer(Modifier.height(14.dp))
                            Text("Sem números inventados.", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(22.dp))
                    Text(when(tab) { 1 -> "CENTRAL DE ANÁLISES"; 2 -> "SEUS FAVORITOS"; else -> "COBERTURA ESPORTIVA" }, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(when(tab) { 1 -> "Probabilidades, gols, escanteios e cartões serão calculados com dados verificáveis."; 2 -> "Salve equipes e competições para acompanhar em um só lugar."; else -> "Futebol e basquete em uma interface escura, rápida e objetiva." }, color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
                    Spacer(Modifier.height(30.dp))
                    Text("MIGUI STATISTIC • VERSÃO INICIAL", color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
