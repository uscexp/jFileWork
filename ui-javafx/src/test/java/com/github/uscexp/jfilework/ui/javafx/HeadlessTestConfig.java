package com.github.uscexp.jfilework.ui.javafx;

/**
 * Applies the system properties required to run JavaFX in headless mode
 * using Monocle.
 *
 * <p>Must be called <em>before</em> the JavaFX toolkit is initialised, i.e.
 * in a {@code @BeforeAll} static method or a JVM argument.</p>
 *
 * <p>The same properties can also be passed directly on the command line:</p>
 * <pre>
 *   -Dglass.platform=Monocle
 *   -Dmonocle.platform=Headless
 *   -Dprism.order=sw
 *   -Dprism.text=t2k
 *   -Dheadless.geometry=1600x1200-32
 * </pre>
 *
 * <p>They are pre-configured in the {@code maven-surefire-plugin} section of
 * {@code ui-javafx/pom.xml} so you normally do not need to call this manually
 * when running {@code mvn test}.</p>
 */
public final class HeadlessTestConfig {

    private HeadlessTestConfig() {
        // utility class — no instances
    }

    /**
     * Activates headless Monocle rendering.
     * Safe to call multiple times (idempotent).
     */
    public static void apply() {
        setIfAbsent("glass.platform",      "Monocle");
        setIfAbsent("monocle.platform",    "Headless");
        setIfAbsent("prism.order",         "sw");
        setIfAbsent("prism.text",          "t2k");
        setIfAbsent("headless.geometry",   "1600x1200-32");
    }

    private static void setIfAbsent(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }
}

