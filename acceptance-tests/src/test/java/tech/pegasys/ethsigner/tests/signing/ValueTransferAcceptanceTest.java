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
package tech.pegasys.ethsigner.tests.signing;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static tech.pegasys.ethsigner.core.jsonrpc.response.JsonRpcError.SIGNING_FROM_IS_NOT_AN_UNLOCKED_ACCOUNT;
import static tech.pegasys.ethsigner.core.jsonrpc.response.JsonRpcError.TRANSACTION_UPFRONT_COST_EXCEEDS_BALANCE;
import static tech.pegasys.ethsigner.tests.dsl.Gas.GAS_PRICE;
import static tech.pegasys.ethsigner.tests.dsl.Gas.INTRINSIC_GAS;

import tech.pegasys.ethsigner.core.jsonrpc.response.JsonRpcError;
import tech.pegasys.ethsigner.core.jsonrpc.response.JsonRpcErrorResponse;
import tech.pegasys.ethsigner.tests.AcceptanceTestBase;
import tech.pegasys.ethsigner.tests.dsl.Account;
import tech.pegasys.ethsigner.tests.dsl.signer.SignerResponse;

import java.math.BigInteger;

import org.junit.Test;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;

public class ValueTransferAcceptanceTest extends AcceptanceTestBase {

  private static final String RECIPIENT = "0x1b00ba00ca00bb00aa00bc00be00ac00ca00da00";
  private static final long FIFTY_TRANSACTIONS = 50;

  @Test
  public void valueTransfer() {
    final BigInteger transferAmountWei = Convert.toWei("1.75", Unit.ETHER).toBigIntegerExact();
    final BigInteger startBalance = ethNode().accounts().balance(RECIPIENT);
    final Transaction transaction =
        Transaction.createEtherTransaction(
            richBenefactor().address(),
            null,
            GAS_PRICE,
            INTRINSIC_GAS,
            RECIPIENT,
            transferAmountWei);

    final String hash = ethSigner().transactions().submit(transaction);
    ethNode().transactions().awaitBlockContaining(hash);

    final BigInteger expectedEndBalance = startBalance.add(transferAmountWei);
    final BigInteger actualEndBalance = ethNode().accounts().balance(RECIPIENT);
    assertThat(actualEndBalance).isEqualTo(expectedEndBalance);
  }

  @Test
  public void valueTransferFromAccountWithInsufficientFunds() {
    final String recipientAddress = "0x1b11ba11ca11bb11aa11bc11be11ac11ca11da11";
    final BigInteger senderStartBalance = ethNode().accounts().balance(richBenefactor());
    final BigInteger recipientStartBalance = ethNode().accounts().balance(recipientAddress);
    final BigInteger transferAmountWei = senderStartBalance.add(BigInteger.ONE);
    final Transaction transaction =
        Transaction.createEtherTransaction(
            richBenefactor().address(),
            richBenefactor().nextNonce(),
            GAS_PRICE,
            INTRINSIC_GAS,
            recipientAddress,
            transferAmountWei);

    final SignerResponse<JsonRpcErrorResponse> signerResponse =
        ethSigner().transactions().submitExceptional(transaction);
    assertThat(signerResponse.status()).isEqualTo(BAD_REQUEST);
    assertThat(signerResponse.jsonRpc().getError())
        .isEqualTo(TRANSACTION_UPFRONT_COST_EXCEEDS_BALANCE);

    final BigInteger senderEndBalance = ethNode().accounts().balance(richBenefactor());
    final BigInteger recipientEndBalance = ethNode().accounts().balance(recipientAddress);
    assertThat(senderEndBalance).isEqualTo(senderStartBalance);
    assertThat(recipientEndBalance).isEqualTo(recipientStartBalance);
  }

  @Test
  public void senderIsNotUnlockedAccount() {
    final Account sender = new Account("0x223b55228fb22b89f2216b7222e5522b8222bd22");
    final String recipientAddress = "0x1b22ba22ca22bb22aa22bc22be22ac22ca22da22";
    final BigInteger senderStartBalance = ethNode().accounts().balance(sender);
    final BigInteger recipientStartBalance = ethNode().accounts().balance(recipientAddress);
    final Transaction transaction =
        Transaction.createEtherTransaction(
            sender.address(),
            sender.nextNonce(),
            GAS_PRICE,
            INTRINSIC_GAS,
            recipientAddress,
            senderStartBalance);

    final SignerResponse<JsonRpcErrorResponse> signerResponse =
        ethSigner().transactions().submitExceptional(transaction);
    assertThat(signerResponse.status()).isEqualTo(BAD_REQUEST);
    assertThat(signerResponse.jsonRpc().getError())
        .isEqualTo(SIGNING_FROM_IS_NOT_AN_UNLOCKED_ACCOUNT);

    final BigInteger senderEndBalance = ethNode().accounts().balance(sender);
    final BigInteger recipientEndBalance = ethNode().accounts().balance(recipientAddress);
    assertThat(senderEndBalance).isEqualTo(senderStartBalance);
    assertThat(recipientEndBalance).isEqualTo(recipientStartBalance);
  }

  @Test
  public void multipleValueTransfers() {
    final BigInteger transferAmountWei = Convert.toWei("1", Unit.ETHER).toBigIntegerExact();
    final BigInteger startBalance = ethNode().accounts().balance(RECIPIENT);
    final Transaction transaction =
        Transaction.createEtherTransaction(
            richBenefactor().address(),
            null,
            GAS_PRICE,
            INTRINSIC_GAS,
            RECIPIENT,
            transferAmountWei);

    String hash = null;
    for (int i = 0; i < FIFTY_TRANSACTIONS; i++) {
      hash = ethSigner().transactions().submit(transaction);
    }
    ethNode().transactions().awaitBlockContaining(hash);

    final BigInteger endBalance = ethNode().accounts().balance(RECIPIENT);
    final BigInteger numberOfTransactions = BigInteger.valueOf(FIFTY_TRANSACTIONS);
    assertThat(endBalance)
        .isEqualTo(startBalance.add(transferAmountWei.multiply(numberOfTransactions)));
  }

  @Test
  public void valueTransferNonceTooLow() {
    valueTransfer(); // call this test to increment the nonce
    final BigInteger transferAmountWei = Convert.toWei("15.5", Unit.ETHER).toBigIntegerExact();
    final Transaction transaction =
        Transaction.createEtherTransaction(
            richBenefactor().address(),
            BigInteger.ZERO,
            GAS_PRICE,
            INTRINSIC_GAS,
            RECIPIENT,
            transferAmountWei);

    final SignerResponse<JsonRpcErrorResponse> jsonRpcErrorResponseSignerResponse =
        ethSigner().transactions().submitExceptional(transaction);

    assertThat(jsonRpcErrorResponseSignerResponse.jsonRpc().getError())
        .isEqualTo(JsonRpcError.NONCE_TOO_LOW);
  }
}
