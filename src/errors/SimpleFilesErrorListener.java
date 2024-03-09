package errors;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;


public class SimpleFilesErrorListener extends BaseErrorListener {

    public static final SimpleFilesErrorListener INSTANCE = new SimpleFilesErrorListener();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        throw new SimpleFilesExecutionException("line " + line + ":" + charPositionInLine + " " + msg);
    }
}
