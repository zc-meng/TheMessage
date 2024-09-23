package com.fengsheng.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    /**
     * 读文本文件
     *
     * @param fileName - 文件路径
     * @return 每一行一个String构成的List
     */
    public static List<String> readLines(String fileName, Charset charset) throws IOException {
        List<String> list = new ArrayList<>();
        String line;
        try (BufferedReader is = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), charset))) {
            while ((line = is.readLine()) != null) {
                list.add(line);
            }
        }
        return list;
    }

    /**
     * 写文本文件
     *
     * @param strList     - 待写入的内容，List的每一个元素为一行
     * @param fileName    - 文件名
     * @param charsetName - 写入的编码格式
     */
    public static void writeLines(List<String> strList, String fileName, Charset charset) throws IOException {
        try (BufferedWriter os = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), charset))) {
            for (String str : strList) {
                os.write(str);
                os.newLine();
            }
        }
    }
}
