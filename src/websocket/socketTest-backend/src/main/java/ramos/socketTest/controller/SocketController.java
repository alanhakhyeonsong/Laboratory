package ramos.socketTest.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import ramos.socketTest.dto.MessageRequest;

@Controller
public class SocketController {

    @MessageMapping("/receive")
    @SendTo("/send")
    public MessageRequest socketHandler(@RequestParam MessageRequest request) {
        String userName = request.getUserName();
        String message = request.getMessage();

        return new MessageRequest(userName, message);
    }
}
