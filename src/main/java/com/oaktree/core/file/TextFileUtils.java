package com.oaktree.core.file;

import java.io.RandomAccessFile;

/**
 * Created by ij on 22/07/15.
 */
public class TextFileUtils {


    public String getFirstLine(RandomAccessFile file) {

    }

    public String getLastLine(RandomAccessFile file) {

    }



    public static void main(String[] args) {
        RandomAccessFile file = new RandomAccessFile(filePath, "r");
        27
        file.seek(position);
        28
        byte[] bytes = new byte[size];
        29
        file.read(bytes);
        30
        file.close();

    }
}
