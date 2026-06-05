package shark.utilityconsole.util;

import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.console.Console;
import shark.utilityconsole.util.searching.ParameterCriterion;

public class ParsingSynthaxUtil {
    public static ParameterCriterion.CriteriaQuantity.Quantity remapQuantitySymbol(String symbol) {
        /**
         * <, <=, =, >, >=
         */
        ParameterCriterion.CriteriaQuantity.Quantity retVal = null;
        if (symbol.equalsIgnoreCase("<")) {
            retVal = ParameterCriterion.CriteriaQuantity.Quantity.LESS_THAN;
        } else if (symbol.equalsIgnoreCase("<=")) {
            retVal = ParameterCriterion.CriteriaQuantity.Quantity.LESS_OR_EQUAL;
        } else if (symbol.equalsIgnoreCase("=")) {
            retVal = ParameterCriterion.CriteriaQuantity.Quantity.EXACTLY;
        } else if (symbol.equalsIgnoreCase(">")) {
            retVal = ParameterCriterion.CriteriaQuantity.Quantity.MORE_THAN;
        } else if (symbol.equalsIgnoreCase(">=")) {
            retVal = ParameterCriterion.CriteriaQuantity.Quantity.MORE_OR_EQUAL;
        } else {
            Console.showMessage("Invalid symbol to remap to QuantitySymbol! Received " + symbol);
        }

        return retVal;
    }

    public static boolean isWeaponSizeSymbol(String symbol) {
        boolean retVal = false;
        retVal = symbol.equalsIgnoreCase("large") || symbol.equalsIgnoreCase("medium") || symbol.equalsIgnoreCase("small");

        return retVal;
    }

    public static WeaponAPI.WeaponSize remapWeaponSizeSymbol(String symbol) {
        WeaponAPI.WeaponSize retVal = null;
        if (symbol.equalsIgnoreCase("small")) {
            retVal = WeaponAPI.WeaponSize.SMALL;
        } else if (symbol.equalsIgnoreCase("medium")) {
            retVal = WeaponAPI.WeaponSize.MEDIUM;
        } else if (symbol.equalsIgnoreCase("large")) {
            retVal = WeaponAPI.WeaponSize.LARGE;
        } else {
            Console.showMessage("Invalid symbol to remap to WeaponSize! Received " + symbol);
        }

        return retVal;
    }
}
