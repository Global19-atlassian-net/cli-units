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

package io.frinx.cli.unit.brocade.ifc.handler.subifc.ip4;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.ifc.Util;
import io.frinx.cli.unit.ifc.base.handler.subifc.ip4.AbstractIpv4ConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;

public final class Ipv4ConfigReader extends AbstractIpv4ConfigReader {

    public Ipv4ConfigReader(Cli cli) {
        super(cli);
    }

    @Override
    protected Pattern getIpLine() {
        return Ipv4AddressReader.INTERFACE_IP_LINE;
    }

    @Override
    protected String getReadCommand(String ifcName, Long subId) {
        Class<? extends InterfaceType> ifcType = Util.parseType(ifcName);
        String ifcNumber = Util.getIfcNumber(ifcName);
        return fT(Ipv4AddressReader.SH_INTERFACE_IP, "ifcType", Util.getTypeOnDevice(ifcType), "ifcNumber", ifcNumber);
    }

    @Override
    public void parseAddressConfig(ConfigBuilder configBuilder, String output, Ipv4AddressNoZone address) {
        Optional<String> optionalAddressLine = ParsingUtils.NEWLINE.splitAsStream(output)
                .filter(line -> line.contains(address.getValue()))
                .findAny();

        if (!optionalAddressLine.isPresent()) {
            return;
        }

        configBuilder.setIp(address);

        ParsingUtils.parseField(optionalAddressLine.get(),
            getIpLine()::matcher,
            m -> Short.parseShort(m.group("prefix")),
            configBuilder::setPrefixLength);
    }
}
