package com.gentv.tv;

import android.content.Context;
import android.content.res.Configuration;
import java.util.Locale;

/** The TV launcher is distributed in Traditional Chinese (Taiwan) only. */
final class LauncherLocale {
    private static final Locale APP_LOCALE = new Locale("zh", "TW");

    private LauncherLocale() {
    }

    /** Always use Traditional Chinese (Taiwan), regardless of the TV or imported config language. */
    static Locale parse(String ignoredTag) {
        return APP_LOCALE;
    }

    /** Apply the fixed app language while preserving the device's other configuration settings. */
    static Context wrap(Context base) {
        Locale.setDefault(APP_LOCALE);
        Configuration c = new Configuration(base.getResources().getConfiguration());
        c.setLocale(APP_LOCALE);
        return base.createConfigurationContext(c);
    }
}
