package shark.utilityconsole.util;

/**
 * Util class for holding all formatting-related methods so that they're uniform across the whole project
 */
public class FormatUtil {

    /**
     * Consistently format all IDs to be the same, so that they don't have to be changed everywhere by hand.
     * Uses 8 characters for all IDs.
     * <b>Longer IDs will not be truncated to this length</b>
     * @param id the ID to format
     * @return formatted ID
     */
    public static String formatId(String id) {
        return String.format("%8s", id);
    }

    /**
     * Formats hull IDs. Uses 64 characters for hull IDs.
     * <b>Longer IDs will not be truncated to this length</b>
     * @param hullId the hull ID to format
     * @return formatted ID
     */
    public static String formatHullId(String hullId) {
        return String.format("%64s", hullId);
    }

    /**
     * Formats names. Works for both ship names and hull names. Uses 32 characters for all names.
     * <b>Longer names will not be truncated to this length</b>
     * @param name the name to format
     * @return formatted name
     */
    public static String formatName(String name) {
        return String.format("%32s", name);
    }
}
