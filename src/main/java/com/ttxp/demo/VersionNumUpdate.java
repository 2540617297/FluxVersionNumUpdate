package com.ttxp.demo;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 版本号更新
 *
 * <p>
 * 创建时间：2024/10/12
 * <p>
 *
 * <p>
 * 修改时间：2024/10/12
 * <p>
 *
 * @author pengtai
 * @version V1.0.0
 */
public class VersionNumUpdate extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取当前项目实例
        Project project = e.getProject();
        if (project == null) {
            return; // 如果没有项目打开，则直接返回
        }

        // 从事件的数据上下文中获取当前选中的文件数组
        VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);

        if (files == null) {
            Messages.showMessageDialog(e.getProject(), "请选择需要更新的文件！", "消息", Messages.getInformationIcon());
        }

        FileDocumentManager.getInstance().saveAllDocuments();

        showDialog(e, files);
    }

    /**
     * 展示二级框
     *
     * <p>Author: pengtai
     * <p>Create Time:2024/10/11
     *
     * @param e
     * @param files
     */
    private void showDialog(AnActionEvent e, VirtualFile[] files) {


        JFrame frame = new JFrame("更新VersionNum");
        frame.setSize(800, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel topPanelName = new JPanel(new GridBagLayout());
        JLabel label4 = new JLabel("姓名：");
        label4.setPreferredSize(new Dimension(80, 30));
        JTextField textField4 = new JTextField();

        topPanelName.add(label4);
        GridBagConstraints c4 = new GridBagConstraints();
        c4.fill = GridBagConstraints.HORIZONTAL;
        c4.weightx = 1.0;
        topPanelName.add(textField4, c4);

        JPanel topPanelMsg = new JPanel(new GridBagLayout());
        JLabel label1 = new JLabel("修改描述：");
        label1.setPreferredSize(new Dimension(80, 30));
        JTextField textField1 = new JTextField(30);
        topPanelMsg.add(label1);
        GridBagConstraints c1 = new GridBagConstraints();
        c1.fill = GridBagConstraints.HORIZONTAL;
        c1.weightx = 1.0;
        topPanelMsg.add(textField1, c1);


        JPanel topPanelTaskNo = new JPanel(new GridBagLayout());
        JLabel label2 = new JLabel("任务/客服 ：");
        label2.setPreferredSize(new Dimension(80, 30));
        JTextField textField2 = new JTextField(30);
        topPanelTaskNo.add(label2);
        GridBagConstraints c2 = new GridBagConstraints();
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.weightx = 1.0;
        topPanelTaskNo.add(textField2, c2);

        // 单选框
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        JRadioButton radioButton1 = new JRadioButton("任务");
        radioButton1.setSelected(true);
        leftPanel.add(radioButton1);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());
        JRadioButton radioButton2 = new JRadioButton("客服");
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

        topPanel.add(topPanelName);
        topPanel.add(topPanelMsg);
        topPanel.add(topPanelTaskNo);
        topPanel.add(panel, BorderLayout.CENTER);

        // 说明框
        StringBuffer filesDirs = new StringBuffer();
        filesDirs.append("总文件个数：" + files.length + "\n");
        Arrays.stream(files).forEach(file -> {
            String path = file.getPath();
            filesDirs.append(path + "\n");
        });


        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel label3 = new JLabel("修改文件：");
        centerPanel.add(label3);

        JTextArea descriptionArea = new JTextArea(15, 130);
        descriptionArea.setEditable(false);
        descriptionArea.setText(filesDirs.toString());
        descriptionArea.setCaretPosition(1);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);

        JPanel bottomPanel = new JPanel();
        JButton confirmButton = new JButton("确认");
        JButton cancelButton = new JButton("取消");
        bottomPanel.add(confirmButton);
        bottomPanel.add(cancelButton);
        confirmButton.addActionListener(subE -> {

            String taskType;
            // 默认选择“任务”
            if (radioButton2.isSelected()) {
                taskType = "客服";
            } else {
                taskType = "任务";
            }

            // 在这里执行你的具体操作
            String msg = textField1.getText();
            String taskNo = textField2.getText();
            String userName = textField4.getText();
            if (userName == null || userName.trim().length() == 0) {
                Messages.showInfoMessage("请输入修改人名称！", "消息");
                return;
            }
            if (msg == null || msg.trim().length() == 0) {
                Messages.showInfoMessage("请输入修改描述！", "消息");
                return;
            }
            if (taskNo == null || taskNo.trim().length() == 0) {
                Messages.showInfoMessage("请输入任务号！", "消息");
                return;
            }

            // 显示表单对话框
            int result = JOptionPane.showConfirmDialog(null, "注意！！！确认是否执行更新！！！", "Confirmation", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                // 用户点击了确认按钮，执行实际的操作
                Project project = e.getProject();
                if (project != null) {


                    System.out.println("姓名：" + userName);
                    System.out.println("修改描述：" + msg);
                    System.out.println("任务号：" + taskNo);

                    // 获取缓存管理器实例
                    // 弃用
                    MyPluginCacheManager setCacheManager = MyPluginCacheManager.getInstance();
                    if (setCacheManager != null) {
                        // 设置缓存的值
                        setCacheManager.setCachedFormValue(userName);
                    }


                    List<ResultObj> resultList = new ArrayList<>();
                    Arrays.stream(files).forEach(file -> {
                        String path = file.getPath();
                        Map<String, String> maxVersionNumAndLine = new HashMap<>();
                        ResultObj resultObj = getMaxVersionNum(path, userName, maxVersionNumAndLine);
                        if (!resultObj.isOk()) {
                            resultList.add(resultObj);
                            return;
                        }
                        ResultObj resultObj1 = insertNewVersionByNewFile(path, maxVersionNumAndLine, msg, taskNo, taskType);
                        if (!resultObj1.isOk()) {
                            resultList.add(resultObj1);
                            return;
                        }
                        file.refresh(false, false);
                    });

                    if (resultList != null && resultList.size() > 0) {
                        showMessageDialog(e, resultList, files);
                    } else {
                        Messages.showInfoMessage("执行成功！请检查！", "消息");
                    }
                    frame.dispose();
                }
            }


        });

        cancelButton.addActionListener(e1 -> frame.dispose());

        //姓名，修改描述，任务号
        frame.add(topPanel, BorderLayout.NORTH);
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

        // 获取缓存管理器实例
        // 临时存储使用，当关闭IDEA后存储数据会清空，此方法弃用
        MyPluginCacheManager cacheManager = MyPluginCacheManager.getInstance();
        if (cacheManager != null) {
            String cachedSetting = cacheManager.getCachedFormValue();
            // 获取缓存的值
            if (cachedSetting != null && cachedSetting.length() > 0) {
                textField4.setText(cachedSetting);
//                textField4.setFocusable(false);
                // 进行一些操作，比如输出缓存的值
                System.out.println("Cached setting: " + cachedSetting);
                textField1.requestFocus();
            }
        }

        frame.setVisible(true);
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
    private void showMessageDialog(AnActionEvent e, List<ResultObj> resultObjList, VirtualFile[] files) {
        JFrame frame = new JFrame("错误信息");
        frame.setSize(900, 800);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // 说明框
        StringBuffer messages = new StringBuffer();
        messages.append("成功文件个数：" + (files.length - resultObjList.size()) + ",失败文件个数：" + resultObjList.size() + "\n");
        for (ResultObj resultObj : resultObjList) {
            messages.append(resultObj.getFilePath()).append(":\n").append(resultObj.getMessage()).append("\n");
        }

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel label3 = new JLabel("错误信息：");
        centerPanel.add(label3);

        JTextArea descriptionArea = new JTextArea(30, 140);
        descriptionArea.setEditable(false);
        descriptionArea.setText(messages.toString());
        descriptionArea.setCaretPosition(1);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);


        JPanel bottomPanel = new JPanel();
        JButton confirmButton = new JButton("确认");
        bottomPanel.add(confirmButton);
        confirmButton.addActionListener(subE -> {
            frame.dispose();
        });

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
     * 获取VXXX最大行和最大值、获取verNum所在行
     *
     * <p>Author: pengtai
     * <p>Create Time:2024/10/11
     *
     * @param filePath
     * @return java.util.Map<java.lang.String, java.lang.String>
     */
    public static ResultObj getMaxVersionNum(String filePath, String userName, Map<String, String> maxVersionNumAndLine) {
        ResultObj resultObj = new ResultObj();
        try {
            File file = new File(filePath);
            //获取文件扩展类型
            String extType;
            String vType = "";
            String fileName = file.getName();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                extType = fileName.substring(dotIndex + 1);
                if (!"java".equalsIgnoreCase(extType) && !"js".equalsIgnoreCase(extType) && !("txt".equalsIgnoreCase(extType) && "UpdateNotes.txt".equalsIgnoreCase(fileName))) {
                    System.out.println("无效的文件：" + filePath);
                    resultObj.setMessage("暂不支持修改该文件");
                    resultObj.setFilePath(filePath);
                    resultObj.setOk(false);
                    return resultObj;
                }
            } else {
                resultObj.setMessage("无效的文件类型");
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }
            try (InputStreamReader isr = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(isr)) {
                String parttenStr1 = "V(\\d+)\\((\\d+)\\)";
                String parttenStr2 = "V\\d+(\\.\\d+){1,3}\\((\\d+)\\)";
                String parttenStr3 = "(\\d+)";

                Pattern partten1 = Pattern.compile(parttenStr1);
                Pattern partten2 = Pattern.compile(parttenStr2);
                Pattern partten3 = Pattern.compile(parttenStr3);

                int maxVersion = 0;
                int lineNumber = 0;
                int maxVersionLineNumber = 0;
                int verNumLineNumber = 0;
                String maxVersionLineStr = "";
                String containsFinal = "";
                // 中间符
                String versionNum = "";
                String beforeMsg = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    Matcher matcher1 = partten1.matcher(line);
                    Matcher matcher2 = partten2.matcher(line);
                    Matcher matcher3 = partten3.matcher(line);
                    boolean find = false;
                    if (line.contains("String verNum ") && line.trim().startsWith("private") && line.contains("private static")) {
                        verNumLineNumber = lineNumber;
                        if (line.contains("final")) {
                            containsFinal = "Y";
                        }else{
                            containsFinal = "N";
                        }
                    } else if (matcher1.find()) {
                        find = true;
                        vType = "1";
                    } else if (matcher2.find()) {
                        find = true;
                        vType = "2";
                    } else if (matcher3.find() && "txt".equalsIgnoreCase(extType) && "UpdateNotes.txt".equalsIgnoreCase(fileName)) {
                        find = true;
                        vType = "3";
                        if (line.startsWith("#")) {
                            continue;
                        }
                    }

                    if (find) {
                        // 获取到完整的记录行信息
                        maxVersionLineStr = line;
                        // 如果能匹配上就默认是最大行
                        if ("3".equals(vType)) {
                            maxVersionLineNumber = lineNumber - 1;
                        } else {
                            maxVersionLineNumber = lineNumber;
                        }

                    }

                    if ((line.contains("String verNum ") && line.trim().startsWith("private") && line.contains("private static")) || line.contains("@Autowired") || line.contains("define")
                            || ("3".equals(vType) && find)) {
                        break;
                    }
                }

                if("".equals(containsFinal) && "java".equalsIgnoreCase(extType)){
                    resultObj.setMessage("未定位到String verNum信息请检查！");
                    resultObj.setFilePath(filePath);
                    resultObj.setOk(false);
                    return resultObj;
                }

                // 查找最后一行的verNum信息
                String currentVersionStr = "";
                // 匹配最大行的数据
                Matcher matcherStr = null;
                if ("1".equals(vType)) {
                    matcherStr = partten1.matcher(maxVersionLineStr);
                } else if ("2".equals(vType)) {
                    matcherStr = partten2.matcher(maxVersionLineStr);
                } else if ("3".equals(vType)) {
                    matcherStr = partten3.matcher(maxVersionLineStr);
                } else {
                    resultObj.setMessage("未定位到版本号信息");
                    resultObj.setFilePath(filePath);
                    resultObj.setOk(false);
                    return resultObj;
                }
                String jsGroup;
                if (matcherStr.find()) {
                    if ("1".equalsIgnoreCase(vType)) {
                        currentVersionStr = matcherStr.group(1);
                    } else if ("2".equalsIgnoreCase(vType)) {
                        jsGroup = matcherStr.group();
                        String replace = jsGroup.replace(".", "");
                        currentVersionStr = replace.substring(replace.indexOf("V") + 1, replace.indexOf("("));
                    } else if ("3".equalsIgnoreCase(vType)) {
                        // 跳过判断使用
                        currentVersionStr = "100";
                    }
                    if (currentVersionStr == null || currentVersionStr.length() == 0) {
                        resultObj.setMessage("未定位到版本号信息");
                        resultObj.setFilePath(filePath);
                        resultObj.setOk(false);
                        return resultObj;
                    }
                    int currentVersion = Integer.parseInt(currentVersionStr);
                    maxVersion = currentVersion;
                }

                // 匹配姓名
                String regexName = "\\b([A-Za-z]+)\\b";
                Pattern patternName = Pattern.compile(regexName);

                // 匹配姓名后出现的修改描述
                String regexMsg = "[^\\s\t]+";
                Pattern patternMsg = Pattern.compile(regexMsg);
                maxVersionLineStr = maxVersionLineStr.replace("\t", "    ");
                Matcher matcherName = patternName.matcher(maxVersionLineStr);
                if (matcherName.find()) {
                    // 姓名后的索引
                    int endIndex = matcherName.end();
                    int nameStartIndex = matcherName.start();

                    // 从姓名后的索引开始匹配描述
                    Matcher matcherMsg = patternMsg.matcher(maxVersionLineStr);
                    matcherMsg.region(endIndex, maxVersionLineStr.length());
                    if (matcherMsg.find()) {
                        // 描述开始的索引
                        int startIndex = matcherMsg.start();

                        // " * V5.1.6(20241014) pengtai      "
                        String versionStr = maxVersionLineStr.substring(0, startIndex);

                        //获取当前日期
                        LocalDate now = LocalDate.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                        String formattedDate = now.format(formatter);
                        int typeLength = 0;
                        if ("3".equals(vType)) {
                            beforeMsg = beforeMsg + formattedDate;
                            int length = beforeMsg.length();
                            int rightWithName = nameStartIndex - length;
                            if (rightWithName <= 0) {
                                rightWithName = 1;
                            }
                            String tmpSpace = "";
                            for (int i = 0; i < rightWithName; i++) {
                                tmpSpace = tmpSpace + " ";
                            }
                            beforeMsg = beforeMsg + tmpSpace;// " * V516(20241014) "

                            typeLength = beforeMsg.length();

                            int subLength = startIndex - typeLength - userName.length();
                            if (subLength <= 0) {
                                subLength = 1;
                            }
                            StringBuffer buffer = new StringBuffer();
                            // 拼接描述空格
                            for (int i = 0; i < subLength; i++) {
                                buffer.append(" ");
                            }
                            beforeMsg = beforeMsg + userName + buffer.toString() + "-";//" * V5.1.6(20241014) pengtai


                        } else {
                            int indexV = versionStr.indexOf("V");

                            int indexLeft = versionStr.indexOf("(");
                            int indexRight = versionStr.indexOf(")");
                            String beforeV = versionStr.substring(0, indexV); //" * "

                            beforeMsg = beforeMsg + beforeV;//" * "

                            int maxVersionNum = maxVersion + 1;

                            if ("2".equalsIgnoreCase(vType)) {
                                String originalVersion = versionStr.substring(indexV + 1, indexLeft);
                                ArrayList<Integer> indexs = new ArrayList<>();
                                getJsSplitIndex(originalVersion, indexs, 0);
                                String newVersion = genNewVersion(maxVersionNum + "", indexs);

                                beforeMsg = beforeMsg + "V" + newVersion; //" * V5.1.6"

                            } else if ("1".equalsIgnoreCase(vType)) {
                                beforeMsg = beforeMsg + "V" + maxVersionNum; //" * V516"
                            }

                            beforeMsg = beforeMsg + "(" + formattedDate + ")"; //" * V5.1.6(20241014)"
                            int length = beforeMsg.length();
                            int rightWithName = nameStartIndex - length;
                            if (rightWithName <= 0) {
                                rightWithName = 1;
                            }
                            String tmpSpace = "";
                            for (int i = 0; i < rightWithName; i++) {
                                tmpSpace = tmpSpace + " ";
                            }
                            beforeMsg = beforeMsg + tmpSpace;// " * V516(20241014) "

                            versionNum = "V" + maxVersionNum + "(" + formattedDate + ")";
                            typeLength = beforeMsg.length();

                            int subLength = startIndex - typeLength - userName.length();
                            if (subLength <= 0) {
                                subLength = 1;
                            }
                            StringBuffer buffer = new StringBuffer();
                            // 拼接描述空格
                            for (int i = 0; i < subLength; i++) {
                                buffer.append(" ");
                            }
                            beforeMsg = beforeMsg + userName + buffer.toString();//" * V5.1.6(20241014) pengtai
                        }
                    } else {
                        System.out.println("No match found.");
                    }
                } else {
                    System.out.println("No match found.");
                }
                System.out.println("最大版本号：V" + maxVersion + "，所在行：" + maxVersionLineNumber + ", verNum所在行：" + verNumLineNumber);
                maxVersionNumAndLine.put("maxVersion", maxVersion + "");
                maxVersionNumAndLine.put("maxVersionLineNumber", maxVersionLineNumber + "");
                maxVersionNumAndLine.put("verNumLineNumber", verNumLineNumber + "");
                maxVersionNumAndLine.put("extType", extType);
                maxVersionNumAndLine.put("beforeMsg", beforeMsg);
                maxVersionNumAndLine.put("versionNum", versionNum);
                maxVersionNumAndLine.put("vType", vType);
                maxVersionNumAndLine.put("containsFinal", containsFinal);
            } catch (IOException e) {
                e.printStackTrace();
                resultObj.setOk(false);
                resultObj.setFilePath(filePath);
                resultObj.setMessage(e.getMessage());
                return resultObj;
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultObj.setOk(false);
            resultObj.setFilePath(filePath);
            resultObj.setMessage(e.getMessage());
            return resultObj;
        }
        resultObj.setOk(true);
        return resultObj;
    }


    /**
     * 新增记录行和编辑记录行
     *
     * <p>Author: pengtai
     * <p>Create Time:2024/10/11
     *
     * @param filePath
     * @param maxVersionNumAndLine
     * @param msg
     * @param taskNo
     */
    public static ResultObj insertNewVersionByNewFile(String filePath, Map<String, String> maxVersionNumAndLine, String msg, String taskNo, String taskType) {

        ResultObj resultObj = new ResultObj();

        try {
            if (maxVersionNumAndLine == null || maxVersionNumAndLine.size() <= 0) {
                resultObj.setMessage("未找到版本号信息");
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }
            //如果为空则直接跳过
            String extType = maxVersionNumAndLine.get("extType");
            if (extType == null || extType.length() == 0) {
                resultObj.setMessage("无效的文件类型");
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }
            String maxVersionLineNumber = maxVersionNumAndLine.get("maxVersionLineNumber");
            if (maxVersionLineNumber == null || maxVersionLineNumber.length() <= 0 || "0".equals(maxVersionLineNumber)) {
                resultObj.setMessage("未找到最大版本号所在行");
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }
            String maxVersion = maxVersionNumAndLine.get("maxVersion");
            if (maxVersion == null || maxVersion.length() <= 0 || "0".equals(maxVersion)) {
                resultObj.setMessage("未识别出最大版本号");
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }

            String verNumLineNumber = maxVersionNumAndLine.get("verNumLineNumber");
            if ("java".equalsIgnoreCase(extType) && (verNumLineNumber == null || verNumLineNumber.length() <= 0 || "0".equals(verNumLineNumber))) {
                resultObj.setMessage("未识别出String verNum所在行");
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }

            // 中间符
            String beforeMsg = maxVersionNumAndLine.get("beforeMsg");
            if (beforeMsg == null || beforeMsg.length() <= 0) {
                resultObj.setMessage("beforeMsg识别出错，请联系管理员！");
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }

            String versionNum = maxVersionNumAndLine.get("versionNum");
            if ("java".equalsIgnoreCase(extType) && (versionNum == null || versionNum.length() <= 0)) {
                resultObj.setMessage("verNum生成失败，请联系管理员检查！");
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }

            // versionNum类型 VXXX-》1、VX.X.X-》2
            String vType = maxVersionNumAndLine.get("vType");
            if (vType == null || vType.length() <= 0) {
                resultObj.setMessage("无效的文件类型");
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }

            String enterLine = System.getProperty("line.separator", "\r\n");
            int targetLineNumber = Integer.valueOf(maxVersionLineNumber) + 1;
            int targetVerNumLineNumber = Integer.valueOf(verNumLineNumber);

            if ("java".equalsIgnoreCase(extType)) {
                if (targetVerNumLineNumber <= targetLineNumber) {
                    resultObj.setMessage("检测出String verNum所在行小于或等于增加updateNotes注释行，请检查！");
                    resultObj.setFilePath(filePath);
                    resultObj.setOk(false);
                    return resultObj;
                }
            }

            String newLine = beforeMsg + msg + "," + taskType + "：" + taskNo;
            String containsFinal = maxVersionNumAndLine.get("containsFinal");
            String verNumNewLine = "";
            if ("Y".equals(containsFinal)) {
                verNumNewLine = "    private static final String verNum = \"" + versionNum + "\";// 版本号";
            } else {
                verNumNewLine = "    private static String verNum = \"" + versionNum + "\";// 版本号";
            }

            int totalLines = 0;
            try {
                totalLines = countLinesInFile(filePath);
            } catch (IOException e) {
                e.printStackTrace();
                resultObj.setOk(false);
                resultObj.setFilePath(filePath);
                resultObj.setMessage(e.getMessage());
                return resultObj;
            }

            try (InputStreamReader isr = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(isr);
                 OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream("temp_file.txt"), StandardCharsets.UTF_8);
                 BufferedWriter writer = new BufferedWriter(osw)) {

                int lineCount = 0;
                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    lineCount++;
                    if (lineCount == targetLineNumber) {
                        writer.write(newLine + enterLine);
                    }
                    if ("java".equalsIgnoreCase(extType) && lineCount == targetVerNumLineNumber) {
                        writer.write(verNumNewLine + enterLine);
                    } else {
                        if ("JS".equalsIgnoreCase(extType) && lineCount == totalLines) {
                            writer.write(currentLine);
                        } else {
                            writer.write(currentLine + enterLine);
                        }
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
                resultObj.setOk(false);
                resultObj.setFilePath(filePath);
                resultObj.setMessage(e.getMessage());
                return resultObj;
            }

            // 将临时文件重命名为原文件，覆盖原文件
            try {
                renameFile("temp_file.txt", filePath);
            } catch (IOException e) {
                e.printStackTrace();
                resultObj.setOk(false);
                resultObj.setFilePath(filePath);
                resultObj.setMessage(e.getMessage());
                return resultObj;
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultObj.setOk(false);
            resultObj.setFilePath(filePath);
            resultObj.setMessage(e.getMessage());
            return resultObj;
        }
        resultObj.setOk(true);
        return resultObj;
    }

    /**
     * 获取总行数
     *
     * <p>Author: pengtai
     * <p>Create Time:2024/10/12
     *
     * @param filePath
     * @return int
     */
    private static int countLinesInFile(String filePath) throws IOException {
        int lines = 0;
        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            while (reader.readLine() != null) {
                lines++;
            }
        } catch (IOException e) {
            throw e;
        }
        return lines;
    }

    /**
     * 重命名文件
     *
     * <p>Author: pengtai
     * <p>Create Time:2024/10/11
     *
     * @param sourcePath
     * @param targetPath
     */
    private static void renameFile(String sourcePath, String targetPath) throws IOException {
        Files.move(Paths.get(sourcePath), Paths.get(targetPath),
                StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 获取所有"."的位置
     * <p>
     * " * V5.1.6(20241014) pengtai      "->[5, 7]
     *
     * <p>Author: pengtai
     * <p>Create Time:2024/10/14
     *
     * @param jsGroup
     * @param indexs
     * @param nowIndex
     */
    public static void getJsSplitIndex(String jsGroup, ArrayList<Integer> indexs, int nowIndex) {
        if (nowIndex < jsGroup.length()) {
            int i = jsGroup.indexOf(".", nowIndex);
            if (i == -1) {
                return;
            }
            indexs.add(i);
            getJsSplitIndex(jsGroup, indexs, i + 1);
        }
    }


    /**
     * 拼接新的versionNum
     *
     * <p>Author: pengtai
     * <p>Create Time:2024/10/14
     *
     * @param maxVersion
     * @param indexs
     * @return java.lang.String
     */
    public static String genNewVersion(String maxVersion, ArrayList<Integer> indexs) {
        String maxVersionTmp = maxVersion;
        for (int i = 0; i < indexs.size(); i++) {
            int index = indexs.get(i);
            maxVersionTmp = maxVersionTmp.substring(0, index) + "." + maxVersionTmp.substring(index);
        }
        return maxVersionTmp;
    }
}
