package shark.utilityconsole.util.searching;

import com.sun.javafx.beans.annotations.NonNull;
import org.lazywizard.console.BaseCommand.CommandResult;

/**
 * Class representing a failed expression processing result, containing only a {@link CommandResult} to show to the user
 */
public class FailedExpressionProcessingResult extends ExpressionProcessingResult {

    public FailedExpressionProcessingResult(@NonNull CommandResult result) {
        super(true, result, null);
    }
}
