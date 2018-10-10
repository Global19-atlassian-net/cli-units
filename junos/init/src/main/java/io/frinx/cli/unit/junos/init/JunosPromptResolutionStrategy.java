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

package io.frinx.cli.unit.junos.init;

import io.frinx.cli.io.PromptResolutionStrategy;
import io.frinx.cli.io.Session;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class JunosPromptResolutionStrategy implements PromptResolutionStrategy {

    private static final int DEFAULT_TIME_TO_WAIT = 1;
    private static final int MAX_TIME_TO_WAIT = 10;

    private static final Pattern JUNOS_GUIDANCE_PATTERN = Pattern.compile("^\\[edit.*\\]$");

    private static final PromptResolutionStrategy DEFAULT_INSTANCE = new JunosPromptResolutionStrategy();

    private JunosPromptResolutionStrategy() {
    }

    public static PromptResolutionStrategy getInstance() {
        return DEFAULT_INSTANCE;
    }

    /**
     * Simple resolution strategy writing newline and expecting a prompt to be printed.<br>
     * In configuration mode of Junos, it need to exclude a line of guidance from output.
     */
    @Override
    @SuppressWarnings({"IllegalCatch", "ConstantName"})
    public String resolvePrompt(Session session, String newline) {
        try {
            int waitTime = DEFAULT_TIME_TO_WAIT;
            String lastRead = "";

            while (waitTime < MAX_TIME_TO_WAIT) {
                session.write(newline + newline).toCompletableFuture().get();
                lastRead = session.readUntilTimeout(waitTime);
                List<String> split = Arrays.stream(lastRead.split(newline))
                        .map(String::trim)
                        .filter(line -> !"".equals(line))
                        .filter(line -> !JUNOS_GUIDANCE_PATTERN.matcher(line).matches())
                        .collect(Collectors.toList());

                if (split.size() == 2 && split.get(0).equals(split.get(1))) {
                    return split.get(0);
                }

                waitTime++;
            }

            throw new IllegalStateException("Unable to parse prompt in " + MAX_TIME_TO_WAIT + " attempts, last output"
                    + " from device: " + lastRead);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException | RuntimeException e) {
            LOG.warn("{}: Unable to perform prompt resolution", session, e);
            throw new IllegalStateException(session + ": Unable to perform prompt resolution", e);
        }
    }
}