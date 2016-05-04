package ir.amv.snippets.sortable.json;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AMV on 5/1/2016.
 */
public class GsonUtil {

    private static Gson gson;

    public static Gson gson() {
        if (gson == null) {
            gson = new Gson();
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
}
