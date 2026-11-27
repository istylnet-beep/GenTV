package com.gentv.mobile.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import com.gentv.mobile.ui.AppText as Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gentv.mobile.TvSummary
import com.gentv.mobile.ui.*
import com.gentv.core.flow.*

@Composable
fun DiscoveryScreen(state: ConnectState, currentTv: TvSummary?, onEvent: (ConnectEvent) -> Unit, onNavigate: (Int) -> Unit, onBack: () -> Unit = {}) {
    val scan = state as? ConnectState.Scanning
    val devices = scan?.found.orEmpty()
    val searching = scan != null && !scan.done
    var manual by rememberSaveable { mutableStateOf(false) }
    var help by rememberSaveable { mutableStateOf(false) }
    var ip by rememberSaveable { mutableStateOf("") }
    val valid = ConnectMachine.isIp(ip)
    val submit = { if (valid) { manual = false; onEvent(ConnectEvent.TypedIp(ip)) } }
    CompanionScreen("尋找裝置", currentTv?.title, 2, onNavigate, showTitle = false) {
        Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DiscoveryRadar(searching)
            Row(Modifier.background(CompanionColors.Raised, CircleShape).padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Outlined.Wifi, null, Modifier.size(16.dp), tint = CompanionColors.Blue)
                Text("同一個區域網路", color = CompanionColors.Text, fontSize = 13.sp)
                Box(Modifier.size(5.dp).background(CompanionColors.Green, CircleShape))
            }
            Text(if (searching) "正在尋找電視與投影機…" else "本次搜尋已完成", color = CompanionColors.Muted, fontSize = 12.sp)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("可連線裝置 (${devices.count { it.host != currentTv?.host } + if (currentTv != null) 1 else 0})", color = CompanionColors.Secondary, fontSize = 14.sp)
            TextButton(onClick = { onEvent(ConnectEvent.StartScan) }) {
                Icon(Icons.Outlined.Refresh, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("重新搜尋", fontSize = 12.sp)
            }
        }
        currentTv?.let { current -> DeviceDiscoveryCard(FoundTv(current.host, name = current.title, model = current.model), true) { onNavigate(0) } }
        devices.filter { it.host != currentTv?.host }.forEach { tv -> DeviceDiscoveryCard(tv, false) { onEvent(ConnectEvent.Pick(tv)) } }
        if (devices.isEmpty() && currentTv == null) CompanionCard {
            Icon(Icons.Outlined.Tv, null, Modifier.size(32.dp), tint = CompanionColors.Muted)
            Text(if (searching) "正在搜尋附近裝置" else "暂未找到可連線裝置", color = CompanionColors.Text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text("請確認電視已開機、已開啟網路除錯，且與手機連線至同一個家庭網路。", color = CompanionColors.Secondary, fontSize = 13.sp)
        }
        CompanionButton("手動輸入 IP 位址直接連線", { manual = true }, Modifier.fillMaxWidth().padding(top = 12.dp))
        TextButton(onClick = { help = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Icon(Icons.Outlined.HelpOutline, null, Modifier.size(16.dp), tint = CompanionColors.Secondary)
            Spacer(Modifier.width(6.dp)); Text("找不到裝置？查看疑難排解指南", color = CompanionColors.Secondary, fontSize = 13.sp)
        }
        if (manual) AlertDialog(onDismissRequest = { manual = false }, title = { Text("指定 IP 位址連線") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("請輸入電視網路設定中顯示的內部 IPv4 位址：")
                OutlinedTextField(ip, { ip = it.filter { c -> c.isDigit() || c == '.' }.take(15) }, label = { Text("電視 IP 位址") }, singleLine = true,
                    isError = ip.count { it == '.' } == 3 && !valid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Go), keyboardActions = KeyboardActions(onGo = { submit() }))
            }
        }, confirmButton = { TextButton(submit, enabled = valid) { Text("立即連線") } }, dismissButton = { TextButton({ manual = false }) { Text("取消") } })
        if (help) AlertDialog(onDismissRequest = { help = false }, title = { Text("裝置搜尋疑難排解") }, text = { Text("1. 手機和電視請連線至同一台路由器，並避免使用訪客網路。\n\n2. 在電視的開發人員選項中開啟 USB／網路除錯；較新的系統可使用無線除錯配對。\n\n3. 电视开启调试后点击重新搜尋，搜索最长约 20 秒。\n\n4. 若路由器啟用了裝置隔離或限制廣播，可手動輸入電視 IP 位址，並在電視上確認授權。") }, confirmButton = { TextButton({ help = false }) { Text("知道了") } })
    }
}

@Composable
private fun DeviceDiscoveryCard(tv: FoundTv, connected: Boolean, onClick: () -> Unit) {
    CompanionCard(Modifier.clickable(role = Role.Button, onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(46.dp).background(CompanionColors.Raised, androidx.compose.foundation.shape.RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Tv, null, Modifier.size(27.dp), tint = if (connected) CompanionColors.Green else CompanionColors.Blue)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(tv.title, localize = false, color = CompanionColors.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(if (tv.host == "demo") "示範裝置 · 非實際電視" else if (connected) "目前連線的電視" else if (tv.tlsPort != null) "無線除錯" else if (tv.legacyOpen) "網路除錯已開啟" else "請確認已開啟除錯", color = CompanionColors.Muted, fontSize = 11.sp)
            }
            TextButton(onClick, colors = ButtonDefaults.textButtonColors(contentColor = if (connected) CompanionColors.Green else CompanionColors.Blue)) {
                Text(if (connected) "已連線" else "连接", fontSize = 13.sp)
                if (!connected) Icon(Icons.Outlined.ChevronRight, null, Modifier.size(15.dp))
            }
        }
    }
}

@Composable
private fun DiscoveryRadar(searching: Boolean) {
    val still = LocalInspectionMode.current
    val angle = if (searching && !still) rememberInfiniteTransition(label = "radar").animateFloat(0f, 360f, infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "beam").value else 38f
    Canvas(Modifier.size(192.dp)) {
        val c = center; val r = size.minDimension / 2
        drawCircle(Brush.radialGradient(listOf(CompanionColors.Blue.copy(alpha = .12f), Color.Transparent)), r)
        drawCircle(CompanionColors.Blue.copy(alpha = .045f), r)
        drawCircle(CompanionColors.Blue.copy(alpha = .07f), r * .80f)
        drawCircle(Color(0xFF303139), r * .64f)
        drawCircle(Color(0xFF292C34), r * .42f)
        drawCircle(CompanionColors.Blue.copy(alpha = .12f), r * .25f)
        drawCircle(CompanionColors.Blue.copy(alpha = .45f), r * .16f, style = Stroke(1.5.dp.toPx()))
        val rad = Math.toRadians(angle.toDouble())
        drawLine(CompanionColors.Blue.copy(alpha = .65f), c, Offset(c.x + kotlin.math.cos(rad).toFloat() * r * .43f, c.y + kotlin.math.sin(rad).toFloat() * r * .43f), 1.5.dp.toPx())
        drawCircle(CompanionColors.Green, 3.dp.toPx(), Offset(r * 1.58f, r * .28f))
        drawCircle(CompanionColors.Blue, 2.5.dp.toPx(), Offset(r * .30f, r * 1.65f))
        drawCircle(CompanionColors.Secondary.copy(alpha = .5f), 2.dp.toPx(), Offset(r * .38f, r * .45f))
    }
}

@Composable
fun ConnectionFlowScreen(state: ConnectState, onEvent: (ConnectEvent) -> Unit, onShareLogs: () -> Unit, onNavigate: (Int) -> Unit, onBack: () -> Unit = {}) {
    val tv = when (state) {
        is ConnectState.Connecting -> state.tv
        is ConnectState.PressAllow -> state.tv
        is ConnectState.Pairing -> state.tv
        is ConnectState.Connected -> state.tv
        is ConnectState.TurnOnDebugging -> state.tv
        is ConnectState.Unreachable -> state.tv
        is ConnectState.Denied -> state.tv
        is ConnectState.Failed -> state.tv
        else -> null
    }
    var code by rememberSaveable(tv?.host) { mutableStateOf("") }
    var port by rememberSaveable(tv?.host) { mutableStateOf("") }
    val waiting = state is ConnectState.Connecting || state is ConnectState.PressAllow || (state is ConnectState.Pairing && state.busy)
    CompanionScreen("連線裝置", null, 2, onNavigate, onBack = onBack) {
        Column(Modifier.fillMaxWidth().padding(vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(Icons.Outlined.Tv, null, Modifier.size(56.dp), tint = CompanionColors.Blue)
            Text(tv?.title ?: "連線電視", localize = tv == null, color = CompanionColors.Text, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        }
        CompanionCard {
            val title = when (state) {
                is ConnectState.Connecting -> "正在建立連線"
                is ConnectState.PressAllow -> "請在電視上確認授權"
                is ConnectState.Pairing -> "無線除錯配对"
                is ConnectState.Connected -> "已連線电视"
                is ConnectState.TurnOnDebugging -> "請開啟網路除錯"
                is ConnectState.Unreachable -> "暂时无法連線電視"
                is ConnectState.Denied -> "電視拒絕連線"
                else -> "尚未完成連線"
            }
            Text(title, color = CompanionColors.Text, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            val description = when (state) {
                is ConnectState.Connecting -> "請讓手機與電視保持在同一個網路，並留意電視上是否出現除錯授權視窗。"
                is ConnectState.PressAllow -> "電視顯示「允許 USB 除錯？」時，請先勾選「一律允許這台電腦」，再使用電視遙控器選擇「允許」。"
                is ConnectState.Pairing -> "在电视的开发者选项中打开“無線除錯 → 使用配对码配对设备”，输入电视显示的六位數配對碼。"
                is ConnectState.TurnOnDebugging -> "电视能够响应，但网络调试尚未开启。请在电视“设置 → 系统 → 开发者选项”打开無線除錯、网络调试或 ADB over network。选项置灰时，可先将电视 Wi-Fi 连接到手机所在网络，再重试。"
                is ConnectState.Unreachable -> "請確認電視已開機、位址正確，且與手機位於同一個家庭網路。訪客網路或路由器隔離設定可能會阻擋連線。"
                is ConnectState.Denied -> "請重新連線，並在電視上選擇「允許」。"
                is ConnectState.Failed -> state.message
                else -> "現在可以使用遙控器及應用程式投放內容。"
            }
            Text(description, color = CompanionColors.Secondary, fontSize = 14.sp)
            if (waiting) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (state is ConnectState.Pairing) {
                OutlinedTextField(code, { code = it.filter(Char::isDigit).take(6) }, Modifier.fillMaxWidth(), label = { Text("六位數配對碼") }, enabled = !state.busy,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), singleLine = true)
                if (state.tv.pairPort == null) OutlinedTextField(port, { port = it.filter(Char::isDigit).take(5) }, Modifier.fillMaxWidth(), label = { Text("配對連接埠") }, enabled = !state.busy,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                state.error?.let { Text(it, color = CompanionColors.Error, fontSize = 13.sp) }
                val validPort = state.tv.pairPort ?: port.toIntOrNull()
                CompanionButton(if (state.busy) "配對中…" else "配對並連線", { onEvent(ConnectEvent.SubmitCode(code, port.toIntOrNull())) }, Modifier.fillMaxWidth(),
                    enabled = !state.busy && code.length == 6 && validPort != null && validPort in 1..65535, primary = true)
            }
        }
        if (state is ConnectState.TurnOnDebugging || state is ConnectState.Unreachable || state is ConnectState.Denied || state is ConnectState.Failed || (state is ConnectState.PressAllow && state.canRetry)) {
            CompanionButton("重新嘗試連線", { onEvent(ConnectEvent.Retry) }, Modifier.fillMaxWidth(), primary = true)
        }
        CompanionButton("選擇其他裝置", { onEvent(ConnectEvent.Back) }, Modifier.fillMaxWidth())
        if (state is ConnectState.Failed) TextButton(onShareLogs) { Text("匯出診斷記錄") }
    }
}
