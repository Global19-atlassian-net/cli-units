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

package io.frinx.cli.iosxr.bgp.handler.neighbor;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.bgp.BgpWriter;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.iosxr.bgp.handler.GlobalConfigWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.cisco.rev180323.BgpNeAfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.afi.safi.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborAfiSafiConfigWriter implements BgpWriter<Config> {

    private Cli cli;

    public NeighborAfiSafiConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> id, Config config,
                                              WriteContext writeContext) throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(RWUtils.cutId(id, Bgp.class));
        Preconditions.checkArgument(bgpOptional.isPresent());
        final Global g = Preconditions.checkNotNull(bgpOptional.get().getGlobal());
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        blockingWriteAndRead(cli, id, config,
                f("router bgp %s %s", g.getConfig().getAs().getValue(), instName),
                f("neighbor %s", new String(id.firstKeyOf(Neighbor.class).getNeighborAddress().getValue())),
                f("address-family %s", GlobalAfiSafiReader.transformAfiToString(config.getAfiSafiName())),
                getReconfigurationCommand(config, false),
                "exit",
                "exit",
                "exit");
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(RWUtils.cutId(id, Bgp.class));
        Preconditions.checkArgument(bgpOptional.isPresent());
        final Global g = Preconditions.checkNotNull(bgpOptional.get().getGlobal());
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        Config reconfig = dataAfter;
        boolean delete = false;
        if ((dataBefore.getAugmentation(BgpNeAfAug.class) !=null &&
                dataBefore.getAugmentation(BgpNeAfAug.class).getSoftReconfiguration() != null) &&
                (dataAfter.getAugmentation(BgpNeAfAug.class) ==null ||
                        dataBefore.getAugmentation(BgpNeAfAug.class).getSoftReconfiguration() == null)) {
            delete = true;
            reconfig = dataBefore;
        }
        blockingWriteAndRead(cli, id, dataAfter,
                f("router bgp %s %s", g.getConfig().getAs().getValue(), instName),
                f("neighbor %s", new String(id.firstKeyOf(Neighbor.class).getNeighborAddress().getValue())),
                f("address-family %s", GlobalAfiSafiReader.transformAfiToString(dataAfter.getAfiSafiName())),
                getReconfigurationCommand(reconfig, delete),
                "exit",
                "exit",
                "exit");
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> id, Config config, WriteContext writeContext)
            throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(RWUtils.cutId(id, Bgp.class));
        if (!bgpOptional.isPresent()) {
            return;
        }
        final Global g = bgpOptional.get().getGlobal();
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        blockingDeleteAndRead(cli, id,
                f("router bgp %s %s", g.getConfig().getAs().getValue(), instName),
                f("neighbor %s", new String(id.firstKeyOf(Neighbor.class).getNeighborAddress().getValue())),
                f("no address-family %s", GlobalAfiSafiReader.transformAfiToString(config.getAfiSafiName())),
                "exit",
                "exit");
    }

    private String getReconfigurationCommand(Config config, boolean delete) {

        if (config.getAugmentation(BgpNeAfAug.class) != null &&
                config.getAugmentation(BgpNeAfAug.class).getSoftReconfiguration() != null) {
            StringBuilder command = new StringBuilder();
            if (delete) {
                command.append("no ");
            }
            command.append("soft-reconfiguration inbound");
            if (config.getAugmentation(BgpNeAfAug.class).getSoftReconfiguration().isAlways()) {
                command.append(" always");
            }
            return command.toString();
        }
        return "";
    }
}