package net.nazarick.artillerytablet.mapcheck;

import com.mojang.blaze3d.platform.NativeImage;
import net.nazarick.artillerytablet.client.screen.TabletChassisPaint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.List;

/**
 * ⚡ REAL-TIME CASE.PNG DESKTOP LIVE VIEWER & AUTO-WATCHER
 * Tự động cập nhật cửa sổ Desktop và file case.png trong nháy mắt mỗi khi code thay đổi!
 */
public class CaseLiveViewer extends JFrame {

    private static final int DESIGN_W = 980;
    private static final int DESIGN_H = 630;

    private static BufferedImage currentFrame = null;
    private static JPanel renderPanel;
    private static JLabel statusLabel;
    private static Path outDir;

    public CaseLiveViewer(Path outputDirectory) {
        outDir = outputDirectory;
        setTitle("Tactical Tablet — case.png Real-Time Live Preview");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(new Color(0x0A, 0x0B, 0x0C));

        renderPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                if (currentFrame != null) {
                    // Fit with aspect ratio
                    int panelW = getWidth();
                    int panelH = getHeight();
                    float scaleX = (float) panelW / DESIGN_W;
                    float scaleY = (float) panelH / DESIGN_H;
                    float scale = Math.min(scaleX, scaleY);
                    int drawW = Math.round(DESIGN_W * scale);
                    int drawH = Math.round(DESIGN_H * scale);
                    int drawX = (panelW - drawW) / 2;
                    int drawY = (panelH - drawH) / 2;

                    g2d.drawImage(currentFrame, drawX, drawY, drawW, drawH, null);
                }
            }
        };
        renderPanel.setPreferredSize(new Dimension(DESIGN_W, DESIGN_H));
        renderPanel.setBackground(new Color(0x0A, 0x0B, 0x0C));

        statusLabel = new JLabel(" [● LIVE] Watching TabletChassisPaint.java — Press [R] to Force Reload | Auto-saves to case.png");
        statusLabel.setForeground(new Color(0x00, 0xE6, 0x5A));
        statusLabel.setFont(new Font("Consolas", Font.BOLD, 13));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        statusLabel.setBackground(new Color(0x12, 0x14, 0x18));
        statusLabel.setOpaque(true);

        setLayout(new BorderLayout());
        add(renderPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_R || e.getKeyCode() == KeyEvent.VK_F5) {
                    triggerReload();
                }
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        triggerReload();
    }

    public static synchronized void triggerReload() {
        try {
            NativeImage nativeImg = TabletChassisPaint.bake();

            BufferedImage img = new BufferedImage(DESIGN_W, DESIGN_H, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < DESIGN_H; y++) {
                for (int x = 0; x < DESIGN_W; x++) {
                    int col = nativeImg.getPixelRGBA(x, y);
                    // NativeImage RGBA to Java ARGB
                    int a = (col >> 24) & 0xFF;
                    int b = (col >> 16) & 0xFF;
                    int g = (col >> 8) & 0xFF;
                    int r = col & 0xFF;
                    img.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
                }
            }

            currentFrame = img;

            // Save to build/mapcheck/case.png & case-master.png
            Files.createDirectories(outDir);
            nativeImg.writeToFile(outDir.resolve("case.png"));
            nativeImg.writeToFile(outDir.resolve("case-master.png"));

            if (renderPanel != null) {
                renderPanel.repaint();
            }
            if (statusLabel != null) {
                statusLabel.setText(" [✓ UPDATED " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] Saved to " + outDir.resolve("case.png"));
            }
            System.out.println("[LIVE] Updated case.png -> " + outDir.resolve("case.png"));
        } catch (Exception ex) {
            ex.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText(" [!] Error: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();

        Path out = Paths.get(args.length > 0 ? args[0] : "build/mapcheck");
        SwingUtilities.invokeLater(() -> new CaseLiveViewer(out));

        // Background File Watcher
        Path watchDir = Paths.get("src/main/java/net/nazarick/artillerytablet/client/screen");
        if (!Files.exists(watchDir)) {
            watchDir = Paths.get("ArtilleryTacticalTablet/src/main/java/net/nazarick/artillerytablet/client/screen");
        }

        if (Files.exists(watchDir)) {
            try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
                watchDir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
                System.out.println("[WATCHER] Watching directory: " + watchDir.toAbsolutePath());

                while (true) {
                    WatchKey key = watcher.take();
                    boolean modified = false;
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.context().toString().endsWith(".java")) {
                            modified = true;
                            break;
                        }
                    }
                    if (modified) {
                        Thread.sleep(150); // debounce
                        SwingUtilities.invokeLater(CaseLiveViewer::triggerReload);
                    }
                    key.reset();
                }
            }
        }
    }
}
