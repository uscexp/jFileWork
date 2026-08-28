package com.github.uscexp.jfilework.ui.javafx;

import org.testfx.framework.junit5.ApplicationTest;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base class for all JavaFX UI tests.
 *
 * <p>Activates headless mode via {@link HeadlessTestConfig} before the
 * JavaFX toolkit starts, so tests run without a physical display (e.g. in CI).</p>
 *
 * <p>Subclasses override {@link #start(Stage)} to load their FXML / scene.</p>
 *
 * <pre>{@code
 * class MyControllerUiTest extends FxTestBase {
 *     @Override
 *     public void start(Stage stage) throws Exception {
 *         FXMLLoader loader = new FXMLLoader(getClass().getResource("my-view.fxml"));
 *         stage.setScene(new Scene(loader.load()));
 *         stage.show();
 *     }
 *
 *     @Test
 *     void myButton_shouldBeVisible() {
 *         verifyThat("#myButton", isVisible());
 *     }
 * }
 * }</pre>
 */
public abstract class FxTestBase extends ApplicationTest {

    @BeforeAll
    static void applyHeadlessConfig() {
        HeadlessTestConfig.apply();
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Subclasses provide their own scene setup.
    }
}

