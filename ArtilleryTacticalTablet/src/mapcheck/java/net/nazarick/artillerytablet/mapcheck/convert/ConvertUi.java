package net.nazarick.artillerytablet.mapcheck.convert;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * The window: four sliders, three previews, and two export buttons.
 *
 * <p><b>Why this has a window at all.</b> Two of the four settings have no correct value that can be
 * worked out — how coarse the grid should be and how many colours the design needs are judgements
 * about one particular picture, and the only way to make them is to change the number and look. A
 * command-line tool would mean a compile-and-look cycle per guess; here the guess is a drag.
 *
 * <p><b>Why three previews and not one.</b> They fail differently and a single picture hides two of
 * the three. The logical grid says whether the palette kept the design's own distinctions; the
 * rectangle view says whether a flat panel came out as one shape or as two hundred slivers — which
 * the colours alone cannot show; and the source says what will actually be compiled. Looking only at
 * the middle one is how a run that produces a beautiful picture and forty thousand draw calls gets
 * approved.
 */
public final class ConvertUi extends JFrame {
    /** The way in from Gradle. Needs no Minecraft — nothing here touches the game. */
    public static final class Main {
        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                ConvertUi ui = new ConvertUi();
                ui.setVisible(true);
                if (args.length > 0) {
                    ui.load(new File(args[0]));
                }
            });
        }
    }

    private BufferedImage source;
    private File sourceFile;
    private Convert.Result result;

    private final JSpinner logicalW = new JSpinner(new SpinnerNumberModel(300, 40, 1200, 10));
    private final JSpinner logicalH = new JSpinner(new SpinnerNumberModel(169, 30, 900, 10));
    private final JCheckBox lockAspect = new JCheckBox("Khoá tỉ lệ theo ảnh gốc", true);
    private final JSlider colours = new JSlider(4, 64, 32);
    private final JSlider noise = new JSlider(0, 24, 3);
    private final JSlider keepContrast = new JSlider(0, 200, 60);
    private final JComboBox<Emit.Rounding> rounding =
            new JComboBox<>(Emit.Rounding.values());

    // The screen, as a share of the picture. Measured off the reference and used as the default,
    // because on every version of this device the display is the one region worth spending no
    // rectangles on — the map draws over all of it at runtime.
    private final JCheckBox flattenScreen = new JCheckBox("Làm phẳng vùng màn hình", true);
    private final JSpinner flatX0 = percent(12.0);
    private final JSpinner flatY0 = percent(14.6);
    private final JSpinner flatX1 = percent(87.8);
    private final JSpinner flatY1 = percent(81.3);

    private static JSpinner percent(double value) {
        return new JSpinner(new SpinnerNumberModel(value, 0.0, 100.0, 0.5));
    }

    private final JTextField packageName =
            new JTextField("net.nazarick.artillerytablet.client.screen");
    private final JTextField className = new JTextField("ConvertedCase");

    private final ImagePanel originalView = new ImagePanel();
    private final ImagePanel logicalView = new ImagePanel();
    private final ImagePanel rectView = new ImagePanel();
    private final JTextArea sourceView = new JTextArea();
    private final JLabel status = new JLabel("Chưa nạp ảnh.");

    private ConvertUi() {
        super("Image to Paint.fill() — bộ chuyển ảnh sang mã Java");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("1 · Ảnh gốc", new JScrollPane(originalView));
        tabs.addTab("2 · Lưới logic (đã giảm màu)", new JScrollPane(logicalView));
        tabs.addTab("3 · Hình chữ nhật đã gộp", new JScrollPane(rectView));
        sourceView.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        sourceView.setEditable(false);
        tabs.addTab("4 · Mã Java", new JScrollPane(sourceView));
        tabs.setSelectedIndex(1);

        add(tabs, BorderLayout.CENTER);
        add(controls(), BorderLayout.WEST);

        status.setBorder(new EmptyBorder(4, 8, 4, 8));
        add(status, BorderLayout.SOUTH);

        setSize(1500, 950);
        setLocationRelativeTo(null);
    }

    private JComponent controls() {
        Box box = Box.createVerticalBox();
        box.setBorder(new EmptyBorder(8, 8, 8, 8));

        JButton open = new JButton("Nạp ảnh…");
        open.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(new File("docs"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                load(chooser.getSelectedFile());
            }
        });
        box.add(row(open));
        box.add(Box.createVerticalStrut(10));

        box.add(label("Độ phân giải logic"));
        JPanel size = new JPanel(new GridLayout(1, 2, 4, 0));
        size.add(logicalW);
        size.add(logicalH);
        box.add(row(size));
        box.add(row(lockAspect));

        JPanel presets = new JPanel(new GridLayout(2, 2, 4, 4));
        for (int[] p : new int[][]{{240, 135}, {300, 169}, {320, 180}, {360, 203}}) {
            JButton preset = new JButton(p[0] + "×" + p[1]);
            preset.addActionListener(e -> {
                lockAspect.setSelected(false);
                logicalW.setValue(p[0]);
                logicalH.setValue(p[1]);
                convert();
            });
            presets.add(preset);
        }
        box.add(row(presets));
        box.add(Box.createVerticalStrut(10));

        box.add(label("Số màu (palette)"));
        box.add(row(slider(colours, 4, 8)));

        box.add(label("Dọn nhiễu — diện tích tối đa bị xoá"));
        box.add(row(slider(noise, 0, 4)));

        box.add(label("Giữ chi tiết — độ tương phản tối thiểu"));
        box.add(row(slider(keepContrast, 0, 50)));
        box.add(Box.createVerticalStrut(10));

        box.add(label("Cách làm tròn toạ độ"));
        box.add(row(rounding));
        box.add(Box.createVerticalStrut(10));

        box.add(row(flattenScreen));
        box.add(label("Vùng màn hình (% trái/trên/phải/dưới)"));
        JPanel flat = new JPanel(new GridLayout(2, 2, 4, 4));
        flat.add(flatX0);
        flat.add(flatY0);
        flat.add(flatX1);
        flat.add(flatY1);
        box.add(row(flat));
        box.add(Box.createVerticalStrut(10));

        box.add(label("Gói (package)"));
        box.add(row(packageName));
        box.add(label("Tên lớp"));
        box.add(row(className));
        box.add(Box.createVerticalStrut(10));

        JButton apply = new JButton("Chuyển đổi lại");
        apply.addActionListener(e -> convert());
        box.add(row(apply));

        JButton exportJava = new JButton("Xuất file .java…");
        exportJava.addActionListener(e -> exportJava());
        box.add(row(exportJava));

        JButton exportPng = new JButton("Xuất ảnh preview .png…");
        exportPng.addActionListener(e -> exportPng());
        box.add(row(exportPng));

        box.add(Box.createVerticalGlue());

        for (JSpinner s : new JSpinner[]{logicalW, logicalH}) {
            s.addChangeListener(e -> {
                if (lockAspect.isSelected() && source != null && s == logicalW) {
                    int w = (int) logicalW.getValue();
                    int h = Math.max(1, Math.round(
                            w * source.getHeight() / (float) source.getWidth()));
                    if ((int) logicalH.getValue() != h) {
                        logicalH.setValue(h);
                    }
                }
                convert();
            });
        }
        for (JSlider s : new JSlider[]{colours, noise, keepContrast}) {
            s.addChangeListener(e -> {
                if (!s.getValueIsAdjusting()) {
                    convert();
                }
            });
        }
        rounding.addActionListener(e -> refreshSource());
        flattenScreen.addActionListener(e -> convert());
        for (JSpinner s : new JSpinner[]{flatX0, flatY0, flatX1, flatY1}) {
            s.addChangeListener(e -> convert());
        }

        JScrollPane scroll = new JScrollPane(box);
        scroll.setPreferredSize(new Dimension(300, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private static JSlider slider(JSlider s, int minorTick, int majorTick) {
        s.setPaintLabels(true);
        s.setPaintTicks(true);
        s.setMinorTickSpacing(minorTick);
        s.setMajorTickSpacing(majorTick);
        return s;
    }

    private static JComponent label(String text) {
        JLabel l = new JLabel(text);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(6, 0, 2, 0));
        return l;
    }

    private static JComponent row(JComponent inner) {
        inner.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                Math.max(inner.getPreferredSize().height, 24)));
        return inner;
    }

    void load(File file) {
        try {
            source = ImageIO.read(file);
            if (source == null) {
                status.setText("Không đọc được ảnh: " + file.getName());
                return;
            }
            sourceFile = file;
            originalView.set(source);
            if (lockAspect.isSelected()) {
                int w = (int) logicalW.getValue();
                logicalH.setValue(Math.max(1, Math.round(
                        w * source.getHeight() / (float) source.getWidth())));
            }
            convert();
        } catch (Exception ex) {
            status.setText("Lỗi nạp ảnh: " + ex.getMessage());
        }
    }

    private void convert() {
        if (source == null) {
            return;
        }
        try {
            Convert.Flatten region = flattenScreen.isSelected()
                    ? new Convert.Flatten(
                            (double) flatX0.getValue() / 100.0,
                            (double) flatY0.getValue() / 100.0,
                            (double) flatX1.getValue() / 100.0,
                            (double) flatY1.getValue() / 100.0)
                    : Convert.Flatten.NONE;
            Convert.Settings settings = new Convert.Settings(
                    (int) logicalW.getValue(), (int) logicalH.getValue(),
                    colours.getValue(), noise.getValue(), keepContrast.getValue(), region);
            result = Convert.run(source, settings);

            int scale = Math.max(1, 1200 / Math.max(1, result.pixels().w));
            logicalView.set(Convert.nearest(result.logicalImage(), scale));
            rectView.set(Convert.rectPreview(result, scale));
            refreshSource();

            int cells = result.cellCount();
            status.setText(String.format(
                    "%d×%d = %,d ô  ·  %d màu thật  ·  %,d hình chữ nhật  "
                            + "(%.1f%% so với vẽ từng ô)  ·  ~%,d lệnh fill()",
                    result.pixels().w, result.pixels().h, cells,
                    result.palette().size(), result.rectCount(),
                    100.0 * result.rectCount() / Math.max(1, cells), result.rectCount()));
        } catch (Exception ex) {
            status.setText("Lỗi chuyển đổi: " + ex);
        }
    }

    private void refreshSource() {
        if (result == null) {
            return;
        }
        sourceView.setText(Emit.source(result.rects(), result.palette(), result.pixels(),
                packageName.getText().trim(), className.getText().trim(),
                sourceFile == null ? "an image" : sourceFile.getName(),
                (Emit.Rounding) rounding.getSelectedItem()));
        sourceView.setCaretPosition(0);
    }

    private void exportJava() {
        if (result == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setSelectedFile(new File(className.getText().trim() + ".java"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Files.write(chooser.getSelectedFile().toPath(),
                    sourceView.getText().getBytes(StandardCharsets.UTF_8));
            status.setText("Đã ghi " + chooser.getSelectedFile());
        } catch (Exception ex) {
            status.setText("Lỗi ghi file: " + ex.getMessage());
        }
    }

    private void exportPng() {
        if (result == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setSelectedFile(new File("converted-preview.png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            ImageIO.write(logicalView.image, "png", chooser.getSelectedFile());
            status.setText("Đã ghi " + chooser.getSelectedFile());
        } catch (Exception ex) {
            status.setText("Lỗi ghi ảnh: " + ex.getMessage());
        }
    }

    /** A panel that shows one picture at its own size, on a dark ground. */
    private static final class ImagePanel extends JPanel {
        private BufferedImage image;

        ImagePanel() {
            setBackground(new Color(0x14, 0x16, 0x18));
        }

        void set(BufferedImage img) {
            this.image = img;
            setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                g.drawImage(image, 0, 0, null);
            }
        }
    }
}
