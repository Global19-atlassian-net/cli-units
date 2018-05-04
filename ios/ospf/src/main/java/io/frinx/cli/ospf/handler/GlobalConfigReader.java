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

package io.frinx.cli.ospf.handler;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.ospf.OspfReader;
import io.frinx.cli.io.Cli;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalConfigReader implements OspfReader.OspfConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public GlobalConfigReader(Cli cli) {
        this.cli = cli;
    }

    static final String SH_OSPF = "show running-config | include ^router ospf|^ router-id";
    static final Pattern ROUTER_ID = Pattern.compile(".*?router-id (?<routerId>[0-9.]+).*");

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String ospfId = instanceIdentifier.firstKeyOf(Protocol.class).getName();
        parseGlobal(blockingRead(SH_OSPF, cli, instanceIdentifier, readContext), configBuilder, ospfId);
    }

    @VisibleForTesting
    static void parseGlobal(String output, ConfigBuilder builder, String ospfId) {
        output = output.replaceAll("\\n|\\r", "");

        output = output.replace("router ospf", "\nrouter ospf");

        NEWLINE.splitAsStream(output)
                .map(String::trim)
                .filter(s -> s.startsWith("router ospf " + ospfId))
                .map(ROUTER_ID::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("routerId"))
                .findFirst()
                .ifPresent(rd -> builder.setRouterId(new DottedQuad(rd)));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((GlobalBuilder) builder).setConfig(config);
    }
}
