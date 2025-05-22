import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class TJAtoBin extends JFrame implements ActionListener {
    private JButton selectButton;
    private JButton convertButton;
    private File selectedFile;
    private String fileContent;

    public TJAtoBin() {
        setLayout(new FlowLayout());
        setSize(300, 100);

        selectButton = new JButton("Select .tja File");
        selectButton.addActionListener(this);
        add(selectButton);

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
            // sets the filter to only .BIN files being selected
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("TJA files", "tja"));
            // if chooser has been finished
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFile = chooser.getSelectedFile();
            }
        } else if (e.getSource() == convertButton) {
            if (selectedFile == null) {
                // if nothing is selected throw error
                JOptionPane.showMessageDialog(this, "You have to select a file.");
                return;
            }
            try {
                // get proper file name wihtout extention
                String name = selectedFile.getName().substring(0, selectedFile.getName().length() - 4);
                // use getproperty to get running directory to help with other people besides me running it
                // uses javas file class to create directory based on the path.
                String dirPath = System.getProperty("user.dir") + "/Songs/" + name;
                File outDir = new File(dirPath);
                outDir.mkdirs();

                //runs the python to convert from the TJA to the .bin
                String binPath = dirPath + "/" + name + ".bin";
                Process process = Runtime.getRuntime().exec(new String[]{
                        "python3", System.getProperty("user.dir") + "/src/Parser/tja_parser.py", selectedFile.getAbsolutePath(), binPath
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void GUI(String[] args) {
        SwingUtilities.invokeLater(() -> new TJAtoBin().setVisible(true));
    }
}
