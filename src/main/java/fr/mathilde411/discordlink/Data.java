package fr.mathilde411.discordlink;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Data {

    private static class Struct {
        @SerializedName("webhook_id")
        public Long webhookId = null;
        @SerializedName("webhook_token")
        public String webhookToken = null;
    }

    private static final Map<String, Field> fields = new HashMap<>();

    static {
        for (Field field : Struct.class.getFields()) {
            field.setAccessible(true);

            String name;
            SerializedName serializedName = field.getAnnotation(SerializedName.class);
            if (serializedName != null)
                name = serializedName.value();
            else
                name = field.getName();

            fields.put(name, field);
        }
    }

    private final Path file;
    private final Gson gson = new Gson();
    private Struct data = new Struct();

    public Data(Path dataFolder) throws IOException {
        Files.createDirectories(dataFolder);
        this.file = dataFolder.resolve("data.json");
        load();
    }

    private void load() throws IOException {
        if (Files.exists(file)) {
            Type mapType = new TypeToken<Struct>() {}.getType();
            data = gson.fromJson(new JsonReader(Files.newBufferedReader(file)), mapType);
        }
    }

    private void save() throws IOException {
        Files.write(file, gson.toJson(data).getBytes());
    }

    public Object get(String key, Object def) {
        if (!fields.containsKey(key))
            throw new IllegalArgumentException("Key " + key + " is invalid.");

        try {
            Object d = fields.get(key).get(data);
            return d == null ? def : d;
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Invalid access.");
        }
    }

    public Object get(String key) {
        return get(key, null);
    }

    public void set(String key, Object val) {
        if (!fields.containsKey(key))
            throw new IllegalArgumentException("Key " + key + " is invalid.");

        try {
            fields.get(key).set(data, val);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Invalid access.");
        }

        try {
            save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
