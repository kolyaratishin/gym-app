package com.gymapp.ui.common;

import com.gymapp.util.GymAppUtils;
import javafx.fxml.FXMLLoader;
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
        LoadedView<T> loadedView = loadView(fxmlPath, controllerConfigurer);

        Stage stage = new Stage();
        GymAppUtils.applyResponsiveStageSize(stage, widthRatio, heightRatio);
        stage.setTitle(title);
        stage.setScene(loadedView.scene());
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
        LoadedView<T> loadedView = loadView(fxmlPath, controllerConfigurer);

        Stage stage = new Stage();
        GymAppUtils.applyResponsiveStageSize(stage, widthRatio, heightRatio);
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(loadedView.scene());
        stage.setResizable(false);
        stage.showAndWait();

        return loadedView.controller();
    }

    private static <T> LoadedView<T> loadView(
            String fxmlPath,
            Consumer<T> controllerConfigurer
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource(fxmlPath));

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(ViewLoader.class.getResource(APP_CSS).toExternalForm());

            T controller = loader.getController();

            if (controllerConfigurer != null) {
                controllerConfigurer.accept(controller);
            }

            return new LoadedView<>(scene, controller);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load view: " + fxmlPath, e);
        }
    }

    private record LoadedView<T>(Scene scene, T controller) {
    }
}