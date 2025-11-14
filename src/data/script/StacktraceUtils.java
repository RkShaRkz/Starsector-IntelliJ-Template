package data.script;

public class StacktraceUtils {

    public static String unwindStacktrace(StackTraceElement[] stacktraceArray) {
        StringBuilder sb = new StringBuilder();
        for (int i = stacktraceArray.length - 1; i > 0; i-- ) {
            sb.append("Element [").append(i).append("]:\t\t").append(stacktraceArray[i]);
        }
        return sb.toString();
    }
}
