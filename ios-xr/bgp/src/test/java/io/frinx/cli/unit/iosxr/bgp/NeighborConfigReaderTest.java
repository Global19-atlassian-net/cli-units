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

package io.frinx.cli.unit.iosxr.bgp;

import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborConfigReader;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.ConfigBuilder;

public class NeighborConfigReaderTest {

    private static final String OUTPUT = "Fri Feb 23 06:18:50.457 UTC\n"
            + " neighbor 5.5.5.5\n"
            + "  remote-as 5000\n"
            + "  use neighbor-group nbrgroup1\n"
            + " neighbor 6.6.6.6\n"
            + "  remote-as 5000\n"
            + "  shutdown\n"
            + " neighbor 8.8.8.8\n"
            + "  remote-as 65000\n"
            + "  use neighbor-group nbrgroup1\n"
            + " neighbor 7.7.7.7\n"
            + "  remote-as 65000\n"
            + "  use neighbor-group nbrgroup1\n"
            + "  description test desc\n"
            + "  password encrypted GCHKNJDJSADNKLSAND\n"
            + "  send-community-ebgp\n"
            + "  remove-private-AS\n"
            + " neighbor 1.2.3.4\n"
            + "  remote-as 1.111\n";

    @Test
    public void test() {
        ConfigBuilder builder = new ConfigBuilder();
        NeighborConfigReader.readNeighbor(OUTPUT, builder, "8.8.8.8");
        Assert.assertEquals(65000, builder.getPeerAs().getValue().intValue());
        Assert.assertTrue(builder.isEnabled());
        Assert.assertEquals("nbrgroup1", builder.getPeerGroup());

        builder = new ConfigBuilder();
        NeighborConfigReader.readNeighbor(OUTPUT, builder, "5.5.5.5");
        Assert.assertEquals(5000, builder.getPeerAs().getValue().intValue());
        Assert.assertTrue(builder.isEnabled());
        Assert.assertEquals("nbrgroup1", builder.getPeerGroup());

        builder = new ConfigBuilder();
        NeighborConfigReader.readNeighbor(OUTPUT, builder, "6.6.6.6");
        Assert.assertEquals(5000, builder.getPeerAs().getValue().intValue());
        Assert.assertFalse(builder.isEnabled());
        Assert.assertNull(builder.getPeerGroup());

        builder = new ConfigBuilder();
        NeighborConfigReader.readNeighbor(OUTPUT, builder, "7.7.7.7");
        Assert.assertEquals(65000, builder.getPeerAs().getValue().intValue());
        Assert.assertTrue(builder.isEnabled());
        Assert.assertEquals("nbrgroup1", builder.getPeerGroup());
        Assert.assertEquals("test desc", builder.getDescription());
        Assert.assertEquals("Encrypted[GCHKNJDJSADNKLSAND]",
                builder.getAuthPassword().getEncryptedString().getValue());
        Assert.assertNotNull(builder.getSendCommunity());
        Assert.assertNotNull(builder.getRemovePrivateAs());

        builder = new ConfigBuilder();
        NeighborConfigReader.readNeighbor(OUTPUT, builder, "1.2.3.4");
        Assert.assertEquals(65647, builder.getPeerAs().getValue().longValue());
    }
}
