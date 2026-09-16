package com.thirstwastaken2.dev.harness;

/**
 * What a launch asked this process to do on its own, and whether to stop it afterwards.
 *
 * <p>Both tools in this source set are meant to be run unattended by an agent that cannot type into a
 * console, and both ended up with the same pair of system properties: one naming the work
 * ({@code -Dthirstwastaken2.benchmark=standard}, {@code -Dthirstwastaken2.agent.script=<file>}) and
 * one, always the first with {@code .exit} on the end, saying the game stops when that work is done.
 * The build scripts set them from {@code -Pbenchmark} and {@code -Pagent}.
 *
 * <p>The convention around them is the part worth stating once. An unattended run has nobody to read
 * the console, so the work starts as soon as the game can accept it — once the server is up, or on
 * the client's first tick — and the last line either tool logs begins with {@code DONE}, so a script
 * can wait for the task to exit and then read one line to learn what happened. The Gradle task exits 0
 * whatever the outcome, which is why that line, and the report or {@code out.jsonl} it points at,
 * carry the status instead.
 *
 * @param argument what to run, trimmed: a profile for the benchmark, a file of requests for the agent.
 *                 Empty when the property was set without a value
 * @param stopWhenDone whether {@code <property>.exit} asked for the game to stop once it has finished
 */
public record Autorun(String argument, boolean stopWhenDone) {
    /**
     * What the launch asked for, or null when it asked for nothing. A property set to an empty value
     * still counts as asking: {@code -Pbenchmark=} means the default profile, and leaving an
     * unattended server running because of it would be worse than running it.
     */
    public static Autorun of(String property) {
        String value = System.getProperty(property);
        return value == null ? null : new Autorun(value.trim(), Boolean.getBoolean(property + ".exit"));
    }
}
