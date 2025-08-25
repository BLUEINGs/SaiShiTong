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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebSocket服务
 */
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    //存放会话对象
    private static Map<String, Session> sessionMap = new HashMap();

    @Autowired
    private BasketballGameMapper basketballGameMapper;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        System.out.println("客户端：" + sid + "建立连接");
        sessionMap.put(sid, session);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        System.out.println("收到来自客户端：" + sid + "的信息:" + message);
    }

    /**
     * 连接关闭调用的方法
     *
     * @param sid
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("连接断开:" + sid);
        sessionMap.remove(sid);
    }

    /**
     * 群发
     *
     * @param
     */
    public void sendToAllClient() {
        Collection<Session> sessions = sessionMap.values();
        List<BasketballGame> allTeamsScores = basketballGameMapper.queryAllTeamsScore();
        for (BasketballGame allTeamsScore : allTeamsScores) {
            Integer teId = allTeamsScore.getTeId();
            List<BasketballGame> ScoreRecords = basketballGameMapper.queryScoreRecords(teId);
            allTeamsScore.getBasketballGames().addAll(ScoreRecords);
        }
        for (Session session : sessions) {
            try {
                //服务器向客户端发送消息
                session.getBasicRemote().sendText(allTeamsScores.toString());
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
