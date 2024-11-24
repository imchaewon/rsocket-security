const { RSocketClient, JsonSerializer, IdentitySerializer } = require('rsocket-core');
const WebsocketClientTransport = require('rsocket-websocket-client').default;

// JWT 토큰
const jwtToken = 'YOUR_GENERATED_JWT_TOKEN_HERE';
const RSocketMimeType = 'message/x.rsocket.authentication.bearer.v0';

// 메타데이터 생성
const metadata = String.fromCharCode(RSocketMimeType.length) + RSocketMimeType + jwtToken;

// RSocket 클라이언트 초기화
const client = new RSocketClient({
  serializers: {
    data: JsonSerializer,
    metadata: IdentitySerializer,
  },
  setup: {
    dataMimeType: 'application/json',
    metadataMimeType: RSocketMimeType,
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
        metadata,
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

