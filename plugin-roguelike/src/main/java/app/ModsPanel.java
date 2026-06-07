package app;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

public class ModsPanel extends JPanel {

    private final ModManager modManager;
    private final Runnable onBack;

    private final JPanel listPanel = new JPanel();
    private final List<JCheckBox> checkBoxes = new ArrayList<>();

    public ModsPanel(ModManager modManager, Runnable onBack) {
        this.modManager = modManager;
        this.onBack = onBack;

        setLayout(new BorderLayout());
        setBackground(new Color(18, 18, 24));

        JLabel title = new JLabel("Моды");
        title.setFont(new Font("Dialog", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 10, 20));
        add(title, BorderLayout.NORTH);

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(18, 18, 24));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(18, 18, 24));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(18, 18, 24));
        JButton back = new JButton("Назад");
        back.addActionListener(e -> onBack.run());
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        listPanel.removeAll();
        checkBoxes.clear();

        List<ModEntry> entries = modManager.getEntries();

        if (entries.isEmpty()) {
            JLabel empty = new JLabel("Папка mods пуста. Положи туда .jar файлы модов.");
            empty.setForeground(Color.WHITE);
            empty.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
            listPanel.add(empty);
        } else {
            for (ModEntry entry : entries) {
                JCheckBox box = new JCheckBox(entry.getDisplayName(), entry.isEnabled());
                box.setForeground(Color.WHITE);
                box.setBackground(new Color(18, 18, 24));
                box.setAlignmentX(LEFT_ALIGNMENT);
                box.addActionListener(e -> modManager.setEnabled(entry.getJarName(), box.isSelected()));
                box.setMaximumSize(new Dimension(700, 30));

                JLabel fileLabel = new JLabel("  " + entry.getJarName());
                fileLabel.setForeground(new Color(180, 180, 180));
                fileLabel.setAlignmentX(LEFT_ALIGNMENT);

                listPanel.add(box);
                listPanel.add(fileLabel);
                listPanel.add(Box.createVerticalStrut(10));

                checkBoxes.add(box);
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }
}