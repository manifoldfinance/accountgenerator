/*
 * Copyright 2020 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.accountgenerator.core.http;

import tech.pegasys.accountgenerator.core.jsonrpc.JsonDecoder;
import tech.pegasys.accountgenerator.core.jsonrpc.JsonRpcRequest;
import tech.pegasys.accountgenerator.core.jsonrpc.response.JsonRpcError;
import tech.pegasys.accountgenerator.core.jsonrpc.response.JsonRpcErrorResponse;
import tech.pegasys.accountgenerator.core.requesthandler.JsonRpcRequestHandler;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.DecodeException;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsonRpcHandler implements Handler<RoutingContext> {

  private static final Logger LOG = LogManager.getLogger();

  private final RequestMapper requestHandlerMapper;
  private final HttpResponseFactory responseFactory;
  private final JsonDecoder jsonDecoder;

  public JsonRpcHandler(
      final HttpResponseFactory responseFactory,
      final RequestMapper requestHandlerMapper,
      final JsonDecoder jsonDecoder) {
    this.responseFactory = responseFactory;
    this.requestHandlerMapper = requestHandlerMapper;
    this.jsonDecoder = jsonDecoder;
  }

  @Override
  public void handle(final RoutingContext context) {

    context
        .vertx()
        .executeBlocking(
            future -> {
              process(context);
              future.complete();
            },
            false,
            res -> {
              if (res.failed()) {
                LOG.error(
                    "An unhandled error occurred while processing " + context.getBodyAsString(),
                    res.cause());
              }
            });
  }

  private void process(final RoutingContext context) {
    try {
      LOG.trace("Request body = {}", context.getBodyAsString());
      final JsonRpcRequest request =
          jsonDecoder.decodeValue(context.getBody(), JsonRpcRequest.class);
      final JsonRpcRequestHandler handler =
          requestHandlerMapper.getMatchingHandler(request.getMethod());
      handler.handle(context, request);
    } catch (final DecodeException | IllegalArgumentException e) {
      sendParseErrorResponse(context, e);
    }
  }

  private void sendParseErrorResponse(final RoutingContext context, final Throwable error) {
    LOG.info("Dropping request from {}", context.request().remoteAddress());
    LOG.debug("Parsing body as JSON failed for: {}", context.getBodyAsString(), error);
    responseFactory.create(
        context.request(),
        HttpResponseStatus.BAD_REQUEST.code(),
        new JsonRpcErrorResponse(JsonRpcError.PARSE_ERROR));
  }
}
