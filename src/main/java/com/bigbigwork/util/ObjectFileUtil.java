package com.bigbigwork.util;

import java.io.*;

public class ObjectFileUtil {

    public static Object read(File file) throws Exception {
        if (file.exists() && file.canRead()) {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file))) {
                return objectInputStream.readObject();
            }
        }
        return null;
    }

    public static void write(File file, Object object) throws IOException {
        if (!file.exists()) file.createNewFile();

        if (file.exists() && file.canWrite()) {
            try (ObjectOutputStream objectInputStream = new ObjectOutputStream(new FileOutputStream(file))) {
                objectInputStream.writeObject(object);
            }
        }
    }
}
