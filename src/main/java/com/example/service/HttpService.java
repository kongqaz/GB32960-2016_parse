package com.example.service;

import com.example.entity.RealTimeData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;
import spark.Spark;

import java.util.List;

public class HttpService {
    private DatabaseService databaseService;
    private Gson gson;

    public HttpService(Config config) {
        this.databaseService = DatabaseService.getInstance(config);
        this.gson = new Gson();
    }

    public void start(int port) {
        Spark.port(port);

        // 获取指定VIN的实时数据
        Spark.get("/api/GB32960_RT/:vin", (req, res) -> {
            String vin = req.params(":vin");
            String data = databaseService.getRealTimeData(vin);

            if (data != null) {
                res.type("application/json");
                return data;
            } else {
                res.status(404);
                return "{\"error\":\"未找到车辆实时数据\"}";
            }
        });

        // 获取所有实时数据
        Spark.get("/api/GB32960_RT", (req, res) -> {
            try {
                List<RealTimeData> dataList = databaseService.getAllRealTimeData();
                JsonObject result = new JsonObject();
                result.addProperty("count", dataList.size());

                JsonObject dataObject = new JsonObject();
                for (RealTimeData data : dataList) {
                    dataObject.addProperty(data.getVin(), data.getJsonData());
                }
                result.add("data", dataObject);

                res.type("application/json");
                return gson.toJson(result);
            } catch (Exception e) {
                res.status(500);
                return "{\"error\":\"获取数据失败\"}";
            }
        });

        System.out.println("HTTP服务已启动，端口: " + port);
    }

    public void stop() {
        Spark.stop();
    }
}
