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
package tech.pegasys.ethsigner;

import tech.pegasys.ethsigner.core.signing.TransactionSigner;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** Hashicorp vault related sub-command */
@Command(
    name = NullSignerSubCommand.COMMAND_NAME,
    description = "This is a signer which creates, and runs nothing.",
    mixinStandardHelpOptions = true)
public class NullSignerSubCommand extends SignerSubCommand {

  public static final String COMMAND_NAME = "NullSigner";

  @Option(names = "--the-data", description = "Some data required for this subcommand", arity = "1")
  private Integer downstreamHttpPort;

  private boolean shouldThrow = false;
  public static final String ERROR_MSG = "Null Signer Failed";

  public NullSignerSubCommand() {}

  public NullSignerSubCommand(boolean shouldThrow) {
    this.shouldThrow = shouldThrow;
  }

  @Override
  public TransactionSigner createSigner() throws TransactionSignerInitializationException {
    return null;
  }

  @Override
  public String getCommandName() {
    return COMMAND_NAME;
  }

  @Override
  public void run() {
    if (shouldThrow) {
      throw new TransactionSignerInitializationException(ERROR_MSG);
    }
  }
}
