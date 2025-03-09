package data.util;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.sun.javafx.beans.annotations.NonNull;

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
}
