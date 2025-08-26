package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.mapper.BasketballGameMapper;
import com.blueing.sports_meet_system.pojo.BasketballRecords;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;

/**
 * WebSocket服务
 */
@Slf4j
@Component
@ServerEndpoint("/ws/{spId}")
public class WebSocketServer {

    //存放会话对象
    private static Map<Integer,List<Session>> sessions = new HashMap();

    @Autowired
    private BasketballGameMapper basketballGameMapper;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("spId") String spId) {
        log.info("用户建立spId为：{}的ws链接",spId);
        List<Session> sessionList = sessions.getOrDefault(Integer.parseInt(spId), new ArrayList<>());
        sessionList.add(session);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("spId") String spId) {
        log.info("用户发送消息:{}。无需理会",message);
    }

    /**
     * 连接关闭调用的方法
     *
     * @param spId
     */
    @OnClose
    public void onClose(Session session,@PathParam("spId") String spId) {
        log.info("用户断开该spId的链接：{}",spId);
        sessions.get(Integer.parseInt(spId)).remove(session);
    }

    /**
     * 群发
     *
     * @param
     */
    public void sendToAllClient(Integer spId, Integer teId) {
        try {
            List<Session> sessionList = sessions.get(spId);
            for (Session session : sessionList) {
                List<BasketballRecords> records = basketballGameMapper.queryScoreRecords(spId);
                for (BasketballRecords record : records) {
                    record.getSpId()
                }
                session.getBasicRemote().sendText(records.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendToAllClient(String json) {
        Collection<Session> sessions = WebSocketServer.sessions.values();

        for (Session session : sessions) {
            try {
                //服务器向客户端发送消息
                session.getBasicRemote().sendText(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
