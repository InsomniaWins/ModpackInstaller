import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class ModpackSetup {

    private static final String JAR_PATH = ModpackSetup.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    private static final String MINECRAFT_FOLDER = System.getProperty("user.home") + "/AppData/Roaming/.minecraft";
    private static final String MODS_FOLDER = MINECRAFT_FOLDER + "/mods";
    private static final String INSTALLER_DIRECTORY = "forge-installer";
    private static final String INSTALLER_NAME = "ForgeInstaller.jar";
    private static final String INSTALLER_PATH = INSTALLER_DIRECTORY + "/" + INSTALLER_NAME;
    private static final String MOD_LIST_URL = "https://github.com/InsomniaWins/ModDownloader2/raw/master/modlist.txt";

    private static final IDownloadProgress DOWNLOAD_PROGRESS_LISTENER = new IDownloadProgress() {

        int currentProgress = 0;
        int totalProgress = 20;

        @Override
        public void progressChanged(long currentFileSize, long totalFileSize) {
            double progressRatio = currentFileSize / (double) totalFileSize;
            int downloadProgress = (int) (100.0 * progressRatio);
            int progressCheck = (int) (20 * progressRatio);

            if (progressCheck != currentProgress) {

                currentProgress = progressCheck;

                System.out.print("\rTotal Progress: |");
                for (int i = 0; i < totalProgress; i++) {
                    if (i > currentProgress) {
                        System.out.print("_");
                    } else {
                        System.out.print("#");
                    }
                }
                System.out.print("|  " + downloadProgress + "%");

                if (currentFileSize == totalFileSize) System.out.print("\n");
            }
        }
    };

    public static void main(String[] args) {

        System.out.println("Welcome to mod-pack setup!");

        System.out.println("Checking forge installation . . . ");
        if (!forgeIsInstalled()) {
            if (forgeInstallerAlreadyExists()) {
                runForgeInstaller();

            } else {
                try {
                    downloadForgeInstaller();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("Forge is already installed.");
        }

        try {
            downloadMods();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        try {
            downloadOptionsFiles();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        System.out.println("\n\nFinished setting up modpack!");
        System.out.println("\n\nWARNING: it is recommended to play with at least 3GB of RAM.\nIf you do not know how to do this, please ask someone who does!");
    }

    private static void downloadOptionsFiles() throws InterruptedException {

        // download minecraft options
        Thread thread = new Thread(new DownloadTask(
                "https://github.com/InsomniaWins/ModDownloader2/raw/master/options.txt",
                MINECRAFT_FOLDER,
                "options.txt",
                "Downloading Minecraft options . . .",
                "Finished downloading Minecraft options!",
                DOWNLOAD_PROGRESS_LISTENER,
                null
        ));

        thread.start();
        thread.join();

        // download optifine options
        thread = new Thread(new DownloadTask(
                "https://github.com/InsomniaWins/ModDownloader2/raw/master/optionsof.txt",
                MINECRAFT_FOLDER,
                "optionsof.txt",
                "Downloading Optifine options . . .",
                "Finished downloading Optifine options!",
                DOWNLOAD_PROGRESS_LISTENER,
                null
        ));

        thread.start();
        thread.join();


        System.out.println("Finished downloading all options files!");

    }

    private static void downloadMods() throws IOException, InterruptedException {

        System.out.println("Getting mod-list . . . ");

        ArrayList<String> modList = new ArrayList<>();

        URL modListUrl = new URL(MOD_LIST_URL);
        Scanner scanner = new Scanner(modListUrl.openStream());

        while (scanner.hasNextLine()) {
            modList.add(scanner.nextLine());
        }

        scanner.close();

        System.out.println("Got mod-list!");



        System.out.println("Downloading mods . . . ");

        for (int i = 0; i < modList.size(); i++) {
            String modUrl = modList.get(i);
            String fileName = modUrl.substring(modUrl.lastIndexOf('/')+1);

            if (!modExists(fileName)) {
                Thread thread = new Thread(new DownloadTask(
                        modUrl,
                        MODS_FOLDER,
                        fileName,
                        "Downloading: " + fileName + " . . . ",
                        "(" + (i+1) + "/" + modList.size() +")  Finished downloading " + fileName +"!",
                        DOWNLOAD_PROGRESS_LISTENER,
                        null
                ));

                thread.start();
                thread.join();

            } else {
                System.out.println(fileName + " already exists in mods folder, continuing to next mod.");
            }
        }

        System.out.println("Finished downloading mods!");
    }

    private static boolean modExists(String fileName) {
        return new File(MODS_FOLDER + "/" + fileName).exists();
    }

    private static void downloadForgeInstaller() throws IOException, InterruptedException {
        Thread thread = new Thread(new DownloadTask(
                "https://maven.minecraftforge.net/net/minecraftforge/forge/1.16.5-36.2.39/forge-1.16.5-36.2.39-installer.jar",
                INSTALLER_DIRECTORY,
                INSTALLER_NAME,
                "Downloading forge installer . . . ",
                "Finished downloading forge installer!",
                DOWNLOAD_PROGRESS_LISTENER,
                ModpackSetup::runForgeInstaller
        ));

        thread.start();
        thread.join();
    }

    public static void runForgeInstaller() {

        System.out.println("Running forge installer . . . ");
        System.out.println("IMPORTANT  >>  Make sure \"Install Client\" is selected, then click \"OK\"");
        System.out.println("IMPORTANT  >>  When forge is done installing, click \"OK\" and wait for this console to finish.");

        while (true) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "\"" + INSTALLER_PATH+"\"");
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Forge Installer: " + line);
                }

                process.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            if (forgeIsInstalled()) break;
            System.out.println("Something went wrong installing forge, running installer again.");
        }

        System.out.println("Finished installing forge.");
    }

    private static boolean forgeInstallerAlreadyExists() {
        File fileCheck = new File(INSTALLER_PATH);
        return fileCheck.exists();
    }

    private static boolean forgeIsInstalled() {
        File dirCheck = new File(MINECRAFT_FOLDER + "/versions/1.16.5-forge-36.2.39");
        return dirCheck.exists();
    }

    private static void setLowestSettings() {



    }
}