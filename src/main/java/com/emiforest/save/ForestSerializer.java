package com.emiforest.save;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ForestSerializer {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();


    public static void save(File file, List<SavedTree> trees) {

        try (FileWriter writer = new FileWriter(file)) {

            GSON.toJson(trees, writer);

            System.out.println("[EMI Forest] JSON guardado");

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    public static List<SavedTree> load(File file) {
        if (!file.exists()) return new ArrayList<>();
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<SavedTree>>(){}.getType();
            List<SavedTree> result = GSON.fromJson(reader, listType);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}