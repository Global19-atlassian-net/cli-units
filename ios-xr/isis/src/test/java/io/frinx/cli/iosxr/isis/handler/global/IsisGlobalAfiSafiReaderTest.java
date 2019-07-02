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

package io.frinx.cli.iosxr.isis.handler.global;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.IPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.IPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.MULTICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.afi.safi.list.Af;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.afi.safi.list.AfBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.afi.safi.list.AfKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.ISIS;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IsisGlobalAfiSafiReaderTest {
    private static final String INSTANCE_NAME = "1000";
    private static final String SH_RUN_ROUTER_ISIS = "show running-config router isis 1000 | include ^ address-family";
    private static final String SH_RUN_ROUTER_ISIS_LINES = " address-family ipv4 unicast\n"
        + " address-family ipv6 multicast\n";

    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;
    private IsisGlobalAfiSafiReader target;

    private static final ProtocolKey PROTOCOL_KEY = new ProtocolKey(ISIS.class, INSTANCE_NAME);
    private static final AfKey AF_KEY = new AfKey(IPV4.class, UNICAST.class);
    private static final InstanceIdentifier<Af> IID_FOR_LIST = IidUtils.createIid(IIDs.NE_NE_PR_PR_IS_GL_AFISAFI,
        NetworInstance.DEFAULT_NETWORK, PROTOCOL_KEY)
        .child(Af.class);
    private static final InstanceIdentifier<Af> IID = IidUtils.createIid(IIDs.NE_NE_PR_PR_IS_GL_AF_AF,
        NetworInstance.DEFAULT_NETWORK, PROTOCOL_KEY, AF_KEY);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new IsisGlobalAfiSafiReader(cli));
    }

    @Test
    public void testGetAllIds_001() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_ROUTER_ISIS_LINES).when(target)
            .blockingRead(SH_RUN_ROUTER_ISIS, cli, IID_FOR_LIST, ctx);

        List<AfKey> result = target.getAllIds(IID_FOR_LIST, ctx);

        Assert.assertThat(result, Matchers.containsInAnyOrder(
            new AfKey(IPV4.class, UNICAST.class),
            new AfKey(IPV6.class, MULTICAST.class)));
    }

    @Test
    public void testReadCurrentAttributes() throws ReadFailedException {
        final AfBuilder builder = new AfBuilder();

        target.readCurrentAttributes(IID, builder, ctx);

        Assert.assertThat(builder.getAfiName(), CoreMatchers.equalTo(IPV4.class));
        Assert.assertThat(builder.getSafiName(), CoreMatchers.equalTo(UNICAST.class));
    }
}
