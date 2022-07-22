package com.iisigroup.cap.manager.ftp;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 * 
 * </pre>
 * 
 * @since 2022年7月8日
 * @author ?
 * @version
 *          <ul>
 *          <li>2022年7月8日,Tim,弱掃議題,是否先搬進去Cap
 *          </ul>
 */
public class PatternParser {
    /**
     * 
     * @param pattern
     *            %yyyy% 西元年 %yyy% 民國年 %MM% 月 %dd% 日 %hh% 時 %mm% 分 %9(02)% %X(01)%
     * @param fileName
     * @return
     */
    public static boolean isMatch(String pattern, String fileName) {
        String regex = pattern;
        regex = regex.replaceAll("%yyyy%", "([0-9]{4})");
        regex = regex.replaceAll("%yyy%", "([0-9]{3})");
        regex = regex.replaceAll("%MM%", "(0[1-9]|1[012])");
        regex = regex.replaceAll("%dd%", "(0[1-9]|[12][0-9]|3[01])");
        regex = regex.replaceAll("%hh%", "([01][0-9]|2[0-3])");
        regex = regex.replaceAll("%mm%", "([0-5][0-9])");
        regex = regex.replaceAll("\\*", "([A-Z]|[a-z]|[0-9]|_|-|\\s)+");
        // handle cobol copybook pattern
        while (regex.indexOf("%9") >= 0) {
            int begin = regex.indexOf("%9") + 1;
            int end = regex.indexOf("%", begin);
            String tmpPattern = regex.substring(begin, end);
            String tmpRegex = "([0-9]{" + getLength(tmpPattern) + "})";
            regex = regex.substring(0, begin - 1) + tmpRegex + regex.substring(end + 1);
        }
        while (regex.indexOf("%X") >= 0) {
            int begin = regex.indexOf("%X") + 1;
            int end = regex.indexOf("%", begin);
            String tmpPattern = regex.substring(begin, end);
            String tmpRegex = "[\\S]{" + getLength(tmpPattern) + "}";
            regex = regex.substring(0, begin - 1) + tmpRegex + regex.substring(end + 1);
        }
        // regex = "^" + regex;
        // regex += "$";
        Pattern regexPattern = Pattern.compile(Pattern.quote(regex));
        Matcher matcher = regexPattern.matcher(fileName);
        boolean t1 = matcher.find();
        boolean t2 = fileName.matches(Pattern.quote(regex));
        boolean t3 = fileName.matches(regex);
        return matcher.find();
    }

    private static String getLength(String data) {
        // 999. or X. or 99V99.
        // 9(1) or X(1)
        // 9(m)V9(n)
        // 9(m)V999
        // 9(14.3)
        String varLength = "";
        int type_pos_begin = 0;
        int type_pos_end = 0;
        int sign = 0;
        type_pos_begin = data.indexOf('(') + 1;
        type_pos_end = data.indexOf(')');
        if (data.indexOf("S9") < 0) {// 有正負符號，長度加 1
            sign = 0;
        } else {
            sign = 1;
        }
        if (type_pos_begin <= 0) {
            // 999. or X. or 99V99.
            type_pos_end = data.indexOf('.');
            type_pos_begin = data.lastIndexOf(' ') + 1;
            if (data.indexOf('V', type_pos_begin + 1) > 0) {
                varLength = String.valueOf(type_pos_end - type_pos_begin - 1 + sign);
            } else {
                varLength = String.valueOf(type_pos_end - type_pos_begin + sign);
            }
        } else if (data.indexOf('V', type_pos_begin + 1) < 0) {
            varLength = data.substring(type_pos_begin, type_pos_end).trim();
            if (varLength.indexOf('.') < 0) {
                // 9(1) or X(1)
                varLength = String.valueOf(Integer.parseInt(varLength) + sign);
            } else {
                // 9(14.3)
                StringTokenizer tokens = new StringTokenizer(varLength, ".");
                int m = Integer.parseInt(tokens.nextToken());
                int n = Integer.parseInt(tokens.nextToken());
                varLength = String.valueOf(m + n + sign);
            }
        } else if (data.indexOf('(', type_pos_begin + 1) < 0) {
            // 9(m)V999
            int m = Integer.parseInt(data.substring(type_pos_begin, type_pos_end).trim());
            type_pos_end = data.indexOf('.');
            type_pos_begin = data.lastIndexOf('V') + 1;
            int n = type_pos_end - type_pos_begin;
            varLength = String.valueOf(m + n + sign);
        } else {
            // 9(m)V9(n)
            int m = Integer.parseInt(data.substring(type_pos_begin, type_pos_end).trim());
            type_pos_begin = data.lastIndexOf('(') + 1;
            type_pos_end = data.lastIndexOf(')');
            int n = Integer.parseInt(data.substring(type_pos_begin, type_pos_end).trim());
            varLength = String.valueOf(m + n + sign);
        }
        return varLength;
    }
}
