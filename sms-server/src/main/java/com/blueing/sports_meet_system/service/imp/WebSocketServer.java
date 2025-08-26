package com.blueing.sports_meet_system.service.imp;

import com.blueing.sports_meet_system.mapper.BasketballGameMapper;
import com.blueing.sports_meet_system.pojo.BasketballGame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.*;

/**
 * WebSocket服务
 */
@Component
@ServerEndpoint("/ws/{spid}")
public class WebSocketServer {

    //存放会话对象
    private static Map<String, Session> sessionMap = new HashMap();

    @Autowired
    private BasketballGameMapper basketballGameMapper;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("spid") String spid) {
        System.out.println("客户端：" + spid + "建立连接");
        sessionMap.put(spid, session);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("spid") String spid) {
        System.out.println("收到来自客户端：" + spid + "的信息:" + message);
    }

    /**
     * 连接关闭调用的方法
     *
     * @param spid
     */
    @OnClose
    public void onClose(@PathParam("spid") String spid) {
        System.out.println("连接断开:" + spid);
        sessionMap.remove(spid);
    }

    /**
     * 群发
     *
     * @param
     */
    public void sendToAllClient(Integer spid,Integer teId) {
        Set<String> strings = sessionMap.keySet();
       for (String string : strings) {
           try {
               //服务器向客户端发送消息
               if(Integer.parseInt(string) == spid)
               sessionMap.get(string).getBasicRemote().sendText(basketballGameMapper.queryScoreRecords(teId).toString());
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
    }

    public void sendToAllClient(String json) {
        Collection<Session> sessions = sessionMap.values();

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
