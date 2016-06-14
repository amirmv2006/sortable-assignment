package ir.amv.snippets.sortable.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ir.amv.snippets.sortable.model.Listing;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by AMV on 5/1/2016.
 */
public class GsonUtil {

    private static Gson gson;

    public static Gson gson() {
        if (gson == null) {
            gson = new GsonBuilder().setPrettyPrinting().create();
        }
        return gson;
    }

    public static <T> List<T> readFromFile(String fileName, Class<T> objectClass) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        List<T> result = new ArrayList<T>();
        while ((line = reader.readLine()) != null) {
            T t = gson().fromJson(line, objectClass);
            result.add(t);
        }
        return result;
    }

    public static void toJson(List<?> notMatched, FileOutputStream fileWriter) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer();
        try {
            for (int i = 0; i < notMatched.size(); i++) {
                Object o = notMatched.get(i);
                byte[] bytes = writer.writeValueAsBytes(o);
                fileWriter.write(bytes);
                String s = System.getProperty("line.separator");
                fileWriter.write(s.getBytes());
                if (i % 1000 == 0) {
                    fileWriter.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
