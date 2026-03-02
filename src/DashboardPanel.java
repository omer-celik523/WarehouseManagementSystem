import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.XChartPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Veri Görselleştirme (Data Visualization) katmanımız.
// Arayüzdeki ham verileri istatistiksel grafiklere dökmek için XChart kütüphanesini kullandım.
public class DashboardPanel extends JPanel {

    // Bağımlılık Enjeksiyonu: AnaEkran'ın bir referansını alarak ortak metodlara ve renklere erişiyorum.
    private AnaEkran anaEkran;
    private JLabel lblToplamUrun, lblToplamRaf, lblBosKapasite, lblDolulukOrani, lblToplamDeger;
    private DefaultTableModel sonUrunlerModel, kritikStokModel;
    private JPanel grafikPaneli;

    public DashboardPanel(AnaEkran anaEkran) {
        this.anaEkran = anaEkran;
        setLayout(new BorderLayout());
        setBackground(anaEkran.getMainBg());

        JPanel scrollIciPanel = new JPanel();
        scrollIciPanel.setLayout(new BoxLayout(scrollIciPanel, BoxLayout.Y_AXIS));
        scrollIciPanel.setBackground(anaEkran.getMainBg());
        scrollIciPanel.setBorder(new EmptyBorder(35, 45, 35, 45));

        JPanel ustPanel = new JPanel(new BorderLayout());
        ustPanel.setOpaque(false);
        ustPanel.setMaximumSize(new Dimension(3000, 40));
        JLabel baslik = new JLabel("Ana Panel - Genel Bakış");
        baslik.setFont(new Font("Segoe UI", Font.BOLD, 28));
        baslik.setForeground(anaEkran.getHeaderTextColor());
        ustPanel.add(baslik, BorderLayout.WEST);

        scrollIciPanel.add(ustPanel);
        scrollIciPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        JPanel statsWrapper = new JPanel(new BorderLayout());
        statsWrapper.setOpaque(false);
        statsWrapper.setMaximumSize(new Dimension(3000, 120));
        statsWrapper.setPreferredSize(new Dimension(1000, 120));

        JPanel statsPanel = new JPanel(new GridLayout(1, 5, 15, 0));
        statsPanel.setOpaque(false);
        lblToplamUrun = new JLabel("0 Çeşit");
        lblToplamRaf = new JLabel("0 Raf");
        lblBosKapasite = new JLabel("0 Birim");
        lblDolulukOrani = new JLabel("% 0");
        lblToplamDeger = new JLabel("0,00 ₺");

        // Custom yazdığım Gradient (Geçişli renk) Component'ini kullanarak tasarımı zenginleştirdim.
        GradientCard cardUrun = new GradientCard("Sistemdeki Ürün", lblToplamUrun, new Color(52, 152, 219), new Color(116, 185, 255));
        GradientCard cardRaf = new GradientCard("Toplam Raf", lblToplamRaf, new Color(39, 174, 96), new Color(85, 239, 196));
        GradientCard cardBos = new GradientCard("Boş Kapasite", lblBosKapasite, new Color(231, 76, 60), new Color(255, 118, 117));
        GradientCard cardDoluluk = new GradientCard("Doluluk Oranı", lblDolulukOrani, new Color(155, 89, 182), new Color(210, 180, 222));
        GradientCard cardDeger = new GradientCard("Toplam Depo Değeri", lblToplamDeger, new Color(243, 156, 18), new Color(241, 196, 15));

        statsPanel.add(cardUrun);
        statsPanel.add(cardRaf);
        statsPanel.add(cardBos);
        statsPanel.add(cardDoluluk);
        statsPanel.add(cardDeger);

        addCardBubble(cardUrun, lblToplamUrun);
        addCardBubble(cardRaf, lblToplamRaf);
        addCardBubble(cardBos, lblBosKapasite);
        addCardBubble(cardDeger, lblToplamDeger);

        statsWrapper.add(statsPanel, BorderLayout.CENTER);
        scrollIciPanel.add(statsWrapper);
        scrollIciPanel.add(Box.createRigidArea(new Dimension(0, 35)));

        // Grafikler için kapsayıcı panel (Layout'u bozulmasın diye boyutlarını kısıtlıyoruz)
        grafikPaneli = new JPanel(new GridLayout(1, 2, 30, 0));
        grafikPaneli.setOpaque(false);
        grafikPaneli.setMaximumSize(new Dimension(3000, 360));
        grafikPaneli.setPreferredSize(new Dimension(1000, 360));
        scrollIciPanel.add(grafikPaneli);
        scrollIciPanel.add(Box.createRigidArea(new Dimension(0, 35)));

        JPanel altPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        altPanel.setOpaque(false);
        altPanel.setMaximumSize(new Dimension(3000, 300));
        altPanel.setPreferredSize(new Dimension(1000, 300));

        JPanel solTabloPanel = new JPanel(new BorderLayout(0, 10));
        solTabloPanel.setOpaque(false);
        JLabel solBaslik = new JLabel("Son Eklenenler (Son 5)");
        solBaslik.setFont(new Font("Segoe UI", Font.BOLD, 20));
        solBaslik.setForeground(anaEkran.getHeaderTextColor());
        solTabloPanel.add(solBaslik, BorderLayout.NORTH);

        String[] solKolonlar = {"Ürün Adı", "Kategori", "Stok"};
        sonUrunlerModel = new DefaultTableModel(null, solKolonlar) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        JTable sonUrunlerTablo = anaEkran.createStyledTable(sonUrunlerModel, anaEkran.isDarkMode() ? new Color(60,60,60) : new Color(236, 240, 241));

        JPanel sagTabloPanel = new JPanel(new BorderLayout(0, 10));
        sagTabloPanel.setOpaque(false);
        JLabel sagBaslik = new JLabel("🚨 Kritik Stok Uyarıları");
        sagBaslik.setFont(new Font("Segoe UI", Font.BOLD, 20));
        sagBaslik.setForeground(new Color(231, 76, 60));
        sagTabloPanel.add(sagBaslik, BorderLayout.NORTH);

        String[] sagKolonlar = {"Ürün Adı", "Kalan Stok", "Kritik Eşik"};
        kritikStokModel = new DefaultTableModel(null, sagKolonlar) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        JTable kritikStokTablo = anaEkran.createStyledTable(kritikStokModel, anaEkran.isDarkMode() ? new Color(100,40,40) : new Color(250, 219, 216));

        DefaultTableCellRenderer dashPaddedRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBorder(new EmptyBorder(0, 10, 0, 10));
                if (table.getModel() == sonUrunlerModel && column == 2) l.setHorizontalAlignment(JLabel.CENTER);
                else if (table.getModel() == kritikStokModel && (column == 1 || column == 2)) l.setHorizontalAlignment(JLabel.CENTER);
                else l.setHorizontalAlignment(JLabel.LEFT);
                return l;
            }
        };

        for (int i = 0; i < sonUrunlerTablo.getColumnCount(); i++) sonUrunlerTablo.getColumnModel().getColumn(i).setCellRenderer(dashPaddedRenderer);
        for (int i = 0; i < kritikStokTablo.getColumnCount(); i++) kritikStokTablo.getColumnModel().getColumn(i).setCellRenderer(dashPaddedRenderer);

        anaEkran.addCellClickBubble(sonUrunlerTablo);
        anaEkran.addCellClickBubble(kritikStokTablo);

        JScrollPane spSon = new JScrollPane(sonUrunlerTablo); spSon.getViewport().setBackground(anaEkran.getTableBg());
        JScrollPane spKritik = new JScrollPane(kritikStokTablo); spKritik.getViewport().setBackground(anaEkran.getTableBg());

        solTabloPanel.add(spSon, BorderLayout.CENTER);
        sagTabloPanel.add(spKritik, BorderLayout.CENTER);

        altPanel.add(solTabloPanel);
        altPanel.add(sagTabloPanel);
        scrollIciPanel.add(altPanel);

        JScrollPane anaScroll = new JScrollPane(scrollIciPanel);
        anaScroll.setBorder(null);
        anaScroll.getVerticalScrollBar().setUnitIncrement(16);
        anaScroll.getViewport().setBackground(anaEkran.getMainBg());

        add(anaScroll, BorderLayout.CENTER);
    }

    // Sistem verileri değiştikçe AnaEkran tarafından tetiklenen (Reactivity) Merkezi Güncelleme Metodu
    public void dashboardGuncelle() {
        lblToplamUrun.setText(anaEkran.getUrunManager().getUrunler().size() + " Çeşit");
        lblToplamRaf.setText(anaEkran.getRafManager().getRafSayisi() + " Raf");

        int bos = anaEkran.getRafManager().getToplamKapasite(), top = 0;
        double genelToplamDeger = 0;

        lblBosKapasite.setText(bos + " Birim");

        for(Urun u : anaEkran.getUrunManager().getUrunler()) {
            top += u.getMiktar();
            genelToplamDeger += (u.getMiktar() * u.getBirimFiyat());
        }

        // Genel depo doluluk oranı için UX Düzeltmesi (1 ürün bile varsa %1 görünsün)
        int genelDoluluk = (bos + top > 0) ? (top * 100 / (bos + top)) : 0;
        if (top > 0 && genelDoluluk == 0) {
            genelDoluluk = 1;
        }
        lblDolulukOrani.setText("% " + genelDoluluk);

        String tamDeger = String.format("%,.2f ₺", genelToplamDeger);
        lblToplamDeger.setText(tamDeger);

        if(sonUrunlerModel != null) {
            sonUrunlerModel.setRowCount(0);
            int limit = Math.min(5, anaEkran.getUrunManager().getUrunler().size());
            for(int i = anaEkran.getUrunManager().getUrunler().size()-1; i >= anaEkran.getUrunManager().getUrunler().size()-limit; i--) {
                Urun u = anaEkran.getUrunManager().getUrunler().get(i);
                sonUrunlerModel.addRow(new Object[]{u.getAd(), u.getKategori(), u.getMiktar()});
            }
        }
        if(kritikStokModel != null) {
            kritikStokModel.setRowCount(0);
            for(Urun u : anaEkran.getUrunManager().getUrunler())
                if(u.getMiktar() <= u.getKritikEsik())
                    kritikStokModel.addRow(new Object[]{u.getAd(), u.getMiktar(), "Sınır: " + u.getKritikEsik()});
        }

        // XCHART GRAFİKLERİNİN DİNAMİK OLARAK ÇİZİLDİĞİ KISIM
        if (grafikPaneli != null) {
            grafikPaneli.removeAll();

            // 1. PASTA GRAFİĞİ (Kategori Dağılımı)
            // Kategorileri gruplamak için HashMap kullanarak veriyi analitik bir formata dönüştürüyoruz.
            Map<String, Integer> katMap = new HashMap<>();
            for (Urun u : anaEkran.getUrunManager().getUrunler()) {
                katMap.put(u.getKategori(), katMap.getOrDefault(u.getKategori(), 0) + u.getMiktar());
            }

            PieChart pieChart = new PieChartBuilder().width(330).height(300).build();
            pieChart.getStyler().setChartTitleVisible(false);
            pieChart.getStyler().setChartBackgroundColor(anaEkran.getSidebarBg());
            pieChart.getStyler().setPlotBackgroundColor(anaEkran.getSidebarBg());
            pieChart.getStyler().setChartFontColor(anaEkran.getHeaderTextColor());
            pieChart.getStyler().setLegendVisible(false);
            pieChart.getStyler().setDecimalPattern("'%'0.0");
            pieChart.getStyler().setLabelType(org.knowm.xchart.style.PieStyler.LabelType.Value);
            pieChart.getStyler().setToolTipsEnabled(true);
            pieChart.getStyler().setToolTipBackgroundColor(anaEkran.getSidebarBg());
            pieChart.getStyler().setToolTipBorderColor(new Color(41, 128, 185)); // ACTIVE_BTN_BG
            pieChart.getStyler().setToolTipHighlightColor(new Color(41, 128, 185));

            Color[] pieColors = new Color[]{
                    new Color(52, 152, 219), new Color(46, 204, 113), new Color(155, 89, 182),
                    new Color(241, 196, 15), new Color(231, 76, 60), new Color(52, 73, 94),
                    new Color(26, 188, 156), new Color(211, 84, 0), new Color(149, 165, 166),
                    new Color(22, 160, 133), new Color(41, 128, 185), new Color(142, 68, 173)
            };
            pieChart.getStyler().setSeriesColors(pieColors);

            JPanel customLegendPanel = new JPanel();
            customLegendPanel.setLayout(new BoxLayout(customLegendPanel, BoxLayout.Y_AXIS));
            customLegendPanel.setBackground(anaEkran.getSidebarBg());
            customLegendPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            int colorIndex = 0;
            if (katMap.isEmpty()) {
                pieChart.addSeries("Veri Yok", 1);
            } else {
                double toplamStok = 0;
                for (int miktar : katMap.values()) {
                    toplamStok += miktar;
                }

                for (Map.Entry<String, Integer> entry : katMap.entrySet()) {
                    double yuzdeOrani = (entry.getValue() / toplamStok) * 100.0;
                    pieChart.addSeries(entry.getKey(), yuzdeOrani);

                    JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
                    itemPanel.setOpaque(false);

                    JLabel colorBox = new JLabel();
                    colorBox.setOpaque(true);
                    colorBox.setBackground(pieColors[colorIndex % pieColors.length]);
                    colorBox.setPreferredSize(new Dimension(14, 14));

                    JLabel lblName = new JLabel(entry.getKey());
                    lblName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    lblName.setForeground(anaEkran.getHeaderTextColor());

                    itemPanel.add(colorBox);
                    itemPanel.add(lblName);
                    customLegendPanel.add(itemPanel);

                    colorIndex++;
                }
            }

            XChartPanel<PieChart> pieCanvas = new XChartPanel<>(pieChart);

            JPanel legendWrapper = new JPanel(new BorderLayout());
            legendWrapper.setBackground(anaEkran.getSidebarBg());
            legendWrapper.add(customLegendPanel, BorderLayout.NORTH);

            JScrollPane legendScroll = new JScrollPane(legendWrapper);
            legendScroll.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, anaEkran.getTableGridColor()));
            legendScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            legendScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            legendScroll.setPreferredSize(new Dimension(210, 0));
            legendScroll.getVerticalScrollBar().setUnitIncrement(16);
            legendScroll.getHorizontalScrollBar().setUnitIncrement(16);

            JPanel pieContent = new JPanel(new BorderLayout());
            pieContent.setOpaque(false);
            pieContent.add(pieCanvas, BorderLayout.CENTER);
            pieContent.add(legendScroll, BorderLayout.EAST);

            JPanel pieWrapper = new JPanel(new BorderLayout(0, 10));
            pieWrapper.setOpaque(false);
            JLabel lblPieBaslik = new JLabel("Kategorilere Göre Stok Dağılımı");
            lblPieBaslik.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblPieBaslik.setForeground(anaEkran.getHeaderTextColor());
            pieWrapper.add(lblPieBaslik, BorderLayout.NORTH);
            pieWrapper.add(pieContent, BorderLayout.CENTER);


            // 2. BAR GRAFİĞİ (Raf Doluluk Oranları)
            List<Object[]> raflar = anaEkran.getRafDao().getRafDetaylari();

            // UI/UX Detayı: Eğer 100 tane raf eklenirse grafik üst üste binip çirkinleşmesin diye
            // raf sayısına göre dinamik (dynamic width) bir genişlik hesaplıyoruz.
            int chartGenislik = Math.max(400, raflar.size() * 55);

            CategoryChart barChart = new CategoryChartBuilder().width(chartGenislik).height(300).build();
            barChart.getStyler().setChartTitleVisible(false);

            barChart.getStyler().setChartBackgroundColor(anaEkran.getSidebarBg());
            barChart.getStyler().setPlotBackgroundColor(anaEkran.getSidebarBg());
            barChart.getStyler().setLegendVisible(false);
            barChart.getStyler().setChartFontColor(anaEkran.getHeaderTextColor());
            barChart.getStyler().setPlotGridLinesColor(anaEkran.getTableGridColor());
            barChart.getStyler().setAxisTickLabelsColor(anaEkran.getTextColor());
            barChart.getStyler().setXAxisLabelRotation(45);
            barChart.getStyler().setYAxisDecimalPattern("'%'0");
            barChart.getStyler().setToolTipsEnabled(true);
            barChart.getStyler().setToolTipBackgroundColor(anaEkran.getSidebarBg());
            barChart.getStyler().setToolTipBorderColor(new Color(41, 128, 185));
            barChart.getStyler().setToolTipHighlightColor(new Color(41, 128, 185));

            List<String> xData = new ArrayList<>();
            List<Integer> yData = new ArrayList<>();

            for (int i = 0; i < raflar.size(); i++) {
                xData.add((i + 1) + ". Raf");
                String dolulukStr = raflar.get(i)[3].toString().replace("%", "");
                yData.add(Integer.parseInt(dolulukStr));
            }

            if (xData.isEmpty()) { xData.add("Raf Yok"); yData.add(0); }
            barChart.addSeries("Doluluk Yüzdesi", xData, yData);

            XChartPanel<CategoryChart> barPanel = new XChartPanel<>(barChart);

            // Uzayan grafiği sağa doğru kaydırabilmek için ScrollPane içine sarıyoruz.
            JScrollPane barScroll = new JScrollPane(barPanel);
            barScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            barScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            barScroll.setBorder(null);
            barScroll.getHorizontalScrollBar().setUnitIncrement(16);

            JPanel barWrapper = new JPanel(new BorderLayout(0, 10));
            barWrapper.setOpaque(false);
            JLabel lblBarBaslik = new JLabel("Rafların Doluluk Oranı (%)");
            lblBarBaslik.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblBarBaslik.setForeground(anaEkran.getHeaderTextColor());
            barWrapper.add(lblBarBaslik, BorderLayout.NORTH);
            barWrapper.add(barScroll, BorderLayout.CENTER);

            // Çizilen grafikleri ana panele yerleştirme
            grafikPaneli.add(pieWrapper);
            grafikPaneli.add(barWrapper);

            grafikPaneli.revalidate();
            grafikPaneli.repaint();
        }
    }

    // Arayüzdeki kartlara MouseListener ekleyerek üzerine basılı tutulduğunda baloncuk (Tooltip) çıkmasını sağlıyorum (Event-Driven).
    private void addCardBubble(JPanel card, JLabel label) {
        MouseAdapter longPressAdapter = new MouseAdapter() {
            private Timer pressTimer;
            @Override public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    pressTimer = new Timer(400, evt -> {
                        String text = label.getText();
                        if (text != null && !text.trim().isEmpty()) {
                            JPopupMenu bubble = new JPopupMenu();
                            bubble.setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 2));
                            bubble.setBackground(anaEkran.getSidebarBg());

                            JLabel lblDeger = new JLabel(text);
                            lblDeger.setFont(new Font("Segoe UI", Font.BOLD, 24));
                            lblDeger.setForeground(anaEkran.getHeaderTextColor());
                            lblDeger.setBorder(new EmptyBorder(10, 20, 10, 20));

                            bubble.add(lblDeger);
                            bubble.show(card, e.getX(), e.getY() + 15);
                        }
                    });
                    pressTimer.setRepeats(false); pressTimer.start();
                }
            }
            @Override public void mouseReleased(MouseEvent e) { if (pressTimer != null) pressTimer.stop(); }
            @Override public void mouseExited(MouseEvent e) { if (pressTimer != null) pressTimer.stop(); }
            @Override public void mouseDragged(MouseEvent e) { if (pressTimer != null) pressTimer.stop(); }
        };
        card.addMouseListener(longPressAdapter);
        card.addMouseMotionListener(longPressAdapter);
        label.addMouseListener(longPressAdapter);
        label.addMouseMotionListener(longPressAdapter);
    }

    // Custom UI Component: JPanel sınıfını kalıtım (Inheritance) alıp paintComponent metodunu Override ederek (Polymorphism)
    // standart düz renkli panelleri modern ve geçişli (gradient) renklere dönüştürdüm.
    private class GradientCard extends JPanel {
        private Color c1, c2;
        public GradientCard(String t, JLabel v, Color c1, Color c2) {
            this.c1 = c1; this.c2 = c2;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(15, 20, 15, 20));
            setOpaque(false);

            JLabel l = new JLabel(t);
            l.setFont(new Font("Segoe UI", Font.BOLD, 14));
            l.setForeground(new Color(255,255,255,220));

            v.setFont(new Font("Segoe UI", Font.BOLD, 26));
            v.setForeground(Color.WHITE);

            add(l);
            add(Box.createVerticalStrut(8));
            add(v);
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setPaint(new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2));
            g2d.fill(new java.awt.geom.RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
        }
    }
}