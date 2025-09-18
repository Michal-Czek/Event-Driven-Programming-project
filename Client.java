git initpackage com.example.cs4076_project2;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class Client extends Application {
    private static final int PORT = 1234;
    public static Socket link;
    private static BufferedReader in;
    private static PrintWriter out;
    private static GridPane timetableGrid;
    private static Button addButton;
    private static Button deleteButton;
    private static Button viewButton;
    private static Button optionButton;
    private static Button requestButton;
    private static Button quitButton;
    private static TextField moduleField;
    private static TextField roomField;
    private static TextField timeField;
    private static TextField dayField;

    public static void main(String[] args) {
        connectToServer();
        launch(args);
    }

    private static void connectToServer() {
        try {
            link = new Socket(InetAddress.getLocalHost(), 1234);
            in = new BufferedReader(new InputStreamReader(link.getInputStream()));
            out = new PrintWriter(link.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Could not connect " + e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public synchronized void start(Stage stage) {
        stage.setTitle("Lecture Scheduler");

        timetableGrid = new GridPane();
        timetableGrid.setHgap(10);
        timetableGrid.setVgap(10);

        addHeader();

        addButton = new Button("Add Lecture");
        deleteButton = new Button("Delete Lecture");
        viewButton = new Button("View Schedule");
        optionButton = new Button("Options");
        requestButton = new Button("Requests");
        quitButton = new Button("Quit");

        moduleField = new TextField();
        moduleField.setPromptText("Module Name");
        roomField = new TextField();
        roomField.setPromptText("Room");
        timeField = new TextField();
        timeField.setPromptText("Time Slot");
        dayField = new TextField();
        dayField.setPromptText("Day (e.g., Monday)");

        addButton.setOnAction(e -> addLectureGUI());
        deleteButton.setOnAction(e -> deleteLectureGUI());
        viewButton.setOnAction(e -> viewScheduleGUI());
        optionButton.setOnAction(e -> handleOptionError());
        requestButton.setOnAction(e -> RequestGUI());
        quitButton.setOnAction(e -> System.exit(0));

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(moduleField, roomField, timeField, dayField, addButton, deleteButton, viewButton, optionButton, requestButton, quitButton);

        Scene scene = new Scene(vbox, 400, 500);
        stage.setScene(scene);
        stage.show();
    }

    private synchronized void addHeader() {
        String[] headers = {"Time", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        for (int i = 0; i < headers.length; i++) {
            Label label = new Label(headers[i]);
            timetableGrid.add(label, i, 0);
        }

        String[] times = {"9:00", "10:00", "11:00", "12:00", "1:00", "2:00", "3:00", "4:00", "5:00"};
        for (int i = 0; i < times.length; i++) {
            Label timeLabel = new Label(times[i]);
            timetableGrid.add(timeLabel, 0, i + 1);
        }
    }

    private synchronized void addLectureGUI() {
        String day = dayField.getText().toLowerCase();
        String time = timeField.getText();
        String room = roomField.getText();
        String module = moduleField.getText();

        if (day.isEmpty() || time.isEmpty() || room.isEmpty() || module.isEmpty()) {
            showAlert("All fields must be filled!");
            return;
        }

        String command = "A/" + day + "/" + time + "/" + room + "/" + module;
        out.println(command);

        try {
            String response = in.readLine();
            if (response != null && response.equals("Lecture Added Successfully!")) {
                showAlert("Lecture Added Successfully!");
            } else {
                showAlert("Failed to Add Lecture!");
            }
        } catch (IOException e) {
            showAlert("Error communicating with the server!");
        }

        dayField.clear();
        timeField.clear();
        roomField.clear();
        moduleField.clear();
    }

    private synchronized void deleteLectureGUI() {
        String day = dayField.getText().toLowerCase();
        String time = timeField.getText();

        if (day.isEmpty() || time.isEmpty()) {
            showAlert("Day and Time must be filled!");
            return;
        }

        String command = "D/" + day + "/" + time + "/";
        out.println(command);

        try {
            String response = in.readLine();
            if (response != null && response.equals("Lecture Deleted Successfully!")) {
                showAlert("Lecture Deleted Successfully!");
            } else {
                showAlert("Failed to Delete Lecture!");
            }
        } catch (IOException e) {
            showAlert("Error communicating with the server!");
        }

        dayField.clear();
        timeField.clear();
    }

    private Stage timetableStage = null;

    private synchronized void viewScheduleGUI() {
        if (timetableStage != null && timetableStage.isShowing()) {
            timetableStage.toFront();
            return;
        }

        timetableGrid.getChildren().clear();
        timetableGrid.setHgap(0);
        timetableGrid.setVgap(0);
        timetableGrid.setPadding(new Insets(10));
        timetableGrid.setAlignment(Pos.CENTER);

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        String[] times = {"9:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-13:00", "13:00-14:00", "14:00-15:00", "15:00-16:00", "16:00-17:00", "17:00-18:00"};

        for (int i = 0; i < days.length; i++) {
            Label label = new Label(days[i]);
            label.setStyle("-fx-font-weight: bold; -fx-alignment: center; -fx-background-color: lightgray; -fx-border-color: black; -fx-padding: 10px;");
            timetableGrid.add(label, i + 1, 0);
        }

        for (int i = 0; i < times.length; i++) {
            Label timeLabel = new Label(times[i]);
            timeLabel.setStyle("-fx-font-weight: bold; -fx-alignment: center; -fx-background-color: lightgray; -fx-border-color: black; -fx-padding: 10px;");
            timetableGrid.add(timeLabel, 0, i + 1);
        }

        String command = "V";
        out.println(command);

        try {
            String[][] parsedSchedule = new String[5][9];
            String line;
            while (!(line = in.readLine()).equals("END")) {
                String[] parts = line.split("/");

                if (parts.length == 4) {
                    String day = parts[0];
                    int time = Integer.parseInt(parts[1]);
                    String room = parts[2];
                    String module = parts[3];

                    int dayIndex = Arrays.asList(days).indexOf(day);
                    if (dayIndex != -1 && time >= 0 && time < 9) {
                        parsedSchedule[dayIndex][time] = module + ", " + room;
                    }
                }
            }

            for (int i = 0; i < days.length; i++) {
                for (int j = 0; j < 9; j++) {
                    String lecture = parsedSchedule[i][j];
                    if (lecture != null) {
                        String[] details = lecture.split(", ");
                        String moduleAndRoom = details[0] + ", " + details[1];
                        Label lectureLabel = new Label(moduleAndRoom);
                        lectureLabel.setStyle("-fx-background-color: lightblue; -fx-border-color: black; -fx-alignment: center; -fx-padding: 10px;");
                        timetableGrid.add(lectureLabel, i + 1, j + 1);
                    } else {
                        Label emptyLabel = new Label();
                        emptyLabel.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-alignment: center; -fx-padding: 10px;");
                        timetableGrid.add(emptyLabel, i + 1, j + 1);
                    }
                }
            }
        } catch (IOException e) {
            showAlert("Error communicating with the server!");
        }

        if (timetableStage == null) {
            timetableStage = new Stage();
            timetableStage.setTitle("Weekly Schedule");
            Scene scene = new Scene(timetableGrid);
            timetableStage.setScene(scene);
        }

        timetableStage.show();
    }

    public synchronized void handleOptionError() {
        String command = "O";
        out.println(command);

        try {
            showAlert(in.readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void RequestGUI() {
        String day = dayField.getText().toLowerCase();

        if (day.isEmpty()) {
            showAlert("Day cannot be empty!");
            return;
        }

        out.println("R/" + day);

        try {
            String response = in.readLine();
            if (response != null) {
                showAlert(response);
            } else {
                showAlert("No response from server.");
            }
        } catch (IOException e) {
            showAlert("Error communicating with the server!");
        }
        dayField.clear();
    }



    private static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
