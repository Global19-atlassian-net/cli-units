package io.frinx.cli.platform.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.ComponentsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.Component;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ComponentReader implements CliOperListReader<Component, ComponentKey, ComponentBuilder> {

    private Cli cli;

    public ComponentReader(Cli cli) {
        this.cli = cli;
    }

    private static final String SH_MODULE = "sh module";
    static final Pattern SEPARATOR = Pattern.compile("^\\s+Mod\\s+", Pattern.MULTILINE);
    static final Pattern LINE = Pattern.compile("\\s*(?<index>\\d+)\\s+(?<ports>\\d+)\\s+(?<type>.+)\\s+(?<model>\\S+)\\s+(?<serial>\\S+)\\s*");
    static final Pattern LINE_HW_SW_FW = Pattern.compile("\\s*(?<index>\\d+)\\s+(?<macs>.+)\\s+(?<hw>\\S+)\\s+(?<fw>\\S+)\\s+(?<sw>\\S+)\\s+(?<status>\\S+)\\s*");

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Component> config) {
        ((ComponentsBuilder) builder).setComponent(config);
    }

    @Override
    public void readCurrentAttributes(InstanceIdentifier<Component> instanceIdentifier, ComponentBuilder componentBuilder, ReadContext readContext) {
        componentBuilder.setName(instanceIdentifier.firstKeyOf(Component.class).getName());
    }

    @Nonnull
    @Override
    public List<ComponentKey> getAllIds(@Nonnull InstanceIdentifier<Component> id, @Nonnull ReadContext context) throws ReadFailedException {
        return getComponents(blockingRead(SH_MODULE, cli, id, context));
    }

    static List<ComponentKey> getComponents(String output) {
        //Split output to tables
        String cardList = SEPARATOR.splitAsStream(output)
                .findFirst()
                .orElse("");

        return parseIds(cardList);
    }

    private static List<ComponentKey> parseIds(String cardList) {
        return ParsingUtils.parseFields(cardList, 0,
                LINE::matcher,
                m -> m.group("index"),
                ComponentKey::new);
    }
}