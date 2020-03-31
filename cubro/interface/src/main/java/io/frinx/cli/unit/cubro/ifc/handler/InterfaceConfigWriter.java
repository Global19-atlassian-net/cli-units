/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.cubro.ifc.handler;

import com.google.common.base.Preconditions;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cubro.extension.rev200317.IfCubroAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE_CUBRO = "interface {$data.name}\n"
            + "{% if ($enabled) %}shutdown\n"
            + "{% else %}no shutdown\n{% endif %}"
            + "{% if ($desc) %}interface comment {$desc}\n{% endif %}"
            + "{% if ($mtu) %}mtu {$mtu}\n{% endif %}"
            + "{% if ($rx) %}rx on \n{% endif %}"
            + "{% if ($speed) %}speed {$speed}\n{% endif %}"
            + "{% if ($innerhash) %}innerhash enable\n{% endif %}"
            + "{% if ($inneracl) %}inneracl enable\n{% endif %}"
            + "{% if ($vxlanterminated) %}vxlanterminated enable\n{% endif %}";

    private static final String DELETE_TEMPLATE_CUBRO = "interface {$data.name}\n"
            + "shutdown\n"
            + "mtu 1500\n"
            + "rx off\n"
            + "no speed\n"
            + "no innerhash enable\n"
            + "no inneracl enable\n"
            + "no vxlanterminated\n";


    private Cli cli;

    public InterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config data,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        // cubro allows creating of physical interfaces
        blockingWriteAndRead(cli, id, data, updateTemplate(null, data));

    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        Preconditions.checkArgument(dataBefore.getType()
                        .equals(dataAfter.getType()), "Changing interface type is not permitted. Before: %s, After: %s",
                dataBefore.getType(), dataAfter.getType());
        try {
            blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataBefore, dataAfter));
        } catch (WriteFailedException e) {
            throw new WriteFailedException.UpdateFailedException(id, dataBefore, dataAfter, e);
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String deleteTemp = fT(DELETE_TEMPLATE_CUBRO, "data", config);
        if (config.getAugmentation(IfCubroAug.class) != null
                && config.getAugmentation(IfCubroAug.class).getElag() != null) {
            for (short elag: config.getAugmentation(IfCubroAug.class).getElag()) {
                deleteTemp += f("no elag %d\n", elag);
            }
        }
        // cubro allows deleting of physical interfaces
        blockingDeleteAndRead(cli, id, deleteTemp.trim());
    }

    private String updateTemplate(Config before, Config after) {
        IfCubroAug cubroAug = after.getAugmentation(IfCubroAug.class);
        String writeTemp;
        if (cubroAug != null) {
            writeTemp = fT(WRITE_TEMPLATE_CUBRO, "before", before, "data", after,
                    "enabled", (after.isEnabled() != null && !after.isEnabled()) ? Chunk.TRUE : null,
                    "desc", after.getDescription(),
                    "mtu", after.getMtu(),
                    "rx", (cubroAug.isRx() != null && cubroAug.isRx()) ? Chunk.TRUE : null,
                    "speed", cubroAug.getSpeed(),
                    "innerhash", (cubroAug.isInnerhash() != null && cubroAug.isInnerhash()) ? Chunk.TRUE : null,
                    "inneracl", (cubroAug.isInneracl() != null && cubroAug.isInneracl()) ? Chunk.TRUE : null,
                    "vxlanterminated", (cubroAug.isVxlanterminated() != null && cubroAug.isVxlanterminated())
                            ? Chunk.TRUE : null);
            if (cubroAug.getElag() != null) {
                for (short elag: cubroAug.getElag()) {
                    writeTemp += f("elag %d\n", elag);
                }
            }
        } else {
            writeTemp = fT(WRITE_TEMPLATE_CUBRO, "data", after,
                    "enabled", (after.isEnabled() != null && !after.isEnabled()) ? Chunk.TRUE : null,
                    "desc", after.getDescription(),
                    "mtu", after.getMtu());
        }
        return writeTemp.trim();
    }
}