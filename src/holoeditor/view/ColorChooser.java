package holoeditor.view;

import javax.swing.*;
import java.awt.*;

/**
 * Displays color swatch buttons and notifies Delegate when one is clicked.
 * The Delegate must call `setColor` to update the button display states.
 */
public class ColorChooser extends JPanel {
    interface Delegate {
        void colorChanged(boolean color);
    }

    JButton blackButton;
    JButton whiteButton;

    public ColorChooser(Delegate delegate) {
        setPreferredSize(new Dimension(100 , 20));

        blackButton = new JButton(new SwatchIcon(Color.black));
        blackButton.addActionListener(e -> delegate.colorChanged(false));
        add(blackButton);

        whiteButton = new JButton(new SwatchIcon(Color.white));
        whiteButton.addActionListener(e -> delegate.colorChanged(true));
        whiteButton.setSelected(true);
        add(whiteButton);
    }

    public void setColor(boolean color) {
        blackButton.setSelected(!color);
        whiteButton.setSelected(color);
    }

    class SwatchIcon implements Icon {
        Color color;
        public SwatchIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x, y, getIconWidth(), getIconHeight());
        }

        @Override
        public int getIconWidth() { return 25; }

        @Override
        public int getIconHeight() { return 25; }
    }
}
