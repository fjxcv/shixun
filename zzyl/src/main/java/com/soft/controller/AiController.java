package com.soft.controller;



import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

@RestController
public class AiController {

    @Autowired
    private ChatModel chatModel;

    //定义接口实现大模型简单聊天
    @RequestMapping("/chat01")
    public String chat01System(@RequestParam(name="msg") String msg){
        //聊天结果一次性响应
        String content = chatModel.call(msg);
        return content;
    }
    //通过流式响应
    @RequestMapping(value = "/chat02",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
    public  Flux<String> chat02System(@RequestParam(name="msg") String msg){;

        //聊天结果一次性响应
        Flux<String> flux = chatModel.stream(msg);

        System.out.println(flux);
        return flux;
    }

}
