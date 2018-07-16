/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.ios.network.instance.handler.l2p2p;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;

public class L2P2PReaderTest {

    private static final String OUTPUT = "interface Loopback0\n"
            + "interface GigabitEthernet0/0\n"
            + "interface GigabitEthernet0/1\n"
            + " xconnect 10.1.0.7 222 pw-class vpn1234\n"
            + "interface GigabitEthernet0/2\n"
            + " xconnect 10.1.0.7 222 pw-class vpn1235\n"
            + "interface GigabitEthernet0/3\n"
            + "interface GigabitEthernet0/3.444\n"
            + "interface GigabitEthernet0/3.666\n"
            + " xconnect 10.1.0.7 666 pw-class vpn1236\n"
            + "interface GigabitEthernet0/4\n"
            + "interface GigabitEthernet0/5\n"
            + " xconnect 10.1.0.7 555 encapsulation mpls\n"
            + "interface GigabitEthernet0/6\n"
            + "interface GigabitEthernet0/7\n";

    private static final List<NetworkInstanceKey> EXPECTED = Lists.newArrayList("vpn1234", "vpn1235", "vpn1236",
            "GigabitEthernet0/5 xconnect 10.1.0.7")
            .stream()
            .map(NetworkInstanceKey::new)
            .collect(Collectors.toList());

    @Test
    public void testIds() throws Exception {
        List<NetworkInstanceKey> networkInstanceKeys = L2P2PReader.parseXconnectIds(OUTPUT);
        assertEquals(EXPECTED, networkInstanceKeys);
    }
}