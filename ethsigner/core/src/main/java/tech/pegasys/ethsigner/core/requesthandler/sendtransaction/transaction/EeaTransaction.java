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
package tech.pegasys.ethsigner.core.requesthandler.sendtransaction.transaction;

import tech.pegasys.ethsigner.core.jsonrpc.EeaSendTransactionJsonParameters;
import tech.pegasys.ethsigner.core.jsonrpc.JsonRpcRequest;
import tech.pegasys.ethsigner.core.jsonrpc.JsonRpcRequestId;
import tech.pegasys.ethsigner.core.requesthandler.sendtransaction.NonceProvider;

import java.math.BigInteger;
import java.util.List;

import com.google.common.base.MoreObjects;
import org.web3j.crypto.Sign.SignatureData;
import org.web3j.protocol.eea.crypto.PrivateTransactionEncoder;
import org.web3j.protocol.eea.crypto.RawPrivateTransaction;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpType;

public class EeaTransaction implements Transaction {
  private static final String JSON_RPC_METHOD = "eea_sendRawTransaction";
  private final EeaSendTransactionJsonParameters transactionJsonParameters;
  private final JsonRpcRequestId id;
  private final NonceProvider nonceProvider;
  private BigInteger nonce;

  EeaTransaction(
      final EeaSendTransactionJsonParameters transactionJsonParameters,
      final NonceProvider nonceProvider,
      final JsonRpcRequestId id) {
    this.transactionJsonParameters = transactionJsonParameters;
    this.nonceProvider = nonceProvider;
    this.id = id;
    this.nonce = transactionJsonParameters.nonce().orElse(null);
  }

  @Override
  public void updateNonce() {
    this.nonce = nonceProvider.getNonce();
  }

  @Override
  public byte[] rlpEncode(final SignatureData signatureData) {
    final RawPrivateTransaction rawTransaction = createTransaction();
    final List<RlpType> values =
        PrivateTransactionEncoder.asRlpValues(rawTransaction, signatureData);
    final RlpList rlpList = new RlpList(values);
    return RlpEncoder.encode(rlpList);
  }

  @Override
  public boolean isNonceUserSpecified() {
    return transactionJsonParameters.nonce().isPresent();
  }

  @Override
  public String sender() {
    return transactionJsonParameters.sender();
  }

  @Override
  public JsonRpcRequest jsonRpcRequest(
      final String signedTransactionHexString, final JsonRpcRequestId id) {
    return Transaction.jsonRpcRequest(signedTransactionHexString, id, JSON_RPC_METHOD);
  }

  @Override
  public JsonRpcRequestId getId() {
    return id;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("transactionJsonParameters", transactionJsonParameters)
        .add("id", id)
        .add("nonceProvider", nonceProvider)
        .add("nonce", nonce)
        .toString();
  }

  private RawPrivateTransaction createTransaction() {
    return RawPrivateTransaction.createTransaction(
        nonce,
        transactionJsonParameters.gasPrice().orElse(DEFAULT_GAS_PRICE),
        transactionJsonParameters.gas().orElse(DEFAULT_GAS),
        transactionJsonParameters.receiver().orElse(DEFAULT_TO),
        transactionJsonParameters.data().orElse(DEFAULT_DATA),
        transactionJsonParameters.privateFrom(),
        transactionJsonParameters.privateFor(),
        transactionJsonParameters.restriction());
  }
}
