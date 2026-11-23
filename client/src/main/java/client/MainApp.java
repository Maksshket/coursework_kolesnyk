package client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.nio.file.Path;
import java.util.logging.Logger;

public class MainApp extends Application {

    private static final Logger LOG = Logger.getLogger(MainApp.class.getName());

    private final HttpHotelClient api = new HttpHotelClient();
    private final TableView<Room> rooms = new TableView<>();
    private final TableView<Specialist> specialists = new TableView<>();
    private final TableView<Request> requests = new TableView<>();
    private final TableView<ExecutionRecord> records = new TableView<>();

    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane(
                new Tab("Номери", roomPane()),
                new Tab("Фахівці", specialistPane()),
                new Tab("Заявки", requestPane()),
                new Tab("Записи виконання", recordPane())
        );
        tabs.getTabs().forEach(t -> t.setClosable(false));
        tabs.setTabMinWidth(235);
        Scene scene = new Scene(tabs, 1050, 650);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Технічні заявки готельного комплексу");
        stage.show();
        refreshAll();
    }
    private Pane roomPane() {
        rooms.getColumns().setAll(col("ID", "id"), col("Номер", "roomNumber"), col("Поверх", "floor"),
                col("Категорія", "roomType"), col("Місць", "capacity"), col("Обладнання", "equipment"));
        TextField number = new TextField(), floor = new TextField(), type = new TextField(),
                capacity = new TextField(), equipment = new TextField();
        Button add = new Button("Додати"), update = new Button("Змінити"), delete = new Button("Видалити"),
                refresh = new Button("Оновити"), importCsv = new Button("Імпортувати CSV");
        ProgressBar progress = new ProgressBar(0);
        progress.setMaxWidth(Double.MAX_VALUE);
        Label importStatus = new Label("CSV не імпортовано");
        add.setOnAction(e -> safe(() -> {
            api.addRoom(readRoom(null, number, floor, type, capacity, equipment));
            refreshRooms();
        }));
        update.setOnAction(e -> safe(() -> {
            Room r = selected(rooms);
            api.updateRoom(readRoom(r.getId(), number, floor, type, capacity, equipment));
            refreshRooms();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteRoom(selected(rooms).getId());
            refreshAll();
        }));
        refresh.setOnAction(e -> refreshRooms());
        importCsv.setOnAction(e -> startImport(importCsv, progress, importStatus));
        rooms.getSelectionModel().selectedItemProperty().addListener((o, a, r) -> {
            if (r != null) {
                number.setText(r.getRoomNumber());
                floor.setText("" + r.getFloor());
                type.setText(r.getRoomType());
                capacity.setText("" + r.getCapacity());
                equipment.setText(r.getEquipment());
            }
        });
        return layout("Номери", rooms, "Операції з номером", new VBox(8, new Label("Номер кімнати"), number, new Label("Поверх"), floor,
                new Label("Категорія"), type, new Label("Місткість"), capacity, new Label("Обладнання"), equipment,
                buttons(add, update, delete, importCsv, refresh),
                new Label("Фоновий імпорт CSV"), progress, importStatus));
    }
    private void startImport(Button importCsv, ProgressBar progress, Label status) {
        ImportRoomsTask task = new ImportRoomsTask(Path.of("sample-data", "rooms.csv"), api);
        progress.progressProperty().bind(task.progressProperty());
        status.textProperty().bind(task.messageProperty());
        importCsv.setDisable(true);
        task.setOnSucceeded(ev -> {
            progress.progressProperty().unbind();
            status.textProperty().unbind();
            status.setText("Готово. Імпортовано: " + task.getValue() + ", пропущено: " + task.getSkipped()
                    + ", дублікатів: " + task.getDuplicates());
            importCsv.setDisable(false);
            refreshRooms();
        });
        task.setOnFailed(ev -> {
            progress.progressProperty().unbind();
            status.textProperty().unbind();
            progress.setProgress(0);
            Throwable error = task.getException();
            LOG.warning("Імпорт зупинено: " + (error == null ? "" : error.getMessage()));
            status.setText("Імпорт зупинено через помилку");
            importCsv.setDisable(false);
            refreshRooms();
            showError(error);
        });
        Thread worker = new Thread(task, "csv-import");
        worker.setDaemon(true);
        worker.start();
    }
    private Room readRoom(Long id, TextField number, TextField floor, TextField type, TextField capacity,
                          TextField equipment) {
        return new Room(id, number.getText(), Integer.parseInt(floor.getText()), type.getText(),
                Integer.parseInt(capacity.getText()), equipment.getText());
    }
    private Pane specialistPane() {
        specialists.getColumns().setAll(col("ID", "id"), col("ПІБ", "fullName"),
                col("Спеціалізація", "specialty"), col("Телефон", "phone"));
        TextField name = new TextField(), specialty = new TextField(), phone = new TextField();
        Button add = new Button("Додати"), update = new Button("Змінити"), delete = new Button("Видалити"),
                refresh = new Button("Оновити");
        add.setOnAction(e -> safe(() -> {
            api.addSpecialist(new Specialist(null, name.getText(), specialty.getText(), phone.getText()));
            refreshSpecialists();
        }));
        update.setOnAction(e -> safe(() -> {
            Specialist s = selected(specialists);
            s.setFullName(name.getText());
            s.setSpecialty(specialty.getText());
            s.setPhone(phone.getText());
            api.updateSpecialist(s);
            refreshSpecialists();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteSpecialist(selected(specialists).getId());
            refreshAll();
        }));
        refresh.setOnAction(e -> refreshSpecialists());
        specialists.getSelectionModel().selectedItemProperty().addListener((o, a, s) -> {
            if (s != null) {
                name.setText(s.getFullName());
                specialty.setText(s.getSpecialty());
                phone.setText(s.getPhone());
            }
        });
        return layout("Фахівці", specialists, "Операції з фахівцем", new VBox(8, new Label("ПІБ"), name, new Label("Спеціалізація"), specialty,
                new Label("Телефон"), phone, buttons(add, update, delete, refresh)));
    }
    private Pane requestPane() {
        requests.getColumns().setAll(col("ID", "id"), col("Номер", "roomId"), col("Фахівець", "specialistId"),
                col("Опис", "description"), col("Стан", "state"), col("F1", "f1"), col("F2", "f2"),
                col("F3", "f3"), col("I", "priorityIndex"));
        TextField roomId = new TextField(), specialistId = new TextField(), description = new TextField(),
                f1 = new TextField(), f2 = new TextField(), f3 = new TextField();
        ComboBox<RequestState> state = new ComboBox<>(FXCollections.observableArrayList(RequestState.values()));
        state.setMaxWidth(Double.MAX_VALUE);
        Button add = new Button("Створити"), update = new Button("Змінити"), change = new Button("Змінити стан"),
                delete = new Button("Видалити"), refresh = new Button("Оновити");
        add.setOnAction(e -> safe(() -> {
            api.addRequest(readRequest(null, roomId, specialistId, description, f1, f2, f3));
            refreshRequests();
        }));
        update.setOnAction(e -> safe(() -> {
            Request q = selected(requests);
            api.updateRequest(readRequest(q.getId(), roomId, specialistId, description, f1, f2, f3));
            refreshRequests();
        }));
        change.setOnAction(e -> safe(() -> {
            api.changeRequestState(selected(requests).getId(), state.getValue());
            refreshRequests();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteRequest(selected(requests).getId());
            refreshAll();
        }));
        refresh.setOnAction(e -> refreshRequests());
        requests.getSelectionModel().selectedItemProperty().addListener((o, a, q) -> {
            if (q != null) {
                roomId.setText("" + q.getRoomId());
                specialistId.setText(q.getSpecialistId() == null ? "" : "" + q.getSpecialistId());
                description.setText(q.getDescription());
                f1.setText("" + q.getF1());
                f2.setText("" + q.getF2());
                f3.setText("" + q.getF3());
            }
        });
        return layout("Заявки", requests, "Операції із заявкою", new VBox(6, new Label("ID номера"), roomId,
                new Label("ID фахівця (необов’язково)"), specialistId, new Label("Опис несправності"), description,
                new Label("F1 – вплив на гостя"), f1, new Label("F2 – тяжкість несправності"), f2,
                new Label("F3 – вплив на доступність номера"), f3,
                new Label("Новий стан"), state, buttons(add, change, update, delete, refresh)));
    }
    private Request readRequest(Long id, TextField roomId, TextField specialistId, TextField description,
                                TextField f1, TextField f2, TextField f3) {
        Long specialist = specialistId.getText().isBlank() ? null : Long.valueOf(specialistId.getText());
        return new Request(id, Long.parseLong(roomId.getText()), specialist, description.getText(),
                RequestState.NEW, Double.parseDouble(f1.getText()), Double.parseDouble(f2.getText()),
                Double.parseDouble(f3.getText()), 0);
    }

    private Pane recordPane() {
        records.getColumns().setAll(col("ID", "id"), col("Заявка", "requestId"), col("Фахівець", "specialistId"),
                col("Дата", "workDate"), col("Годин", "hoursSpent"), col("Опис", "description"));
        TextField requestId = new TextField(), specialistId = new TextField(), date = new TextField(),
                hours = new TextField(), description = new TextField();
        Button add = new Button("Додати"), update = new Button("Змінити"), delete = new Button("Видалити"),
                refresh = new Button("Оновити");
        add.setOnAction(e -> safe(() -> {
            api.addRecord(readRecord(null, requestId, specialistId, date, hours, description));
            refreshRecords();
        }));
        update.setOnAction(e -> safe(() -> {
            api.updateRecord(readRecord(selected(records).getId(), requestId, specialistId, date, hours, description));
            refreshRecords();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteRecord(selected(records).getId());
            refreshRecords();
        }));
        refresh.setOnAction(e -> refreshRecords());
        records.getSelectionModel().selectedItemProperty().addListener((o, a, r) -> {
            if (r != null) {
                requestId.setText("" + r.getRequestId());
                specialistId.setText("" + r.getSpecialistId());
                date.setText(r.getWorkDate());
                hours.setText("" + r.getHoursSpent());
                description.setText(r.getDescription());
            }
        });
        return layout("Записи виконання", records, "Операції із записом виконання", new VBox(8, new Label("ID заявки"), requestId, new Label("ID фахівця"), specialistId,
                new Label("Дата виконання"), date, new Label("Витрачено годин"), hours,
                new Label("Опис виконаних дій"), description, buttons(add, update, delete, refresh)));
    }
    private ExecutionRecord readRecord(Long id, TextField requestId, TextField specialistId, TextField date,
                                       TextField hours, TextField description) {
        return new ExecutionRecord(id, Long.parseLong(requestId.getText()), Long.parseLong(specialistId.getText()),
                date.getText(), Double.parseDouble(hours.getText()), description.getText());
    }

    private void refreshAll() {
        refreshRooms();
        refreshSpecialists();
        refreshRequests();
        refreshRecords();
    }
    private void refreshRooms() {
        safe(() -> rooms.setItems(FXCollections.observableArrayList(api.getRooms())));
    }

    private void refreshSpecialists() {
        safe(() -> specialists.setItems(FXCollections.observableArrayList(api.getSpecialists())));
    }

    private void refreshRequests() {
        safe(() -> requests.setItems(FXCollections.observableArrayList(api.getRequests())));
    }

    private void refreshRecords() {
        safe(() -> records.setItems(FXCollections.observableArrayList(api.getRecords())));
    }

    private <T> T selected(TableView<T> table) {
        T v = table.getSelectionModel().getSelectedItem();
        if (v == null) {
            throw new IllegalArgumentException("Виберіть запис");
        }
        return v;
    }
    private void safe(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            LOG.warning(e.getMessage());
            showError(e);
        }
    }

    private void showError(Throwable e) {
        Alert d = new Alert(Alert.AlertType.ERROR, e == null ? "Помилка" : e.getMessage(), ButtonType.OK);
        d.showAndWait();
    }
    private Pane layout(String listTitle, TableView<?> table, String formTitle, Pane form) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Label left = new Label(listTitle);
        left.getStyleClass().add("section-title");
        VBox leftBox = new VBox(10, left, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        Label right = new Label(formTitle);
        right.getStyleClass().add("section-title-small");
        form.getStyleClass().add("form");
        VBox rightBox = new VBox(12, right, form);
        rightBox.setPrefWidth(330);
        rightBox.setMinWidth(330);

        HBox root = new HBox(15, leftBox, new Separator(Orientation.VERTICAL), rightBox);
        HBox.setHgrow(leftBox, Priority.ALWAYS);
        root.setPadding(new Insets(12, 15, 15, 15));
        return root;
    }
    /** Кнопки сіткою по дві в ряд: перша - головна (синя), остання непарна займає всю ширину. */
    private GridPane buttons(Button... items) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        ColumnConstraints half = new ColumnConstraints();
        half.setPercentWidth(50);
        grid.getColumnConstraints().addAll(half, half);
        items[0].getStyleClass().add("primary");
        for (int i = 0; i < items.length; i++) {
            items[i].setMaxWidth(Double.MAX_VALUE);
            boolean lastOdd = (i == items.length - 1) && (items.length % 2 == 1);
            if (lastOdd) {
                grid.add(items[i], 0, i / 2, 2, 1);
            } else {
                grid.add(items[i], i % 2, i / 2);
            }
        }
        return grid;
    }

    private <S, T> TableColumn<S, T> col(String title, String property) {
        TableColumn<S, T> c = new TableColumn<>(title);
        c.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>(property));
        return c;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
