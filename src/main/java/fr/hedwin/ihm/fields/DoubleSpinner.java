package fr.hedwin.ihm.fields;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DoubleSpinner extends JSpinner {

    private static final long serialVersionUID = 1L;
    private static final double STEP_RATIO = 0.01;

    private SpinnerNumberModel model;

    public DoubleSpinner() {
        super();
        // Model setup
        model = new SpinnerNumberModel(0.0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0.01);
        this.setModel(model);

        // Step recalculation
        this.addChangeListener(e -> {
            Double value = getDouble();
            // Steps are sensitive to the current magnitude of the value
            long magnitude = Math.round(Math.log10(value));
            double stepSize = STEP_RATIO * Math.pow(10, magnitude);
            model.setStepSize(stepSize);
        });
    }

    public DoubleSpinner(double value){
        this();
        setValue(value);
    }

    /**
     * Returns the current value as a Double
     */
    public Double getDouble() {
        return (Double)getValue();
    }

}
