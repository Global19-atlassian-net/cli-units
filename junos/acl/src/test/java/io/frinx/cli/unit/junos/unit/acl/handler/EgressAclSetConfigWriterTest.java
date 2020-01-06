/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.junos.unit.acl.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.EgressAclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.egress.acl.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.egress.acl.set.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EgressAclSetConfigWriterTest {
    private static final String INTERFACE_NAME = "ge-0/0/1";
    private static final String UNIT_NUMBER = "9999";
    private static final String FILTER_NAME = "FILTER-001";
    private static final Class<? extends ACLTYPE> NET_TYPE = ACLIPV4.class;
    private static final InstanceIdentifier<Config> IID = IIDs.AC_INTERFACES
        .child(Interface.class, new InterfaceKey(new InterfaceId(INTERFACE_NAME + "." + UNIT_NUMBER)))
        .child(EgressAclSets.class)
        .child(EgressAclSet.class, new EgressAclSetKey(FILTER_NAME, NET_TYPE))
        .child(Config.class);
    private static final Config DATA = new ConfigBuilder()
        .setSetName(FILTER_NAME)
        .setType(NET_TYPE)
        .build();

    @Mock
    private Cli cli;
    @Mock
    private WriteContext writeContext;

    private EgressAclSetConfigWriter target;

    private ArgumentCaptor<Command> commands;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = new EgressAclSetConfigWriter(cli);

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        commands = ArgumentCaptor.forClass(Command.class);
    }

    @Test
    public void testWriteCurrentAttributes() throws Exception {
        target.writeCurrentAttributes(IID, DATA, writeContext);

        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());

        Assert.assertThat(commands.getAllValues().size(), CoreMatchers.is(1));
        Assert.assertThat(commands.getAllValues().get(0).getContent(), CoreMatchers.equalTo(
            "set interfaces ge-0/0/1 unit 9999 family inet filter output FILTER-001\n"));
    }

    @Test
    public void testDeleteCurrentAttributes() throws Exception {
        target.deleteCurrentAttributes(IID, DATA, writeContext);

        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());

        Assert.assertThat(commands.getAllValues().size(), CoreMatchers.is(1));
        Assert.assertThat(commands.getAllValues().get(0).getContent(), CoreMatchers.equalTo(
            "delete interfaces ge-0/0/1 unit 9999 family inet filter output FILTER-001\n"));
    }
}