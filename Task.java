package application;

public class Task {

    private String name;
    private int rewardMinutes;
    private boolean completed;

    public Task(String name, int rewardMinutes) {

        this.name = name;
        this.rewardMinutes = rewardMinutes;
        this.completed = false;
    }

    public String getName() {
        return name;
    }

    public int getRewardMinutes() {
        return rewardMinutes;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void toggleCompleted() {
        completed = !completed;
    }

    @Override
    public String toString() {

        return name + "  (+" + rewardMinutes + " min)";
    }
}