package com.xaral.musicfission.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvertorService {
    public static Map<String, Object> stringToMap(String string) {
        Map<String, Object> answer = new HashMap<>();
        if (!string.startsWith("{")) return answer;
        string = string.substring(1, string.length() - 1);
        List<String> list = new ArrayList<>();
        int symbolStart = 0;
        int symbolEnd = 0;
        boolean open2 = false;
        boolean open = false;
        int index = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == ',' && symbolStart == symbolEnd && !open && !open2) {
                list.add(string.substring(index, i));
                index = i + 2;
            }
            else if (string.charAt(i) == '\\') {
                i++;
            }
            else if ((string.charAt(i) == '[' || string.charAt(i) == '{') && !open && !open2) symbolStart++;
            else if ((string.charAt(i) == ']' || string.charAt(i) == '}') && !open && !open2) symbolEnd++;
            else if (string.charAt(i) == '\'' && !open2) open = !open;
            else if (string.charAt(i) == '\"' && !open) open2 = !open2;
        }
        list.add(string.substring(index));
        for (String str : list) {
            for (int j = 0; j < str.length(); j++) {
                if (str.charAt(j) == ':') {
                    String str2 = str.substring(j + 2);
                    Object obj = str2;
                    if (str2.charAt(0) == '[') obj = stringToList(str2);
                    else if (str2.charAt(0) == '{') obj = stringToMap(str2);
                    else if (str2.charAt(0) == '\'' || str2.charAt(0) == '\"') obj = str2.substring(1, str2.length() - 1).replace("\\", "");
                    if (str.charAt(0) == ' ')
                        answer.put(str.substring(2, j - 1), obj);
                    else
                        answer.put(str.substring(1, j - 1), obj);
                    break;
                }
            }
        }
        return answer;
    }

    public static List<Object> stringToList(String string) {
        List<Object> answer = new ArrayList<>();
        if (!string.startsWith("[")) return answer;
        string = string.substring(1, string.length() - 1);
        int symbolStart = 0;
        int symbolEnd = 0;
        boolean open2 = false;
        boolean open = false;
        int index = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == ',' && symbolStart == symbolEnd && !open && !open2) {
                answer.add(string.substring(index, i));
                index = i + 2;
            }
            else if (string.charAt(i) == '\\') {
                i++;
            }
            else if ((string.charAt(i) == '[' || string.charAt(i) == '{') && !open && !open2) symbolStart++;
            else if ((string.charAt(i) == ']' || string.charAt(i) == '}') && !open && !open2) symbolEnd++;
            else if (string.charAt(i) == '\'' && !open2) open = !open;
            else if (string.charAt(i) == '\"' && !open) open2 = !open2;
        }
        answer.add(string.substring(index));
        for (int i = 0; i < answer.size(); i++) {
            if (answer.get(i).toString().startsWith("'") || answer.get(i).toString().startsWith("\"")) answer.set(i, answer.get(i).toString().substring(1, answer.get(i).toString().length() - 1).replace("\\", ""));
            else if (answer.get(i).toString().startsWith("[")) answer.set(i, stringToList(answer.get(i).toString()));
            else if (answer.get(i).toString().startsWith("{")) answer.set(i, stringToMap(answer.get(i).toString()));
        }
        return answer;
    }

    public static String mapToString(Map<String, ?> map) {
        String answer = "{";
        for (String key : map.keySet()) {
            answer += "'" + key + "': ";
            if (map.get(key) instanceof  List)
                answer += listToString((List<?>) map.get(key)) + ", ";
            else if (map.get(key) instanceof Map)
                answer += mapToString((Map<String, ?>) map.get(key)) + ", ";
            else if (map.get(key) instanceof String)
                answer += "'" + ((String) map.get(key)).replace("\'", "\\\'").replace("\"", "\\\"") + "', ";
        }
        if (answer.length() > 2)
            answer = answer.substring(0, answer.length() - 2);
        answer += "}";
        return answer;
    }

    public static String listToString(List<?> list) {
        String answer = "[";
        for (Object element : list) {
            if (element instanceof Map)
                answer += mapToString((Map<String, ?>) element) + ", ";
            else if (element instanceof List)
                answer += listToString((List<?>) element) + ", ";
            else if (element instanceof String && !((String) element).isEmpty())
                answer += "'" + ((String) element).replace("\'", "\\\'").replace("\"", "\\\"") + "', ";
        }
        if (answer.length() > 1)
            answer = answer.substring(0, answer.length() - 2);
        answer += "]";
        return answer;
    }
}
