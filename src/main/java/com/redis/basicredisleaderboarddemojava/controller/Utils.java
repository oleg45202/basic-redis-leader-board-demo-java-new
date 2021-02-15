package com.redis.basicredisleaderboarddemojava.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {
    public static String resetData(boolean isDataReady, Jedis jedis, String data_ready_redis_key, String redis_leaderboard) {
        boolean is_ok = true;
        if (!isDataReady){
            try {
                JSONArray companyJsonArray = new JSONArray(readFile("src/main/resources/data.json"));
                JSONObject companyJson;
                String symbol;
                for (int i=0; i<companyJsonArray.length(); i++) {
                    companyJson = new JSONObject(companyJsonArray.getString(i));
                    symbol = companyJson.get("symbol").toString().toLowerCase();
                    jedis.zadd(redis_leaderboard, Double.parseDouble(companyJson.get("marketCap").toString()), symbol);
                    jedis.hset(symbol, "company", companyJson.get("company").toString());
                    jedis.hset(symbol, "country", companyJson.get("country").toString());
                }
                jedis.set(data_ready_redis_key, "true");
            } catch (Exception e) {
                is_ok = false;
            }
        }
        return String.format("{\"succes\":%s}", is_ok);
    }


    private static String readFile(String filename) {
        String result = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            result = sb.toString();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    protected static String getRedisDataZrangeWithScores(int start, int end, Jedis jedis, String redis_leaderboard) {
        List<JSONObject> topList = new ArrayList<>();
        AtomicInteger index = new AtomicInteger(0);
        jedis.zrangeWithScores(redis_leaderboard, start, end).forEach((k) -> {
            JSONObject json = new JSONObject();
            Map<String, String> company = jedis.hgetAll(k.getElement());
            try {
                json.put("marketCap", ((Double) k.getScore()).longValue());
                json.put("symbol", k.getElement());
                json.put("rank", index.incrementAndGet());
                json.put("country", company.get("country"));
                json.put("company", company.get("company"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            topList.add(json);
        });
        return topList.toString();
    }

    protected static String getRedisDataZrevrangeWithScores(int start, int end, Jedis jedis, String redis_leaderboard) {
        List<JSONObject> topList = new ArrayList<>();
        AtomicInteger index = new AtomicInteger(0);
        jedis.zrevrangeWithScores(redis_leaderboard, start, end).forEach((k) -> {
            JSONObject json = new JSONObject();
            Map<String, String> company = jedis.hgetAll(k.getElement());
            try {
                json.put("marketCap", ((Double) k.getScore()).longValue());
                json.put("symbol", k.getElement());
                json.put("rank", index.incrementAndGet());
                json.put("country", company.get("country"));
                json.put("company", company.get("company"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            topList.add(json);
        });
        return topList.toString();
    }


}
