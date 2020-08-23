package ui;

import store.Store;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;

public class GlobalFeaturePane extends JPanel {

    public GlobalFeaturePane(Store store){
        this.setBorder(BorderFactory.createTitledBorder(""));
        ItemListener featureListener = e -> {
            store.metadata.getFeatureToggler().getGlobalFeatures().put(((JCheckBox) e.getItem()).getText(), e.getStateChange() == ItemEvent.SELECTED);
            store.marshellMetadata();
        };
        for (Map.Entry<String, Boolean> entry : store.metadata.getFeatureToggler().getGlobalFeatures().entrySet()) {
            JCheckBox cb = new JCheckBox(entry.getKey(), entry.getValue());
            cb.addItemListener(featureListener);
            this.add(cb);
        }
    }
}
