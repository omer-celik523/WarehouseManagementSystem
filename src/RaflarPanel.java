import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RaflarPanel extends JPanel {
    private AnaEkran anaEkran;

    public RaflarPanel(AnaEkran anaEkran, DefaultTableModel rafTabloModeli, Color mainBg, Color tableBg, Color tableGridColor, Color headerTextColor) {
        this.anaEkran = anaEkran;

        setBackground(mainBg);
        setBorder(new EmptyBorder(30, 40, 30, 40));
        setLayout(new BorderLayout(0, 30));

        JLabel baslik = new JLabel("Raf Düzeni ve Ayarları");
        baslik.setFont(new Font("Segoe UI", Font.BOLD, 28));
        baslik.setForeground(headerTextColor);
        add(baslik, BorderLayout.NORTH);

        JPanel tabloPanel = new JPanel(new BorderLayout());
        tabloPanel.setOpaque(false);

        JTable rafTablo = new JTable(rafTabloModeli);
        rafTablo.setRowHeight(40);
        rafTablo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        rafTablo.setShowGrid(true);
        rafTablo.setShowVerticalLines(true);
        rafTablo.setShowHorizontalLines(true);
        rafTablo.setGridColor(tableGridColor);
        rafTablo.setIntercellSpacing(new Dimension(1, 1));
        rafTablo.setEnabled(false);
        rafTablo.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        rafTablo.getTableHeader().setBackground(new Color(39, 174, 96));
        rafTablo.getTableHeader().setForeground(Color.WHITE);
        rafTablo.setBackground(tableBg);
        rafTablo.setForeground(headerTextColor);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < 4; i++) rafTablo.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(rafTablo);
        scroll.getViewport().setBackground(tableBg);
        tabloPanel.add(scroll, BorderLayout.CENTER);

        JPanel islemGrid = new JPanel(new GridLayout(1, 2, 40, 40));
        islemGrid.setOpaque(false);
        islemGrid.setBorder(new EmptyBorder(30, 50, 30, 50));

        JButton btnRafEkle = createBigActionButton("🗄️ Yeni Raf Ekle", "Depoya yeni kapasite alanları tanımlayın", new Color(46, 204, 113));
        JButton btnRafSil = createBigActionButton("🗑️ Raf Sil", "Mevcut boş rafları sistemden kaldırın", new Color(231, 76, 60));

        btnRafEkle.addActionListener(e -> anaEkran.cokluRafEkleFormuAc());
        btnRafSil.addActionListener(e -> anaEkran.rafSilFormuAc());

        islemGrid.add(btnRafEkle);
        islemGrid.add(btnRafSil);

        JPanel splitPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        splitPanel.setOpaque(false);
        splitPanel.add(tabloPanel);
        splitPanel.add(islemGrid);

        add(splitPanel, BorderLayout.CENTER);
    }

    private JButton createBigActionButton(String t, String s, Color c) {
        JButton b = new JButton("<html><center><font size='5' color='#FFFFFF'>" + t + "</font><br><font size='3' color='#F0F0F0'>" + s + "</font></center></html>");
        b.setBackground(c);
        b.setFocusPainted(false);
        b.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        return b;
    }
}