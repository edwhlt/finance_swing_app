package fr.hedwin.ihm.components;

import fr.hedwin.objects.Tiers;

import javax.swing.*;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class FormFrame<T> extends JDialog {

    protected Consumer<T> onSubmit;
    protected final JFrame parent;

    public FormFrame(JFrame parent, String name, Consumer<T> onSubmit){
        super(parent, name, true);
        this.parent = parent;
        this.onSubmit = onSubmit;
    }

}
