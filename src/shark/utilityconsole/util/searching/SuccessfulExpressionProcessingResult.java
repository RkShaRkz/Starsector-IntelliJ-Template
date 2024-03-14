package shark.utilityconsole.util.searching;

import com.sun.javafx.beans.annotations.NonNull;
import shark.utilityconsole.util.CommonUtil;

/**
 * Class representing a successful expression processing result, containing only a {@link ParameterCriterion}
 * extracted from the user's input, to be added to the {@link SearchCriteria} before calling {@link CommonUtil#findShips(SearchCriteria, CommonUtil.FindShipsListener)}
 */
public class SuccessfulExpressionProcessingResult extends ExpressionProcessingResult {

    public SuccessfulExpressionProcessingResult(@NonNull ParameterCriterion result) {
        super(false, null, result);
    }
}
