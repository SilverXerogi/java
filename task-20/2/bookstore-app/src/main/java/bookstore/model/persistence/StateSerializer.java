package bookstore.model.persistence; // или model.persistence, в зависимости от вашего пакета


import bookstore.model.BookRequest;
import bookstore.model.Inventory;
import bookstore.model.Order;
import bookstore.model.factory.DefaultBookStoreFactory;


import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Map;

public class StateSerializer {

    private static final String STATE_FILE = "app_state.ser";
    private static final String TEMP_STATE_FILE = "app_state.ser.tmp";

    public static void saveState(Inventory inventory, Map<Integer, Order> orders, Map<Integer, BookRequest> requests, DefaultBookStoreFactory factory) {
        AppState state = new AppState(inventory, orders, requests, factory);
        Path tempPath = Paths.get(TEMP_STATE_FILE);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempPath.toFile()))) {
            oos.writeObject(state);
            oos.flush(); // Убедиться, что все данные записаны в файл
            oos.close(); // Явно закрываем поток

            Path statePath = Paths.get(STATE_FILE);
            // Пытаемся удалить целевой файл, если он существует (может помочь с правами/блокировками)
            try {
                Files.deleteIfExists(statePath);
            } catch (Exception e) {
                System.err.println("Предупреждение: Не удалось удалить старый файл состояния перед заменой: " + e.getMessage());
            }

            // Атомарная замена файла
            try {
                Files.move(tempPath, statePath);
                System.out.println("Состояние приложения сохранено в " + STATE_FILE);
            } catch (IOException moveEx) {
                System.err.println("Ошибка при переименовании временного файла: " + moveEx.getMessage());
                // Если move не удался, удалим временный файл, чтобы не накапливать мусор
                try {
                    Files.deleteIfExists(tempPath);
                } catch (IOException deleteEx) {
                    System.err.println("Ошибка при удалении временного файла после неудачного move: " + deleteEx.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при сериализации состояния: " + e.getMessage());
            e.printStackTrace();
            // Убедимся, что временный файл удаляется даже в случае исключения в блоке try
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException deleteEx) {
                System.err.println("Ошибка при удалении временного файла после исключения: " + deleteEx.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static AppState loadState() {
        Path statePath = Paths.get(STATE_FILE);
        if (!Files.exists(statePath)) {
            System.out.println("Файл состояния " + STATE_FILE + " не найден. Загрузка с нуля.");
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(statePath.toFile()))) {
            AppState state = (AppState) ois.readObject();
            System.out.println("Состояние приложения загружено из " + STATE_FILE);
            return state;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка при десериализации состояния: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}