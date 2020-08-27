package eu.pollux28.imggen.config;
//Code used from Simplex Terrain <https://github.com/SuperCoder7979/simplexterrain>, with permission from SuperCoder79
import eu.pollux28.imggen.ImgGen;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void init() {
        MainConfigData configData = null;
        try {

            Path configDir = Paths.get("", "config", "imggen.json");
            if (Files.exists(configDir)) {
                configData = ConfigUtil.gson.fromJson(new FileReader(configDir.toFile()), MainConfigData.class);
                //save new values
                if (!configData.configVersion.equals(ImgGen.VERSION)) {
                    configData.configVersion = ImgGen.VERSION;
                    BufferedWriter writer = new BufferedWriter(new FileWriter(configDir.toFile()));
                    writer.write(ConfigUtil.gson.toJson(configData));
                    writer.close();
                }
            } else {
                configData = new MainConfigData();
                SecurityManager sec;
                Paths.get("", "config").toFile().mkdirs();
                BufferedWriter writer = new BufferedWriter(new FileWriter(configDir.toFile()));
                writer.write(ConfigUtil.gson.toJson(configData));

                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImgGen.CONFIG = configData;
    }
}
