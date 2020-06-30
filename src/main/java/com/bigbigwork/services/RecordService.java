package com.bigbigwork.services;

import com.bigbigwork.util.KeyCodes;
import com.bigbigwork.vo.Command;
import javafx.scene.control.TableView;
import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;
import lc.kra.system.mouse.GlobalMouseHook;
import lc.kra.system.mouse.event.GlobalMouseAdapter;
import lc.kra.system.mouse.event.GlobalMouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RecordService implements Runnable {
    private boolean run = true;
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final TableView<Command> tableView;
    List<GlobalKeyEvent> keys = new ArrayList<>();
    AtomicInteger id = new AtomicInteger(1);

    public RecordService(TableView<Command> tableView) {
        this.tableView = tableView;
    }

    private void addMouseItem(GlobalMouseEvent event){
        Command command = new Command();
        command.setId(id.getAndIncrement());

        command.setType("点击事件");
        command.setOperation(String.format("x:%d,y:%d",event.getX(),event.getY()));

        tableView.getItems().add(command);
    }

    private void addKeyboardItem(GlobalKeyEvent event){
        Command command = new Command();
        command.setId(id.getAndIncrement());

        command.setType("键盘录入");

        command.setOperation(keyString(event));
        command.setKeyCode(event.getVirtualKeyCode() + "");

        tableView.getItems().add(command);
    }

    private void addKeyboardItems(List<GlobalKeyEvent> events){
        Command command = new Command();
        command.setId(id.getAndIncrement());

        command.setType("键盘录入");

        command.setOperation(events.stream().map(this::keyString).collect(Collectors.joining(",")));
        command.setKeyCode(events.stream().map(it -> it.getVirtualKeyCode()+"").collect(Collectors.joining(",")));

        tableView.getItems().add(command);
    }
    
    public String keyString(GlobalKeyEvent event){
        String keyText = KeyCodes.getKeyText(event.getVirtualKeyCode());
        LOG.info("code:{}, text:{}", event.getVirtualKeyCode(), keyText);
        return keyText;
    }

    @Override
    public void run() {
        // Might throw a UnsatisfiedLinkError if the native library fails to load or a RuntimeException if hooking fails
        GlobalMouseHook mouseHook = new GlobalMouseHook(); // Add true to the constructor, to switch to raw input mode
        GlobalKeyboardHook keyboardHook = new GlobalKeyboardHook(true); // Use false here to switch to hook instead of raw input

        LOG.info("Global mouse and keyboard hook successfully started, press [escape] key to shutdown.");

        for (Map.Entry<Long,String> mouse:GlobalMouseHook.listMice().entrySet()) {
            System.out.format("%d: %s\n", mouse.getKey(), mouse.getValue());
        }

        mouseHook.addMouseListener(new GlobalMouseAdapter() {

            @Override
            public void mousePressed(GlobalMouseEvent event)  {
                LOG.info(String.valueOf(event));
                addMouseItem(event);
            }

            @Override
            public void mouseReleased(GlobalMouseEvent event)  {
//                LOG.info(String.valueOf(event));
            }

            @Override
            public void mouseMoved(GlobalMouseEvent event) {
//                LOG.info(String.valueOf(event));
            }

            @Override
            public void mouseWheel(GlobalMouseEvent event) {
//                LOG.info(String.valueOf(event));
            }

        });

        keyboardHook.addKeyListener(new GlobalKeyAdapter() {
            @Override
            public void keyPressed(GlobalKeyEvent event) {
                if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_ESCAPE) {
                    run = false;
                }else if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_CONTROL) {
                    keys.add(event);
                }else if(!keys.isEmpty()){
                    keys.add(event);
                }else{
                    addKeyboardItem(event);
                }

            }

            @Override
            public void keyReleased(GlobalKeyEvent event) {
                super.keyReleased(event);
                if (event.getVirtualKeyCode() == GlobalKeyEvent.VK_CONTROL) {
                    addKeyboardItems(keys);
                    keys.clear();
                }
            }
        });

        try {
            while(run) {
                Thread.sleep(128);
            }
        } catch(InterruptedException e) {
            //Do nothing
        } finally {
            mouseHook.shutdownHook();
            keyboardHook.shutdownHook();
        }
        tableView.refresh();
        LOG.info(tableView.getItems().toString());
    }
}
