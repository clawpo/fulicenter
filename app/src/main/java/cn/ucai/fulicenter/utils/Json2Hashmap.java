package cn.ucai.fulicenter.utils;


import com.ta.utdid2.android.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Author: Maximilian Voss (info@maexchen.net)
 * This Class is a utility to transform Map to a JSONObject and vise versa.
 */
public class Json2Hashmap {

    /**
     * This method creates a JSONObject from a Map
     * @param map representing the JSONObject
     * @return a JSONObject based on the Map
     * @throws JSONException if JSON generation failed
     */
    public static JSONObject map2json(Map<String, Object> map) throws JSONException {
        List<String> keyList = new ArrayList<String>();

        for (String key : map.keySet()) {
            keyList.add(key);
        }
        Collections.sort(keyList);

        JSONObject obj = new JSONObject();
        inflateJsonObject(map, keyList, obj);

        return obj;
    }

    /**
     * This method creates a Map from a JSONObject
     * @param jsonObject the JSONObject to convert into Map
     * @return converted Map
     * @throws JSONException if an error occurs parsing the JSONObject
     */
    public static Map<String, Object> json2map(JSONObject jsonObject) throws JSONException {
        return json2map("", jsonObject);
    }


    /**
     * This method creates a Map from a JSONObject
     * @param prefix a string which is prefixed the object paths
     * @param jsonObject the JSONObject to convert into Map
     * @return converted Map
     * @throws JSONException if an error occurs parsing the JSONObject
     */
    public static Map<String, Object> json2map(String prefix, JSONObject jsonObject) throws JSONException {
        return flatJsonObject(prefix, jsonObject);
    }


    /**
     * This method creates a Map from a JSONObject
     * @param jsonString a valid JSON String which is converted to JSONObject
     * @return converted Map
     * @throws JSONException if an error occurs parsing the JSON String
     */
    public static Map<String, Object> json2map(String jsonString) throws JSONException {
        return json2map("", jsonString);
    }


    /**
     * This method creates a Map from a JSONObject
     * @param prefix a string which is prefixed the object paths
     * @param jsonString a valid JSON String which is converted to JSONObject
     * @return converted Map
     * @throws JSONException if an error occurs parsing the JSON String
     */
    public static Map<String, Object> json2map(String prefix, String jsonString) throws JSONException {
        if (jsonString == null || StringUtils.isEmpty(jsonString)) {
            return new HashMap<String, Object>();
        }
        JSONObject obj = new JSONObject(jsonString);
        return flatJsonObject(prefix, obj);
    }


    private static Map<String, Object> flatJsonObject(String parentPath, JSONObject obj) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> iter = obj.keys();
        while (iter.hasNext()) {
            String key = iter.next();

            String keyPath;
            if (parentPath == null || StringUtils.isEmpty(parentPath)) {
                keyPath = key;
            } else {
                keyPath = parentPath + "." + key;
            }

            if (obj.isNull(key)) {
                map.put(keyPath, null);
            } else {
                Object child = obj.get(key);
                map.putAll(getJsonValue(keyPath, child));
            }
        }

        return map;
    }

    private static Map<String, Object> getJsonValue(String path, Object obj) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        if (obj instanceof String) {
            map.put(path, obj);
        } else if (obj instanceof Double) {
            map.put(path, obj);
        } else if (obj instanceof Boolean) {
            map.put(path, obj);
        } else if (obj instanceof Integer) {
            map.put(path, obj);
        } else if (obj instanceof Long) {
            map.put(path, obj);
        } else if (obj instanceof JSONObject) {
            map.putAll(flatJsonObject(path, (JSONObject) obj));
        } else if (obj instanceof JSONArray) {
            JSONArray childArr = (JSONArray) obj;
            if (childArr.length() > 0) {
                for (int i = 0; i < childArr.length(); i++) {
                    map.putAll(getJsonValue(path + "[" + i + "]", childArr.get(i)));
                }
            } else {
                map.put(path + "[0]", null);
            }
        } else {
            throw new JSONException("Type: " + obj.getClass() + " is not supported!");
        }

        return map;
    }

    private static void inflateJsonObject(Map<String, Object> map, List<String> keys, JSONObject obj)
            throws JSONException {

        for (String key : keys) {
            storeJsonValue(obj, key, map.get(key));
        }
    }

    private static void storeJsonValue(JSONObject obj, String key, Object value) throws JSONException {
        if (key.matches("^\\w+\\[.+")) {

            String arrayName = key.substring(0, key.indexOf("["));
            String subKey = key.substring(key.indexOf("]") + 1);
            String indexStr = key.substring(key.indexOf("[") + 1, key.indexOf("]"));
            int index = Integer.parseInt(indexStr);

            JSONArray arrayObj;
            if (!obj.has(arrayName)) {
                arrayObj = new JSONArray();
                obj.put(arrayName, arrayObj);
            } else {
                arrayObj = obj.getJSONArray(arrayName);
            }

            storeJsonValue(arrayObj, index, subKey, value);
        } else if (key.matches("^\\w+\\.\\w+.+")) {
            String childName = key.substring(0, key.indexOf("."));
            String subKey = key.substring(key.indexOf(".") + 1);

            JSONObject childObj;
            if (!obj.has(childName)) {
                childObj = new JSONObject();
                obj.put(childName, childObj);
            } else {
                childObj = obj.getJSONObject(childName);
            }

            storeJsonValue(childObj, subKey, value);
        } else {
            if (value == null) {
                obj.put(key, JSONObject.NULL);
            } else {
                obj.put(key, value);
            }
        }

    }

    private static void storeJsonValue(JSONArray obj, int index, String key, Object value) throws JSONException {
        if (key.matches("^\\w+\\.\\w+.+")) {
            String childName = key.substring(0, key.indexOf("."));
            String subKey = key.substring(key.indexOf(".") + 1);

            JSONObject childObj;

            if (obj.length() > index) {
                if (!((JSONObject) obj.get(index)).has(childName)) {
                    childObj = new JSONObject();
                    ((JSONObject) obj.get(index)).put(childName, childObj);
                } else {
                    childObj = ((JSONObject) obj.get(index)).getJSONObject(childName);
                }
            } else {
                childObj = new JSONObject();
                obj.put(childObj);
            }

            storeJsonValue(childObj, subKey, value);
        } else if (key.matches("^\\.\\w+.+")) {
            String subKey = key.substring(1);

            JSONObject childObj;

            if (obj.length() <= index) {
                createChildObjects(obj, index);
            }
            childObj = obj.getJSONObject(index);
            storeJsonValue(childObj, subKey, value);
        } else {
            if (value != null) {
                obj.put(value);
            }
        }

    }

    private static void createChildObjects(JSONArray obj, int count) {
        JSONObject childObj;

        int currentCount = obj.length();
        if (currentCount > count) {
            return;
        }
        for (int i = currentCount; i < count + 1; i++) {
            childObj = new JSONObject();
            obj.put(childObj);
        }
    }
}
