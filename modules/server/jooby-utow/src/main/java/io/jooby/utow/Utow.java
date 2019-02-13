/**
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    Copyright 2014 Edgar Espina
 */
package io.jooby.utow;

import io.jooby.Jooby;
import io.jooby.Server;
import io.jooby.internal.utow.UtowHandler;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.encoding.EncodingHandler;
import org.xnio.Options;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utow extends Server.Base {

  private Undertow server;

  private int port = 8080;

  private boolean gzip;

  private long maxRequestSize = _10MB;

  private int bufferSize = _16KB;

  private int workerThreads = Math.max(Runtime.getRuntime().availableProcessors(), 2) * 8;

  private List<Jooby> applications = new ArrayList<>();

  @Override public Server port(int port) {
    this.port = port;
    return this;
  }

  public Server gzip(boolean enabled) {
    this.gzip = enabled;
    return this;
  }

  @Override public int port() {
    return port;
  }

  @Nonnull @Override public Server maxRequestSize(long maxRequestSize) {
    this.maxRequestSize = maxRequestSize;
    return this;
  }

  @Nonnull @Override public Server bufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
    return this;
  }

  @Override public Server workerThreads(int workerThreads) {
    this.workerThreads = workerThreads;
    return this;
  }

  @Override public Server start(Jooby application) {

    applications.add(application);

    addShutdownHook();

    HttpHandler handler = new UtowHandler(applications.get(0), bufferSize, maxRequestSize);

    if (gzip) {
      handler = new EncodingHandler.Builder().build(null).wrap(handler);
    }

    server = Undertow.builder()
        .addHttpListener(port, "0.0.0.0")
        .setBufferSize(bufferSize)
        /** Socket : */
        .setSocketOption(Options.BACKLOG, 8192)
        /** Server: */
        // HTTP/1.1 is keep-alive by default, turn this option off
        .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false)
        .setServerOption(UndertowOptions.ALWAYS_SET_DATE, false)
        .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, false)
        .setServerOption(UndertowOptions.DECODE_URL, false)
        /** Worker: */
        .setWorkerOption(Options.WORKER_NAME, "utow")
        .setWorkerThreads(workerThreads)
        .setHandler(handler)
        .build();

    server.start();
    // NOT IDEAL, but we need to fire onStart after server.start to get access to Worker
    fireStart(applications, server.getWorker());

    fireReady(Collections.singletonList(application));

    return this;
  }

  @Override public Server stop() {
    fireStop(applications);
    applications = null;
    if (server != null) {
      server.stop();
      server = null;
    }
    return this;
  }

}