package data.scripts;

import com.fs.starfarer.api.Global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VayraTags {
    public static final String PREFIX = "_";

    public static final String ROUND = "cartridge";

    public static final String EWAR = "ew";

    public static final String DIST = "nearby";

    public static final String OBSOLETE = "coal";

    public static final String LETTER = "ZZZzZZ";

    public static String de(String q) {
        String u = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        q = q.replaceAll("[^" + u + "=]", "");
        String t = (q.charAt(q.length() - 1) == '=') ? ((q.charAt(q.length() - 2) == '=') ? "AA" : "A") : "";
        String r = "";
        q = q.substring(0, q.length() - t.length()) + t;
        for (int v = 0; v < q.length(); v += 4) {
            int s = (u.indexOf(q.charAt(v)) << 18) + (u.indexOf(q.charAt(v + 1)) << 12) + (u.indexOf(q.charAt(v + 2)) << 6) + u.indexOf(q.charAt(v + 3));
            r = r + (char) (s >>> 16 & 0xFF) + (char) (s >>> 8 & 0xFF) + (char) (s & 0xFF);
        }
        return r.substring(0, r.length() - t.length());
    }

    private static final String TEST = "nearby".charAt(0) + "ew" + "_" + "cartridge".charAt(7) + "nearby".charAt(2) + "coal".substring(3) + "cartridge".substring(1).charAt(0) + "coal".substring(0, 1) + "cartridge".charAt(3) + "cartridge".charAt(5) + "cartridge".substring(0, 1) + "_" + "coal".charAt(1) + "nearby".charAt(3) + "cartridge".charAt(6) + "nearby".substring(1).charAt(0) + "cartridge".substring(2).substring(2).charAt(0);

    private static final String TEST2 = "cartridge".substring(1).charAt(0) + "ew".charAt(0) + "si" + "nearby".charAt(3);

    public static final List<String> E = new ArrayList<>(Arrays.asList(TEST, "diableavionics", "sindrian_diktat", "nearby".charAt(2) + "r" + "i" + "array".charAt(0), "fpe", "legioinfernalis"));

    public static boolean readSpecial() {
        return (Global.getSector().getStarSystem(TEST2) != null);
    }
}
