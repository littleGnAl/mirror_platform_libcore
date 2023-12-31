/*
 * Copyright (c) 2012, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package test.java.time.format;

import static org.testng.Assert.assertEquals;

import android.platform.test.annotations.LargeTest;

import java.text.DateFormatSymbols;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DecimalStyle;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalQueries;
import java.time.zone.ZoneRulesProvider;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import jdk.test.lib.RandomFactory;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/*
 * @test
 * @bug 8081022 8151876 8166875 8189784 8206980
 * @key randomness
 */

/**
 * Test ZoneTextPrinterParser
 */
@Test
public class TestZoneTextPrinterParser extends AbstractTestPrinterParser {

    protected static DateTimeFormatter getFormatter(Locale locale, TextStyle style) {
        return new DateTimeFormatterBuilder().appendZoneText(style)
                                             .toFormatter(locale)
                                             .withDecimalStyle(DecimalStyle.of(locale));
    }

    @LargeTest
    public void test_printText() {
        Random r = RandomFactory.getRandom();
        // Android-changed: only run one iteration.
        int N = 1;
        Locale[] locales = Locale.getAvailableLocales();
        Set<String> zids = ZoneRulesProvider.getAvailableZoneIds();
        ZonedDateTime zdt = ZonedDateTime.now();

        //System.out.printf("locale==%d, timezone=%d%n", locales.length, zids.size());
        while (N-- > 0) {
            zdt = zdt.withDayOfYear(r.nextInt(365) + 1)
                     .with(ChronoField.SECOND_OF_DAY, r.nextInt(86400));
            // Android-changed: loop over locales first to speed up test. TimeZoneNames are cached
            // per locale, but the cache only holds the most recently used locales.
            /*
            for (String zid : zids) {
                if (zid.equals("ROC") || zid.startsWith("Etc/GMT")) {
                    continue;      // TBD: match jdk behavior?
                }
                zdt = zdt.withZoneSameLocal(ZoneId.of(zid));
                TimeZone tz = TimeZone.getTimeZone(zid);
                boolean isDST = tz.inDaylightTime(new Date(zdt.toInstant().toEpochMilli()));
                for (Locale locale : locales) {
                    boolean isDST = tz.inDaylightTime(new Date(zdt.toInstant().toEpochMilli()));
                    String longDisplayName = tz.getDisplayName(isDST, TimeZone.LONG, locale);
                    String shortDisplayName = tz.getDisplayName(isDST, TimeZone.SHORT, locale);
                    if ((longDisplayName.startsWith("GMT+") && shortDisplayName.startsWith("GMT+"))
                            || (longDisplayName.startsWith("GMT-") && shortDisplayName.startsWith("GMT-"))) {
                        printText(locale, zdt, TextStyle.FULL, tz, tz.getID());
                        printText(locale, zdt, TextStyle.SHORT, tz, tz.getID());
                        continue;
                    }
             */
            for (Locale locale : locales) {
                // Android-changed: "ji" isn't correctly aliased to "yi", see http//b/8634320.
                if (locale.getLanguage().equals("ji")) {
                    continue;
                }
                for (String zid : zids) {
                    if (zid.equals("ROC") || zid.startsWith("Etc/GMT")) {
                        continue;      // TBD: match jdk behavior?
                    }
                    // Android-changed (http://b/33197219): TimeZone.getDisplayName() for
                    // non-canonical time zones are not correct.
                    if (!zid.equals(getSystemCanonicalID(zid))) {
                        continue;
                    }
                    zdt = zdt.withZoneSameLocal(ZoneId.of(zid));
                    TimeZone tz = TimeZone.getTimeZone(zid);
                    // Android-changed: We don't have long names for GMT.
                    if (tz.getID().equals("GMT")) {
                        continue;
                    }
                    boolean isDST = tz.inDaylightTime(new Date(zdt.toInstant().toEpochMilli()));
                    printText(locale, zdt, TextStyle.FULL, tz,
                        tz.getDisplayName(isDST, TimeZone.LONG, locale));
                    printText(locale, zdt, TextStyle.SHORT, tz,
                        tz.getDisplayName(isDST, TimeZone.SHORT, locale));
                }
            }
        }
    }

    // BEGIN Android-added: Get non-custom system canonical time zone Id from ICU.
    private static String getSystemCanonicalID(String zid) {
        if (android.icu.util.TimeZone.UNKNOWN_ZONE_ID.equals(zid)) {
            return zid;
        }
        boolean[] isSystemID = { false };
        String canonicalID = android.icu.util.TimeZone.getCanonicalID(zid, isSystemID);
        if (canonicalID == null || !isSystemID[0]) {
            return null;
        }
        return canonicalID;
    }
    // END Android-added: Get non-custom system canonical time zone Id from ICU.

    private void printText(Locale locale, ZonedDateTime zdt, TextStyle style, TimeZone zone, String expected) {
        String result = getFormatter(locale, style).format(zdt);
        // Android-changed: TimeZone.getDisplayName() will never return "GMT".
        if (result.startsWith("GMT") && expected.equals("GMT+00:00")) {
            return;
        }
        if (!result.equals(expected)) {
            if (result.equals("FooLocation")) { // from rules provider test if same vm
                return;
            }
            System.out.println("----------------");
            System.out.printf("tdz[%s]%n", zdt.toString());
            System.out.printf("[%-5s, %5s] :[%s]%n", locale.toString(), style.toString(),result);
            System.out.printf(" %5s, %5s  :[%s] %s%n", "", "", expected, zone);
        }
        assertEquals(result, expected);
    }

    // Android-changed: disable test as it doesn't assert anything and produces a lot of output.
    @Test(enabled = false)
    public void test_ParseText() {
        Locale[] locales = new Locale[] { Locale.ENGLISH, Locale.JAPANESE, Locale.FRENCH };
        Set<String> zids = ZoneRulesProvider.getAvailableZoneIds();
        for (Locale locale : locales) {
            parseText(zids, locale, TextStyle.FULL, false);
            parseText(zids, locale, TextStyle.FULL, true);
            parseText(zids, locale, TextStyle.SHORT, false);
            parseText(zids, locale, TextStyle.SHORT, true);
        }
    }

    private static Set<ZoneId> preferred = new HashSet<>(Arrays.asList(new ZoneId[] {
        ZoneId.of("EST", ZoneId.SHORT_IDS),
        ZoneId.of("Asia/Taipei"),
        ZoneId.of("Asia/Macau"),
        ZoneId.of("CET"),
    }));

    private static Set<ZoneId> preferred_s = new HashSet<>(Arrays.asList(new ZoneId[] {
         ZoneId.of("EST", ZoneId.SHORT_IDS),
         ZoneId.of("CET"),
         ZoneId.of("Australia/South"),
         ZoneId.of("Australia/West"),
         ZoneId.of("Asia/Shanghai"),
    }));

    private static Set<ZoneId> none = new HashSet<>();

    @DataProvider(name="preferredZones")
    Object[][] data_preferredZones() {
        // Android-changed: Differences in time zone name handling.
        // Android and java.time (via the RI) have differences in how they handle Time Zone Names.
        // - Android doesn't use IANA abbreviates (usually 3-letter abbreviations) except where they
        //   are widely used in a given locale (so CST will not resolve to "Chinese Standard Time").
        // - Android doesn't provide long names for zones like "CET". Only the Olson IDs like
        //   "Europe/London" have names attached to them.
        // - When no preferred zones are provided then no guarantee is made about the specific zone
        //   returned.
        // - Android uses the display name "Taipei Standard Time" as CLDR does.
        // Basically Android time zone parsing sticks strictly to what can be done with the data
        // provided by IANA and CLDR and avoids introducing additional values (like specific order
        // and additional names) to those.
        return new Object[][] {
            // {"America/New_York", "Eastern Standard Time", none,      Locale.ENGLISH, TextStyle.FULL},
//          {"EST",              "Eastern Standard Time", preferred, Locale.ENGLISH, TextStyle.FULL},
            // {"Europe/Paris",     "Central European Time", none,      Locale.ENGLISH, TextStyle.FULL},
//          {"CET",              "Central European Time", preferred, Locale.ENGLISH, TextStyle.FULL}, no three-letter ID in CLDR
            // {"Asia/Shanghai",    "China Standard Time",   none,      Locale.ENGLISH, TextStyle.FULL},
            {"Asia/Macau",       "China Standard Time",   preferred, Locale.ENGLISH, TextStyle.FULL},
            // {"Asia/Taipei",      "China Standard Time",   preferred, Locale.ENGLISH, TextStyle.FULL},
            // {"America/Chicago",  "CST",                   none,      Locale.ENGLISH, TextStyle.SHORT},
            // {"Asia/Taipei",      "CST",                   preferred, Locale.ENGLISH, TextStyle.SHORT},
            // Australia/South is a valid synonym for Australia/Adelaide, so this test will pass.
            {"Australia/South",  "ACST",                  preferred_s, new Locale("en", "AU"), TextStyle.SHORT},
            // {"America/Chicago",  "CDT",                   none,        Locale.ENGLISH, TextStyle.SHORT},
            // {"Asia/Shanghai",    "CDT",                   preferred_s, Locale.ENGLISH, TextStyle.SHORT},
            // {"America/Juneau",   "AKST",                  none,      Locale.ENGLISH, TextStyle.SHORT},
            // {"America/Juneau",   "AKDT",                  none,      Locale.ENGLISH, TextStyle.SHORT},
            {"Pacific/Honolulu", "HST",                   none,      Locale.ENGLISH, TextStyle.SHORT},
            // {"America/Halifax",  "AST",                   none,      Locale.ENGLISH, TextStyle.SHORT},
            {"Z",                "Z",                     none,      Locale.ENGLISH, TextStyle.SHORT},
            {"Z",                "Z",                     none,      Locale.US,      TextStyle.SHORT},
            {"Z",                "Z",                     none,      Locale.CANADA,  TextStyle.SHORT},
       };
    }

    @Test(dataProvider="preferredZones")
    public void test_ParseText(String expected, String text, Set<ZoneId> preferred, Locale locale, TextStyle style) {
        DateTimeFormatter fmt = new DateTimeFormatterBuilder().appendZoneText(style, preferred)
                                                              .toFormatter(locale)
                                                              .withDecimalStyle(DecimalStyle.of(locale));

        String ret = fmt.parse(text, TemporalQueries.zone()).getId();

        System.out.printf("[%-5s %s] %24s -> %s(%s)%n",
                          locale.toString(),
                          style == TextStyle.FULL ? " full" :"short",
                          text, ret, expected);

        assertEquals(ret, expected);

    }


    private void parseText(Set<String> zids, Locale locale, TextStyle style, boolean ci) {
        System.out.println("---------------------------------------");
        DateTimeFormatter fmt = getFormatter(locale, style, ci);
        for (String[] names : new DateFormatSymbols(locale).getZoneStrings()) {
            if (!zids.contains(names[0])) {
                continue;
            }
            String zid = names[0];
            String expected = ZoneName.toZid(zid, locale);

            parse(fmt, zid, expected, zid, locale, style, ci);
            int i = style == TextStyle.FULL ? 1 : 2;
            for (; i < names.length; i += 2) {
                parse(fmt, zid, expected, names[i], locale, style, ci);
            }
        }
    }

    private void parse(DateTimeFormatter fmt,
                       String zid, String expected, String text,
                       Locale locale, TextStyle style, boolean ci) {
        if (ci) {
            text = text.toUpperCase();
        }
        String ret = fmt.parse(text, TemporalQueries.zone()).getId();
        // TBD: need an excluding list
        // assertEquals(...);
        if (ret.equals(expected) ||
            ret.equals(zid) ||
            ret.equals(ZoneName.toZid(zid)) ||
            ret.equals(expected.replace("UTC", "UCT"))) {
            return;
        }
        System.out.printf("[%-5s %s %s %16s] %24s -> %s(%s)%n",
                          locale.toString(),
                          ci ? "ci" : "  ",
                          style == TextStyle.FULL ? " full" :"short",
                          zid, text, ret, expected);
    }

    private DateTimeFormatter getFormatter(Locale locale, TextStyle style, boolean ci) {
        DateTimeFormatterBuilder db = new DateTimeFormatterBuilder();
        if (ci) {
            db = db.parseCaseInsensitive();
        }
        return db.appendZoneText(style)
                 .toFormatter(locale)
                 .withDecimalStyle(DecimalStyle.of(locale));
    }

}
