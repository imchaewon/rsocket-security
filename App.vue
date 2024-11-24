<template>
  <div>
    <ul>
      <li v-for="(message, index) in messages" :key="index">{{ message }}</li>
    </ul>
    <input v-model="inputMessage" @keyup.enter="sendMessage" />
    <button @click="sendMessage">Send</button>
  </div>
</template>

<script>
import { RSocketClient } from "rsocket-core";
import RSocketWebSocketClient from "rsocket-websocket-client";
import { BufferEncoders } from "rsocket-core";
import { encodeCompositeMetadata } from "rsocket-composite-metadata";

export default {
  setup() {
    const host = "127.0.0.1";
    const port = 7777;
    const jwtToken = "your-jwt-token";
    const route = "hello";

    // MIME Types
    const metadataMimeType = "message/x.rsocket.composite-metadata.v0";
    const routingMimeType = "message/x.rsocket.routing.v0";
    const bearerMimeType = "message/x.rsocket.authentication.bearer.v0";

    const client = new RSocketClient({
      setup: {
        keepAlive: 60000,
        lifetime: 180000,
        dataMimeType: "application/octet-stream",
        metadataMimeType,
      },
      transport: new RSocketWebSocketClient(
        { url: `ws://${host}:${port}` },
        BufferEncoders
      ),
    });

    client.connect().then((socket) => {
      const metadata = encodeCompositeMetadata([
        {
          mimeType: routingMimeType,
          value: Buffer.from(String.fromCharCode(route.length) + route),
        },
        {
          mimeType: bearerMimeType,
          value: Buffer.from(jwtToken),
        },
      ]);

      socket
        .requestResponse({
          data: Buffer.from("Requesting hello"),
          metadata,
        })
        .subscribe({
          onComplete: (response) => console.log("Response:", response.data.toString()),
          onError: (error) => console.error("Error:", error),
        });
    });
  },
};

</script>
