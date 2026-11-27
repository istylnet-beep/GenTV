package com.gentv.mobile.ui

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

enum class AppLanguage(val code: String) {
    TRADITIONAL_CHINESE("zh-TW");
    companion object { fun from(code: String?) = TRADITIONAL_CHINESE }
}

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.TRADITIONAL_CHINESE }
val LocalChangeLanguage = staticCompositionLocalOf<(AppLanguage) -> Unit> { {} }

/** Legacy screen copy is centralized here; device names, user input and reports bypass this adapter. */
object AppCopy {
    private val entries by lazy { translationEntries() }
    private val chinese by lazy { entries.associate { it.first to it.second } }
    private data class Template(val pattern: Regex, val replacement: String)
    private val templates by lazy {
        entries.mapNotNull { (english, traditional) ->
            if (!english.contains("{0}")) return@mapNotNull null
            val tokens = Regex("\\{(\\d+)\\}").findAll(english).toList()
            var position = 0
            val pattern = buildString {
                append("^")
                for (token in tokens) {
                    append(Regex.escape(english.substring(position, token.range.first)))
                    append("(.+?)")
                    position = token.range.last + 1
                }
                append(Regex.escape(english.substring(position))); append("$")
            }
            Template(Regex(pattern, RegexOption.DOT_MATCHES_ALL), traditional)
        }
    }
    fun translate(text: String, language: AppLanguage): String {
        chinese[text]?.let { return it }
        for (template in templates) {
            val match = template.pattern.matchEntire(text) ?: continue
            return Regex("\\{(\\d+)\\}").replace(template.replacement) { token ->
                match.groupValues.getOrNull(token.groupValues[1].toInt() + 1) ?: token.value
            }
        }
        return text
    }
}

@Composable
fun AppText(
    text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified, fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null, fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified, textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null, lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip, softWrap: Boolean = true, maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1, onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current, localize: Boolean = true,
) {
    androidx.compose.material3.Text(
        text = if (localize) AppCopy.translate(text, LocalAppLanguage.current) else text,
        modifier = modifier, color = color, fontSize = fontSize, fontStyle = fontStyle, fontWeight = fontWeight,
        fontFamily = fontFamily, letterSpacing = letterSpacing, textDecoration = textDecoration,
        textAlign = textAlign, lineHeight = lineHeight, overflow = overflow, softWrap = softWrap,
        maxLines = maxLines, minLines = minLines, onTextLayout = onTextLayout, style = style,
    )
}
