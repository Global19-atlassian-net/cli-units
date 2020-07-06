/*
 * Copyright © 2020 Frinx and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.frinx.cli.unit.saos.l2.cft.handler;

import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.l2.cft.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2CftConfigWriterTest {

    private static final String MODE_ONE = "l2-cft set mode mef-ce1\nconfiguration save";
    private static final String MODE_TWO = "l2-cft set mode mef-ce2\nconfiguration save";

    @Mock
    private Cli cli;

    private final InstanceIdentifier iid = IIDs.L2CFT;

    private final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

    private L2CftConfigWriter writer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new L2CftConfigWriter(cli);
    }

    @Test
    public void writeCurrentAttributesTest_01() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, createConfig("mef-ce2"), null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        Assert.assertEquals(MODE_TWO, commands.getValue().getContent());
    }

    @Test
    public void writeCurrentAttributesTest_02() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, null, null);

        Mockito.verifyZeroInteractions(cli);
    }

    @Test
    public void updateCurrentAttributesTest_01() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, createConfig("mef-ce2"), createConfig("mef-ce1"), null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        Assert.assertEquals(MODE_ONE, commands.getValue().getContent());
    }

    @Test
    public void updateCurrentAttributesTest_02() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, createConfig("mef-ce1"), createConfig("mef-ce2"), null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        Assert.assertEquals(MODE_TWO, commands.getValue().getContent());
    }

    @Test
    public void updateCurrentAttributesTest_03() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, createConfig("mef-ce1"), createConfig("mef-ce1"), null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        Assert.assertEquals("", commands.getValue().getContent());
    }

    @Test
    public void deleteCurrentAttributesTest() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, createConfig("mef-ce2"), null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        Assert.assertEquals(MODE_ONE, commands.getValue().getContent());
    }

    private Config createConfig(String mode) {
        return new ConfigBuilder().setMode(mode).build();
    }
}
