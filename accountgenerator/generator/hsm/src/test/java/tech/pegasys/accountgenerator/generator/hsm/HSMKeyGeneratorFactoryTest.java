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
package tech.pegasys.accountgenerator.generator.hsm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import tech.pegasys.accountgenerator.core.KeyGenerator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class HSMKeyGeneratorFactoryTest {

  private static String library;
  private static String slot;
  private static String pin;

  private static HSMKeyGeneratorFactory factory;
  private static HSMWalletProvider provider;

  @BeforeAll
  public static void beforeAll() {
    Properties p = new Properties();
    InputStream is = ClassLoader.getSystemResourceAsStream("softhsm-wallet-002" + ".properties");
    try {
      p.load(is);
      library = p.getProperty("library");
      slot = p.getProperty("slot");
      pin = p.getProperty("pin");
    } catch (IOException e) {
      fail("Properties file not found");
    }

    org.junit.jupiter.api.Assumptions.assumeTrue((new File(library)).exists());
    provider = new HSMWalletProvider(new HSMConfig(library, slot, pin));
    provider.initialize();
    factory = new HSMKeyGeneratorFactory(provider, Path.of("/tmp"));
  }

  @AfterAll
  public static void afterAll() {
    if (factory != null) {
      factory.shutdown();
    }
  }

  @Test
  public void success() {
    final KeyGenerator generator = factory.getGenerator();
    assertThat(generator).isNotNull();
    assertThat(generator.generate()).isNotNull();
  }
}
