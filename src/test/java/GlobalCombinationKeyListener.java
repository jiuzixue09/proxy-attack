import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GlobalCombinationKeyListener implements NativeKeyListener {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    List<Integer> keys = new ArrayList<>();
    List<Event> list;

    public GlobalCombinationKeyListener(List<Event> list) {
        this.list = list;
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
            try {
                LOG.info(list.toString());
                GlobalScreen.unregisterNativeHook();
            } catch (NativeHookException nativeHookException) {
                nativeHookException.printStackTrace();
            }
        } else if (e.getKeyCode() == NativeKeyEvent.VC_CONTROL) {
            keys.add(e.getKeyCode());
        }else if(!keys.isEmpty()){
            keys.add(e.getKeyCode());
        }else{
            list.add(new Event(e.getKeyCode()));
            LOG.info("Key Pressed: " + e.getKeyCode() + "\t" + NativeKeyEvent.getKeyText(e.getKeyCode()));
        }

    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_CONTROL) {
            list.add(new Event(new ArrayList<>(keys)));
            keys.clear();
        }else{
            System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        }

    }

}