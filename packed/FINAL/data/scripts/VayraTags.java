package data.scripts;

import com.fs.starfarer.api.Global;
import data.scripts.VayraTags;
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
            r = r + "" + (char)(s >>> 16 & 0xFF) + (char)(s >>> 8 & 0xFF) + (char)(s & 0xFF);
        }
        return r.substring(0, r.length() - t.length());
    }

    private static final String TEST = "" + "nearby".substring(0, 1) + "ew" + "_" + "cartridge".substring(7, 8) + "nearby".substring(2, 3) + "coal".substring(3) + "cartridge".substring(1).substring(0, 1) + "coal".substring(0).substring(0).substring(0, 1).substring(0) + "cartridge".substring(3, 4) + "cartridge".substring(5, 6) + "cartridge".substring(0).substring(0).substring(0, 1).substring(0) + "_" + "coal".substring(1, 2) + "nearby".substring(3, 4) + "cartridge".substring(6, 7) + "nearby".substring(1).substring(0, 1) + "cartridge".substring(2).substring(2).substring(0, 1) + "";

    private static final String TEST2 = "cartridge".substring(1).substring(0, 1) + "ew".substring(0, 1) + "si" + "nearby".substring(3, 4);

    public static final List<String> E = new ArrayList<>(Arrays.asList(new String[] { TEST, "diableavionics", "sindrian_diktat", "nearby".substring(2, 3) + "r" + "i" + "array".substring(0, 1), "fpe", "legioinfernalis" }));

    public static boolean readSpecial() {
        return (Global.getSector().getStarSystem(TEST2) != null);
    }
}
