package com.forjrking.gsonplugin;

import java.util.ArrayList;

/**
 * @Description: com.ke.gsonplugin
 * @Author: 岛主
 * @CreateDate: 2019/11/5 0005 下午 2:19
 * @Version: 1.0.0
 */
public class StringUtil {


    /**
     * 字符串截取
     *
     * @param in  原始数据
     * @param max 最大文字截取尺寸
     */
    private static String subString(String in, int max) {
        float j = 0;  // 半角数目
        int offset = 0;

        if (max == -1) max = Integer.MAX_VALUE;

        if (in != null && max > 0) {
            for (offset = 0; offset < in.codePointCount(0, in.length()); offset++) {
                int c = in.codePointAt(offset);

                if (j > max) {
                    break;
                } else {
                    if (c > 32 && c <= 127) {
                        j += 0.5;
                    } else if (c == 10 || c == 13) {
                        break;
                    } else {
                        j += 1;
                    }
                }
            }
        }

        if (in != null) {
            offset = offset > in.length() ? in.length() : offset;
            return in.substring(0, offset);
        } else {
            return "";
        }
    }

    /**
     * 字符串截取
     *
     * @param in           原始数据
     * @param maxLine      做多行数
     * @param eachLineSize 每行字数
     */
    public static String[] subString(String in, int maxLine, int eachLineSize) {
        String inputString = in;
        ArrayList<String> arrayList = new ArrayList<>();

        for (int i = 0; i < maxLine; i++) {
            String tmp = subString(inputString, eachLineSize);

            if (i == maxLine - 1 && i > 0) {
                tmp += ".";
            }

            arrayList.add(tmp);

            inputString = inputString.substring(Math.min(inputString.length(), tmp.length()),
                    inputString.length());
            if (inputString.startsWith("\r\n")) {
                inputString = inputString.substring(2, inputString.length());
            } else if (inputString.startsWith("\r")) {
                inputString = inputString.substring(1, inputString.length());
            } else if (inputString.startsWith("\n")) {
                inputString = inputString.substring(1, inputString.length());
            }

            if (inputString.length() == 0) {
                break;
            }
        }

        String[] ret = new String[arrayList.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = arrayList.get(i);
        }

        return ret;
    }

}
