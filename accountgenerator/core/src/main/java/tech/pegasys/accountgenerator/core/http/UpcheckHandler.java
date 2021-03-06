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

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class UpcheckHandler implements Handler<RoutingContext> {

  @Override
  public void handle(final RoutingContext routingContext) {
    routingContext.response().end("I'm up!");
  }
}
