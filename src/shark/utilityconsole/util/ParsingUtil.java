package shark.utilityconsole.util;

import org.lazywizard.console.Console;
import shark.utilityconsole.util.searching.ParameterCriterion;

public class ParsingUtil {
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
}
