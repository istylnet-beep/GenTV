package com.gentv.tv;

import static org.junit.Assert.assertEquals;

import java.util.Locale;
import org.junit.Test;

public class LauncherLocaleTest {
    @Test
    public void appLanguageIsAlwaysTraditionalChineseTaiwan() {
        assertEquals("zh", LauncherLocale.parse("en").getLanguage());
        assertEquals("TW", LauncherLocale.parse("zh-CN").getCountry());
        assertEquals("zh", LauncherLocale.parse("").getLanguage());
        assertEquals("TW", LauncherLocale.parse(null).getCountry());
    }

    @Test
    public void appLocaleDoesNotFollowDeviceOrImportedConfig() {
        assertEquals(new Locale("zh", "TW"), LauncherLocale.parse("en-US"));
        assertEquals(new Locale("zh", "TW"), LauncherLocale.parse("zh-CN"));
        assertEquals(new Locale("zh", "TW"), LauncherLocale.parse("vi"));
    }
}
