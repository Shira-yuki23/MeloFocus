package application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalTime;
import java.util.Optional;

public class TimerController {

    @FXML private Label timerLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Label breakBankLabel;
    @FXML private Label sessionLabel;
    @FXML private Label modeLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Spinner<Integer> studySpinner;
    @FXML private Spinner<Integer> breakSpinner;
    @FXML private ListView<Task> taskList;
    @FXML private TextField taskField;
    @FXML private TextField rewardField;

    private Timeline timer;
    private Timeline clock;
    private int totalSeconds;
    private int remainingSeconds;
    private boolean isBreak = false;
    private int breakBank = 0;
    private int sessionCount = 1;

    @FXML
    public void initialize() {

        studySpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 300, 25)
        );
        breakSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 5)
        );

        taskList.setItems(FXCollections.observableArrayList());

        taskList.setCellFactory(param -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    CheckBox checkBox = new CheckBox(task.toString());
                    checkBox.setSelected(task.isCompleted());
                    checkBox.setOnAction(e -> {
                        if (!task.isCompleted()) {
                            task.setCompleted(true);
                            breakBank += task.getRewardMinutes();
                            updateBreakBank();
                        } else {
                            task.setCompleted(false);
                        }
                        checkBox.setSelected(task.isCompleted());
                    });
                    setGraphic(checkBox);
                    setText(null);
                }
            }
        });

        updateBreakBank();
        updateSessionLabel();
        updateModeLabel();
        startClock();
    }

    @FXML
    private void startTimer() {
        if (timer != null) timer.stop();

        int minutes = isBreak
                ? breakSpinner.getValue()
                : studySpinner.getValue();

        totalSeconds = minutes * 60;
        remainingSeconds = totalSeconds;
        progressBar.setProgress(1);
        updateTimerLabel();

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            updateTimerLabel();
            progressBar.setProgress(
                (double) remainingSeconds / totalSeconds
            );

            if (remainingSeconds <= 0) {
                timer.stop();
                if (!isBreak) {
                    sessionCount++;
                    updateSessionLabel();
                    Platform.runLater(this::askBreakDecision);
                } else {
                    isBreak = false;
                    updateModeLabel();
                    startTimer();
                }
            }
        }));

        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void askBreakDecision() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Break Time");
        alert.setHeaderText("Session Complete 🌸");
        alert.setContentText("Do you want to take a break?");

        ButtonType yes = new ButtonType("YES");
        ButtonType no  = new ButtonType("NO");
        alert.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == yes) {
            isBreak = true;
            updateModeLabel();
            startTimer();
        } else {
            breakBank += breakSpinner.getValue();
            updateBreakBank();
            isBreak = false;
            updateModeLabel();
            startTimer();
        }
    }

    @FXML
    private void redeemBreak() {
        Stage redeemStage = new Stage();
        redeemStage.setTitle("Redeem Break");
        redeemStage.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label title = new Label("Redeem Break Time");
        title.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: bold;"
        );

        Label bankInfo = new Label("Break Bank: " + breakBank + " min");
        bankInfo.setStyle("-fx-font-size: 14px;");

        Label prompt = new Label("How many minutes do you want?");

        Spinner<Integer> minuteSpinner = new Spinner<>(1, 300, 5);
        minuteSpinner.setEditable(true);
        minuteSpinner.setPrefWidth(120);

        Label statusLabel = new Label("");

        Button confirmBtn = new Button("Start Break");
        confirmBtn.setOnAction(e -> {
            int requested = minuteSpinner.getValue();
            breakBank -= requested;
            updateBreakBank();

            if (breakBank < 0) {
                statusLabel.setText(
                    "⚠ You're in debt by "
                    + Math.abs(breakBank) + " min!"
                );
                statusLabel.setStyle(
                    "-fx-text-fill: red; -fx-font-size: 13px;"
                );
            }

            isBreak = true;
            updateModeLabel();
            breakSpinner.getValueFactory().setValue(requested);
            startTimer();
            redeemStage.close();
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> redeemStage.close());

        HBox buttons = new HBox(10, confirmBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER);

        root.getChildren().addAll(
            title, bankInfo, prompt,
            minuteSpinner, statusLabel, buttons
        );

        Scene scene = new Scene(root, 300, 260);
        redeemStage.setScene(scene);
        redeemStage.show();
    }

    @FXML
    private void pauseTimer() {
        if (timer != null) timer.pause();
    }

    @FXML
    private void resetTimer() {
        if (timer != null) timer.stop();
        remainingSeconds = 0;
        timerLabel.setText("00:00");
        progressBar.setProgress(0);
    }

    @FXML
    private void addTask() {
        String name = taskField.getText();
        if (name == null || name.trim().isEmpty()) return;

        int reward = 0;
        try {
            reward = Integer.parseInt(rewardField.getText());
        } catch (Exception e) {
            reward = 0;
        }

        taskList.getItems().add(new Task(name, reward));
        taskField.clear();
        rewardField.clear();
    }

    @FXML
    private void deleteTask() {
        Task selected = taskList.getSelectionModel().getSelectedItem();
        if (selected != null) taskList.getItems().remove(selected);
    }

    @FXML
    private void completeTask() {
        Task selected = taskList.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.isCompleted()) {
            selected.toggleCompleted();
            breakBank += selected.getRewardMinutes();
            updateBreakBank();
            taskList.refresh();
        }
    }

    private void updateTimerLabel() {
        int m = remainingSeconds / 60;
        int s = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", m, s));
    }

    private void updateBreakBank() {
        if (breakBank < 0) {
            breakBankLabel.setText(
                "Break Bank: " + breakBank + " min ⚠ (in debt)"
            );
            breakBankLabel.setStyle("-fx-text-fill: red;");
        } else {
            breakBankLabel.setText(
                "Break Bank: " + breakBank + " min"
            );
            breakBankLabel.setStyle("-fx-text-fill: black;");
        }
    }

    private void updateSessionLabel() {
        sessionLabel.setText("Session #" + sessionCount);
    }

    private void updateModeLabel() {
        modeLabel.setText(isBreak ? "BREAK MODE" : "FOCUS MODE");
    }

    private void startClock() {
        clock = new Timeline(
            new KeyFrame(Duration.seconds(1), e ->
                currentTimeLabel.setText(
                    LocalTime.now().withNano(0).toString()
                )
            )
        );
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }
}