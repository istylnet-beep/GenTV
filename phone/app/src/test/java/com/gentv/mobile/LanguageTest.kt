package com.gentv.mobile

import com.gentv.mobile.data.Store
import com.gentv.mobile.ui.AppCopy
import com.gentv.mobile.ui.AppLanguage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LanguageTest {
    @get:Rule val tmp = TemporaryFolder()

    @Test fun freshInstallAndLegacyPreferencesAlwaysUseTaiwanTraditionalChinese() {
        assertEquals("zh-TW", Store(tmp.root).language())
        for (legacy in listOf("en", "zh", "invalid")) {
            java.io.File(tmp.root, "language.txt").writeText(legacy)
            assertEquals("zh-TW", Store(tmp.root).language())
            assertEquals(AppLanguage.TRADITIONAL_CHINESE, AppLanguage.from(legacy))
        }
        Store(tmp.root).saveLanguage("zh-TW")
        assertEquals("zh-TW", Store(tmp.root).language())
    }

    @Test fun translationsUseTaiwanTraditionalChineseAndKeepDynamicValues() {
        assertEquals("應用管理", AppCopy.translate("Apps", AppLanguage.TRADITIONAL_CHINESE))
        assertEquals("設定", AppCopy.translate("Settings", AppLanguage.TRADITIONAL_CHINESE))
        assertEquals("已停用 4 項", AppCopy.translate("4 disabled", AppLanguage.TRADITIONAL_CHINESE))
        assertEquals("可用 5.7 GB / 總容量 9.8 GB", AppCopy.translate("5.7 GB free / 9.8 GB total", AppLanguage.TRADITIONAL_CHINESE))
        assertEquals("com.example.player/.Main", AppCopy.translate("com.example.player/.Main", AppLanguage.TRADITIONAL_CHINESE))
    }
}
