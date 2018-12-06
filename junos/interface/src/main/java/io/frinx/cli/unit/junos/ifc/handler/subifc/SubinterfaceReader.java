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

package io.frinx.cli.unit.junos.ifc.handler.subifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.junos.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.SubinterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceReader
    implements CliConfigListReader<Subinterface, SubinterfaceKey, SubinterfaceBuilder> {

    public static final String SEPARATOR = " unit ";

    private Cli cli;

    public SubinterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<SubinterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Subinterface> instanceIdentifier,
        @Nonnull ReadContext readContext) throws ReadFailedException {
        String id = instanceIdentifier.firstKeyOf(Interface.class).getName();

        return parseSubinterfaceIds(
            blockingRead(InterfaceReader.SHOW_INTERFACES, cli, instanceIdentifier, readContext), id);
    }

    private static List<SubinterfaceKey> parseSubinterfaceIds(String output, String ifcName) {
        return InterfaceReader.parseAllInterfaceIds(output)
            // Now exclude interfaces
            .stream()
            .filter(InterfaceReader::isSubinterface)
            .map(InterfaceKey::getName)
            .filter(subifcName -> subifcName.startsWith(ifcName))
            .map(name -> name.substring(name.lastIndexOf(SEPARATOR) + SEPARATOR.length()))
            .map(subifcIndex -> new SubinterfaceKey(Long.valueOf(subifcIndex)))
            .collect(Collectors.toList());
    }

    static String getSubinterfaceName(InstanceIdentifier<?> id) {
        InterfaceKey ifcKey = id.firstKeyOf(Interface.class);
        SubinterfaceKey subKey = id.firstKeyOf(Subinterface.class);

        return ifcKey.getName() + SEPARATOR + subKey.getIndex().toString();
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Subinterface> list) {
        ((SubinterfacesBuilder) builder).setSubinterface(list);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Subinterface> id,
        @Nonnull SubinterfaceBuilder builder, @Nonnull ReadContext readContext) {
        builder.setIndex(id.firstKeyOf(Subinterface.class).getIndex());
    }
}