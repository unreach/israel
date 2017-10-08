/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.unreach.israel.transport;

import io.unreach.israel.ServiceProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An HTTP2 client that allows you to send HTTP2 frames to a server. Inbound and outbound frames are
 * logged. When run from the command-line, sends a single HEADERS frame to the server and gets back
 * a "Hello World" response.
 */
public final class Http2ClientTest {


    public static void main(String[] args) throws Exception {

        Http2Client client = new Http2Client();

        io.unreach.israel.transport.Channel channel = client.connect(new ServiceProvider("127.0.0.1", 8080));


        // String result = (String) channel.getHandler().invoke("io.unreach.israel.hello_1.0.0", "say", null);

        //  client.destory(channel);

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        for (int i = 0; i < 20; i++) {
            final int a = i;
            final int streamId = 3 + i * 2;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String a2 = (String) channel.getHandler().invoke("io.unreach.israel.hello_1.0.0", "say" + a, null);
                        System.out.println(a2);
                        // getHttpResponseHandler(streamId, a+" test " + streamId, channel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        // executorService.awaitTermination(10, TimeUnit.SECONDS);

    }


}
