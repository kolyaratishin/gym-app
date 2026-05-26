package com.gymapp.ui.common;

public final class DialogService {

    private DialogService() {
    }

    public static boolean showConfirm(String title, String message) {
        ConfirmDialogController controller =
                ViewLoader.showModalAndReturnController(
                        "/fxml/common/ConfirmDialogView.fxml",
                        title,
                        0.35,
                        0.35,
                        c -> c.setData(title, message)
                );

        return controller.isConfirmed();
    }

    public static void showInfo(String title, String message) {
        ViewLoader.showModalAndReturnController(
                "/fxml/common/InfoDialogView.fxml",
                title,
                0.35,
                0.35,
                (InfoDialogController controller) -> controller.setData(title, message)
        );
    }

    public static void showInfoDialog(String title, String message) {
        ViewLoader.showModalAndReturnController(
                "/fxml/common/InfoDialogView.fxml",
                title,
                0.35,
                0.35,
                (InfoDialogController controller) ->
                        controller.setData(title, message)
        );
    }
}