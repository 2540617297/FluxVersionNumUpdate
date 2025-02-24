package com.ttxp.demo;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.ttxp.demo.handlefile.ConfirmButtonListener;
import com.ttxp.demo.util.MyPluginCacheManager;
import com.ttxp.demo.util.ResultObj;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.ttxp.demo.util.VNUCval.*;

/**
 * 更新版本号-二级框代码
 *
 * <p>
 * 创建时间：2025/2/24
 * <p>
 *
 * <p>
 * 修改时间：2025/2/24
 * <p>
 *
 * @author pengtai
 * @version V1.0.0
 */
public class VNUGUI {

    // 主窗口
    public JFrame frame;
    // 设置-更新updateNotes
    public JMenuItem updateItem;
    // 设置-缓存任务号、更新描述
    public JMenuItem cacheItem;
    // 设置-复制updateNotes
    public JMenuItem copyItem;
    // 修改描述
    public JTextField textField1;
    // 任务号
    public JTextField textField2;
    // 姓名
    public JTextField textField4;
    // 确认按钮
    public JRadioButton radioButton1;
    // 取消按钮
    public JRadioButton radioButton2;

    /**
     * 展示二级框
     *
     * <p>Author: pengtai
     * <p>Create Time:2024/10/11
     *
     * @param e
     * @param files
     */
    public void showDialog(AnActionEvent e, VirtualFile[] files) {

        // 二级框
        frame = new JFrame(F_TITLE_K_L);
        frame.setSize(800, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        createMenuBar();

        // 创建顶部输入面板
        JPanel topPanel = createTopPanel();

        HashSet<String> containsUpdateNotes = new HashSet<>();
        AtomicInteger dictoryFileNum = new AtomicInteger();
        // 创建文件信息展示面板
        JPanel middlePanel = createMiddlePanel(files, containsUpdateNotes, dictoryFileNum);

        // 主要逻辑，is here................
        // 创建底部按钮面板
        JPanel bottomPanel = createBottomPanel(e, files, containsUpdateNotes, dictoryFileNum);
        // end................


        // 将面板添加到主窗口
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(middlePanel);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // 调整窗口大小以适应组件
        frame.pack();

        // 将窗口显示在屏幕中央
        centerFrameOnScreen();

        // 获取缓存管理器实例
        restoreDataFromCache();

        frame.setVisible(true);
    }

    /**
     * 设置菜单项
     *
     * <p>Author: pengtai
     * <p>Create Time:2025/2/24
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(F_SETTINGS_K_L);

        updateItem = new JMenuItem(S_UPDATE_ITEM_F_L);
        cacheItem = new JMenuItem(S_CACHE_ITEM_F_L);
        copyItem = new JMenuItem(S_CACHE_COPYNOTES_F_L);

        fileMenu.add(updateItem);
        fileMenu.add(cacheItem);
        fileMenu.add(copyItem);

        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);

        // 为菜单项添加点击事件监听器
        setupMenuItem(updateItem, S_UPDATE_ITEM_S_L, S_UPDATE_ITEM_F_L, S_UPDATE_KEY);
        setupMenuItem(cacheItem, S_CACHE_ITEM_S_L, S_CACHE_ITEM_F_L, S_CACHE_KEY);
        setupMenuItem(copyItem, S_CACHE_COPYNOTES_S_L, S_CACHE_COPYNOTES_F_L, S_COPY_KEY);
    }

    /**
     * 创建输入框
     *
     * <p>Author: pengtai
     * <p>Create Time:2025/2/24
     *
     * @param labelText
     * @return javax.swing.JPanel
     */
    private JPanel createInputPanel(String labelText) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(80, 30));
        JTextField textField = new JTextField(30);
        panel.add(label);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panel.add(textField, c);
        return panel;
    }

    /**
     * 创建单选框
     *
     * <p>Author: pengtai
     * <p>Create Time:2025/2/24
     *
     * @return javax.swing.JPanel
     */
    private JPanel createRadioPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        radioButton1 = new JRadioButton(F_TASKTYPE_R_L);
        radioButton1.setSelected(true);
        leftPanel.add(radioButton1);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());
        radioButton2 = new JRadioButton(F_TASKTYPE_K_L);
        rightPanel.add(radioButton2);

        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.weightx = 0.5;
        left.fill = GridBagConstraints.CENTER;

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.weightx = 0.5;
        right.fill = GridBagConstraints.CENTER;

        panel.add(leftPanel, left);
        panel.add(rightPanel, right);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(radioButton1);
        buttonGroup.add(radioButton2);

        return panel;
    }

    /**
     * 创建顶部面板（姓名、修改描述、任务号）
     *
     * <p>Author: pengtai
     * <p>Create Time:2025/2/24
     *
     * @return javax.swing.JPanel
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // 姓名输入框
        JPanel topPanelName = createInputPanel(F_USERNAME_K_L);
        textField4 = (JTextField)topPanelName.getComponent(1);

        // 修改描述输入框
        JPanel topPanelMsg = createInputPanel(F_UPDATEMSG_K_L);
        textField1 = (JTextField)topPanelMsg.getComponent(1);
        textField1.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e1) {
                // 当输入框获得焦点时，暂时不做处理
            }

            @Override
            public void focusLost(FocusEvent e2) {
                // 当 JTextField 失去焦点时触发
                // 这里可以添加你自己的逻辑，比如验证输入内容
                String text = textField1.getText();
                boolean success = false;
                if (text != null && !text.isEmpty()) {
                    printLog(text);
                    String[] keys = {"任务：", "客服：", "任务:", "客服:"};
                    for (String key : keys) {
                        if (text.contains(key)) {
                            int index = text.indexOf(key);
                            String taskMsg = text.substring(0, index);
                            if (taskMsg.endsWith(",") || taskMsg.endsWith("，")) {
                                taskMsg = taskMsg.substring(0, taskMsg.length() - 1);
                            }
                            textField1.setText(taskMsg);
                            textField2.setText(text.substring(index + 3));
                            if (key.contains("任务")) {
                                radioButton1.setSelected(true);
                            } else {
                                radioButton2.setSelected(true);
                            }
                            success = true;
                            break;
                        }
                    }
                    if (success) {
                        Messages.showInfoMessage(MSG_SPLITTASKMSG + "《" + text + "》", MSG_MESSGE);
                    }
                }
            }
        });

        // 任务号输入框
        JPanel topPanelTaskNo = createInputPanel(F_RORK_K_L);
        textField2 = (JTextField)topPanelTaskNo.getComponent(1);

        // 单选框
        JPanel radioPanel = createRadioPanel();

        topPanel.add(topPanelName);
        topPanel.add(topPanelMsg);
        topPanel.add(topPanelTaskNo);
        topPanel.add(radioPanel);

        return topPanel;
    }

    /**
     * 创建中间面板（文件列表）
     *
     * <p>Author: pengtai
     * <p>Create Time:2025/2/24
     *
     * @param files
     * @param containsUpdateNotes
     * @param dictoryFileNum
     * @return javax.swing.JPanel
     */
    private JPanel createMiddlePanel(VirtualFile[] files, HashSet<String> containsUpdateNotes, AtomicInteger dictoryFileNum) {
        StringBuffer filesDirs = new StringBuffer();
        AtomicInteger fileNum = new AtomicInteger();

        // 统计文件和目录数量
        Stream.of(files).forEach(file -> {
            if (file.isDirectory()) {
                dictoryFileNum.incrementAndGet();
            } else {
                fileNum.incrementAndGet();
            }
        });

        filesDirs.append(F_FILESNUM_K_L + files.length + "," + F_DIRECTORY_K_L + dictoryFileNum.get() + "," + F_FILE_K_L + fileNum.get() + "\n");

        // 遍历文件数组，记录文件路径
        Stream.of(files).forEach(file -> {
            if (file.isDirectory()) {
                return;
            }
            String path = file.getPath();
            if (path.contains("UpdateNotes.txt")) {
                containsUpdateNotes.add(path);
            }
            filesDirs.append(path + "\n");
        });

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel label3 = new JLabel(F_UPDATEFILES_K_L);
        centerPanel.add(label3);

        JTextArea descriptionArea = new JTextArea(15, 130);
        descriptionArea.setEditable(false);
        descriptionArea.setText(filesDirs.toString());
        descriptionArea.setCaretPosition(1);

        JScrollPane scrollPane = new JScrollPane(descriptionArea);

        JPanel middleContainerPanel = new JPanel();
        middleContainerPanel.setLayout(new BoxLayout(middleContainerPanel, BoxLayout.Y_AXIS));
        middleContainerPanel.add(centerPanel);
        middleContainerPanel.add(scrollPane);

        return middleContainerPanel;
    }

    /**
     * 创建底部面板（确认、取消）
     *
     * <p>Author: pengtai
     * <p>Create Time:2025/2/24
     *
     * @param e
     * @param files
     * @param containsUpdateNotes
     * @param dictoryFileNum
     * @return javax.swing.JPanel
     */
    private JPanel createBottomPanel(AnActionEvent e, VirtualFile[] files, HashSet<String> containsUpdateNotes, AtomicInteger dictoryFileNum) {
        JPanel bottomPanel = new JPanel();

        JButton confirmButton = new JButton(F_CONFIRM_K_L);
        JButton cancelButton = new JButton(F_CANCEL_K_L);

        // 确认后回调
        confirmButton.addActionListener(new ConfirmButtonListener(e, files, containsUpdateNotes, dictoryFileNum, this));

        cancelButton.addActionListener(e1 -> frame.dispose());

        bottomPanel.add(confirmButton);
        bottomPanel.add(cancelButton);

        return bottomPanel;
    }


    private void centerFrameOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
    }

    private void restoreDataFromCache() {
        MyPluginCacheManager cacheManager = MyPluginCacheManager.getInstance();
        if (cacheManager != null) {
            Map<String, String> cachedSetting = cacheManager.getCachedFormValue();
            if (cachedSetting != null && !cachedSetting.isEmpty()) {
                // 从缓存中取userName
                String userName = cachedSetting.get(F_USERNAME);
                if (userName != null && !userName.isEmpty()) {
                    textField4.setText(userName);
                    // 进行一些操作，比如输出缓存的值
                    printLog("Cached setting: " + cachedSetting);
                    textField1.requestFocus();
                }

                // 更新菜单项的选中状态和文本
                VNUGUI.updateMenuItem(cachedSetting, S_UPDATE_KEY, updateItem, S_UPDATE_ITEM_S_L, S_UPDATE_ITEM_F_L);
                VNUGUI.updateMenuItem(cachedSetting, S_CACHE_KEY, cacheItem, S_CACHE_ITEM_S_L, S_CACHE_ITEM_F_L);
                VNUGUI.updateMenuItem(cachedSetting, S_COPY_KEY, copyItem, S_CACHE_COPYNOTES_S_L, S_CACHE_COPYNOTES_F_L);

                // 如果设置为缓存，则更新相应的文本框和单选按钮
                if ("Y".equals(cachedSetting.get(S_CACHE_KEY))) {
                    // 修改描述
                    String notes = cachedSetting.get(F_NOTES);
                    if (notes != null && !notes.isEmpty()) {
                        textField1.setText(notes);
                    }
                    // 任务号
                    String taskNo = cachedSetting.get(F_TASKNO);
                    if (taskNo != null && !taskNo.isEmpty()) {
                        textField2.setText(taskNo);
                    }

                    // 任务类型
                    String taskType = cachedSetting.get(F_TASKTYPE);
                    if (taskType != null && !taskType.isEmpty()) {
                        if (F_TASKTYPE_R_L.equals(taskType)) {
                            radioButton1.setSelected(true);
                        } else {
                            radioButton2.setSelected(true);
                        }
                    }
                }
            }
        }
    }

    /**
     * 展示失败信息
     *
     * <p>Author: pengtai
     * <p>Create Time:2024/10/21
     *
     * @param e
     * @param resultObjList
     */
    public void showMessageDialog(AnActionEvent e, List<ResultObj> resultObjList, int successNum, int failFileNum, int directoryFileNum) {
        JFrame frame = new JFrame("错误信息");
        frame.setSize(900, 800);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // 说明框
        StringBuffer messages = new StringBuffer();
        messages.append(MSG_SUCCESSFILENUM + successNum + "," + F_DIRECTORY_K_L + directoryFileNum + "," + MSG_FAILFILENUM + failFileNum + "\n");
        for (ResultObj resultObj : resultObjList) {
            if (!resultObj.isOk()) {
                messages.append(resultObj.getFilePath()).append(":\n").append(resultObj.getMessage()).append("\n");
            }
        }

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel label3 = new JLabel(MSG_FAILMSG);
        centerPanel.add(label3);

        JTextArea descriptionArea = new JTextArea(30, 140);
        descriptionArea.setEditable(false);
        descriptionArea.setText(messages.toString());
        descriptionArea.setCaretPosition(1);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);


        JPanel bottomPanel = new JPanel();
        JButton confirmButton = new JButton(F_CONFIRM_K_L);
        bottomPanel.add(confirmButton);
        confirmButton.addActionListener(subE -> frame.dispose());

        //修改文件
        JPanel middleContainerPanel = new JPanel();
        middleContainerPanel.setLayout(new BoxLayout(middleContainerPanel, BoxLayout.Y_AXIS));
        middleContainerPanel.add(centerPanel);
        middleContainerPanel.add(scrollPane);
        frame.add(middleContainerPanel);
        //确认、取消确认按钮
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.pack();

        // 将窗口显示在屏幕中央
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
        frame.setVisible(true);
    }

    /**
     * 设置菜单项的点击事件监听器
     *
     * @param item           菜单项
     * @param selectedText   选中时显示的文本
     * @param unselectedText 未选中时显示的文本
     * @param cacheKey       缓存键
     */
    protected static void setupMenuItem(JMenuItem item, String selectedText, String unselectedText, String cacheKey) {
        item.addActionListener(e -> {
            // 切换菜单项的勾选状态
            item.setSelected(!item.isSelected());
            if (item.isSelected()) {
                item.setText(selectedText);
            } else {
                item.setText(unselectedText);
            }
            printLog(item.getText() + "是否被勾选: " + item.isSelected());

            // 获取缓存管理器实例
            MyPluginCacheManager setCacheManager = MyPluginCacheManager.getInstance();
            if (setCacheManager != null) {
                // 设置缓存的值
                Map<String, String> cachedFormValue = setCacheManager.getCachedFormValue();
                if (cachedFormValue == null || cachedFormValue.size() == 0) {
                    cachedFormValue = new HashMap<>();
                }
                if (item.isSelected()) {
                    cachedFormValue.put(cacheKey, "Y");
                } else {
                    cachedFormValue.put(cacheKey, "N");
                }
                setCacheManager.setCachedFormValue(cachedFormValue);
            }
        });
    }

    /**
     * 更新菜单项的选中状态和文本的方法
     *
     * <p>Author: pengtai
     * <p>Create Time:2025/2/24
     *
     * @param cachedSetting
     * @param key
     * @param menuItem
     * @param selectedText
     * @param unselectedText
     */
    protected static void updateMenuItem(Map<String, String> cachedSetting, String key, JMenuItem menuItem, String selectedText, String unselectedText) {
        String value = cachedSetting.get(key);
        if (value != null && !value.isEmpty()) {
            if ("Y".equals(value)) {
                menuItem.setSelected(true);
                menuItem.setText(selectedText);
            } else {
                menuItem.setSelected(false);
                menuItem.setText(unselectedText);
            }
        } else {
            menuItem.setSelected(false);
            menuItem.setText(unselectedText);
        }
    }
}
