package com.gymapp.ui.client.mebmership;

import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.membership.db.domain.VisitPolicy;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class ClientMembershipManualFieldsBinder {

    private final DatePicker endDatePicker;
    private final TextField remainingVisitsField;

    public ClientMembershipManualFieldsBinder(
            DatePicker endDatePicker,
            TextField remainingVisitsField
    ) {
        this.endDatePicker = endDatePicker;
        this.remainingVisitsField = remainingVisitsField;
    }

    public void update(boolean manualMode, MembershipType selectedType) {
        endDatePicker.setDisable(!manualMode);
        remainingVisitsField.setDisable(true);

        if (!manualMode) {
            endDatePicker.setValue(null);
            remainingVisitsField.clear();
            return;
        }

        boolean visitBased = selectedType != null
                && selectedType.getVisitPolicy() == VisitPolicy.LIMITED_BY_VISITS;

        remainingVisitsField.setDisable(!visitBased);

        if (!visitBased) {
            remainingVisitsField.clear();
        }
    }
}