package com.bigbigwork.util;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class KeySprite {

    public static int mouseClick(int startX, int startY) throws AWTException {
        Robot robot = new Robot();
        robot.mouseMove(startX, startY);
        robot.delay(100);
        robot.mousePress(KeyEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);

        return 1;
    }

    public static void paste(String content) throws AWTException {
        StringSelection s = new StringSelection(content);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s, null);

        Robot robot = new Robot();

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    public static void enter() throws AWTException {
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_ENTER);
    }

    public static void setSystemClipboard(String content){
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(content);
        clipboard.setContents(selection, null);
    }

    public static String getSystemClipboard() throws IOException, UnsupportedFlavorException {
        String content = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        return content;
    }

}