/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jumptracker;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import javax.swing.undo.UndoManager;

/**
 *
 * @author Infaera
 */
public class TrackerFrame extends javax.swing.JFrame {

    //ArrayList<String> jumperList = new ArrayList<>();
    //String trackerLink = "Tracker.ini", jumperLink = "Jumper.jt";
    public static final String VERSION = "0.7.9.0";
    private String newVersion = VERSION, dlink = "",
            versionLink = "https://infaera.neocities.org/JumpTracker/JumpTrackerInfo.html",
            reddit = "https://www.reddit.com/r/JumpChain/comments/hbjstz/jump_tracker/?sort=new";
    //vlink = "https://docs.google.com/document/d/1MAYkleEIkNsMyw7OM1XLYxD-pfVoLHG4OJVRQXGiSsE/edit?usp=sharing";
    File basicPath = new File("Jump Tracker").getAbsoluteFile(),
            jumperPath = new File(basicPath.toString(), "Jumper"),
            jumpPath = new File(basicPath.toString(), "Jump"),
            backupPath = new File(basicPath.toString(), "Backup"),
            trackerFile = new File(basicPath.toString(), "Tracker.ini"),
            jumperFile = new File(jumperPath.toString(), "Jumper.jt");

    private static final Color DARK_RED = new Color(124, 0, 0);
    private static final Color LIGHT_RED = new Color(255, 25, 25);
    private static long saveTime = 0;
    private String copyTo = "";
    boolean jchange = false, ochange = false;
    private boolean mouseIsDragging = false, theme = false, autosave;
    private UndoManager manager1 = new UndoManager(), manager2 = new UndoManager();
    private String onlineMessage = null;

    Jumper jumper;// = new Jumper();
    Jump jump;
    Option option;

    public TrackerFrame() {
        initComponents();
        initExtras();

        checkOnline();

        checkFolders();

        try {
            loadTracker();
            loadJumper();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(rootPane,
                    "Loading Failed!",
                    "ERROR!", JOptionPane.WARNING_MESSAGE);
            ex.printStackTrace();
            System.exit(-1);
        }
        //System.out.println("Versiona: " + getClass().getPackage().getImplementationVersion());

        autosave();
    }

    private void initExtras() {
        jTextArea1.getDocument().addUndoableEditListener(manager1);
        jTextArea2.getDocument().addUndoableEditListener(manager2);

        TrackerFrameMouseAdaptor myMouseAdaptor1 = new TrackerFrameMouseAdaptor(jList1, model1);
        jList1.addMouseListener(myMouseAdaptor1);
        jList1.addMouseMotionListener(myMouseAdaptor1);
        TrackerFrameMouseAdaptor myMouseAdaptor2 = new TrackerFrameMouseAdaptor(jList2, model2);
        jList2.addMouseListener(myMouseAdaptor2);
        jList2.addMouseMotionListener(myMouseAdaptor2);
        TrackerFrameMouseAdaptor myMouseAdaptor3 = new TrackerFrameMouseAdaptor(jList3, model3);
        jList3.addMouseListener(myMouseAdaptor3);
        jList3.addMouseMotionListener(myMouseAdaptor3);

        jList2.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Option) {
                    if (((Option) value).isActive()) {
                        if (!theme) {
                            setForeground(new Color(22, 22, 22));
                        } else if (theme) {
                            setForeground(new Color(222, 222, 222));
                            for (int i : jList2.getSelectedIndices()) {
                                if (i == index) {
                                    setForeground(new Color(22, 22, 22));
                                }
                            }
                        }
                    } else {
                        if (theme) {
                            setForeground(LIGHT_RED);
                        } else {
                            setForeground(DARK_RED);
                        }
                    }
                }
                return c;
            }
        });
        jList3.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Option) {
                    Option tempOption = (Option) value;
                    if (tempOption.isActive()) {
                        if (theme) {
                            setForeground(new Color(222, 222, 222));
                        } else {
                            setForeground(new Color(22, 22, 22));
                        }
                    } else {
                        if (theme) {
                            setForeground(LIGHT_RED);
                        } else {
                            setForeground(DARK_RED);
                        }
                    }
                }
                return c;
            }
        });
    }

    private void checkOnline() {
        try {
            if (versionLink.equals("") || versionLink == null) {
                return;
            }

            String raw = URLIO.sendGET(versionLink).replace("\n", "");
            System.out.println(raw);
            String[] data = raw.split("<br>");
            for (String s : data) {
                if (s.startsWith("version=")) {
                    newVersion = data[0].replace("version=", "");
                } else if (s.startsWith("link=")) {
                    dlink = data[1].replace("link=", "");
                } else if (s.startsWith("message=")) {
                    onlineMessage = s.replaceFirst("message=", "");
                }
            }

            Version current = new Version(VERSION);
            Version check = new Version(newVersion);
            if (current.compareTo(check) == -1) {
                this.setTitle(this.getTitle() + " (Version " + newVersion + " Available)");
            }// System.out.println("Latest = " + newVersion);
        } catch (Exception e) {
            System.out.println("Error: Unable to Check Version");
            e.printStackTrace();
        }
    }

    private void autosave() {
        autosave = true;
        TrackerFrame frame = this;
        Thread autoSave;
        autoSave = new Thread() {
            @Override
            public void run() {
                try {
                    while (autosave) {
                        Thread.sleep(60000);
                        if (frame.isFocused()) {
                            frame.saveJumper();
                        }
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        autoSave.start();
    }

    private void checkFolders() {
        checkPaths();
        try {
            File[] files = new File(".").listFiles();
            for (File f : files) {
                if (f.getName().equals("Tracker.ini")) {
                    String data = loadData(f);
                    saveData(trackerFile, data);
                    f.delete();
                }
                if (f.getName().endsWith(".jt")) {
                    String data = loadData(f);
                    File nfile = new File(jumperPath, f.getName());
                    File bfile = new File(backupPath, f.getName());

                    if (!nfile.exists()) {
                        if (!nfile.exists()) {
                            saveData(nfile, data);
                        }
                        if (!bfile.exists()) {
                            saveData(bfile, data);
                            f.delete();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(rootPane,
                    "Unable to Edit Necessaray Files",
                    "ERROR!", JOptionPane.WARNING_MESSAGE);
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    private void checkPaths() {
        try {
            if (!basicPath.exists()) {
                if (basicPath.mkdirs()) {
                    if (!jumpPath.mkdirs()) {
                        System.out.println("Unable to Create Jump Folder");
                    }
                    if (!jumperPath.mkdirs()) {
                        System.out.println("Unable to Create Jumper Folder");
                    }
                    if (!backupPath.mkdirs()) {
                        System.out.println("Unable to Create Backup Folder");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Unable to Create Folders");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(rootPane,
                    "Unable to Create Necessaray Paths",
                    "ERROR!", JOptionPane.WARNING_MESSAGE);
            ex.printStackTrace();
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
            }
            System.exit(-1);
        }
    }

    private void loadTracker() {
        if (trackerFile.exists()) {

            String[] tData = loadData(trackerFile).split("\n");
            for (String s : tData) {
                String[] line = s.split("=");
                switch (line[0]) {
                    case "file":
                        jumperFile = new File(jumperPath, line[1]);
                        break;
                    case "location":
                        String[] la = s.split("=")[1].split(",");
                        setLocation(Integer.parseInt(la[0]), Integer.parseInt(la[1]));
                        break;
                    case "dimension":
                        String[] da = line[1].split(",");
                        setSize(Integer.parseInt(da[0]), Integer.parseInt(da[1]));
                        break;
                    case "font":
                        String[] fa = s.replaceFirst("font=", "").split(",");
                        Font font = new Font(fa[0], Integer.parseInt(fa[1]), Integer.parseInt(fa[2]));
                        setTextFieldFonts(font);
                        break;
                    default:
                        if (s.contains(".jt")) {
                            jumperFile = new File(jumperPath, s);
                        }
                        break;
                }
            }
        } else {
            saveTracker();
        }
    }

    private void loadJumper() {
        if (!jumperFile.exists()) {
            createJumper();
        }
        String raw = loadData(jumperFile);
        if (raw.startsWith("<jumptracker>")) {
            jumper = Conversion.HTMLToJumper(raw);
        } else {
            try {
                jumper = C075.textToJumper(raw);
            } catch (Exception e) {
                try {
                    jumper = C071.textToJumper(raw);
                } catch (Exception ex) {
                    e.printStackTrace();
                }
            }
        }

        jTextField1.setText(jumper.getName());
        jump = jumper.getJumpList().get(0);
        option = jump.getOptionList().get(0);
        updateTypeComboBox();
        loadOption(0);

        model1.clear();
        jumper.getJumpList().forEach((j) -> {
            model1.addElement(j);
        });

        model2.clear();
        jump.getOptionList().forEach((o) -> {
            model2.addElement(o);
        });

        if (!model1.isEmpty()) {
            jList1.setSelectedIndex(0);
        }
        if (!model2.isEmpty()) {
            jList2.setSelectedIndex(0);
        }

    }

    public void updateTypeComboBox() {
        defaultComboBoxModel1 = new DefaultComboBoxModel(jumper.getTypesArray());
        jComboBox1.setModel(defaultComboBoxModel1);
    }

    private void resetJumper() {
        model1.clear();
        model2.clear();
        createJumper();
    }

    private void createJumper() {
        jumperFile = new File(jumperPath, "Jumper.jt");
        jumper = new Jumper("Jumper");
        jTextField1.setText("Jumper");

        newJump();
        loadJump(0);

        saveJumper();
        saveTracker();
    }

    private void newJump() {
        int step = (int) spinnerModel1.getStepSize();
        jump = new Jump("Jump " + (jumper.getJumpList().size() + 1));
        jump.setStepValue(step);
        jumper.addJump(jump);
        model1.addElement(jump);
        model2.removeAllElements();
        newOption();
    }

    private void newOption() {
        String tempType;
        if (jump.getOptionList().isEmpty()) {
            tempType = "Origin";
            option = new Option("Origin");
        } else {
            tempType = option.getType();
            option = new Option("Perk " + (jump.getOptionList().size() + 1));
        }
        option.setJumpName(jump.getName());
        option.setActive(jCheckBoxMenuItem1.isSelected());
        jump.addOption(option);
        model2.addElement(option);
        option.type = tempType;
        jComboBox1.setSelectedItem(tempType);
    }

    private void loadJump(int index) {
        jump = jumper.getJump(index);
        jTextField2.setText(jump.getName());
        jSpinner1.setValue(jump.getPoints());
        setStepValue(jump.getStepValue());
        switch (jump.getStepValue()) {
            case 1:
                jRadioButtonMenuItem1.setSelected(true);
                break;
            case 5:
                jRadioButtonMenuItem2.setSelected(true);
                break;
            case 10:
                jRadioButtonMenuItem3.setSelected(true);
                break;
            case 25:
                jRadioButtonMenuItem4.setSelected(true);
                break;
            case 50:
                jRadioButtonMenuItem4.setSelected(true);
                break;
            case 100:
                jRadioButtonMenuItem5.setSelected(true);
                break;
        }
        calcDisplayValue();
        //jLabel2.setText(jump.calcCosts() + "");

        model2.clear();
        //model2 = (DefaultListModel) jList2.getModel();
        jump.getOptionList().forEach((o) -> {
            model2.addElement(o);
        });
        jList2.setSelectedIndex(jump.getSelectedOption());
    }

    private void loadOption(int index) {
        option = jump.getOption(index);
        loadOption();
    }

    private void loadOption() {
        jTextField3.setText(option.getName());
        jTextArea1.setText(option.getDescription());
        jTextArea1.setCaretPosition(0);
        jTextArea2.setText(option.getNotes());
        jTextArea2.setCaretPosition(0);
        jSpinner2.setValue(option.getPoints());
        jCheckBox1.setSelected(option.isChain());
        jCheckBox4.setSelected(option.isActive());
        defaultComboBoxModel1.setSelectedItem(option.getType());

        SwingUtilities.invokeLater(new LaterUpdater(option.getScrollPoint()));
    }

    class LaterUpdater implements Runnable {

        private final int val;

        public LaterUpdater(int i) {
            this.val = i;
        }

        @Override
        public void run() {
            jScrollPane3.getVerticalScrollBar().setValue(val);
        }
    }

    private void runSearch() {
        try {
            ArrayList<Option> searchList = new ArrayList();
            jumper.getJumpList().forEach((j) -> {
                if (jComboBox3.getSelectedIndex() == 0 || j.getName().equals(jComboBox3.getSelectedItem())) {
                    searchList.addAll(j.getOptionList());
                }
            });

            String oType = (String) jComboBox2.getSelectedItem();
            int pointTemp = (int) jSpinner3.getValue();
            boolean activeTemp = jCheckBox5.isSelected();
            boolean chainTemp = jCheckBox2.isSelected();
            String[] searchArray = jTextField4.getText().toLowerCase().split(" ");

            if (searchList.isEmpty() || oType == null || oType.equals("")) {
                return;
            }

            ArrayList<Option> tempList = new ArrayList();
            for (Option so : searchList) {
                String optionString = so.getName() + " " + so.getDescription() + " " + so.getNotes();
                //System.out.println(so.getJumpName());
                if ((so.getJumpName().equals(jComboBox3.getSelectedItem())
                        || jComboBox3.getSelectedIndex() == 0)
                        && (so.getType().equals(oType) || oType.equals("All"))
                        && (so.getPoints() == pointTemp || pointTemp == 0)
                        && (!activeTemp || activeTemp && so.isActive())
                        && (!chainTemp || chainTemp && so.isChain())
                        && (stringContainsAll(optionString.toLowerCase(), searchArray))) {
                    tempList.add(so);

                }
            }

            model3.clear();
            model3 = (DefaultListModel) jList3.getModel();
            tempList.forEach((o) -> {
                model3.addElement(o);
            });
            jList3.setSelectedIndex(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean stringContainsAll(String str, String[] sa) {
        for (String s : sa) {
            if (!str.contains(s)) {
                return false;
            }
        }
        return true;
    }

    private void deleteJumperSelection() {
        String[] list = getNameList(jumperPath.listFiles(), ".jt");
        String s = (String) JOptionPane.showInputDialog(this,
                "", "Select Jumper to Delete", JOptionPane.PLAIN_MESSAGE, null,
                list, list[0]);

        if ((s != null) && (s.length() > 0)) {
            new File(jumperPath, s + ".jt").delete();
            if (jumper.getName().equals(s)) {
                resetJumper();
                //createJumper();
                //loadInfo();
            }
        }
    }

    private void loadJumperSelection() {
        String[] list = getNameList(jumperPath.listFiles(), ".jt");
        String s = (String) JOptionPane.showInputDialog(this,
                "", "Select Jumper to Load", JOptionPane.PLAIN_MESSAGE, null,
                list, list[0]);

        if ((s != null) && (s.length() > 0)) {
            jumperFile = new File(jumperPath, s + ".jt");
            loadJumper();
            saveTracker();
        }
    }

    private String getJumpSelection() {
        String[] list = getNameList(jumpPath.listFiles(), ".jump");
        String selection = (String) JOptionPane.showInputDialog(this,
                "", "Select Jump to Import", JOptionPane.PLAIN_MESSAGE, null,
                list, list[0]);

        if ((selection != null) && (selection.length() > 0)) {
            return selection;
        }
        return null;
    }

    private String[] getNameList(File[] files, String ext) {
        ArrayList<String> list = new ArrayList<>();
        list.add("");
        for (File f : files) {
            if (f.getName().endsWith(ext)) {
                list.add(f.getName().replace(ext, ""));
            }
        }
        return list.toArray(new String[list.size()]);
    }

    private void calcDisplayValue() {
        int value = jump.calcCosts();
        setDisplayValue(value);
    }

    private void setDisplayValue(int value) {
        jLabel2.setText("" + value);
        if (value < 0) {
            if (theme) {
                jLabel2.setForeground(LIGHT_RED.brighter().brighter());
            } else {
                jLabel2.setForeground(DARK_RED);
            }
        } else {
            if (theme) {
                jLabel2.setForeground(new Color(222, 222, 222));
            } else {
                jLabel2.setForeground(new Color(22, 22, 22));
            }
        }
    }

    private void setTextFieldFonts(Font font) {
        this.setFont(font);
        jTextArea1.setFont(font);
        jTextArea2.setFont(font);
        jTextArea3.setFont(font);
        jTextArea4.setFont(font);
        jTextField1.setFont(font);
        jTextField2.setFont(font);
        jTextField3.setFont(font);
        jTextField4.setFont(font);
        jList1.setFont(font);
        jList2.setFont(font);
        jList3.setFont(font);
        jComboBox1.setFont(font);
        jComboBox2.setFont(font);
        jComboBox3.setFont(font);
//        jSpinner1.setFont(font);
//        jSpinner2.setFont(font);
//        jSpinner3.setFont(font);
        jLabel1.setFont(font);
        jLabel2.setFont(font);
        jLabel3.setFont(font);
        jLabel4.setFont(font);
        jLabel5.setFont(font);
        jLabel6.setFont(font);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList<>();
        jSpinner1 = new javax.swing.JSpinner();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea()
        /*{
            @Override
            public boolean getScrollableTracksViewportHeight() {
                return false;
            }
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return false;
            }
        }*/
        ;
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jTextField2 = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jTextField3 = new javax.swing.JTextField();
        jSpinner2 = new javax.swing.JSpinner();
        jComboBox1 = new javax.swing.JComboBox<>();
        jButton6 = new javax.swing.JButton();
        jCheckBox4 = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jSpinner3 = new javax.swing.JSpinner();
        jCheckBox2 = new javax.swing.JCheckBox();
        jTextField4 = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane5 = new javax.swing.JScrollPane();
        jList3 = new javax.swing.JList<>();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTextArea3 = new javax.swing.JTextArea();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTextArea4 = new javax.swing.JTextArea();
        jLabel6 = new javax.swing.JLabel();
        jCheckBox3 = new javax.swing.JCheckBox();
        jComboBox3 = new javax.swing.JComboBox<>();
        jCheckBox5 = new javax.swing.JCheckBox();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        jMenuItem21 = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        jCheckBoxMenuItem3 = new javax.swing.JCheckBoxMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem22 = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        jMenuItem14 = new javax.swing.JMenuItem();
        jMenuItem13 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItem18 = new javax.swing.JMenuItem();
        jMenu7 = new javax.swing.JMenu();
        jMenuItem23 = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jMenuItem16 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jMenuItem19 = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        jMenu6 = new javax.swing.JMenu();
        jRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem2 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem3 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem4 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem6 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem5 = new javax.swing.JRadioButtonMenuItem();
        jMenuItem20 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItem2 = new javax.swing.JCheckBoxMenuItem();
        jMenuItem17 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem15 = new javax.swing.JMenuItem();
        jMenuItem9 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Jump Tracker " + VERSION);
        setMinimumSize(new java.awt.Dimension(600, 500));
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                formWindowLostFocus(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jTabbedPane1.setOpaque(true);
        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        jPanel1.setPreferredSize(new java.awt.Dimension(600, 600));

        model2 = new DefaultListModel<String>();
        jList2.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jList2.setModel(model2);
        jList2.setToolTipText("Selection List");
        jList2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jList2MousePressed(evt);
            }
        });
        jList2.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList2ValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jList2);

        jSpinner1.setToolTipText("Points for the Jump");
        jSpinner1.setValue(1000);
        spinnerModel1 = (SpinnerNumberModel) new SpinnerNumberModel();// jSpinner1.getModel();
        spinnerModel1.setStepSize(25);
        jSpinner1.setModel(spinnerModel1);
        jSpinner1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner1StateChanged(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setToolTipText("Description");
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setMargin(new java.awt.Insets(5, 5, 5, 5));
        jTextArea1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextArea1KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextArea1KeyReleased(evt);
            }
        });
        jScrollPane3.setViewportView(jTextArea1);
        jScrollPane3.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener(){
            @Override
            public void adjustmentValueChanged(AdjustmentEvent evt) {
                jScrollPane3AdjustmentEvent(evt);
            }
        });

        jLabel1.setText("CP");

        jButton1.setText("Add Jump");
        jButton1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextArea2.setColumns(20);
        jTextArea2.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jTextArea2.setLineWrap(true);
        jTextArea2.setRows(5);
        jTextArea2.setToolTipText("Notes");
        jTextArea2.setWrapStyleWord(true);
        jTextArea2.setMargin(new java.awt.Insets(5, 5, 5, 5));
        jTextArea2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextArea2KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextArea2KeyReleased(evt);
            }
        });
        jScrollPane4.setViewportView(jTextArea2);

        jTextField2.setToolTipText("Jumpchain Document Name");
        jTextField2.setMargin(new java.awt.Insets(0, 3, 0, 0));
        jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField2KeyReleased(evt);
            }
        });

        jCheckBox1.setToolTipText("Chain");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel2.setText("1000");
        jLabel2.setToolTipText("End Value");

        jButton3.setText("Add Option");
        jButton3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton5.setText("Save");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jTextField1.setToolTipText("Name your Jumper");
        jTextField1.setMargin(new java.awt.Insets(0, 3, 0, 0));
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });

        model1 = new DefaultListModel<String>();
        jList1.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jList1.setModel(model1);
        jList1.setToolTipText("CYOA List");
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jList1MousePressed(evt);
            }
        });
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        jTextField3.setToolTipText("Option Name");
        jTextField3.setMargin(new java.awt.Insets(0, 3, 0, 0));
        jTextField3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField3KeyReleased(evt);
            }
        });

        spinnerModel2 = (SpinnerNumberModel) new SpinnerNumberModel();//jSpinner2.getModel();
        spinnerModel2.setStepSize(25);
        jSpinner2.setModel(spinnerModel2);
        jSpinner2.setToolTipText("Option Value");
        jSpinner2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner2StateChanged(evt);
            }
        });

        defaultComboBoxModel1 = new DefaultComboBoxModel();
        jComboBox1.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jComboBox1.setModel(defaultComboBoxModel1);
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jButton6.setText("Load");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jCheckBox4.setToolTipText("Active");
        jCheckBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox4ActionPerformed(evt);
            }
        });

        jButton2.setText("Add Multi Option");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                    .addComponent(jTextField3)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jComboBox1, 0, 121, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox1))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel2)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton3)
                            .addComponent(jButton1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jScrollPane2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox4)
                            .addComponent(jCheckBox1))
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Jumper", jPanel1);

        defaultComboBoxModel2 = new DefaultComboBoxModel();
        jComboBox2.setModel(defaultComboBoxModel2);
        jComboBox2.setToolTipText("Search By Type");
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });

        spinnerModel3 = (SpinnerNumberModel) new SpinnerNumberModel();// jSpinner1.getModel();
        spinnerModel3.setStepSize(25);
        jSpinner3.setModel(spinnerModel3);
        jSpinner3.setToolTipText("Search by Points");
        jSpinner3.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner3StateChanged(evt);
            }
        });

        jCheckBox2.setToolTipText("Chain");
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        jTextField4.setToolTipText("Search by Words");
        jTextField4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextField4KeyTyped(evt);
            }
        });

        model3 = new DefaultListModel<String>();
        jList3.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jList3.setModel(model3);
        jList3.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList3ValueChanged(evt);
            }
        });
        jScrollPane5.setViewportView(jList3);

        jLabel3.setText("Jump Name");

        jLabel4.setText("Option Name");

        jLabel5.setText("CP: ");

        jTextArea3.setEditable(false);
        jTextArea3.setColumns(20);
        jTextArea3.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jTextArea3.setLineWrap(true);
        jTextArea3.setRows(5);
        jTextArea3.setToolTipText("Description");
        jTextArea3.setWrapStyleWord(true);
        jTextArea3.setMargin(new java.awt.Insets(5, 5, 5, 5));
        jScrollPane6.setViewportView(jTextArea3);

        jTextArea4.setEditable(false);
        jTextArea4.setColumns(20);
        jTextArea4.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jTextArea4.setLineWrap(true);
        jTextArea4.setRows(5);
        jTextArea4.setToolTipText("Notes");
        jTextArea4.setWrapStyleWord(true);
        jTextArea4.setMargin(new java.awt.Insets(5, 5, 5, 5));
        jScrollPane7.setViewportView(jTextArea4);

        jLabel6.setText("Scenario");

        jCheckBox3.setToolTipText("Chain");
        jCheckBox3.setEnabled(false);

        defaultComboBoxModel3 = new javax.swing.DefaultComboBoxModel<>(new String[] { "All" });
        jComboBox3.setModel(defaultComboBoxModel3);
        jComboBox3.setToolTipText("Search by Jump");
        jComboBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox3ActionPerformed(evt);
            }
        });

        jCheckBox5.setToolTipText("Active");
        jCheckBox5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jCheckBox3))
                                .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                            .addComponent(jScrollPane6)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinner3, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField4)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinner3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel5)
                                .addComponent(jLabel6))
                            .addComponent(jCheckBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        jTabbedPane1.addTab("Search", jPanel2);

        jMenu1.setText("File");

        jMenuItem4.setText("Exit");
        jMenuItem4.setToolTipText("Close Program");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem4);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Jumper");

        jMenuItem3.setText("New");
        jMenuItem3.setToolTipText("Create New Jumper");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuItem2.setText("Load");
        jMenuItem2.setToolTipText("Load Existing Jumper");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenuItem1.setText("Save");
        jMenuItem1.setToolTipText("Save Jumper");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);
        jMenu2.add(jSeparator9);

        jMenuItem21.setText("Backup");
        jMenuItem21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem21ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem21);
        jMenu2.add(jSeparator8);

        jCheckBoxMenuItem3.setSelected(true);
        jCheckBoxMenuItem3.setText("Auto Save");
        jCheckBoxMenuItem3.setToolTipText("Always On at Start");
        jCheckBoxMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jCheckBoxMenuItem3);
        jMenu2.add(jSeparator2);

        jMenuItem5.setText("Delete");
        jMenuItem5.setToolTipText("Delete a Jumper");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem5);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Jump");

        jMenuItem22.setText("Add Jump");
        jMenuItem22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem22ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem22);
        jMenu3.add(jSeparator5);

        jMenuItem7.setText("Output Selection");
        jMenuItem7.setToolTipText("Copy Jump Selections to the Clipboard");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem7);

        jMenuItem6.setText("Output Details");
        jMenuItem6.setToolTipText("Copy Jump Details to Clipboard");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem6);
        jMenu3.add(jSeparator7);

        jMenuItem14.setText("Export Jump");
        jMenuItem14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem14ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem14);

        jMenuItem13.setText("Import Jump");
        jMenuItem13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem13ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem13);
        jMenu3.add(jSeparator3);

        jMenuItem18.setText("Delete Jump");
        jMenuItem18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem18ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem18);

        jMenuBar1.add(jMenu3);

        jMenu7.setText("Option");

        jMenuItem23.setText("Add Option");
        jMenuItem23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem23ActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItem23);
        jMenu7.add(jSeparator6);

        jMenuItem16.setText("Copy To");
        jMenuItem16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem16ActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItem16);

        jMenuItem10.setText("Set All Active");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItem10);

        jMenuItem11.setText("Set All Inactive");
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItem11);
        jMenu7.add(jSeparator4);

        jMenuItem19.setText("Delete Option");
        jMenuItem19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem19ActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItem19);

        jMenuBar1.add(jMenu7);

        jMenu5.setText("Config");

        jMenu6.setText("Step Value");

        buttonGroup1.add(jRadioButtonMenuItem1);
        jRadioButtonMenuItem1.setText("1");
        jMenu6.add(jRadioButtonMenuItem1);
        jRadioButtonMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setStepValue(1);
                jump.setStepValue(1);
            }
        });

        buttonGroup1.add(jRadioButtonMenuItem2);
        jRadioButtonMenuItem2.setText("5");
        jMenu6.add(jRadioButtonMenuItem2);
        jRadioButtonMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setStepValue(5);
                jump.setStepValue(5);
            }
        });

        buttonGroup1.add(jRadioButtonMenuItem3);
        jRadioButtonMenuItem3.setText("10");
        jMenu6.add(jRadioButtonMenuItem3);
        jRadioButtonMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setStepValue(10);
                jump.setStepValue(10);
            }
        });

        buttonGroup1.add(jRadioButtonMenuItem4);
        jRadioButtonMenuItem4.setText("25");
        jMenu6.add(jRadioButtonMenuItem4);
        jRadioButtonMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setStepValue(25);
                jump.setStepValue(25);
            }
        });

        buttonGroup1.add(jRadioButtonMenuItem6);
        jRadioButtonMenuItem6.setSelected(true);
        jRadioButtonMenuItem6.setText("50");
        jMenu6.add(jRadioButtonMenuItem6);
        jRadioButtonMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setStepValue(50);
                jump.setStepValue(50);
            }
        });

        buttonGroup1.add(jRadioButtonMenuItem5);
        jRadioButtonMenuItem5.setText("100");
        jMenu6.add(jRadioButtonMenuItem5);
        jRadioButtonMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setStepValue(100);
                jump.setStepValue(100);
            }
        });

        jMenu5.add(jMenu6);

        jMenuItem20.setText("Change Fonts");
        jMenuItem20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem20ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem20);

        jMenuItem12.setText("Edit Type List");
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem12);

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("Auto Active");
        jMenu5.add(jCheckBoxMenuItem1);

        jCheckBoxMenuItem2.setSelected(true);
        jCheckBoxMenuItem2.setText("Linked Points");
        jCheckBoxMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItem2ActionPerformed(evt);
            }
        });
        jMenu5.add(jCheckBoxMenuItem2);

        jMenuItem17.setText("Theme Swap (WIP)");
        jMenuItem17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem17ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem17);

        jMenuBar1.add(jMenu5);

        jMenu4.setText("Help");

        jMenuItem8.setText("About");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem8);

        jMenuItem15.setText("Update");
        jMenuItem15.setToolTipText("");
        jMenuItem15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem15ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem15);

        jMenuItem9.setText("Links");
        jMenuItem9.setToolTipText("");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem9);

        jMenuBar1.add(jMenu4);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // <editor-fold defaultstate="collapsed" desc="Event Code">  
    // Select Delete File Menu Item
    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        jTabbedPane1.setSelectedIndex(0);
        deleteJumperSelection();
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    // Exit Menu Item
    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    // New Jumper Menu Item
    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        if (jTextField1.getText().equals("Jumper")) {
            int dialogResult = JOptionPane.showConfirmDialog(this,
                    "Please give your Jumper a new Name and Save."
                    + "\nCreating a New Jumper will erase current progress."
                    + "\nAre you sure you wish to Continue?",
                    "Create a New Jumper?", JOptionPane.YES_NO_OPTION);
            if (dialogResult != 0) {
                return;
            }
        }

        jTabbedPane1.setSelectedIndex(0);
        resetJumper();
        jTextField1.requestFocus();
        jTextField1.selectAll();
        //createJumper();
        //loadInfo();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    // Select Load Jumper File Menu Item
    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        jTabbedPane1.setSelectedIndex(0);
        saveJumper();
        loadJumperSelection();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    // Save Jumper File Menu Item
    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        //jTabbedPane1.setSelectedIndex(0);
        saveJumper();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(Conversion.jumpToOutputDetails(jump)), null);
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(Conversion.jumpToOutputSelections(jump)), null);
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        if (jTabbedPane1.getSelectedIndex() == 1) {
            saveJumper();

            //ArrayList<String> names = new ArrayList<>();
            String[] names = new String[jumper.getJumpList().size() + 1];
            names[0] = "All Jumps";
            for (int i = 0; i < jumper.getJumpList().size(); i++) {
                names[i + 1] = jumper.getJumpList().get(i).getName();
            }

            Object current = jComboBox3.getSelectedItem();
            defaultComboBoxModel3 = new DefaultComboBoxModel(names);
            jComboBox3.setModel(defaultComboBoxModel3);
            int index = defaultComboBoxModel3.getIndexOf(current);
            if (index > 0) {
                jComboBox3.setSelectedIndex(index);
            }

            defaultComboBoxModel2 = new DefaultComboBoxModel(jumper.getTypesArray());
            jComboBox2.setModel(defaultComboBoxModel2);

            runSearch();
            //loadSearchOptions();
        }
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void jList3ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList3ValueChanged
        if (mouseIsDragging) {
            return;
        }
        if (jList3.getSelectedIndex() != -1) {
            Option so = (Option) model3.getElementAt(jList3.getSelectedIndex());
            jLabel3.setText(so.getJumpName());
            jLabel4.setText(so.getName());
            jLabel5.setText("CP: " + so.getPoints());
            jLabel6.setText(so.getType());
            jCheckBox3.setSelected(so.isChain());
            //jCheckBox6.setSelected(so.isActive());
            jTextArea3.setText(so.getDescription());
            jTextArea3.setCaretPosition(0);
            jTextArea4.setText(so.getNotes());
            jTextArea4.setCaretPosition(0);
        } else {
            jLabel3.setText("Empty Selection");
            jLabel4.setText("Empty Selection");
            jLabel5.setText("CP: ?");
            jLabel6.setText("?");
            jCheckBox3.setSelected(false);
            jTextArea3.setText("");
            jTextArea4.setText("");
        }
    }//GEN-LAST:event_jList3ValueChanged

    private void jTextField4KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField4KeyTyped
        runSearch();
    }//GEN-LAST:event_jTextField4KeyTyped

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        runSearch();
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void jSpinner3StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner3StateChanged
        runSearch();
    }//GEN-LAST:event_jSpinner3StateChanged

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        runSearch();
    }//GEN-LAST:event_jComboBox2ActionPerformed

    // Load Jumper File Button
    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        saveJumper();
        loadJumperSelection();
    }//GEN-LAST:event_jButton6ActionPerformed

    // Option Types
    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        String item = (String) jComboBox1.getSelectedItem();
        option.setType(item);
//        String[] newList = new String[jComboBox1.getItemCount()];
//        newList[0] = (String) jComboBox1.getSelectedItem();
//        for (int i = 1; i < jComboBox1.getItemCount(); i++) {
//            if (!jComboBox1.getItemAt(i-1).equals(item)) {
//                newList[i] = jComboBox1.getItemAt(i);
//            }
//        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    // Option Points
    private void jSpinner2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner2StateChanged
        option.setPoints((Integer) jSpinner2.getValue());
        calcDisplayValue();
        //jLabel2.setText("" + jump.calcCosts());
    }//GEN-LAST:event_jSpinner2StateChanged

    // Option Name Field
    private void jTextField3KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField3KeyReleased
        option.setName(jTextField3.getText());
        model2.setElementAt(option, jList2.getSelectedIndex());
    }//GEN-LAST:event_jTextField3KeyReleased

    //Jump List Change
    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
        if (mouseIsDragging) {
            return;
        }
        if (jList1.getSelectedIndex() != -1) {
            organizeOptions();
            loadJump(jList1.getSelectedIndex());
        }
    }//GEN-LAST:event_jList1ValueChanged

    // Jumper Name Field
    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
        jumper.setName(jTextField1.getText());
    }//GEN-LAST:event_jTextField1KeyReleased

    // saveJumperButton
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        saveJumper();
    }//GEN-LAST:event_jButton5ActionPerformed

    //Add Option Button
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        newOption();
        jList2.setSelectedValue(option, true);
        jTextField3.requestFocus();
        jTextField3.selectAll();
        //save();
    }//GEN-LAST:event_jButton3ActionPerformed

    // Chain Check Box
    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        option.setChain(jCheckBox1.isSelected());
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    // Jump Name Field
    private void jTextField2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField2KeyReleased
        jump.setName(jTextField2.getText());
        jump.getOptionList().forEach((o) -> {
            o.setJumpName(jump.name);
        });
        model1.setElementAt(jump, jList1.getSelectedIndex());
    }//GEN-LAST:event_jTextField2KeyReleased

    // Option Note Area
    private void jTextArea2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea2KeyReleased
        option.setNotes(jTextArea2.getText());
    }//GEN-LAST:event_jTextArea2KeyReleased

    // Add Jump Button
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        newJump();
        jList1.setSelectedValue(jump, true);
        jTextField2.requestFocus();
        jTextField2.selectAll();
    }//GEN-LAST:event_jButton1ActionPerformed

    // Option Description Area
    class Edit {

        boolean type;
        String change;

        Edit(boolean type, String change) {
            this.type = type;
            this.change = change;
        }

        public boolean equals(Edit ref) {
            return ((this.type == ref.type) && this.change.equals(ref.change));
        }
    }

    //ArrayList<Edit> dal = new ArrayList<>();
    private void jTextArea1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea1KeyReleased
        String currentText = jTextArea1.getText();
        option.setDescription(currentText);
    }//GEN-LAST:event_jTextArea1KeyReleased

    // Jump Points
    private void jSpinner1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner1StateChanged
        jump.setPoints((Integer) jSpinner1.getValue());
        calcDisplayValue();
        //jLabel2.setText("" + jump.calcCosts());
    }//GEN-LAST:event_jSpinner1StateChanged

    //Option List Change
    private void jList2ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList2ValueChanged
        if (mouseIsDragging) {
            return;
        }
        int[] selected = jList2.getSelectedIndices();
        //System.out.println("Selected: ");
        if (selected.length == 1) {
            loadOption(jList2.getSelectedIndex());
            jump.setSelectedOption(jList2.getSelectedIndex());
            calcDisplayValue();
        } else if (selected.length > 1) {
            int value = 0;
            for (int i = 0; i < selected.length; i++) {
                value += jump.getOption(selected[i]).getPoints();
            }
            setDisplayValue(value);
        }
    }//GEN-LAST:event_jList2ValueChanged

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (jumper == null) {
            System.out.println("Empty Jumper");
            return;
        }
        saveJumper();
        saveTracker();
    }//GEN-LAST:event_formWindowClosing

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        JOptionPane.showMessageDialog(rootPane, new JLabel(
                "<html><center>This project was designed with Jumpchain in mind"
                + "<br>That said, it can be used for most any CYOA"
                + "<br>Infaera has Helped!</center></html>", JLabel.CENTER),
                "Jump Tracker " + VERSION, JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
        JOptionPane.showMessageDialog(rootPane, new MessageWithLink(
                "<center>Infaera's Discord: <a href=\"https://discord.gg/TxnqD3D\">https://discord.gg/TxnqD3D</a>"
                + "<br>Infaera's Patreon: <a href=\"https://www.patreon.com/Infaera\">https://www.patreon.com/Infaera</a></center>"),
                "Infaera's Links", JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jComboBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox3ActionPerformed
        runSearch();
    }//GEN-LAST:event_jComboBox3ActionPerformed

    private void jCheckBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox4ActionPerformed
        option.setActive(jCheckBox4.isSelected());
//        jLabel2.setText("" + jump.calcCosts());
        calcDisplayValue();
        //jList2.setSelectedIndex(index);
        int index = model2.indexOf(option);
        jList2.setSelectedIndex(index);
    }//GEN-LAST:event_jCheckBox4ActionPerformed

    //isActive Search
    private void jCheckBox5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox5ActionPerformed
        runSearch();
    }//GEN-LAST:event_jCheckBox5ActionPerformed

    //Set All Active
    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
        jump.getOptionList().forEach((o) -> {
            o.setActive(true);
        });
        jCheckBox4.setSelected(true);
        jCheckBox4ActionPerformed(evt);
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    //Set All Inactive
    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
        jump.getOptionList().forEach((o) -> {
            o.setActive(false);
        });
        jCheckBox4.setSelected(false);
        jCheckBox4ActionPerformed(evt);
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    TypeFrame typeFrame;
    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed
        if (typeFrame != null && typeFrame.isVisible()) {
            typeFrame.toFront();
        } else {
            typeFrame = new TypeFrame(this, jumper.getTypeList());
            typeFrame.setVisible(true);
        }
    }//GEN-LAST:event_jMenuItem12ActionPerformed

    //Import Jump Menu Item
    private void jMenuItem13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem13ActionPerformed
        importJump();
    }//GEN-LAST:event_jMenuItem13ActionPerformed

    //Export Jump Menu Item
    private void jMenuItem14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem14ActionPerformed
        exportJump();
    }//GEN-LAST:event_jMenuItem14ActionPerformed

    private void jCheckBoxMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItem2ActionPerformed
        jumper.setPointsLinked(jCheckBoxMenuItem2.isSelected());
    }//GEN-LAST:event_jCheckBoxMenuItem2ActionPerformed

    private void jMenuItem15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem15ActionPerformed
        JOptionPane.showMessageDialog(rootPane, new MessageWithLink(
                "<center><a href=\"" + dlink + "\">Google Drive Download</a>"
                + "<br><a href=\"" + reddit + "\">Reddit Link</a></center>"),
                "Version " + newVersion + " Available", JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_jMenuItem15ActionPerformed

    //Copy Option to Another Jump
    private void jMenuItem16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem16ActionPerformed
        copyOptionTo();
    }//GEN-LAST:event_jMenuItem16ActionPerformed

    private void jList2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList2MousePressed
        if (evt.getModifiers() == MouseEvent.BUTTON3_MASK) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem item = new JMenuItem("Set Active");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    activateSelectedOptionsFromList(true);
                }
            });
            JMenuItem item2 = new JMenuItem("Set Inactive");
            item2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    activateSelectedOptionsFromList(false);
                }
            });
            JMenuItem item3 = new JMenuItem("Add Option");
            item3.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    newOption();
                    jList2.setSelectedValue(option, true);
                    jTextField3.requestFocus();
                    jTextField3.selectAll();
                }
            });
            JMenuItem item4 = new JMenuItem("Copy To");
            item4.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    copyOptionTo();
                }
            });
            JMenuItem item5 = new JMenuItem("Delete");
            item5.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteSelectedOptions();
                }
            });

            menu.add(item);
            menu.add(item2);
            menu.add(new JSeparator());
            menu.add(item3);
            menu.add(new JSeparator());
            menu.add(item4);
            menu.add(new JSeparator());
            menu.add(item5);
            menu.show(jList2, evt.getX() + 1, evt.getY() + 1);
        }
    }//GEN-LAST:event_jList2MousePressed

    private void jMenuItem17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem17ActionPerformed
        themeShift();
    }//GEN-LAST:event_jMenuItem17ActionPerformed

    //Delete Jump Menu Item
    private void jMenuItem18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem18ActionPerformed
        deleteSelectedJump();
    }//GEN-LAST:event_jMenuItem18ActionPerformed

    //Delete Option Menu Item
    private void jMenuItem19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem19ActionPerformed
        deleteSelectedOptions();
    }//GEN-LAST:event_jMenuItem19ActionPerformed

    private void jMenuItem20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem20ActionPerformed
        JFontChooser jfc = new JFontChooser();
        jfc.setSelectedFont(this.getFont());
        if (jfc.showDialog(this) == JFontChooser.OK_OPTION) {
            setTextFieldFonts(jfc.getSelectedFont());
        }
    }//GEN-LAST:event_jMenuItem20ActionPerformed

    private void jMenuItem21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem21ActionPerformed
        if (!backupPath.exists()) {
            try {
                backupPath.mkdirs();
                File bPath = new File(backupPath, jumper.getName() + ".jt");
                saveData(bPath, Conversion.jumperToHTML(jumper));
            } catch (Exception e) {
            }
        }

    }//GEN-LAST:event_jMenuItem21ActionPerformed

    private void jMenuItem22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem22ActionPerformed
        newJump();
        jList1.setSelectedValue(jump, true);
        jTextField2.requestFocus();
        jTextField2.selectAll();
    }//GEN-LAST:event_jMenuItem22ActionPerformed

    private void jMenuItem23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem23ActionPerformed
        newOption();
        jList2.setSelectedValue(option, true);
        jTextField3.requestFocus();
        jTextField3.selectAll();
    }//GEN-LAST:event_jMenuItem23ActionPerformed

    private void jList1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MousePressed
        if (evt.getModifiers() == MouseEvent.BUTTON3_MASK) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem item = new JMenuItem("Output Selections");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Toolkit.getDefaultToolkit().getSystemClipboard()
                            .setContents(new StringSelection(Conversion
                                    .jumpToOutputSelections(jump)), null);
                }
            });
            JMenuItem item2 = new JMenuItem("Output Details");
            item2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Toolkit.getDefaultToolkit().getSystemClipboard()
                            .setContents(new StringSelection(Conversion
                                    .jumpToOutputDetails(jump)), null);
                }
            });
            JMenuItem item3 = new JMenuItem("Export Jump");
            item3.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    exportJump();
                }
            });
            JMenuItem item4 = new JMenuItem("Import Jump");
            item4.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    importJump();
                }
            });
            JMenuItem item5 = new JMenuItem("Delete Jump");
            item5.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteSelectedJump();
                }
            });

            menu.add(item);
            menu.add(item2);
            menu.add(new JSeparator());
            menu.add(item3);
            menu.add(item4);
            menu.add(new JSeparator());
            menu.add(item5);
            menu.show(jList1, evt.getX() + 1, evt.getY() + 1);
        }
    }//GEN-LAST:event_jList1MousePressed

    private void jCheckBoxMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItem3ActionPerformed
        if (jCheckBoxMenuItem3.isSelected()) {
            if (!autosave) {
                autosave();
            }
        } else {
            autosave = false;
        }
    }//GEN-LAST:event_jCheckBoxMenuItem3ActionPerformed

    private void jTextArea1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea1KeyPressed
        if (evt.isControlDown()) {
            if (evt.getKeyCode() == KeyEvent.VK_Z) {
                if (manager1.canUndo()) {
                    manager1.undo();
                }
            } else if (evt.getKeyCode() == KeyEvent.VK_Y) {
                if (manager1.canRedo()) {
                    manager1.redo();
                }
            }
        }
    }//GEN-LAST:event_jTextArea1KeyPressed

    private void formWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowLostFocus
        saveJumper();
    }//GEN-LAST:event_formWindowLostFocus

    private void jTextArea2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextArea2KeyPressed
        if (evt.isControlDown()) {
            if (evt.getKeyCode() == KeyEvent.VK_Z) {
                if (manager2.canUndo()) {
                    manager2.undo();
                }
            } else if (evt.getKeyCode() == KeyEvent.VK_Y) {
                if (manager2.canRedo()) {
                    manager2.redo();
                }
            }
        }
    }//GEN-LAST:event_jTextArea2KeyPressed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        newOption();
        jList2.setSelectedValue(option, true);
        jTextField3.requestFocus();
        jTextField3.selectAll();
    }//GEN-LAST:event_jButton2ActionPerformed
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Event Extras">
    private void jScrollPane3AdjustmentEvent(AdjustmentEvent evt) {
        if (option != null) {
            evt.getValue();
            option.setScrollPoint(jScrollPane3.getVerticalScrollBar().getValue());
        }
    }

    public void activateSelectedOptionsFromList(boolean a) {
        for (int index : jList2.getSelectedIndices()) {
            Object o = model2.get(index);
            if (o instanceof Option) {
                ((Option) o).setActive(a);
            }
        }
        jCheckBox4.setSelected(option.isActive());
        this.calcDisplayValue();
    }

    private void setStepValue(int val) {
        spinnerModel1.setStepSize(val);
        spinnerModel2.setStepSize(val);
        spinnerModel3.setStepSize(val);
    }

    private void exportJump() {
        checkPaths();
        if (!jumpPath.exists()) {
            return;
        }
        String text;
        text = Conversion.jumpToHTML(jump);
        File file = new File(jumpPath, jump.getName() + ".jump");
        saveData(file, text);
    }

    private void importJump() {
        String selection = getJumpSelection();
        File path = new File(jumpPath, selection + ".jump");
        if (!path.exists()) {
            return;
        }
        String data = loadData(path);
        Jump newJump;
        try {
            newJump = Conversion.HTMLToJump(data);
        } catch (Exception e) {
            try {
                newJump = C075.textToJump(data);
            } catch (Exception ex) {
                e.printStackTrace();
                return;
            }
        }
        jumper.addJump(newJump);
        model1.addElement(newJump);
        jList1.setSelectedValue(newJump, true);
    }

    private void deleteSelectedJump() {

        int toDeleteIndex = jList1.getSelectedIndex();
        int[] selected = jList1.getSelectedIndices();
        Jump[] list = new Jump[selected.length];

        for (int i = 0; i < list.length; i++) {
            list[i] = jumper.getJump(selected[i]);
        }
        if (0 != JOptionPane.showConfirmDialog(rootPane, "Delete " + Arrays.toString(list) + "?", "Delete Jump", JOptionPane.YES_NO_OPTION)) {
            return;
        }
        for (Jump j : list) {
            model1.removeElement(j);
            jumper.deleteJump(j);
        }
        //loadJump(0);
        if (model1.size() == 0) {
            newJump();
        }
        jList1.setSelectedIndex(0);

        if (model1.size() == 0) {
            newOption();
        }
        if (toDeleteIndex < jumper.getJumpList().size()) {
            jList1.setSelectedIndex(toDeleteIndex);
        } else if (toDeleteIndex > 0) {
            jList1.setSelectedIndex(toDeleteIndex - 1);
        } else {
            jList1.setSelectedIndex(0);
        }
    }

    private void deleteSelectedOptions() {
        int toDeleteIndex = jList2.getSelectedIndex();
        int[] selected = jList2.getSelectedIndices();
        Option[] list = new Option[selected.length];

        for (int i = 0; i < list.length; i++) {
            list[i] = jump.getOption(selected[i]);
        }

        if (0 != JOptionPane.showConfirmDialog(rootPane, "Delete "
                + Arrays.toString(list) + "?", "Delete Option",
                JOptionPane.YES_NO_OPTION)) {
            return;
        }

        for (Option o : list) {
            model2.removeElement(o);
            jump.deleteOption(o);
        }

//        model2.remove(toDeleteIndex);
//        jump.deleteOption(jump.getOption(toDeleteIndex));
        if (model2.size() == 0) {
            newOption();
        }
        if (toDeleteIndex < jump.getOptionList().size()) {
            jList2.setSelectedIndex(toDeleteIndex);
        } else if (toDeleteIndex > 0) {
            jList2.setSelectedIndex(toDeleteIndex - 1);
        } else {
            jList2.setSelectedIndex(0);
        }
    }

    private void copyOptionTo() {
        String[] list = new String[jumper.getJumpList().size()];
        for (int i = 0; i < jumper.getJumpList().size(); i++) {
            list[i] = jumper.getJump(i).getName();
        }

        int[] selected = jList2.getSelectedIndices();

        if (copyTo.equals("") || list.length > 0 && !Arrays.stream(list).anyMatch(copyTo::equals)) {
            copyTo = list[0];
        }

        String s = (String) JOptionPane.showInputDialog(this,
                "", "Copy To", JOptionPane.PLAIN_MESSAGE, null,
                list, copyTo);
        if (s == null || s.equals("")) {
            return;
        }

        copyTo = s;

        for (Jump j : jumper.getJumpList()) {
            if (j.getName().equals(s)) {
                for (int i : selected) {
                    j.addOption(jump.getOption(i));
                }
            }
        }
    }
// </editor-fold>

    //  <editor-fold defaultstate="collapsed" desc="List Sorting Code"> 
    private void organizeOptions() {
        ArrayList<Option> nlist = new ArrayList();
        for (int i = 0; i < model2.getSize(); i++) {
            nlist.add((Option) model2.get(i));
        }
        //jump.options = nlist;
        jump.setOptions(nlist);
    }

    private void organizeJumps() {
        ArrayList<Jump> nlist = new ArrayList();
        for (int i = 0; i < model1.getSize(); i++) {
            nlist.add((Jump) model1.get(i));
        }
        //jumper.jumpList = nlist;
        jumper.setJumpList(nlist);
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="FileIO Code"> 
    private String loadData(File link) {
        try {
            return FileIO.bufferedRead(link);
        } catch (Exception ex) {
//            Logger.getLogger(TrackerFrame.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
        return null;
    }

    private void saveData(File link, String data) {
        try {
            FileIO.bufferedWrite(link, data);
        } catch (Exception ex) {
//            Logger.getLogger(TrackerFrame.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }

    private void saveTracker() {

        //String data = "";
        //data = jumperList.stream().map((s) -> s+"\n").reduce(data, String::concat);
        String tData = "file=" + jumperFile.getName();
        tData += "\n" + "location=" + ((int) this.getLocation().getX()) + "," + ((int) this.getLocation().getY());
        tData += "\n" + "dimension=" + ((int) this.getSize().getWidth()) + "," + ((int) this.getSize().getHeight());
        tData += "\n" + "font=" + this.getFont().getName() + "," + this.getFont().getStyle() + "," + this.getFont().getSize();
        saveData(trackerFile, tData);//data);
    }

    private void saveJumper() {

        checkPaths();
        if (!jumperPath.exists()) {
            System.out.println("Jumper Path Unavailable");
            return;
        }

        organizeOptions();
        organizeJumps();

        if (jTextField1.getText().equals("")) {
            return;
        }
        if (!jumperFile.exists()) {
            jumperPath.mkdirs();
        }
        if (jTextField1.getText().equals(jumperFile.getName().replace(".jt", ""))) {
//            saveData(jumperFile, Conversion.jumperToText(jumper));
            String data = Conversion.jumperToHTML(jumper);
            if (data == null && data.equals("")) {
                System.out.println("Cannot Save Data: Empty String");
            } else {
                saveData(jumperFile, data);
            }
        } else {
            File temp = jumperFile;
            jumperFile = new File(jumperPath, jTextField1.getText() + ".jt");
            saveData(jumperFile, Conversion.jumperToHTML(jumper));
            saveTracker();
            temp.delete();
        }
        saveTime = System.currentTimeMillis();
    }
// </editor-fold>

    public void themeShift() {

        theme = !theme;

        ArrayList<Component> myComponents = new ArrayList<Component>();
        myComponents.addAll(getAllComponents(this));
        myComponents.add(this);

        if (theme) { //Dark Theme
            for (Component c : myComponents) {
                c.setForeground(new Color(225, 225, 225));
                c.setBackground(new Color(64, 64, 64));
                if (c instanceof JTextArea) {
                    ((JTextArea) c).setCaretColor(new Color(235, 235, 235));
                } else if (c instanceof JTextField) {
                    ((JTextField) c).setCaretColor(new Color(235, 235, 235));
                }
            }
        } else if (!theme) { //Light Theme
            for (Component c : myComponents) {
                c.setForeground(new Color(25, 25, 25));
                c.setBackground(new Color(222, 222, 222));
                if (c instanceof JPanel) {
                    c.setBackground(new Color(225, 225, 225));
                } else if (c instanceof JButton) {
                    c.setBackground(new Color(151, 151, 185));
                } else if (c instanceof JTabbedPane) {
                    c.setBackground(new Color(225, 225, 225));
                } else if (c instanceof JMenu) {
                    c.setBackground(new Color(225, 225, 225));
                } else if (c instanceof JTextArea) {
                    ((JTextArea) c).setCaretColor(Color.BLACK);
                    ((JTextArea) c).setBackground(Color.WHITE);
                } else if (c instanceof JTextField) {
                    ((JTextField) c).setCaretColor(Color.BLACK);
                    ((JTextField) c).setBackground(Color.WHITE);
                } else if (c instanceof JList) {
                    ((JList) c).setBackground(Color.WHITE);
                }
            }
        }
    }

    public static ArrayList<Component> getAllComponents(final Container c) {
        Component[] components = c.getComponents();
        ArrayList<Component> returnList = new ArrayList<Component>();
        for (Component component : components) {
            returnList.add(component);
            if (component instanceof Container) {
                returnList.addAll(getAllComponents((Container) component));
            }
        }
        return returnList;
    }

    public static ArrayList<Component> getRelevantComponents(final Container c) {
        Component[] components = c.getComponents();
        ArrayList<Component> returnList = new ArrayList<Component>();
        for (Component component : components) {
            returnList.add(component);
            if (component instanceof JTextArea
                    || component instanceof JScrollPane
                    || component instanceof JTextField
                    || component instanceof JLabel
                    || component instanceof JComboBox) {
                returnList.addAll(getAllComponents((Container) component));
            }
        }
        return returnList;
    }

    // <editor-fold defaultstate="collapsed" desc="Declaration Code"> 
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem2;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem3;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.DefaultComboBoxModel defaultComboBoxModel1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.DefaultComboBoxModel defaultComboBoxModel2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.DefaultComboBoxModel defaultComboBoxModel3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.DefaultListModel model1;
    private javax.swing.JList<String> jList1;
    private javax.swing.DefaultListModel model2;
    private javax.swing.JList<String> jList2;
    private javax.swing.DefaultListModel model3;
    private javax.swing.JList<String> jList3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem13;
    private javax.swing.JMenuItem jMenuItem14;
    private javax.swing.JMenuItem jMenuItem15;
    private javax.swing.JMenuItem jMenuItem16;
    private javax.swing.JMenuItem jMenuItem17;
    private javax.swing.JMenuItem jMenuItem18;
    private javax.swing.JMenuItem jMenuItem19;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem20;
    private javax.swing.JMenuItem jMenuItem21;
    private javax.swing.JMenuItem jMenuItem22;
    private javax.swing.JMenuItem jMenuItem23;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem2;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem3;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem4;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem5;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JSpinner jSpinner1;
    private SpinnerNumberModel spinnerModel1;
    private javax.swing.JSpinner jSpinner2;
    private SpinnerNumberModel spinnerModel2;
    private javax.swing.JSpinner jSpinner3;
    private SpinnerNumberModel spinnerModel3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextArea jTextArea3;
    private javax.swing.JTextArea jTextArea4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Mouse Adaptor Class"> 
    class TrackerFrameMouseAdaptor extends MouseInputAdapter {

        private boolean mouseDragging = false;
        private int dragSourceIndex;

        private JList jList;
        private DefaultListModel model;

        TrackerFrameMouseAdaptor(JList rootJList, DefaultListModel rootModel) {
            jList = rootJList;
            model = rootModel;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                dragSourceIndex = jList.getSelectedIndex();
                mouseDragging = true;
                mouseIsDragging = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            mouseDragging = false;
            mouseIsDragging = false;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (mouseDragging) {
                int currentIndex = jList.locationToIndex(e.getPoint());
                if (currentIndex != dragSourceIndex) {
                    int dragTargetIndex = jList.getSelectedIndex();
                    Object dragElement = model.get(dragSourceIndex);
                    model.remove(dragSourceIndex);
                    model.add(dragTargetIndex, dragElement);
                    dragSourceIndex = currentIndex;
                    if (dragElement instanceof Option) {
                        jump.getOptionList().remove((Option) dragElement);
                        jump.getOptionList().add(dragTargetIndex, (Option) dragElement);
                    } else if (dragElement instanceof Jump) {
                        jumper.getJumpList().remove((Jump) dragElement);
                        jumper.getJumpList().add(dragTargetIndex, (Jump) dragElement);
                    }
                }
            }
        }
    }
// </editor-fold>

}

// <editor-fold defaultstate="collapsed" desc="MessageWithLink Class"> 
class MessageWithLink extends JEditorPane {

    private static final long serialVersionUID = 1L;

    public MessageWithLink(String htmlBody) {
        super("text/html", "<html><body style=\"" + getStyle() + "\">" + htmlBody + "</body></html>");
        addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    URLIO.openWebpage(e.getURL());
                }
            }
        });
        setEditable(false);
        setBorder(null);
    }

    static StringBuffer getStyle() {
        // for copying style
        JLabel label = new JLabel();
        Font font = label.getFont();
        Color color = label.getBackground();

        // create some css from the label's font
        StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
        style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
        style.append("font-size:" + font.getSize() + "pt;");
        style.append("background-color: rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ");");
        return style;
    }
}
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Theme Colors"> 
class MyDefaultMetalTheme extends DefaultMetalTheme {

    public ColorUIResource getWindowTitleInactiveBackground() {
        return new ColorUIResource(java.awt.Color.BLUE);
    }

    public ColorUIResource getWindowTitleBackground() {
        return new ColorUIResource(java.awt.Color.BLUE);
    }

    public ColorUIResource getPrimaryControlHighlight() {
        return new ColorUIResource(java.awt.Color.BLUE);
    }

    public ColorUIResource getPrimaryControlDarkShadow() {
        return new ColorUIResource(java.awt.Color.BLUE);
    }

    public ColorUIResource getPrimaryControl() {
        return new ColorUIResource(java.awt.Color.BLUE);
    }

    public ColorUIResource getControlHighlight() {
        return new ColorUIResource(java.awt.Color.BLUE);
    }

    public ColorUIResource getControlDarkShadow() {
        return new ColorUIResource(java.awt.Color.BLUE);
    }

    public ColorUIResource getControl() {
        return new ColorUIResource(java.awt.Color.BLUE);
    }
}
// </editor-fold> 
// <editor-fold defaultstate="collapsed" desc="ProxyAction"> 

class ProxyAction extends AbstractAction {

    private Action action;

    public ProxyAction(Action action) {
        this.action = action;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        action.actionPerformed(e);
        System.out.println("Paste Occured...");
    }

}
// </editor-fold> 
