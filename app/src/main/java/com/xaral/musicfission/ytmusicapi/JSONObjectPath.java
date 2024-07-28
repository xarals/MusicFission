package com.xaral.musicfission.ytmusicapi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class JSONObjectPath {
    private final JSONObject jsonObject;
    public JSONObjectPath(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public Object get(Object[] path) {
        try {
            Object obj = jsonObject;
            for (Object name : path) {
                if (name instanceof Integer)
                    obj = ((JSONArray) obj).get((int) name);
                else
                    obj = ((JSONObject) obj).get(name.toString());
            }
            return obj;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject getJSONObject(Object[] path) {
        try {
            Object obj = jsonObject;
            for (Object name : path) {
                if (name instanceof Integer)
                    obj = ((JSONArray) obj).get((int) name);
                else
                    obj = ((JSONObject) obj).get(name.toString());
            }
            return (JSONObject) obj;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONArray getJSONArray(Object[] path) {
        try {
            Object obj = jsonObject;
            for (Object name : path) {
                if (name instanceof Integer)
                    obj = ((JSONArray) obj).get((int) name);
                else
                    obj = ((JSONObject) obj).get(name.toString());
            }
            return (JSONArray) obj;
        } catch (Exception e) {
            return null;
        }
    }

    public String getString(Object[] path) {
        try {
            Object obj = jsonObject;
            for (Object name : path) {
                if (name instanceof Integer)
                    obj = ((JSONArray) obj).get((int) name);
                else
                    obj = ((JSONObject) obj).get(name.toString());
            }
            return obj.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public Integer getInt(Object[] path) {
        try {
            Object obj = jsonObject;
            for (Object name : path) {
                if (name instanceof Integer)
                    obj = ((JSONArray) obj).get((int) name);
                else
                    obj = ((JSONObject) obj).get(name.toString());
            }
            return (Integer) obj;
        } catch (Exception e) {
            return null;
        }
    }
}
