package com.migui.statistic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val API_BASE = "https://migui-football-api.games-teste2018.workers.dev"
private val Bg = Color(0xFF080B10)
private val Panel = Color(0xFF141A22)
private val Muted = Color(0xFF8994A3)
private val Accent = Color(0xFF27D17F)

private data class MatchItem(
    val id: Int,
    val home: String,
    val away: String,
    val utcDate: String,
    val status: String,
    val homeScore: String,
    val awayScore: String,
    val matchday: String
)

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
    var matches by remember { mutableStateOf<List<MatchItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        val result = withContext(Dispatchers.IO) { loadPremierLeagueMatches() }
        matches = result.first
        error = result.second
        loading = false
    }

    val visibleMatches = when (selected) {
        "Ao vivo" -> matches.filter { it.status == "IN_PLAY" || it.status == "PAUSED" }
        "Próximos" -> matches.filter { it.status == "SCHEDULED" || it.status == "TIMED" }
        else -> matches.filter { isToday(it.utcDate) }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Bg, surface = Panel, primary = Accent,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Scaffold(
            containerColor = Bg,
            bottomBar = {
                NavigationBar(containerColor = Panel, contentColor = Color.White) {
                    listOf(
                        Icons.Default.SportsSoccer to "Jogos",
                        Icons.Default.Analytics to "Análises",
                        Icons.Default.Star to "Favoritos"
                    ).forEachIndexed { i, item ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = { Icon(item.first, contentDescription = item.second) },
                            label = { Text(item.second) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Accent,
                                indicatorColor = Color(0xFF20372E),
                                selectedTextColor = Accent,
                                unselectedTextColor = Muted
                            )
                        )
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Bg)
                    .padding(padding).padding(horizontal = 18.dp)
            ) {
                item {
                    Spacer(Modifier.height(22.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("MIGUI", color = Color.White, fontSize = 28.sp,
                                fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                            Text("STATISTIC", color = Accent, fontSize = 12.sp,
                                fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                        }
                        Surface(color = Panel, shape = RoundedCornerShape(50)) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, null, tint = Accent,
                                    modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(7.dp))
                                Text(
                                    SimpleDateFormat("dd MMM yyyy", Locale("pt", "BR"))
                                        .format(Date()).uppercase(Locale("pt", "BR")),
                                    fontSize = 11.sp, color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(26.dp))
                    Text("PREMIER LEAGUE • INGLATERRA", color = Muted,
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Hoje", "Ao vivo", "Próximos").forEach { label ->
                            FilterChip(
                                selected = selected == label,
                                onClick = { selected = label },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF20372E),
                                    selectedLabelColor = Accent,
                                    labelColor = Muted
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    if (loading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Accent,
                            trackColor = Panel
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Buscando partidas oficiais...", color = Muted, fontSize = 13.sp)
                    } else if (error != null) {
                        InfoPanel("Não foi possível carregar os jogos", error!!)
                    } else if (visibleMatches.isEmpty()) {
                        InfoPanel(
                            "Nenhuma partida encontrada",
                            if (selected == "Hoje") "Não há partidas da Premier League para hoje na resposta da fonte."
                            else "Não há partidas nesta categoria no período consultado."
                        )
                    }
                    if (!loading && error == null && visibleMatches.isNotEmpty()) {
                        Text("${visibleMatches.size} PARTIDA(S) ENCONTRADA(S)",
                            color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                    }
                    if (tab == 1) {
                        Spacer(Modifier.height(16.dp))
                        InfoPanel("Central de análises",
                            "As análises estatísticas serão adicionadas após validarmos a cobertura dos dados.")
                    } else if (tab == 2) {
                        Spacer(Modifier.height(16.dp))
                        InfoPanel("Favoritos",
                            "A função de salvar equipes e competições será implementada em uma próxima etapa.")
                    }
                    Spacer(Modifier.height(20.dp))
                }

                if (tab == 0 && !loading && error == null) {
                    items(visibleMatches, key = { it.id }) { match ->
                        MatchCard(match)
                        Spacer(Modifier.height(10.dp))
                    }
                }

                item {
                    Spacer(Modifier.height(18.dp))
                    Text("FONTE: FOOTBALL-DATA.ORG",
                        color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
                    Text("Dados sujeitos à disponibilidade da API. Sem números inventados.",
                        color = Muted, fontSize = 10.sp)
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoPanel(title: String, message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.QueryStats, null, tint = Accent, modifier = Modifier.size(30.dp))
            Spacer(Modifier.height(10.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(6.dp))
            Text(message, color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun MatchCard(match: MatchItem) {
    val isLive = match.status == "IN_PLAY" || match.status == "PAUSED"
    val isFinished = match.status == "FINISHED"
    val statusLabel = when (match.status) {
        "IN_PLAY" -> "AO VIVO"
        "PAUSED" -> "INTERVALO"
        "FINISHED" -> "ENCERRADO"
        "POSTPONED" -> "ADIADO"
        "CANCELLED" -> "CANCELADO"
        else -> formatKickoff(match.utcDate)
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("PREMIER LEAGUE", color = Muted, fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.weight(1f))
                Text(statusLabel, color = if (isLive) Accent else Muted,
                    fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(match.home, color = Color.White, fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(
                    if (isFinished || isLive) "${match.homeScore}  -  ${match.awayScore}" else "×",
                    color = if (isLive) Accent else Color.White,
                    fontSize = 17.sp, fontWeight = FontWeight.Bold
                )
                Text(match.away, color = Color.White, fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
            if (match.matchday.isNotBlank()) {
                Spacer(Modifier.height(9.dp))
                Text("Rodada ${match.matchday}", color = Muted, fontSize = 10.sp)
            }
        }
    }
}

private fun loadPremierLeagueMatches(): Pair<List<MatchItem>, String?> {
    var connection: HttpURLConnection? = null
    return try {
        val today = Calendar.getInstance()
        val from = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(today.time)
        today.add(Calendar.DAY_OF_YEAR, 14)
        val to = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(today.time)
        val url = URL("$API_BASE/api/matches?dateFrom=$from&dateTo=$to")
        connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 20000
        }
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val body = stream.bufferedReader().use { it.readText() }
        if (code !in 200..299) {
            val message = runCatching { JSONObject(body).optString("message") }.getOrNull()
            return Pair(emptyList(), message?.takeIf { it.isNotBlank() }
                ?: "A fonte respondeu com código HTTP $code.")
        }
        val root = JSONObject(body)
        val array = root.optJSONArray("matches") ?: return Pair(emptyList(), "A resposta não trouxe a lista de partidas.")
        val result = mutableListOf<MatchItem>()
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val home = item.optJSONObject("homeTeam")?.optString("shortName")
                ?.takeIf { !it.isNullOrBlank() && it != "null" }
                ?: item.optJSONObject("homeTeam")?.optString("name", "Mandante") ?: "Mandante"
            val away = item.optJSONObject("awayTeam")?.optString("shortName")
                ?.takeIf { !it.isNullOrBlank() && it != "null" }
                ?: item.optJSONObject("awayTeam")?.optString("name", "Visitante") ?: "Visitante"
            val score = item.optJSONObject("score")?.optJSONObject("fullTime")
            val homeScore = if (score == null || score.isNull("home")) "–" else score.optString("home")
            val awayScore = if (score == null || score.isNull("away")) "–" else score.optString("away")
            result.add(
                MatchItem(
                    id = item.optInt("id", i),
                    home = home,
                    away = away,
                    utcDate = item.optString("utcDate", ""),
                    status = item.optString("status", "SCHEDULED"),
                    homeScore = homeScore,
                    awayScore = awayScore,
                    matchday = item.optInt("matchday", 0).takeIf { it > 0 }?.toString() ?: ""
                )
            )
        }
        Pair(result.sortedBy { it.utcDate }, null)
    } catch (e: Exception) {
        Pair(emptyList(), "Falha na conexão: ${e.localizedMessage ?: "erro desconhecido"}")
    } finally {
        connection?.disconnect()
    }
}

private fun isToday(utcDate: String): Boolean {
    if (utcDate.isBlank()) return false
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(utcDate) ?: return false
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date) ==
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    } catch (_: Exception) {
        false
    }
}

private fun formatKickoff(utcDate: String): String {
    if (utcDate.isBlank()) return "HORÁRIO A CONFIRMAR"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(utcDate) ?: return "HORÁRIO A CONFIRMAR"
        SimpleDateFormat("dd/MM • HH:mm", Locale("pt", "BR")).format(date)
    } catch (_: Exception) {
        "HORÁRIO A CONFIRMAR"
    }
}
