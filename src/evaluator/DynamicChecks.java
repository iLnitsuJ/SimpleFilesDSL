package evaluator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;

public class DynamicChecks {


    // Checks if the destination folder has enough size for the target size, true if enough

    public static boolean destFolderSizeCheck(String path, float approx_target_size_in_bytes){
        boolean hasEnough = false;
        Path folderPath = Path.of(path);
        try {
            FileStore fileStore = Files.getFileStore(folderPath);
            hasEnough = fileStore.getUsableSpace() > approx_target_size_in_bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return hasEnough;
    }

    // Returns easier to understand runtime error message based on exception handled
    public static void runtimeExceptionHandling(Exception e, String action, String reason){
        String message = "";

        if (e instanceof FileNotFoundException){
            message = reason + "\n" + "File not found exception occurred,  " + action + " was not performed.\n";
        } else if (e instanceof NoSuchFileException){
            message = reason + "\n" + e.getMessage() + " " + action + " was not performed.\n";
        } else if (e instanceof IOException){
            message = reason + "\n" + e.getMessage();
        } else {
            // TODO: exceptions not handled yet
            message = reason + "\n" + e.getMessage();
        }

        System.err.println(message);

    }


}
