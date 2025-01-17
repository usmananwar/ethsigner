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

import static org.assertj.core.api.Java6Assertions.assertThat;

import tech.pegasys.ethsigner.core.http.RequestMapper;
import tech.pegasys.ethsigner.core.requesthandler.JsonRpcRequestHandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RequestMapperTest {
  @Mock private JsonRpcRequestHandler defaultHandler;
  @Mock private JsonRpcRequestHandler handler1;
  @Mock private JsonRpcRequestHandler handler2;

  @Test
  public void returnsHandleForAssociatedRpcMethod() {
    final RequestMapper requestMapper = new RequestMapper(defaultHandler);
    requestMapper.addHandler("foo", handler1);
    requestMapper.addHandler("bar", handler2);
    requestMapper.addHandler("default", defaultHandler);
    assertThat(requestMapper.getMatchingHandler("foo")).isSameAs(handler1);
    assertThat(requestMapper.getMatchingHandler("bar")).isSameAs(handler2);
  }

  @Test
  public void returnsDefaultHandlerForUnknownRpcMethod() {
    final RequestMapper requestMapper = new RequestMapper(defaultHandler);
    assertThat(requestMapper.getMatchingHandler("")).isEqualTo(defaultHandler);
    assertThat(requestMapper.getMatchingHandler("nothing")).isEqualTo(defaultHandler);
  }
}
