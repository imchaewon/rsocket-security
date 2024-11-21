<template>
  <div id="app">
    <h1>RSocket JWT Example</h1>
    <div>
      <label for="token">JWT Token:</label>
      <input type="text" v-model="token" id="token" placeholder="Optional" />
    </div>
    <div>
      <label for="method">Method:</label>
      <input type="text" v-model="method" id="method" placeholder="Method Name" />
    </div>
    <div>
      <label for="name">Name:</label>
      <input type="text" v-model="name" id="name" placeholder="Name Argument" />
    </div>
    <button @click="sendMessage">Send Message</button>
    <p v-if="response"><strong>Response:</strong> {{ response }}</p>
  </div>
</template>

<script>
import { RSocketClient } from "rsocket-core";
import RSocketWebSocketClient from "rsocket-websocket-client";
import { encodeCompositeMetadata } from "rsocket-composite-metadata";

// JWT 토큰을 Base64로 인코딩하는 함수
//const encodeBearerToken = (token) => `Bearer ${btoa(token)}`;

// 전역 클라이언트 인스턴스
let clientInstance = null;

export default {
  data() {
    return {
      token: "", // JWT Token
      method: "", // RSocket Method
      name: "", // Name Argument
      response: null, // Response from Server
    };
  },
  methods: {
    async sendMessage() {
      if (!clientInstance) {
        try {
          // RSocket 클라이언트 생성
          clientInstance = new RSocketClient({
            setup: {
              dataMimeType: "text/plain", // Data MIME type
              metadataMimeType: "message/x.rsocket.composite-metadata.v0", // Composite Metadata MIME type
            },
            transport: new RSocketWebSocketClient({
              url: "ws://localhost:7777/", // RSocket 서버 URL
            }),
          });
        } catch (error) {
          console.error("Failed to create RSocket client:", error);
          this.response = "Failed to initialize RSocket client.";
          return;
        }
      }

      const route = this.method || "hello.secure"; // 기본 라우트 설정
      const token = this.token || "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImF1ZCI6ImhlbGxvLXNlcnZpY2UiLCJzY29wZSI6IkFETUlOIiwiaXNzIjoiaGVsbG8tc2VydmljZS1kZW1vIiwiZXhwIjoxNzMyMjA3Mjg1LCJqdGkiOiJjOTM2OTgzZi1lMzYxLTRjYWEtYWEyNS01YzY4MTE0NzY3MzIifQ.eQX8eN6wyt2I2LRrjO_BVgX_1axzKCJGDqinu2-Npmo"; // 기본 JWT 설정
      const name = this.name || "Guest"; // 기본 이름 설정

      try {
        // 클라이언트 연결
        const rsocket = await clientInstance.connect();

        console.log('route:', route, 'token:', token)

        // Metadata 생성
        const routingMetadata = new TextEncoder().encode(route); // 라우팅 데이터를 Uint8Array로 인코딩
        //const bearerMetadata = new TextEncoder().encode(encodeBearerToken(token)); // JWT 토큰 데이터를 Uint8Array로 인코딩
        const bearerMetadata = new TextEncoder().encode(token); // JWT 토큰 데이터를 Uint8Array로 인코딩

        // Composite Metadata 생성
        const compositeMetadata = encodeCompositeMetadata([
          [
            "message/x.rsocket.routing.v0", // 라우팅 MIME 타입
            routingMetadata, // 라우팅 데이터
          ],
          [
            "message/x.rsocket.authentication.bearer.v0", // 인증 MIME 타입
            bearerMetadata, // 인증 데이터
          ],
        ]);

        console.log("Composite Metadata:", compositeMetadata);

        // 요청 Payload 생성
        const payload = {
          data: name, // 서버로 보낼 데이터
          metadata: compositeMetadata, // 메타데이터
        };

        // 서버에 요청 전송
        const response = await rsocket
          .requestResponse(payload)
          .then((res) => res.data);

        this.response = response;

        // 연결 닫기
        rsocket.close();
        clientInstance = null;
      } catch (error) {
        console.error("Error during RSocket communication:", error);
        this.response = "An error occurred: " + error.message;

        // 오류 발생 시 클라이언트 재설정
        if (clientInstance) {
          clientInstance.close();
          clientInstance = null;
        }
      }
    },
  },
};
</script>




<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>