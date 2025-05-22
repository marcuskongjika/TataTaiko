import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class TJAtoBin extends JFrame implements ActionListener {
    private JButton selectButton;
    private JButton convertButton;
    private File selectedFile;
    final private String fileChecksum = "aa850984f3ac38edb5d6a6cad82b9763aff777bcd4ebce95bf569af78e39bad3";
    boolean safe = false;

    public TJAtoBin() {
        // sets basic window layout and appearance
        setLayout(new FlowLayout());
        setSize(300, 100);
        setTitle("TJA to BIN Converter");

        // initializes button to select file
        selectButton = new JButton("Select .tja File");
        selectButton.addActionListener(this);
        add(selectButton);

        // initializes button to start conversion
        convertButton = new JButton("Start Conversion");
        convertButton.addActionListener(this);
        add(convertButton);
    }

    // action method to check whether each button was hit and does the proper thing
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectButton) {
            // creates file chooser object
            JFileChooser chooser = new JFileChooser();
            // sets the filter to only .TJA files being selected
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("TJA files", "tja"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFile = chooser.getSelectedFile();
            }
        } else if (e.getSource() == convertButton) {
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(this, "You have to select a file.");
                return;
            }

            // check  the Python script before running
            File pythonScript = new File(System.getProperty("user.dir") + "/src/Parser/tja_parser.py");
            byte[] pythonBytes = {1};
            try {
                pythonBytes = Files.readAllBytes(pythonScript.toPath());
            } catch (IOException ex) {ex.printStackTrace();
            }

            String hashedPython = org.apache.commons.codec.digest.DigestUtils.sha256Hex(pythonBytes);
            if (hashedPython.equals(fileChecksum)) {
                safe = true;
            } else {
                throw new SecurityException("SHA checksum mismatch, File has been tampered with");
            }

            try {
                // get proper file name without extension
                String name = selectedFile.getName().substring(0, selectedFile.getName().length() - 4);
                String dirPath = System.getProperty("user.dir") + "/Songs/" + name;
                File outDir = new File(dirPath);
                outDir.mkdirs();

                if (safe) {
                    String binPath = dirPath + "/" + name + ".bin";
                    Process process = Runtime.getRuntime().exec(new String[] {
                            "python3", pythonScript.getAbsolutePath(), selectedFile.getAbsolutePath(), binPath
                    });
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TJAtoBin().setVisible(true));
    }
}
