# Spring Boot & Vue.js STOMP WebSocket 채팅 테스트
STOMP를 활용하여 간단한 WebSocket 연결 및 채팅 구현

## Used Stack
- Backend: Spring Boot, Spring MVC, Web Socket(STOMP)
- Frontend: Vue.js, Vue CLI, sockjs-client, stomp-client

## Web Socket?
![](https://velog.velcdn.com/images/songs4805/post/cc5d64ac-9342-49a6-82d8-2a0b03e37668/image.png)

웹 소켓이란 **두 프로그램** 간의 메시지를 교환하기 위한 **통신 방법** 중 하나로, 현재 인터넷 환경(HTML5)에서 많이 사용되는 방식이다.

다음과 같은 특징이 있다.
- 양방향 통신(Full-Duplex)
  - 데이터 송수신을 동시에 처리할 수 있는 통신 방법
  - 클라이언트와 서버가 서로에게 원할 때 데이터를 주고받을 수 있다.
  - **일반적으로 널리 쓰이는 HTTP 통신**은 Client가 요청을 보내는 경우에만 Server가 응답하는 **단방향 통신**
- 실시간 네트워킹 (Real Time-Networking)
  - 웹 환경에서 **연속된 데이터를 빠르게 노출**
  - ex) 채팅, 주식, 비디오 데이터
  - 여러 단말기에 빠르게 데이터를 교환

웹 소켓 이전에는 HTTP에서 Polling 방식(일정 주기 요청 송신), Long Polling, Streaming을 사용했다. 이 방식은 Real-time 통신이라 하기 애매한 정도이거나 Request/Response Header가 불필요하게 크다.

웹 소켓은 핸드 쉐이킹 방식으로 동작한다.  
![](https://velog.velcdn.com/images/songs4805/post/fc40d8c9-b8d9-44a5-9157-b7cea79569c5/image.jpg)
- 최초 접속에서만 http 프로토콜 위에서 handshaking을 하기 때문에 http header를 사용한다.
- 웹소켓을 위한 별도의 포트는 없으며, 기존 포트(http-80, https-443)을 사용
- 프레임으로 구성된 메시지라는 논리적 단위로 송수신
- 메시지에 포함될 수 있는 교환 가능한 메시지는 텍스트와 바이너리

웹 소켓 기능을 제공하는 `Socket.io`, `SockJS`는 다음과 같은 한계가 있다.
- HTML5 이전의 기술로 구현된 서비스에서 **웹 소켓처럼** 사용할 수 있도록 도와주는 기술
- 문자열들을 주고 받을 수 있게 해줄 뿐 그 이상의 일을 하진 않는다.
- 주고 받은 문자열의 해독은 온전히 애플리케이션에 맡긴다.
- HTTP는 형식을 정해두었기 때문에 모두가 약속을 따르기만 하면 해석할 수 있다. 하지만, WebSocket은 형식이 정해져 있지 않기 때문에 애플리케이션에서 쉽게 해석하기 힘들다.
- 때문에 WebSocket 방식은 sub-protocols을 사용해서 주고 받는 메시지의 형태를 약속하는 경우가 많다.

## STOMP(Simple Text Oriented Message Protocol)?
- STOMP는 채팅 통신을 하기 위한 형식을 정의
- HTTP와 유사하게 간단히 정의되어 해석하기 편한 프로토콜
- 일반적으로 웹소켓 위에서 사용
- 프레임 기반의 프로토콜이고, 프레임은 명령, 헤더, 바디로 구성된다.
- 자주 사용되는 명령은 CONNECT, SEND, SUBSCRIBE, DISCONNECT 등이 있다.
- 헤더와 바디는 빈 라인으로 구분되며, 바디의 끝은 NULL 문자로 설정한다.

// Spring 단에서 좀 더 자세한 설명은 다음 영상을 참고하자. → [[10분 테코톡] ✨ 아론의 웹소켓&스프링 - Youtube 우아한 Tech](https://www.youtube.com/watch?v=rvss-_t6gzg)

## 구현 내용
![](https://velog.velcdn.com/images/songs4805/post/a18361fd-67d2-49d2-951b-8357b22e9e55/image.png)
서로 다른 브라우저를 통해 `localhost:3000`에 실행 중인 Vue.js 클라이언트로 접속하여 예시로 메시지를 입력 후 전송하면 위와 같이 나타난다. 현재는 따로 DB 연결 없이 단순 Web Socket만 구현한 결과이기 때문에 새로고침을 눌렀을 경우, 해당 메시지 내역들은 모두 사라진다.  
// DB에 넣을 시 유저 정보, 채팅방, 메시지 정보 등을 엔티티화 한 뒤 처리하는 로직이 필요하다. (이에 대한 예시가 많음) Spring Security 및 Spring Data JPA를 적절하게 사용해보자.

![](https://velog.velcdn.com/images/songs4805/post/f762af9c-834a-49e5-95c1-9525b27454dd/image.png)

## Codes
코드 상의 주요 내용은 다음과 같다.  

Vue.js에서 처리한 내용은 크게 소켓 연결, 메시지 전송, 메시지 수신으로 3가지이다.  
`InputMessage.vue`
```javascript
<template>
  <div id="container">
    userName: <input type="text" v-model="userName" />
    message: <input type="text" v-model="message" @keyup="sendMessage" />
    <div v-for="(item, idx) in receiveList" :key="idx">
      <h3>userName: {{ item.userName }}</h3>
      <h3>message: {{ item.message }}</h3>
    </div>
  </div>
</template>

<script>
import Stomp from 'webstomp-client'
import SockJS from 'sockjs-client'

export default {
  name: "InputMessage",
  data() {
    return {
      userName: '',
      message: '',
      receiveList: [],
    }
  },
  created() {
    this.connect();
  },
  methods: {
    sendMessage(e) {
      if (e.keyCode === 13 && this.userName !== '' && this.message !== '') {
        this.send();
        this.message = '';
      }
    },
    send() {
      console.log("Send message:" + this.message);
      if (this.stompClient && this.stompClient.connected) {
        const msg = {
          userName: this.userName,
          message: this.message
        };
        this.stompClient.send("/receive", JSON.stringify(msg), {});
      }
    },
    connect() {
      const serverURL = "http://localhost:8080"
      let socket = new SockJS(serverURL);
      this.stompClient = Stomp.over(socket);
      console.log(`소켓 연결을 시도합니다. 서버 주소: ${serverURL}`)
      this.stompClient.connect(
          {},
          frame => {
            // 소켓 연결 성공
            this.connected = true;
            console.log('소켓 연결 성공', frame);
            // 서버의 메시지 전송 endpoint를 구독합니다.
            // 이런형태를 pub sub 구조라고 합니다.
            this.stompClient.subscribe("/send", res => {
              console.log('구독으로 받은 메시지 입니다.', res.body);
              // 받은 데이터를 json으로 파싱하고 리스트에 넣어줍니다.
              this.receiveList.push(JSON.parse(res.body))
            });
          },
          error => {
            // 소켓 연결 실패
            console.log('소켓 연결 실패', error);
            this.connected = false;
          }
      );
    },
  }
}
</script>

<style scoped>

</style>
```

Web Socket은 다음과 같이 설정하면 된다.  
`WebSocketConfig.java`
```java
package ramos.socketTest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 클라이언트가 메시지를 구독할 endpoint 정의
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/send");
    }

    // CORS 허용
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
```

이 후 메시지를 전달/수신 할 컨트롤러는 다음과 같다.
```java
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
```

## References
- [[10분 테코톡] 🧲코일의 Web Socket - Youtube 우아한 Tech](https://www.youtube.com/watch?v=MPQHvwPxDUw)
- [[10분 테코톡] ✨ 아론의 웹소켓&스프링 - Youtube 우아한 Tech](https://www.youtube.com/watch?v=rvss-_t6gzg)
- [vue spring boot 웹소켓 채팅 만들기](https://velog.io/@skyepodium/vue-spring-boot-stomp-%EC%9B%B9%EC%86%8C%EC%BC%93)
- [[요청강의] Spring Sockets - WebSocket, SockJS, STOMP - Youtube 시니어코딩](https://www.youtube.com/watch?v=gQyRxPjssWg)