package com.example.service;

import com.example.entity.RealTimeData;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.util.List;

public class HttpService {
    private DatabaseService databaseService;
    private Gson gson;
    private static final Logger logger = LoggerFactory.getLogger(HttpService.class);

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
                logger.info("get /api/GB32960_RT/:vin OK");
                return data;
            } else {
                res.status(404);
                logger.error("get /api/GB32960_RT/:vin fail");
                return "{\"error\":\"未找到车辆实时数据\"}";
            }
        });

        // 获取所有实时数据
        Spark.get("/api/GB32960_RT", (req, res) -> {
            try {
                List<RealTimeData> dataList = databaseService.getAllRealTimeData();
                JsonObject result = new JsonObject();
                result.addProperty("count", dataList.size());

//                JsonObject dataObject = new JsonObject();
                JsonArray dataArray= new JsonArray();
                for (RealTimeData data : dataList) {
//                    dataObject.addProperty(data.getVin(), data.getJsonData());
                    dataArray.add(data.getJsonData());
                }
//                result.add("data", dataObject);
                result.add("data", dataArray);

                res.type("application/json");
                logger.info("get /api/GB32960_RT OK");
                return gson.toJson(result);
            } catch (Exception e) {
                res.status(500);
                logger.error("get /api/GB32960_RT fail");
                return "{\"error\":\"获取数据失败\"}";
            }
        });

        System.out.println("HTTP服务已启动，端口: " + port);
    }

    public void stop() {
        Spark.stop();
    }
}
