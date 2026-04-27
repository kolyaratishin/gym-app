package com.gymapp.infrastructure.util;

import javafx.stage.Stage;

public class GymAppUtils {
    public static void applyResponsiveStageSize(Stage stage, double widthPercent, double heightPercent) {
        javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();

        double width = bounds.getWidth() * widthPercent;
        double height = bounds.getHeight() * heightPercent;

        stage.setWidth(width);
        stage.setHeight(height);
        stage.setMinWidth(width * 0.8);
        stage.setMinHeight(height * 0.8);
    }
}
