package gscript.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public final class TitledPanel extends JPanel {

    private final JLabel label;
    private JLabel rightLabel;

    public TitledPanel(Icon icon, String title, String textAtRight, Color backgroundColor) {
        setLayout(new BorderLayout());
        setBorder(new CompoundBorder(new LineBorder(Color.GRAY, 1, false), new EmptyBorder(5, 5, 5, 5)));
        label = new JLabel(title, icon, SwingConstants.LEFT);
        label.setOpaque(false);
        add(label, BorderLayout.WEST);

        if (textAtRight != null) {
            rightLabel = new JLabel(textAtRight);
            rightLabel.setOpaque(false);
            add(rightLabel, BorderLayout.EAST);
        }

        setBackground(backgroundColor);
    }

    public TitledPanel(Icon icon, String title) {
        this(icon, title, null, null);
    }

    public void setTitle(String title) {
        label.setText(title);
    }

}
