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
import java.util.logging.Level;
import java.util.logging.Logger;
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
        fldOptionDescription.getDocument().addUndoableEditListener(manager1);
        fldOptionNotes.getDocument().addUndoableEditListener(manager2);

        TrackerFrameMouseAdaptor myMouseAdaptor1 = new TrackerFrameMouseAdaptor(pnJumpList, model1);
        pnJumpList.addMouseListener(myMouseAdaptor1);
        pnJumpList.addMouseMotionListener(myMouseAdaptor1);
        TrackerFrameMouseAdaptor myMouseAdaptor2 = new TrackerFrameMouseAdaptor(pnOptionList, model2);
        pnOptionList.addMouseListener(myMouseAdaptor2);
        pnOptionList.addMouseMotionListener(myMouseAdaptor2);
        TrackerFrameMouseAdaptor myMouseAdaptor3 = new TrackerFrameMouseAdaptor(jList3, model3);
        jList3.addMouseListener(myMouseAdaptor3);
        jList3.addMouseMotionListener(myMouseAdaptor3);

        pnOptionList.setCellRenderer(new DefaultListCellRenderer() {
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
                            for (int i : pnOptionList.getSelectedIndices()) {
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
            pnJumpList.setSelectedIndex(0);
        }
        if (!model2.isEmpty()) {
            pnOptionList.setSelectedIndex(0);
        }

    }

    public void updateTypeComboBox() {
        defaultComboBoxModel1 = new DefaultComboBoxModel(jumper.getTypesArray());
        cbOptionType.setModel(defaultComboBoxModel1);
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

    public void newOption() {
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
        cbOptionType.setSelectedItem(tempType);
    }

    private void loadJump(int index) {
        jump = jumper.getJump(index);
        jTextField2.setText(jump.getName());
        spnJumpBaseCP.setValue(jump.getPoints());
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
        pnOptionList.setSelectedIndex(jump.getSelectedOption());
    }

    private void loadOption(int index) {
        option = jump.getOption(index);
        loadOption();
    }

    private void loadOption() {
        fldOptionName.setText(option.getName());
        fldOptionDescription.setText(option.getDescription());
        fldOptionDescription.setCaretPosition(0);
        fldOptionNotes.setText(option.getNotes());
        fldOptionNotes.setCaretPosition(0);
        spnOptionCost.setValue(option.getPoints());
        chkChainOption.setSelected(option.isChain());
        chkActiveOption.setSelected(option.isActive());
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
        lblAvailableCP.setText("" + value);
        if (value < 0) {
            if (theme) {
                lblAvailableCP.setForeground(LIGHT_RED.brighter().brighter());
            } else {
                lblAvailableCP.setForeground(DARK_RED);
            }
        } else {
            if (theme) {
                lblAvailableCP.setForeground(new Color(222, 222, 222));
            } else {
                lblAvailableCP.setForeground(new Color(22, 22, 22));
            }
        }
    }

    private void setTextFieldFonts(Font font) {
        this.setFont(font);
        fldOptionDescription.setFont(font);
        fldOptionNotes.setFont(font);
        jTextArea3.setFont(font);
        jTextArea4.setFont(font);
        jTextField1.setFont(font);
        jTextField2.setFont(font);
        fldOptionName.setFont(font);
        jTextField4.setFont(font);
        pnJumpList.setFont(font);
        pnOptionList.setFont(font);
        jList3.setFont(font);
        cbOptionType.setFont(font);
        jComboBox2.setFont(font);
        jComboBox3.setFont(font);
//        jSpinner1.setFont(font);
//        jSpinner2.setFont(font);
//        jSpinner3.setFont(font);
        lblCP.setFont(font);
        lblAvailableCP.setFont(font);
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
        pnOptionList = new javax.swing.JList<>();
        spnJumpBaseCP = new javax.swing.JSpinner();
        jScrollPane3 = new javax.swing.JScrollPane();
        fldOptionDescription = new javax.swing.JTextArea()
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
        lblCP = new javax.swing.JLabel();
        btnAddJump = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        fldOptionNotes = new javax.swing.JTextArea();
        jTextField2 = new javax.swing.JTextField();
        chkChainOption = new javax.swing.JCheckBox();
        lblAvailableCP = new javax.swing.JLabel();
        btnAddOption = new javax.swing.JButton();
        btnSaveJumper = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        pnJumpList = new javax.swing.JList<>();
        fldOptionName = new javax.swing.JTextField();
        spnOptionCost = new javax.swing.JSpinner();
        cbOptionType = new javax.swing.JComboBox<>();
        btnLoadJumper = new javax.swing.JButton();
        chkActiveOption = new javax.swing.JCheckBox();
        btnAddMulti = new javax.swing.JButton();
        spnOptionCount = new javax.swing.JSpinner();
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
        mnuFile = new javax.swing.JMenu();
        mniImportJumperCSV = new javax.swing.JMenuItem();
        mniImportJumpCSV = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        mniExit = new javax.swing.JMenuItem();
        mnuJumper = new javax.swing.JMenu();
        mniNewJumper = new javax.swing.JMenuItem();
        mniLoadJumper = new javax.swing.JMenuItem();
        mniSaveJumper = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        mniBackupJumper = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        mniAutoSave = new javax.swing.JCheckBoxMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        mniDeleteJumper = new javax.swing.JMenuItem();
        mnuJump = new javax.swing.JMenu();
        mniAddJump = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        mniExportJump = new javax.swing.JMenuItem();
        mniImportJump = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        mniDeleteJump = new javax.swing.JMenuItem();
        mnuOption = new javax.swing.JMenu();
        jMenuItem23 = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jMenuItem16 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jMenuItem19 = new javax.swing.JMenuItem();
        mnuConfig = new javax.swing.JMenu();
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
        mnuHelp = new javax.swing.JMenu();
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
        pnOptionList.setModel(model2);
        pnOptionList.setToolTipText("Selection List");
        pnOptionList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                pnOptionListMousePressed(evt);
            }
        });
        pnOptionList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                pnOptionListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(pnOptionList);

        spnJumpBaseCP.setToolTipText("Points for the Jump");
        spnJumpBaseCP.setValue(1000);
        spinnerModel1 = (SpinnerNumberModel) new SpinnerNumberModel();// spnJumpBaseCP.getModel();
        spinnerModel1.setStepSize(25);
        spnJumpBaseCP.setModel(spinnerModel1);
        spnJumpBaseCP.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnJumpBaseCPStateChanged(evt);
            }
        });

        fldOptionDescription.setColumns(20);
        fldOptionDescription.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        fldOptionDescription.setLineWrap(true);
        fldOptionDescription.setRows(5);
        fldOptionDescription.setToolTipText("Description");
        fldOptionDescription.setWrapStyleWord(true);
        fldOptionDescription.setMargin(new java.awt.Insets(5, 5, 5, 5));
        fldOptionDescription.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                fldOptionDescriptionKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fldOptionDescriptionKeyReleased(evt);
            }
        });
        jScrollPane3.setViewportView(fldOptionDescription);
        jScrollPane3.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener(){
            @Override
            public void adjustmentValueChanged(AdjustmentEvent evt) {
                jScrollPane3AdjustmentEvent(evt);
            }
        });

        lblCP.setText("CP");

        btnAddJump.setText("Add Jump");
        btnAddJump.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        btnAddJump.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddJumpActionPerformed(evt);
            }
        });

        fldOptionNotes.setColumns(20);
        fldOptionNotes.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        fldOptionNotes.setLineWrap(true);
        fldOptionNotes.setRows(5);
        fldOptionNotes.setToolTipText("Notes");
        fldOptionNotes.setWrapStyleWord(true);
        fldOptionNotes.setMargin(new java.awt.Insets(5, 5, 5, 5));
        fldOptionNotes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                fldOptionNotesKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fldOptionNotesKeyReleased(evt);
            }
        });
        jScrollPane4.setViewportView(fldOptionNotes);

        jTextField2.setToolTipText("Jumpchain Document Name");
        jTextField2.setMargin(new java.awt.Insets(0, 3, 0, 0));
        jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField2KeyReleased(evt);
            }
        });

        chkChainOption.setToolTipText("Chain");
        chkChainOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkChainOptionActionPerformed(evt);
            }
        });

        lblAvailableCP.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lblAvailableCP.setText("1000");
        lblAvailableCP.setToolTipText("End Value");

        btnAddOption.setText("Add Option");
        btnAddOption.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        btnAddOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddOptionActionPerformed(evt);
            }
        });

        btnSaveJumper.setText("Save");
        btnSaveJumper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveJumperActionPerformed(evt);
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
        pnJumpList.setModel(model1);
        pnJumpList.setToolTipText("CYOA List");
        pnJumpList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                pnJumpListMousePressed(evt);
            }
        });
        pnJumpList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                pnJumpListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(pnJumpList);

        fldOptionName.setToolTipText("Option Name");
        fldOptionName.setMargin(new java.awt.Insets(0, 3, 0, 0));
        fldOptionName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fldOptionNameKeyReleased(evt);
            }
        });

        spinnerModel2 = (SpinnerNumberModel) new SpinnerNumberModel();//jSpinner2.getModel();
        spinnerModel2.setStepSize(25);
        spnOptionCost.setModel(spinnerModel2);
        spnOptionCost.setToolTipText("Option Value");
        spnOptionCost.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnOptionCostStateChanged(evt);
            }
        });

        defaultComboBoxModel1 = new DefaultComboBoxModel();
        cbOptionType.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        cbOptionType.setModel(defaultComboBoxModel1);
        cbOptionType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbOptionTypeActionPerformed(evt);
            }
        });

        btnLoadJumper.setText("Load");
        btnLoadJumper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadJumperActionPerformed(evt);
            }
        });

        chkActiveOption.setToolTipText("Active");
        chkActiveOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkActiveOptionActionPerformed(evt);
            }
        });

        btnAddMulti.setText("Add Multi Option");
        btnAddMulti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddMultiActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnAddJump, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnLoadJumper, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSaveJumper, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(btnAddMulti)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnOptionCount, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(btnAddOption, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(lblCP)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(spnJumpBaseCP, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(12, 12, 12)
                            .addComponent(lblAvailableCP, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(12, 12, 12))
                        .addComponent(jTextField2)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                    .addComponent(fldOptionName)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(cbOptionType, 0, 120, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnOptionCost, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkActiveOption)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkChainOption))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fldOptionName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(spnJumpBaseCP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblAvailableCP)
                                .addComponent(cbOptionType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(spnOptionCost, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblCP, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(btnLoadJumper, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnSaveJumper, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAddOption)
                            .addComponent(btnAddJump))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jScrollPane2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(btnAddMulti, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(spnOptionCount)))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkActiveOption)
                            .addComponent(chkChainOption))
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
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

        spinnerModel3 = (SpinnerNumberModel) new SpinnerNumberModel();// spnJumpBaseCP.getModel();
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
                            .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
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

        mnuFile.setText("File");

        mniImportJumperCSV.setText("Import Jumper CSV");
        mniImportJumperCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniImportJumperCSVActionPerformed(evt);
            }
        });
        mnuFile.add(mniImportJumperCSV);

        mniImportJumpCSV.setText("Import Jump CSV");
        mniImportJumpCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniImportJumpCSVActionPerformed(evt);
            }
        });
        mnuFile.add(mniImportJumpCSV);
        mnuFile.add(jSeparator10);

        mniExit.setText("Exit");
        mniExit.setToolTipText("Close Program");
        mniExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniExitActionPerformed(evt);
            }
        });
        mnuFile.add(mniExit);

        jMenuBar1.add(mnuFile);

        mnuJumper.setText("Jumper");

        mniNewJumper.setText("New");
        mniNewJumper.setToolTipText("Create New Jumper");
        mniNewJumper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniNewJumperActionPerformed(evt);
            }
        });
        mnuJumper.add(mniNewJumper);

        mniLoadJumper.setText("Load");
        mniLoadJumper.setToolTipText("Load Existing Jumper");
        mniLoadJumper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniLoadJumperActionPerformed(evt);
            }
        });
        mnuJumper.add(mniLoadJumper);

        mniSaveJumper.setText("Save");
        mniSaveJumper.setToolTipText("Save Jumper");
        mniSaveJumper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniSaveJumperActionPerformed(evt);
            }
        });
        mnuJumper.add(mniSaveJumper);
        mnuJumper.add(jSeparator9);

        mniBackupJumper.setText("Backup");
        mniBackupJumper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniBackupJumperActionPerformed(evt);
            }
        });
        mnuJumper.add(mniBackupJumper);
        mnuJumper.add(jSeparator8);

        mniAutoSave.setSelected(true);
        mniAutoSave.setText("Auto Save");
        mniAutoSave.setToolTipText("Always On at Start");
        mniAutoSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniAutoSaveActionPerformed(evt);
            }
        });
        mnuJumper.add(mniAutoSave);
        mnuJumper.add(jSeparator2);

        mniDeleteJumper.setText("Delete");
        mniDeleteJumper.setToolTipText("Delete a Jumper");
        mniDeleteJumper.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniDeleteJumperActionPerformed(evt);
            }
        });
        mnuJumper.add(mniDeleteJumper);

        jMenuBar1.add(mnuJumper);

        mnuJump.setText("Jump");

        mniAddJump.setText("Add Jump");
        mniAddJump.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniAddJumpActionPerformed(evt);
            }
        });
        mnuJump.add(mniAddJump);
        mnuJump.add(jSeparator5);

        jMenuItem7.setText("Output Selection");
        jMenuItem7.setToolTipText("Copy Jump Selections to the Clipboard");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        mnuJump.add(jMenuItem7);

        jMenuItem6.setText("Output Details");
        jMenuItem6.setToolTipText("Copy Jump Details to Clipboard");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        mnuJump.add(jMenuItem6);
        mnuJump.add(jSeparator7);

        mniExportJump.setText("Export Jump");
        mniExportJump.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniExportJumpActionPerformed(evt);
            }
        });
        mnuJump.add(mniExportJump);

        mniImportJump.setText("Import Jump");
        mniImportJump.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniImportJumpActionPerformed(evt);
            }
        });
        mnuJump.add(mniImportJump);
        mnuJump.add(jSeparator3);

        mniDeleteJump.setText("Delete Jump");
        mniDeleteJump.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniDeleteJumpActionPerformed(evt);
            }
        });
        mnuJump.add(mniDeleteJump);

        jMenuBar1.add(mnuJump);

        mnuOption.setText("Option");

        jMenuItem23.setText("Add Option");
        jMenuItem23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem23ActionPerformed(evt);
            }
        });
        mnuOption.add(jMenuItem23);
        mnuOption.add(jSeparator6);

        jMenuItem16.setText("Copy To");
        jMenuItem16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem16ActionPerformed(evt);
            }
        });
        mnuOption.add(jMenuItem16);

        jMenuItem10.setText("Set All Active");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        mnuOption.add(jMenuItem10);

        jMenuItem11.setText("Set All Inactive");
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        mnuOption.add(jMenuItem11);
        mnuOption.add(jSeparator4);

        jMenuItem19.setText("Delete Option");
        jMenuItem19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem19ActionPerformed(evt);
            }
        });
        mnuOption.add(jMenuItem19);

        jMenuBar1.add(mnuOption);

        mnuConfig.setText("Config");

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

        mnuConfig.add(jMenu6);

        jMenuItem20.setText("Change Fonts");
        jMenuItem20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem20ActionPerformed(evt);
            }
        });
        mnuConfig.add(jMenuItem20);

        jMenuItem12.setText("Edit Type List");
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        mnuConfig.add(jMenuItem12);

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("Auto Active");
        mnuConfig.add(jCheckBoxMenuItem1);

        jCheckBoxMenuItem2.setSelected(true);
        jCheckBoxMenuItem2.setText("Linked Points");
        jCheckBoxMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItem2ActionPerformed(evt);
            }
        });
        mnuConfig.add(jCheckBoxMenuItem2);

        jMenuItem17.setText("Theme Swap (WIP)");
        jMenuItem17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem17ActionPerformed(evt);
            }
        });
        mnuConfig.add(jMenuItem17);

        jMenuBar1.add(mnuConfig);

        mnuHelp.setText("Help");

        jMenuItem8.setText("About");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        mnuHelp.add(jMenuItem8);

        jMenuItem15.setText("Update");
        jMenuItem15.setToolTipText("");
        jMenuItem15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem15ActionPerformed(evt);
            }
        });
        mnuHelp.add(jMenuItem15);

        jMenuItem9.setText("Links");
        jMenuItem9.setToolTipText("");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        mnuHelp.add(jMenuItem9);

        jMenuBar1.add(mnuHelp);

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
    private void mniDeleteJumperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniDeleteJumperActionPerformed
        jTabbedPane1.setSelectedIndex(0);
        deleteJumperSelection();
    }//GEN-LAST:event_mniDeleteJumperActionPerformed

    // Exit Menu Item
    private void mniExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniExitActionPerformed
        this.dispose();
    }//GEN-LAST:event_mniExitActionPerformed

    // New Jumper Menu Item
    private void mniNewJumperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniNewJumperActionPerformed
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
    }//GEN-LAST:event_mniNewJumperActionPerformed

    // Select Load Jumper File Menu Item
    private void mniLoadJumperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniLoadJumperActionPerformed
        jTabbedPane1.setSelectedIndex(0);
        saveJumper();
        loadJumperSelection();
    }//GEN-LAST:event_mniLoadJumperActionPerformed

    // Save Jumper File Menu Item
    private void mniSaveJumperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSaveJumperActionPerformed
        //jTabbedPane1.setSelectedIndex(0);
        saveJumper();
    }//GEN-LAST:event_mniSaveJumperActionPerformed

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
    private void btnLoadJumperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadJumperActionPerformed
        saveJumper();
        loadJumperSelection();
    }//GEN-LAST:event_btnLoadJumperActionPerformed

    // Option Types
    private void cbOptionTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbOptionTypeActionPerformed
        String item = (String) cbOptionType.getSelectedItem();
        option.setType(item);
//        String[] newList = new String[jComboBox1.getItemCount()];
//        newList[0] = (String) jComboBox1.getSelectedItem();
//        for (int i = 1; i < jComboBox1.getItemCount(); i++) {
//            if (!jComboBox1.getItemAt(i-1).equals(item)) {
//                newList[i] = jComboBox1.getItemAt(i);
//            }
//        }
    }//GEN-LAST:event_cbOptionTypeActionPerformed

    // Option Points
    private void spnOptionCostStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnOptionCostStateChanged
        option.setPoints((Integer) spnOptionCost.getValue());
        calcDisplayValue();
        //jLabel2.setText("" + jump.calcCosts());
    }//GEN-LAST:event_spnOptionCostStateChanged

    // Option Name Field
    private void fldOptionNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fldOptionNameKeyReleased
        option.setName(fldOptionName.getText());
        model2.setElementAt(option, pnOptionList.getSelectedIndex());
    }//GEN-LAST:event_fldOptionNameKeyReleased

    //Jump List Change
    private void pnJumpListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_pnJumpListValueChanged
        if (mouseIsDragging) {
            return;
        }
        if (pnJumpList.getSelectedIndex() != -1) {
            organizeOptions();
            loadJump(pnJumpList.getSelectedIndex());
        }
    }//GEN-LAST:event_pnJumpListValueChanged

    // Jumper Name Field
    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
        jumper.setName(jTextField1.getText());
    }//GEN-LAST:event_jTextField1KeyReleased

    // saveJumperButton
    private void btnSaveJumperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveJumperActionPerformed
        saveJumper();
    }//GEN-LAST:event_btnSaveJumperActionPerformed

    //Add Option Button
    private void btnAddOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddOptionActionPerformed
        newOption();
        pnOptionList.setSelectedValue(option, true);
        fldOptionName.requestFocus();
        fldOptionName.selectAll();
        //save();
    }//GEN-LAST:event_btnAddOptionActionPerformed

    // Chain Check Box
    private void chkChainOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkChainOptionActionPerformed
        option.setChain(chkChainOption.isSelected());
    }//GEN-LAST:event_chkChainOptionActionPerformed

    // Jump Name Field
    private void jTextField2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField2KeyReleased
        jump.setName(jTextField2.getText());
        jump.getOptionList().forEach((o) -> {
            o.setJumpName(jump.name);
        });
        model1.setElementAt(jump, pnJumpList.getSelectedIndex());
    }//GEN-LAST:event_jTextField2KeyReleased

    // Option Note Area
    private void fldOptionNotesKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fldOptionNotesKeyReleased
        option.setNotes(fldOptionNotes.getText());
    }//GEN-LAST:event_fldOptionNotesKeyReleased

    // Add Jump Button
    private void btnAddJumpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddJumpActionPerformed
        newJump();
        pnJumpList.setSelectedValue(jump, true);
        jTextField2.requestFocus();
        jTextField2.selectAll();
    }//GEN-LAST:event_btnAddJumpActionPerformed

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
    private void fldOptionDescriptionKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fldOptionDescriptionKeyReleased
        String currentText = fldOptionDescription.getText();
        option.setDescription(currentText);
    }//GEN-LAST:event_fldOptionDescriptionKeyReleased

    // Jump Points
    private void spnJumpBaseCPStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnJumpBaseCPStateChanged
        jump.setPoints((Integer) spnJumpBaseCP.getValue());
        calcDisplayValue();
        //jLabel2.setText("" + jump.calcCosts());
    }//GEN-LAST:event_spnJumpBaseCPStateChanged

    //Option List Change
    private void pnOptionListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_pnOptionListValueChanged
        if (mouseIsDragging) {
            return;
        }
        int[] selected = pnOptionList.getSelectedIndices();
        //System.out.println("Selected: ");
        if (selected.length == 1) {
            loadOption(pnOptionList.getSelectedIndex());
            jump.setSelectedOption(pnOptionList.getSelectedIndex());
            calcDisplayValue();
        } else if (selected.length > 1) {
            int value = 0;
            for (int i = 0; i < selected.length; i++) {
                value += jump.getOption(selected[i]).getPoints();
            }
            setDisplayValue(value);
        }
    }//GEN-LAST:event_pnOptionListValueChanged

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

    private void chkActiveOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkActiveOptionActionPerformed
        option.setActive(chkActiveOption.isSelected());
//        jLabel2.setText("" + jump.calcCosts());
        calcDisplayValue();
        //jList2.setSelectedIndex(index);
        int index = model2.indexOf(option);
        pnOptionList.setSelectedIndex(index);
    }//GEN-LAST:event_chkActiveOptionActionPerformed

    //isActive Search
    private void jCheckBox5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox5ActionPerformed
        runSearch();
    }//GEN-LAST:event_jCheckBox5ActionPerformed

    //Set All Active
    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
        jump.getOptionList().forEach((o) -> {
            o.setActive(true);
        });
        chkActiveOption.setSelected(true);
        chkActiveOptionActionPerformed(evt);
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    //Set All Inactive
    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
        jump.getOptionList().forEach((o) -> {
            o.setActive(false);
        });
        chkActiveOption.setSelected(false);
        chkActiveOptionActionPerformed(evt);
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
    private void mniImportJumpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniImportJumpActionPerformed
        importJump();
    }//GEN-LAST:event_mniImportJumpActionPerformed

    //Export Jump Menu Item
    private void mniExportJumpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniExportJumpActionPerformed
        exportJump();
    }//GEN-LAST:event_mniExportJumpActionPerformed

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

    private void pnOptionListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnOptionListMousePressed
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
                    pnOptionList.setSelectedValue(option, true);
                    fldOptionName.requestFocus();
                    fldOptionName.selectAll();
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
            menu.show(pnOptionList, evt.getX() + 1, evt.getY() + 1);
        }
    }//GEN-LAST:event_pnOptionListMousePressed

    private void jMenuItem17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem17ActionPerformed
        themeShift();
    }//GEN-LAST:event_jMenuItem17ActionPerformed

    //Delete Jump Menu Item
    private void mniDeleteJumpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniDeleteJumpActionPerformed
        deleteSelectedJump();
    }//GEN-LAST:event_mniDeleteJumpActionPerformed

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

    private void mniBackupJumperActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniBackupJumperActionPerformed
        if (!backupPath.exists()) {
            try {
                backupPath.mkdirs();
                File bPath = new File(backupPath, jumper.getName() + ".jt");
                saveData(bPath, Conversion.jumperToHTML(jumper));
            } catch (Exception e) {
            }
        }else{
            File bPath = new File(backupPath, jumper.getName() + ".jt");
            saveData(bPath, Conversion.jumperToHTML(jumper));
        }

    }//GEN-LAST:event_mniBackupJumperActionPerformed

    private void mniAddJumpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniAddJumpActionPerformed
        newJump();
        pnJumpList.setSelectedValue(jump, true);
        jTextField2.requestFocus();
        jTextField2.selectAll();
    }//GEN-LAST:event_mniAddJumpActionPerformed

    private void jMenuItem23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem23ActionPerformed
        newOption();
        pnOptionList.setSelectedValue(option, true);
        fldOptionName.requestFocus();
        fldOptionName.selectAll();
    }//GEN-LAST:event_jMenuItem23ActionPerformed

    private void pnJumpListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnJumpListMousePressed
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
            menu.show(pnJumpList, evt.getX() + 1, evt.getY() + 1);
        }
    }//GEN-LAST:event_pnJumpListMousePressed

    private void mniAutoSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniAutoSaveActionPerformed
        if (mniAutoSave.isSelected()) {
            if (!autosave) {
                autosave();
            }
        } else {
            autosave = false;
        }
    }//GEN-LAST:event_mniAutoSaveActionPerformed

    private void fldOptionDescriptionKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fldOptionDescriptionKeyPressed
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
    }//GEN-LAST:event_fldOptionDescriptionKeyPressed

    private void formWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowLostFocus
        saveJumper();
    }//GEN-LAST:event_formWindowLostFocus

    private void fldOptionNotesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fldOptionNotesKeyPressed
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
    }//GEN-LAST:event_fldOptionNotesKeyPressed

    private void btnAddMultiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddMultiActionPerformed
        int count = Integer.parseInt(spnOptionCount.getValue().toString());
        for( int i = 0; i < count; i++){
            newOption();
        }
        pnOptionList.setSelectedValue(option, true);
        fldOptionName.requestFocus();
        fldOptionName.selectAll();
    }//GEN-LAST:event_btnAddMultiActionPerformed

    private void mniImportJumperCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniImportJumperCSVActionPerformed
        String[] list = getNameList(jumperPath.listFiles(), ".csv");
        String s = (String) JOptionPane.showInputDialog(this,
                "", "Select CSV to Load", JOptionPane.PLAIN_MESSAGE, null,
                list, list[0]);
        
        CSVtoXML xmlTest = new CSVtoXML();
        System.out.println(jumperPath + "\\" + s + ".csv");
        xmlTest.convertFile(
        		jumperPath + "\\" + s + ".csv", 
        		jumperPath + "\\" + "importedCSV" + ".jt", 
        		",");
        
        try {
            CSVtoXML.clean(jumperPath + "\\" + "importedCSV" + ".jt");
        } catch (IOException ex) {
            Logger.getLogger(TrackerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_mniImportJumperCSVActionPerformed

    private void mniImportJumpCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniImportJumpCSVActionPerformed
        
        FileDialog fd = new FileDialog(this, "Choose a File", FileDialog.LOAD);
        fd.setVisible(true);
        
        String[] list = getNameList(fd.getFiles(),".csv");
        
        System.out.println(fd.getDirectory());
        System.out.println(fd.getFile());
        
        CSVtoXML xmlTest = new CSVtoXML();
        xmlTest.convertFile(
        		fd.getDirectory() + "\\" + fd.getFile(), 
        		jumpPath + "\\" + "importedJumpCSV" + ".jump", 
        		",");
        
        try {
            CSVtoXML.cleanJump(jumpPath + "\\" + "importedJumpCSV" + ".jump");
        } catch (IOException ex) {
            Logger.getLogger(TrackerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        importJump();
    }//GEN-LAST:event_mniImportJumpCSVActionPerformed
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Event Extras">
    private void jScrollPane3AdjustmentEvent(AdjustmentEvent evt) {
        if (option != null) {
            evt.getValue();
            option.setScrollPoint(jScrollPane3.getVerticalScrollBar().getValue());
        }
    }

    public void activateSelectedOptionsFromList(boolean a) {
        for (int index : pnOptionList.getSelectedIndices()) {
            Object o = model2.get(index);
            if (o instanceof Option) {
                ((Option) o).setActive(a);
            }
        }
        chkActiveOption.setSelected(option.isActive());
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
        pnJumpList.setSelectedValue(newJump, true);
    }

    private void deleteSelectedJump() {

        int toDeleteIndex = pnJumpList.getSelectedIndex();
        int[] selected = pnJumpList.getSelectedIndices();
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
        pnJumpList.setSelectedIndex(0);

        if (model1.size() == 0) {
            newOption();
        }
        if (toDeleteIndex < jumper.getJumpList().size()) {
            pnJumpList.setSelectedIndex(toDeleteIndex);
        } else if (toDeleteIndex > 0) {
            pnJumpList.setSelectedIndex(toDeleteIndex - 1);
        } else {
            pnJumpList.setSelectedIndex(0);
        }
    }

    private void deleteSelectedOptions() {
        int toDeleteIndex = pnOptionList.getSelectedIndex();
        int[] selected = pnOptionList.getSelectedIndices();
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
            pnOptionList.setSelectedIndex(toDeleteIndex);
        } else if (toDeleteIndex > 0) {
            pnOptionList.setSelectedIndex(toDeleteIndex - 1);
        } else {
            pnOptionList.setSelectedIndex(0);
        }
    }

    private void copyOptionTo() {
        String[] list = new String[jumper.getJumpList().size()];
        for (int i = 0; i < jumper.getJumpList().size(); i++) {
            list[i] = jumper.getJump(i).getName();
        }

        int[] selected = pnOptionList.getSelectedIndices();

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
    private javax.swing.JButton btnAddJump;
    private javax.swing.JButton btnAddMulti;
    private javax.swing.JButton btnAddOption;
    private javax.swing.JButton btnLoadJumper;
    private javax.swing.JButton btnSaveJumper;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cbOptionType;
    private javax.swing.DefaultComboBoxModel defaultComboBoxModel1;
    private javax.swing.JCheckBox chkActiveOption;
    private javax.swing.JCheckBox chkChainOption;
    private javax.swing.JTextArea fldOptionDescription;
    private javax.swing.JTextField fldOptionName;
    private javax.swing.JTextArea fldOptionNotes;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem2;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.DefaultComboBoxModel defaultComboBoxModel2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.DefaultComboBoxModel defaultComboBoxModel3;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.DefaultListModel model3;
    private javax.swing.JList<String> jList3;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem15;
    private javax.swing.JMenuItem jMenuItem16;
    private javax.swing.JMenuItem jMenuItem17;
    private javax.swing.JMenuItem jMenuItem19;
    private javax.swing.JMenuItem jMenuItem20;
    private javax.swing.JMenuItem jMenuItem23;
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
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JSpinner jSpinner3;
    private SpinnerNumberModel spinnerModel3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea3;
    private javax.swing.JTextArea jTextArea4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JLabel lblAvailableCP;
    private javax.swing.JLabel lblCP;
    private javax.swing.JMenuItem mniAddJump;
    private javax.swing.JCheckBoxMenuItem mniAutoSave;
    private javax.swing.JMenuItem mniBackupJumper;
    private javax.swing.JMenuItem mniDeleteJump;
    private javax.swing.JMenuItem mniDeleteJumper;
    private javax.swing.JMenuItem mniExit;
    private javax.swing.JMenuItem mniExportJump;
    private javax.swing.JMenuItem mniImportJump;
    private javax.swing.JMenuItem mniImportJumpCSV;
    private javax.swing.JMenuItem mniImportJumperCSV;
    private javax.swing.JMenuItem mniLoadJumper;
    private javax.swing.JMenuItem mniNewJumper;
    private javax.swing.JMenuItem mniSaveJumper;
    private javax.swing.JMenu mnuConfig;
    private javax.swing.JMenu mnuFile;
    private javax.swing.JMenu mnuHelp;
    private javax.swing.JMenu mnuJump;
    private javax.swing.JMenu mnuJumper;
    private javax.swing.JMenu mnuOption;
    private javax.swing.DefaultListModel model1;
    private javax.swing.JList<String> pnJumpList;
    private javax.swing.DefaultListModel model2;
    private javax.swing.JList<String> pnOptionList;
    private javax.swing.JSpinner spnJumpBaseCP;
    private SpinnerNumberModel spinnerModel1;
    private javax.swing.JSpinner spnOptionCost;
    private SpinnerNumberModel spinnerModel2;
    private javax.swing.JSpinner spnOptionCount;
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
