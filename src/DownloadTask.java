import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class DownloadTask implements Runnable {
    private String startMessage = "";
    private String finishMessage = "";
    private String fileUrl;
    private IDownloadProgress progressChangedCallback;
    private String outputName;
    private String outputDirectory;
    private Runnable finishCallback;

    public DownloadTask(String fileUrl, String outputDirectory, String outputName, String startMessage, String finishMessage, IDownloadProgress progressChangedCallback, Runnable finishCallback) {

        this.startMessage = startMessage;
        this.finishMessage = finishMessage;
        this.fileUrl = fileUrl;
        this.finishCallback = finishCallback;
        this.outputDirectory = outputDirectory;
        this.outputName = outputName;
        this.progressChangedCallback = progressChangedCallback;

    }

    @Override
    public void run() {
        try {
            Files.createDirectories(Path.of(outputDirectory));

            if (!startMessage.isEmpty()) System.out.println(startMessage);

            URL url = new URL(fileUrl);
            URLConnection connection = url.openConnection();

            // get file size
            if (connection instanceof HttpURLConnection httpConnection) httpConnection.setRequestMethod("HEAD");

            connection.getInputStream();
            long totalFileSize = connection.getContentLengthLong();

            if (connection instanceof HttpURLConnection httpsConnection) httpsConnection.disconnect();

            // get data
            BufferedInputStream inputStream = new BufferedInputStream(url.openStream());
            File outputFile = new File(outputDirectory + File.separator + outputName);
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            BufferedOutputStream outputStream = new BufferedOutputStream(fileOutputStream);

            long currentFileSize = 0;
            if (progressChangedCallback != null) progressChangedCallback.progressChanged(currentFileSize, totalFileSize);
            int byteDatum = inputStream.read();
            while (byteDatum != -1) {
                currentFileSize++;
                outputStream.write(byteDatum);

                if (progressChangedCallback != null)
                    progressChangedCallback.progressChanged(currentFileSize, totalFileSize);

                byteDatum = inputStream.read();
            }
            inputStream.close();
            outputStream.close();

            if (!finishMessage.isEmpty()) System.out.println(finishMessage);
            if (finishCallback != null) finishCallback.run();

        } catch (Exception e) {
            System.out.println("Failed to download file: " + fileUrl);
            e.printStackTrace();
            System.exit(1);
        }

    }

}
