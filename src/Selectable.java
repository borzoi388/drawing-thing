import java.util.List;

public abstract class Selectable {
    public int selectedIndex;
    public void select(int num) {
        selectedIndex = num;
    }
    public int getSelectedIndex() {
        return selectedIndex;
    }
    public abstract List<String> getSelectables();
}