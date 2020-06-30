import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GlobalMouseListener implements NativeMouseInputListener {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    List<Event> list;

    public GlobalMouseListener(List<Event> list) {
        this.list = list;
    }

    public void nativeMouseClicked(NativeMouseEvent e) {
        LOG.info("Mouse Clicked: " + e.getClickCount());
    }

    public void nativeMousePressed(NativeMouseEvent e) {
        list.add(new Event(e.getPoint()));
        LOG.info("Mouse Pressed={}, point={}" , e.getButton(), e.getPoint());
    }

    public void nativeMouseReleased(NativeMouseEvent e) {
//        System.out.println("Mouse Released: " + e.getButton());
    }

    public void nativeMouseMoved(NativeMouseEvent e) {
//        System.out.println("Mouse Moved: " + e.getX() + ", " + e.getY());
    }

    public void nativeMouseDragged(NativeMouseEvent e) {
//        System.out.println("Mouse Dragged: " + e.getX() + ", " + e.getY());
    }

}