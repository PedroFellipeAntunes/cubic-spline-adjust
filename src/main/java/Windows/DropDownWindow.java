package Windows;

import Operation.Operations;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class DropDownWindow {
    private JFrame frame;
    private JLabel dropLabel;
    
    private boolean loading = false;
    private Font defaultFont = UIManager.getDefaults().getFont("Label.font");
    
    public DropDownWindow() {
        frame = new JFrame("Color Adjustment");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setLayout(new BorderLayout());
        
        dropLabel = new JLabel("Drop IMAGE files here", SwingConstants.CENTER);
        dropLabel.setPreferredSize(new Dimension(300, 200));
        dropLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        dropLabel.setForeground(Color.WHITE);
        dropLabel.setOpaque(true);
        dropLabel.setBackground(Color.BLACK);
        dropLabel.setTransferHandler(new TransferHandler() {
            public boolean canImport(TransferHandler.TransferSupport support) {
                if (!loading && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    return true;
                }
                
                return false;
            }
            
            public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                
                try {
                    Transferable transferable = support.getTransferable();
                    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    
                    for (File file : files) {
                        if (!file.getName().toLowerCase().endsWith(".png")
                                && !file.getName().toLowerCase().endsWith(".jpg")
                                && !file.getName().toLowerCase().endsWith(".jpeg")) {
                            JOptionPane.showMessageDialog(frame,
                                    "Incorrect image format, use: png, jpg or jpeg \n" + file.getName(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            
                            return false;
                        }
                    }
                    
                    dropLabel.setText("LOADING (1/" + files.size() + ")");
                    loading = true;
                    
                    frame.repaint();
                    
                    new Thread(() -> {
                        int filesProcessed = 1;
                        
                        for (File file : files) {
                            try {
                                Operations.processFile(file.getPath());
                            } catch (Exception e) {
                                // Show error to user
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(
                                            frame,
                                            "Error processing file: " + file.getName() + "\n" + e.getMessage(),
                                            "Processing Error",
                                            JOptionPane.ERROR_MESSAGE
                                    );
                                });
                                
                                e.printStackTrace();
                            }
                            
                            filesProcessed++;
                            
                            final int finalFilesProcessed = filesProcessed;
                            
                            SwingUtilities.invokeLater(() -> {
                                dropLabel.setText("LOADING (" + finalFilesProcessed + "/" + files.size() + ")");
                            });
                        }
                        
                        SwingUtilities.invokeLater(() -> {
                            dropLabel.setText("Images Generated");
                            
                            Timer resetTimer = new Timer(1000, e2 -> {
                                dropLabel.setText("Drop IMAGE files here");
                                loading = false;
                            });
                            
                            resetTimer.setRepeats(false);
                            resetTimer.start();
                        });
                    }).start();
                    
                    return true;
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });
        frame.add(dropLabel, BorderLayout.CENTER);
        
        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int xPos = (screenSize.width - frame.getWidth()) / 2;
        int yPos = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(xPos, yPos);
        
        frame.setVisible(true);
    }
    
    private void setButtonsVisuals(JButton button) {
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(100, 40));
    }
    
    private void setButtonVisuals(JButton button) {
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(40, 40));
    }
    
    private void setButtonsVisualsColors(JButton button, Color color) {
        button.setBackground(color);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(40, 40));
    }
    
    private void ableOrDisableButton(JButton button) {
        button.setEnabled(!loading);
    }
}