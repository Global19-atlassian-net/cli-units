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

package io.frinx.cli.unit.iosxr.bfd.handler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.IfBfdExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.IfBfdExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.top.bfd.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.top.bfd.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;

public class ConfigReaderTest {

    private static final String CONFIGURATION_1 = "Mon Nov 27 14:04:50.483 UTC\n"
            + "interface Bundle-Ether2\n"
            + " description testt\n"
            + " bfd mode ietf\n"
            + " bfd address-family ipv4 fast-detect\n"
            + " bundle id 400 mode on\n"
            + "!\n"
            + "\n";

    private static final Config EXPECTED_CONFIG_1 = new ConfigBuilder()
            .setId("Bundle-Ether2")
            .addAugmentation(IfBfdExtAug.class, new IfBfdExtAugBuilder().build())
            .build();

    private static final String CONFIGURATION_2 = "Tue Nov 28 09:41:57.064 UTC\n"
            + "interface Bundle-Ether1\n"
            + " bfd mode ietf\n"
            + " bfd address-family ipv4 multiplier 30\n"
            + " bfd address-family ipv4 destination 10.1.1.1\n"
            + " bfd address-family ipv4 fast-detect\n"
            + " bfd address-family ipv4 minimum-interval 4\n"
            + " ipv4 address 10.0.0.5 255.255.255.0\n"
            + "!\n"
            + "\n";

    private static final Config EXPECTED_CONFIG_2 = new ConfigBuilder()
            .setId("Bundle-Ether1")
            .setDesiredMinimumTxInterval(4L)
            .setDetectionMultiplier(30)
            .addAugmentation(IfBfdExtAug.class, new IfBfdExtAugBuilder()
                    .setRemoteAddress(new IpAddress(new Ipv4Address("10.1.1.1")))
                    .build())
            .build();

    @Test
    public void parseBfdConfigTest() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        ConfigReader.parseBfdConfig(CONFIGURATION_1, "Bundle-Ether2", configBuilder);
        Assert.assertEquals(EXPECTED_CONFIG_1, configBuilder.build());

        configBuilder = new ConfigBuilder();
        ConfigReader.parseBfdConfig(CONFIGURATION_2, "Bundle-Ether1", configBuilder);
        Assert.assertEquals(EXPECTED_CONFIG_2, configBuilder.build());

    }
}