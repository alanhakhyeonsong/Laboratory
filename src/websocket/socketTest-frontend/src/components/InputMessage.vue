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