const { RSocketClient, JsonSerializer, IdentitySerializer } = require('rsocket-core');
const WebsocketClientTransport = require('rsocket-websocket-client').default; // CommonJS 가져오기

// JWT 토큰
const jwtToken = 'YOUR_GENERATED_JWT_TOKEN_HERE';

// RSocket 클라이언트 초기화
const client = new RSocketClient({
  serializers: {
    data: JsonSerializer,
    metadata: IdentitySerializer,
  },
  setup: {
    dataMimeType: 'application/json',
    metadataMimeType: 'message/x.rsocket.authentication.bearer.v0',
    keepAlive: 10000,
    lifetime: 20000,
  },
  transport: new WebsocketClientTransport({
    url: 'ws://localhost:7777',
  }),
});

// 연결
client.connect().subscribe({
  onComplete: (rsocket) => {
    console.log('RSocket 연결 성공');
    rsocket
      .requestResponse({
        data: { message: 'Hello, Server!' },
        metadata: String.fromCharCode(jwtToken.length) + jwtToken,
      })
      .subscribe({
        onComplete: (response) => {
          console.log('응답 데이터:', response.data);
        },
        onError: (error) => {
          console.error('에러 발생:', error);
        },
      });
  },
  onError: (error) => {
    console.error('RSocket 연결 실패:', error);
  },
});

