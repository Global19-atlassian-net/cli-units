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

package io.frinx.cli.unit.iosxr.unit.acl.handler;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;

public class AclSetReaderTest {

    @Test
    public void testParse() {
        List<AclSetKey> aclSetKeys = AclSetReader.parseAccessLists("ipv6 access-list acl2\n"
                + " 10 deny ipv6 host dead::beef any\n"
                + "!\n"
                + "ipv4 access-list acl1\n"
                + " 10 permit ipv4 any any\n"
                + "!\n"
                + "telnet vrf default ipv4 server max-servers 16 access-list VTY-ACCESS-IN\n"
                + "!\n");

        Assert.assertEquals(Lists.newArrayList(
                new AclSetKey("acl2", ACLIPV6.class),
                new AclSetKey("acl1", ACLIPV4.class)),
                aclSetKeys);
    }
}