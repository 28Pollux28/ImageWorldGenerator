package eu.pollux28.imggen.config;
//Code used from Simplex Terrain <https://github.com/SuperCoder7979/simplexterrain>, with permission from SuperCoder79
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.pollux28.imggen.ImgGen;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ConfigUtil {
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static <T> Constructor<T> getConstructor(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static <T> T create(Constructor<T> constructor) {
        if (constructor == null) {
            return null;
        }

        try {
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T getFromConfig(Class<T> configClass, Path path) {
        T config = create(getConstructor(configClass));
        try {
            //config exists: return value
            if (Files.exists(path)) {
                config = ConfigUtil.gson.fromJson(new FileReader(path.toFile()), configClass);

                //update to newest config using le epic reflection hacks
                String version = (String) config.getClass().getField("configVersion").get(config);
                //boolean reload = (Boolean) config.getClass().getField("reloadConfig").get(config);
                if (!version.equals(ImgGen.VERSION)) {
                    config.getClass().getField("configVersion").set(config, ImgGen.VERSION);
                    BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()));
                    writer.write(ConfigUtil.gson.toJson(config));
                    writer.close();
                }
                /*if (reload){
                    MainConfigData data = new MainConfigData();
                    Field[] fields = data.getClass().getFields();
                    List<Field> fieldL =Arrays.asList(fields);
                    List<Field>fieldL2 = Arrays.asList(config.getClass().getFields());
                    for(Field f : List.copyOf(fieldL2)){
                        if(fieldL.size()==0){
                            break;
                        }
                        for(Field f2 : fieldL){
                            if (f.getName().equals(f2.getName())){
                                fieldL.remove(f2);
                                fieldL2.remove(f);
                                break;
                            }
                        }
                    }
                    if(fieldL2.size()!=0){
                        for (Field f:fieldL2){
                            f.set(config,data.getClass().getField(f.getName()).get(config));
                        }
                        BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()));
                        writer.write(ConfigUtil.gson.toJson(config));
                        writer.close();
                    }
                    config = ConfigUtil.gson.fromJson(new FileReader(path.toFile()), configClass);
                    return config;
                }*/
            } else {
                //config does not exist: write value
                BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()));
                writer.write(ConfigUtil.gson.toJson(config));
                writer.close();
            }
        } catch (IOException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return config;
    }
}
