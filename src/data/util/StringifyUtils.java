package data.util;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.sun.javafx.beans.annotations.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class StringifyUtils
{
    public static String shortMarketApiStringFromSet(Set<MarketAPI> marketSet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        for (MarketAPI market : marketSet) {
            sb.append(shortMarketApiString(market));
        }
        sb.append(" }");

        return sb.toString();
    }

    public static String shortMarketApiString(@NonNull MarketAPI market) {
        StringBuilder sb = new StringBuilder();
        sb.append("MarketAPI[").append("name=").append(market.getName()).append(", faction=").append(market.getFaction()).append("]");

        return sb.toString();
    }

    public static String stringifyColonyList(Collection<MarketAPI> coloniesCollection) {
        StringBuffer sb = new StringBuffer();
        for (Iterator<MarketAPI> iter = coloniesCollection.iterator(); iter.hasNext();  ) {
            MarketAPI colony = iter.next();
            sb
                    .append("Faction ID: ")
                    .append(colony.getFactionId())
                    .append(", ");
        }
        // rewind last two letters if non-empty
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }

        return sb.toString();
    }
}
