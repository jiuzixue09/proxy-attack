import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Event {
    private Point point;
    private List<Integer> keyCodes = new ArrayList<>();

    public Event(Point point) {
        this.point = point;
    }

    public Event(List<Integer> keyCodes) {
        this.keyCodes = keyCodes;
    }

    public Event(Integer keyCode) {
        setKeyCode(keyCode);
    }

    public Event() {
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public List<Integer> getKeyCodes() {
        return keyCodes;
    }

    public void setKeyCodes(List<Integer> keyCodes) {
        this.keyCodes = keyCodes;
    }

    public void setKeyCode(Integer keyCode) {
        keyCodes = new ArrayList<>();
        keyCodes.add(keyCode);
    }

    @Override
    public String toString() {
        return "Event{" +
                "point=" + point +
                ", keyCodes=" + keyCodes +
                '}';
    }
}
