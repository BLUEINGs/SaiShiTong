package com.blueing.sports_meet_system.service.ws;

import com.blueing.sports_meet_system.entity.GameEvent;
import com.blueing.sports_meet_system.mapper.BasketballGameMapper;
import com.blueing.sports_meet_system.pojo.Basketball;
import com.blueing.sports_meet_system.utils.SpringContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebSocket服务
 */
@Slf4j
@Component
@ServerEndpoint("/gameLogs/{spId}")
public class LogsStreamServer {

    // @Autowired
    private BasketballGameMapper basketballGameMapper;

    private ObjectMapper objectMapper;

    // 存放会话对象
    private static Map<Integer, List<Session>> sessions = new HashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("spId") String spId) {
        if (basketballGameMapper == null) {
            basketballGameMapper = SpringContextHolder.getBean(BasketballGameMapper.class);
        }
        if (objectMapper == null) {
            objectMapper = SpringContextHolder.getBean(ObjectMapper.class);
        }
        log.info("用户建立spId为：{}的日志传输链接", spId);
        List<Session> sessionList = sessions.getOrDefault(Integer.parseInt(spId), new ArrayList<>());
        ;
        // session.getBasicRemote().sendText(basketballGameMapper.queryScoreRecords(Integer.parseInt(spId)).toString());
        sessionList.add(session);
        sessions.put( Integer.parseInt(spId),sessionList);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("spId") String spId) {
        log.info("用户发送消息:{}。无需理会", message);
    }

    /**
     * 连接关闭调用的方法
     *
     * @param spId
     */
    @OnClose
    public void onClose(Session session, @PathParam("spId") String spId) {
        // TODO 这里onClose应该不会给到session对象，还得用Map维护
        log.info("用户断开该spId的链接：{}", spId);
        if (session != null) {
            sessions.get(Integer.parseInt(spId)).remove(session);
        }
    }

    public void sendToAllClient(Integer spId, List<GameEvent> logs) {
        if (basketballGameMapper == null) {
            basketballGameMapper = SpringContextHolder.getBean(BasketballGameMapper.class);
        }
        if (objectMapper == null) {
            objectMapper = SpringContextHolder.getBean(ObjectMapper.class);
        }

        List<Session> sessionList = sessions.get(spId);
        if (sessionList == null) {
            return;
        }
        for (Session session : sessionList) {
            if(logs.isEmpty()){
                return;
            }
            try {
                session.getBasicRemote().sendText(objectMapper.writeValueAsString(logs));
            } catch (IOException e) {
                log.info("json解析失败");
            }
        }
    }
}

