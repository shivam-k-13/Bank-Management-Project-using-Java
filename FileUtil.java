import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    public static <T> void writeToFile(String filename, List<T> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> readFromFile(String filename) {
        List<T> data = new ArrayList<>();
        File file = new File(filename);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                data = (List<T>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                // Ignore, will return empty list
            }
        }
        return data;
    }
}