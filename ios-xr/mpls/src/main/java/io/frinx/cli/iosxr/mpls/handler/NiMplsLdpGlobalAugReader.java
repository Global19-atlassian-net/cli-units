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

package io.frinx.cli.iosxr.mpls.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.mpls.MplsReader;
import io.frinx.cli.io.Cli;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.extension.rev180822.NiMplsLdpGlobalAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.extension.rev180822.NiMplsLdpGlobalAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.mpls.ldp.global.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NiMplsLdpGlobalAugReader implements MplsReader.MplsConfigReader<NiMplsLdpGlobalAug,
    NiMplsLdpGlobalAugBuilder> {

    private Cli cli;

    private static final String SH_LDP_INT = "show running-config mpls ldp";

    public NiMplsLdpGlobalAugReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<NiMplsLdpGlobalAug> instanceIdentifier,
            @Nonnull NiMplsLdpGlobalAugBuilder builder,
            @Nonnull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SH_LDP_INT, cli, instanceIdentifier, readContext);
        parseEnabled(output, builder);
    }

    @VisibleForTesting
    public static void parseEnabled(String output, NiMplsLdpGlobalAugBuilder configBuilder) {
        if (!output.isEmpty()) {
            configBuilder.setEnabled(true);
        }
    }

    @Override
    public void merge(Builder<? extends DataObject> arg0, NiMplsLdpGlobalAug arg1) {
        ((ConfigBuilder) arg0).addAugmentation(NiMplsLdpGlobalAug.class, arg1);
    }

}
