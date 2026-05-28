package application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.time.LocalTime;
import java.util.Optional;

public class TimerController {

    @FXML
    private Label timerLabel;

    @FXML
    private Label currentTimeLabel;

    @FXML
    private Label breakBankLabel;

    @FXML
    private Label sessionLabel;

    @FXML
    private Label modeLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Spinner<Integer> studySpinner;

    @FXML
    private Spinner<Integer> breakSpinner;

    @FXML
    private ListView<Task> taskList;

    @FXML
    private TextField taskField;

    @FXML
    private TextField rewardField;

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
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1, 300, 25
                )
        );

        breakSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1, 120, 5
                )
        );

        taskList.setItems(
                FXCollections.observableArrayList()
        );

        // SHOW TASKS AS CHECKBOXES

        taskList.setCellFactory(param -> new ListCell<Task>() {

            @Override
            protected void updateItem(Task task, boolean empty) {

                super.updateItem(task, empty);

                if (empty || task == null) {

                    setGraphic(null);
                    setText(null);

                } else {

                    CheckBox checkBox =
                            new CheckBox(task.getName());

                    checkBox.setSelected(
                            task.isCompleted()
                    );

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

        if (timer != null) {
            timer.stop();
        }

        int minutes;

        if (isBreak) {

            minutes = breakSpinner.getValue();

        } else {

            minutes = studySpinner.getValue();
        }

        totalSeconds = minutes * 60;

        remainingSeconds = totalSeconds;

        progressBar.setProgress(1);

        updateTimerLabel();

        timer = new Timeline(

                new KeyFrame(Duration.seconds(1), e -> {

                    remainingSeconds--;

                    updateTimerLabel();

                    progressBar.setProgress(
                            (double) remainingSeconds
                                    / totalSeconds
                    );

                    if (remainingSeconds <= 0) {

                        timer.stop();

                        if (!isBreak) {

                            sessionCount++;

                            updateSessionLabel();

                            askBreakDecision();

                        } else {

                            isBreak = false;

                            updateModeLabel();

                            startTimer();
                        }
                    }
                })
        );

        timer.setCycleCount(
                Timeline.INDEFINITE
        );

        timer.play();
    }

    private void askBreakDecision() {

        Alert alert =
                new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Break Time");

        alert.setHeaderText(
                "Session Complete 🌸"
        );

        alert.setContentText(
                "Do you want to take a break?"
        );

        ButtonType yes =
                new ButtonType("YES");

        ButtonType no =
                new ButtonType("NO");

        alert.getButtonTypes().setAll(
                yes,
                no
        );

        Optional<ButtonType> result =
                alert.showAndWait();

        if (result.isPresent()
                && result.get() == yes) {

            isBreak = true;

            updateModeLabel();

            startTimer();

        } else {

            breakBank +=
                    breakSpinner.getValue();

            updateBreakBank();

            isBreak = false;

            updateModeLabel();

            startTimer();
        }
    }

    @FXML
    private void pauseTimer() {

        if (timer != null) {

            timer.pause();
        }
    }

    @FXML
    private void resetTimer() {

        if (timer != null) {

            timer.stop();
        }

        remainingSeconds = 0;

        timerLabel.setText("00:00");

        progressBar.setProgress(0);
    }

    @FXML
    private void addTask() {

        String name =
                taskField.getText();

        if (name == null
                || name.trim().isEmpty()) {

            return;
        }

        int reward = 0;

        try {

            reward = Integer.parseInt(
                    rewardField.getText()
            );

        } catch (Exception e) {

            reward = 0;
        }

        Task task =
                new Task(name, reward);

        taskList.getItems().add(task);

        taskField.clear();

        rewardField.clear();
    }

    @FXML
    private void deleteTask() {

        Task selected =
                taskList.getSelectionModel()
                        .getSelectedItem();

        if (selected != null) {

            taskList.getItems()
                    .remove(selected);
        }
    }

    @FXML
    private void completeTask() {

        Task selected =
                taskList.getSelectionModel()
                        .getSelectedItem();

        if (selected != null
                && !selected.isCompleted()) {

            selected.toggleCompleted();

            breakBank +=
                    selected.getRewardMinutes();

            updateBreakBank();

            taskList.refresh();
        }
    }

    private void updateTimerLabel() {

        int m =
                remainingSeconds / 60;

        int s =
                remainingSeconds % 60;

        timerLabel.setText(

                String.format(
                        "%02d:%02d",
                        m,
                        s
                )
        );
    }

    private void updateBreakBank() {

        breakBankLabel.setText(

                "Break Bank: "
                        + breakBank
                        + " min"
        );
    }

    private void updateSessionLabel() {

        sessionLabel.setText(

                "Session #"
                        + sessionCount
        );
    }

    private void updateModeLabel() {

        if (isBreak) {

            modeLabel.setText(
                    "BREAK MODE"
            );

        } else {

            modeLabel.setText(
                    "FOCUS MODE"
            );
        }
    }

    private void startClock() {

        clock = new Timeline(

                new KeyFrame(
                        Duration.seconds(1),
                        e -> {

                            currentTimeLabel.setText(

                                    LocalTime.now()
                                            .withNano(0)
                                            .toString()
                            );
                        }
                )
        );

        clock.setCycleCount(
                Timeline.INDEFINITE
        );

        clock.play();
    }
}