package com.gymapp.ui.common;

import com.gymapp.audit.ErrorHandler;
import com.gymapp.audit.ErrorLogMessages;
import com.gymapp.util.GymAppUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.Consumer;

public final class ViewLoader {

    private static final String APP_CSS = "/css/app.css";

    private ViewLoader() {
    }

    public static <T> Stage openWindow(
            String fxmlPath,
            String title,
            double widthRatio,
            double heightRatio,
            Consumer<T> controllerConfigurer
    ) {
        LoadedView<T> loadedView =
                load(fxmlPath, controllerConfigurer);

        Stage stage = new Stage();
        GymAppUtils.applyResponsiveStageSize(
                stage,
                widthRatio,
                heightRatio
        );

        stage.setTitle(title);
        stage.setScene(createScene(loadedView.root()));
        stage.show();

        return stage;
    }

    public static <T> T showModalAndReturnController(
            String fxmlPath,
            String title,
            double widthRatio,
            double heightRatio,
            Consumer<T> controllerConfigurer
    ) {
        LoadedView<T> loadedView =
                load(fxmlPath, controllerConfigurer);

        Stage stage = new Stage();

        GymAppUtils.applyResponsiveStageSize(
                stage,
                widthRatio,
                heightRatio
        );

        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(createScene(loadedView.root()));
        stage.setResizable(false);
        stage.showAndWait();

        return loadedView.controller();
    }

    public static Parent loadContent(String fxmlPath) {
        return load(fxmlPath, null).root();
    }

    private static <T> LoadedView<T> load(
            String fxmlPath,
            Consumer<T> controllerConfigurer
    ) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(ViewLoader.class.getResource(fxmlPath));

            Parent root = loader.load();

            T controller = loader.getController();

            if (controllerConfigurer != null) {
                controllerConfigurer.accept(controller);
            }

            return new LoadedView<>(root, controller);

        } catch (Exception e) {
            ErrorHandler.logOnly(
                    ErrorLogMessages.VIEW_LOADER_LOAD_VIEW,
                    "fxmlPath=" + fxmlPath,
                    e
            );

            throw new RuntimeException(
                    "Failed to load view: " + fxmlPath,
                    e
            );
        }
    }

    private static Scene createScene(Parent root) {
        Scene scene = new Scene(root);

        scene.getStylesheets().add(
                ViewLoader.class
                        .getResource(APP_CSS)
                        .toExternalForm()
        );

        return scene;
    }

    private record LoadedView<T>(
            Parent root,
            T controller
    ) {
    }
}