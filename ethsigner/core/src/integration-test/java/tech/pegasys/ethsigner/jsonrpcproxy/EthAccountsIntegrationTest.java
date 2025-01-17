/*
 * Copyright 2019 ConsenSys AG.
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
package tech.pegasys.ethsigner.jsonrpcproxy;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

import tech.pegasys.ethsigner.core.jsonrpc.response.JsonRpcSuccessResponse;

import java.util.Map;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.json.Json;
import org.junit.Test;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthAccounts;

public class EthAccountsIntegrationTest extends IntegrationTestBase {

  @Test
  public void ethAccountsRequestFromWeb3jRespondsWithNodesAddress() {

    final Request<?, EthAccounts> requestBody = jsonRpc().ethAccounts();
    final Map<String, String> expectedHeaders =
        singletonMap("Content", HttpHeaderValues.APPLICATION_JSON.toString());

    final JsonRpcSuccessResponse responseBody =
        new JsonRpcSuccessResponse(requestBody.getId(), singletonList(unlockedAccount));

    sendRequestThenVerifyResponse(
        request.ethSigner(Json.encode(requestBody)),
        response.ethSigner(expectedHeaders, Json.encode(responseBody)));
  }
}
