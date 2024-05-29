package com.zhangz.springbootssedemo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping(path = "sse")
public class SseRest {
    private static Map<String, SseEmitter> sseCache = new ConcurrentHashMap<>();

    @GetMapping(path = "")
    public String index() {
        return "index.html";
    }
    @GetMapping(path = "subscribe")
    @ResponseBody
    public SseEmitter push(String id) throws IOException {
        // 超时时间设置为3s，用于演示客户端自动重连
        SseEmitter sseEmitter = new SseEmitter(36_000L);
        // 设置前端的重试时间为1s
        sseEmitter.send(SseEmitter.event().reconnectTime(1000).data(id + " 连接成功"));
        sseCache.put(id, sseEmitter);
        System.out.println("add " + id);
        sseEmitter.onTimeout(() -> {
            System.out.println(id + " 超时");
            sseCache.remove(id);
        });
        sseEmitter.onCompletion(() -> System.out.println(id + " 完成！！！"));
        return sseEmitter;
    }

    @GetMapping(path = "push")
    @ResponseBody
    public String push(String id, String content) throws IOException {
        SseEmitter sseEmitter = sseCache.get(id);
        if (sseEmitter != null) {
            sseEmitter.send(content);
        }
        return id + " push";
    }

    @GetMapping(path = "over")
    @ResponseBody
    public String over(String id) {
        SseEmitter sseEmitter = sseCache.get(id);
        if (sseEmitter != null) {
            sseEmitter.complete();
            sseCache.remove(id);
        }
        return id + " over";
    }
}
