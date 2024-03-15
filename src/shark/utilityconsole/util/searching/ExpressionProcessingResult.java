package shark.utilityconsole.util.searching;

import org.jetbrains.annotations.Nullable;
import org.lazywizard.console.BaseCommand.CommandResult;

/**
 * Simple POJO class holding a few things
 * @see FailedExpressionProcessingResult
 * @see {@link SuccessfulExpressionProcessingResult}
 */
public class ExpressionProcessingResult {
    private final boolean failed;
    private final CommandResult commandResult;
    private final ParameterCriterion result;

    protected ExpressionProcessingResult(boolean failed, CommandResult commandResult, ParameterCriterion result) {
        this.failed = failed;
        this.commandResult = commandResult;
        this.result = result;
    }

    /**
     * Was the expression processing successful
     * @return whether the expression processing has failed or not
     */
    public boolean isSuccess() {
        return !failed;
    }

    /**
     * Available <b>only</b> if the command has already failed before executing ({@link #failed} is true), or <b>null</b> otherwise
     * @return the {@link CommandResult} to return to the user
     */
    @Nullable
    public CommandResult getCommandResult() {
        return commandResult;
    }

    /**
     * Available <b>only</b> if the command has not failed and the expression processing was successful, or <b>null</b> otherwise
     * @return the {@link ParameterCriterion} extracted from the user's input
     */
    public ParameterCriterion getResult() {
        return result;
    }
}

