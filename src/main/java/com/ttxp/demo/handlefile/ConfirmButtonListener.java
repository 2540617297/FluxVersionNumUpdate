package com.ttxp.demo.handlefile;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.ttxp.demo.VNUGUI;
import com.ttxp.demo.util.MyPluginCacheManager;
import com.ttxp.demo.util.ResultObj;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ttxp.demo.util.VNUCval.*;

/**
 * 确认按钮-事件监听-回调
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
public class ConfirmButtonListener implements ActionListener {

    private AnActionEvent e;
    private VNUGUI vnugui;

    public ConfirmButtonListener(AnActionEvent e, VNUGUI vnugui) {
        this.e = e;
        this.vnugui = vnugui;
    }


    @Override
    public void actionPerformed(ActionEvent e1) {
        VirtualFile[] files = vnugui.updateItem.isSelected() ? vnugui.getFlattenedFiles() : vnugui.orgFiles;
        String taskType;
        // 默认选择“任务”
        if (vnugui.radioButton2.isSelected()) {
            taskType = F_TASKTYPE_K_L;
        } else {
            taskType = F_TASKTYPE_R_L;
        }

        // 在这里执行你的具体操作
        String msg = vnugui.textField1.getText();
        String taskNo = vnugui.textField2.getText();
        String userName = vnugui.textField4.getText();
        if (userName == null || userName.trim().length() == 0) {
            Messages.showInfoMessage(MSG_200USERNAME, MSG_MESSGE);
            return;
        }

        // 是否包含项目
        ArrayList<String> resultProject = new ArrayList<String>();
        if (msg == null || msg.trim().length() == 0) {
            Messages.showInfoMessage(MSG_200NOTES, MSG_MESSGE);
            return;
        }
        msg = msg.trim();
        if (F_TASKTYPE_R_L.equals(taskType)) {
            // 正则表达式说明：
            // 1. 【([^】]+)】：匹配中文方括号（左闭右闭），捕获括号内非】的内容
            // 2. \\(([^)]+)\\)：匹配英文圆括号（左闭右闭），捕获括号内非)的内容（括号需转义）
            // 3. （([^）]+)）：匹配中文圆括号（左闭右闭），捕获括号内非）的内容
            // 4. |：逻辑或，匹配三种格式中的任意一种
            Pattern BRACKET_PATTERN = Pattern.compile("【([^】]+)】|\\(([^)]+)\\)|（([^）]+)）");
            Matcher matcher = BRACKET_PATTERN.matcher(msg);
            int matchCount = 0;
            // 限制最多匹配10次
            while (matcher.find() && matchCount < 10) {
                matchCount++;
                String content = null;
                // 依次检查三个捕获组（哪个有值取哪个）
                for (int i = 1; i <= 3; i++) {
                    if (matcher.group(i) != null) {
                        content = matcher.group(i);
                        break;
                    }
                }
                // 过滤空内容或纯空格内容
                if (content == null || content.trim().isEmpty()) {
                    continue;
                }
                resultProject.add(content);
            }
        }
        if (taskNo == null || taskNo.trim().length() == 0) {
            Messages.showInfoMessage(MSG_200RORK, MSG_MESSGE);
            return;
        }

        // 消息
        String message = MSG_CONFIRM;
        if (F_TASKTYPE_R_L.equals(taskType)) {
            message = MSG_PROJECTCONFIRM + "\n" + MSG_CONFIRM;
        }
        if (resultProject != null && resultProject.size() > 0) {
            String htmlMessage = "<html>" +
                    "<body>" +
                    "<span style='color:red;'>" + resultProject + "</span>&nbsp;&nbsp;" + MSG_PROJECTCONFIRM +
                    "<div style='text-align:center;'>" +
                    "<br><b>" + MSG_CONFIRM + "</b>" +
                    "</div>" +
                    "</body>" +
                    "</html>";
            message = htmlMessage;
        }
        // 显示表单对话框
        int result = JOptionPane.showConfirmDialog(null, message, "Confirmation", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            // 用户点击了确认按钮，执行实际的操作
            Project project = e.getProject();
            if (project != null) {
                // 输出日志
                printLog(F_USERNAME_K_L + userName);
                printLog(F_UPDATEMSG_K_L + msg);
                printLog(F_RORK_K_L + taskNo);

                // 获取缓存管理器实例
                MyPluginCacheManager setCacheManager = MyPluginCacheManager.getInstance();
                if (setCacheManager != null) {
                    // 设置缓存的值
                    Map<String, String> cachedFormValue = setCacheManager.getCachedFormValue();
                    if (cachedFormValue == null || cachedFormValue.size() == 0) {
                        cachedFormValue = new HashMap<>();
                    }
                    cachedFormValue.put(F_USERNAME, userName);
                    cachedFormValue.put(F_NOTES, msg);
                    cachedFormValue.put(F_TASKNO, taskNo);
                    cachedFormValue.put(F_TASKTYPE, taskType);
                    setCacheManager.setCachedFormValue(cachedFormValue);
                }

                // 循环所有文件依次处理
                java.util.List<ResultObj> resultList = new ArrayList<>();
                AtomicBoolean hasErr = new AtomicBoolean(false);
                AtomicInteger failFileNum = new AtomicInteger();
                AtomicInteger successFileNum = new AtomicInteger();
                String finalMsg = msg;
                Arrays.stream(files).forEach(file -> {
                    String path = file.getPath();
                    if (file.isDirectory()) {
                        ResultObj resultObj = new ResultObj();
                        resultObj.setOk(true);
                        resultObj.setFilePath(path);
                        successFileNum.set(successFileNum.get() + 1);
                        resultList.add(resultObj);
                        return;
                    }
                    Map<String, String> maxVersionNumAndLine = new HashMap<>();
                    ResultObj resultObj = getMaxVersionNum(path, userName, maxVersionNumAndLine);
                    if (!resultObj.isOk()) {
                        hasErr.set(true);
                        resultList.add(resultObj);
                        failFileNum.set(failFileNum.get() + 1);
                        return;
                    }
                    ResultObj resultObj1 = insertNewVersionByNewFile(path, maxVersionNumAndLine, finalMsg, taskNo, taskType);
                    if (!resultObj1.isOk()) {
                        hasErr.set(true);
                        resultList.add(resultObj1);
                        failFileNum.set(failFileNum.get() + 1);
                        return;
                    } else {
                        resultList.add(resultObj1);
                        successFileNum.set(successFileNum.get() + 1);
                    }
                    file.refresh(false, false);
                });


                if (vnugui.copyItem.isSelected()) {
                    for (ResultObj resultObj : resultList) {
                        if (resultObj.isOk()) {
                            // 获取系统剪贴板实例
                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            // 要复制到剪贴板的文本内容
                            String textToCopy = msg + "," + taskType + "：" + taskNo;
                            // 创建一个StringSelection对象，用于包装要复制的字符串
                            StringSelection selection = new StringSelection(textToCopy);
                            // 将包装好的内容设置到剪贴板中
                            clipboard.setContents(selection, null);
                            break;
                        }
                    }
                }

                // 判断是否需要同步更新updateNotes
                /*if (vnugui.updateItem.isSelected()) {
                    HashSet<String> directorys = new HashSet<>();
                    for (ResultObj resultObj : resultList) {
                        if (resultObj.isOk()) {
                            String filePath = resultObj.getFilePath();
                            if (filePath != null && filePath.length() > 0) {
                                String[] pattern = {"/src/main/webapp/", "/src/main/resources/", "/src/main/java/"};
                                String projectPath = "";
                                for (String s : pattern) {
                                    int i = filePath.indexOf(s);
                                    if (i != -1) {
                                        projectPath = filePath.substring(0, i);
                                    }
                                }
                                if (projectPath != null && projectPath.length() > 0) {
                                    directorys.add(projectPath + "/src/main/resources/updatenotes/UpdateNotes.txt");
                                }
                            }

                        }
                    }
                    printLog("search updateNotes dictory:" + directorys.toString());
                    printLog("updateNotes dictory:" + containsUpdateNotes.toString());

                    // 去重
                    HashSet<String> distinctDirectorys = new HashSet<>();
                    for (String dictory : directorys) {
                        boolean contains = false;
                        for (String containsUpdateNote : containsUpdateNotes) {
                            if (containsUpdateNote.equals(dictory)) {
                                contains = true;
                                break;
                            }
                        }
                        if (!contains) {
                            distinctDirectorys.add(dictory);
                        }
                    }
                    printLog("distinct updateNotes dictory:" + distinctDirectorys.toString());

                    // 更新UpdateNotes
                    for (String filePath : distinctDirectorys) {
                        Map<String, String> maxVersionNumAndLine = new HashMap<>();
                        ResultObj resultObj = getMaxVersionNum(filePath, userName, maxVersionNumAndLine);
                        if (!resultObj.isOk()) {
                            hasErr.set(true);
                            resultList.add(resultObj);
                            failFileNum.set(failFileNum.get() + 1);
                            continue;
                        }
                        ResultObj resultObj1 = insertNewVersionByNewFile(filePath, maxVersionNumAndLine, msg, taskNo, taskType);
                        if (!resultObj1.isOk()) {
                            hasErr.set(true);
                            resultList.add(resultObj1);
                            failFileNum.set(failFileNum.get() + 1);
                            continue;
                        } else {
                            resultList.add(resultObj1);
                            successFileNum.set(successFileNum.get() + 1);
                            VirtualFile virtualFile = VfsUtil.findFileByIoFile(new File(filePath), true);
                            if (virtualFile != null) {
                                virtualFile.refresh(false, false);
                            }
                        }
                    }

                }*/

                // 判断是否有报错返回展示对应信息
                if (hasErr.get()) {
                    vnugui.showMessageDialog(e, resultList, successFileNum.get(), failFileNum.get());
                } else {
                    Messages.showInfoMessage(MSG_SUCCESS, MSG_MESSGE);
                }
                vnugui.frame.dispose();
            }
        }


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
            // 区分不同的versionNum (1 = VXXX(XXXXXXXX)/ 2 = VX.X.X(XXXXXXXX)/ 3 = XXXXXXXX)
            String vType = "";
            // 文件名
            String fileName = file.getName();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                extType = fileName.substring(dotIndex + 1);
                if (!"java".equalsIgnoreCase(extType) && !"js".equalsIgnoreCase(extType) && !("txt".equalsIgnoreCase(extType) && "UpdateNotes.txt".equalsIgnoreCase(fileName))) {
                    printLog("无效的文件：" + filePath);
                    resultObj.setMessage(MSG_208NOTSUPPORT);
                    resultObj.setFilePath(filePath);
                    resultObj.setOk(false);
                    return resultObj;
                }
            } else {
                resultObj.setMessage(MSG_208TASKTYPE);
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }
            try (InputStreamReader isr = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(isr)) {
                // VXXX(XXXXXXXX)
                String parttenStr1 = "V(\\d+)\\((\\d+)\\)";
                // VX.X.X(XXXXXXXX)
                String parttenStr2 = "V\\d+(\\.\\d+){1,3}\\((\\d+)\\)";
                // XXXXXXXX
                String parttenStr3 = "(\\d+)";

                Pattern partten1 = Pattern.compile(parttenStr1);
                Pattern partten2 = Pattern.compile(parttenStr2);
                Pattern partten3 = Pattern.compile(parttenStr3);

                int maxVersion = 0;
                int lineNumber = 0;
                // VXXX(XXXXXXXX)所在行 行号
                int maxVersionLineNumber = 0;
                // String verNum所在行
                int verNumLineNumber = 0;
                // VXXX(XXXXXXXX)所在行数据
                String maxVersionLineStr = "";
                // String verNum所在行是否包含final
                String verNumLine = "";
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

                    // public static final String verNum
                    // private static final String verNum
                    if (isVerNumDeclaration(line)) {
                        verNumLineNumber = lineNumber;
                        int start = line.indexOf("\"");
                        if (start != -1) {
                            // 从第一个双引号的下一个位置开始查找第二个双引号
                            int end = line.indexOf("\"", start + 1);
                            if (end != -1) {
                                String startStr = line.substring(0, start + 1); // 包含第一个双引号
                                String endStr = line.substring(end); // 包含第二个双引号
                                verNumLine = startStr + "(XXXX)" + endStr;
                            }
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

                    if (shouldBreakLoop(line, vType, find)) {
                        break;
                    }
                }

                if ("".equals(verNumLine) && "java".equalsIgnoreCase(extType)) {
                    resultObj.setMessage(MSG_NOSTRINGNUM);
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
                    // 未定位到版本号信息
                    resultObj.setMessage(MSG_NOVERSIONNUM);
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
                        resultObj.setMessage(MSG_NOVERSIONNUM);
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
                        printLog("No match found.");
                    }
                } else {
                    printLog("No match found.");
                }
                printLog("最大版本号：V" + maxVersion + "，所在行：" + maxVersionLineNumber + ", verNum所在行：" + verNumLineNumber);
                maxVersionNumAndLine.put("maxVersion", maxVersion + "");
                maxVersionNumAndLine.put("maxVersionLineNumber", maxVersionLineNumber + "");
                maxVersionNumAndLine.put("verNumLineNumber", verNumLineNumber + "");
                maxVersionNumAndLine.put("extType", extType);
                maxVersionNumAndLine.put("beforeMsg", beforeMsg);
                maxVersionNumAndLine.put("versionNum", versionNum);
                maxVersionNumAndLine.put("vType", vType);
                maxVersionNumAndLine.put("verNumLine", verNumLine);
            } catch (IOException e) {
                if (logPrint) {
                    e.printStackTrace();
                }
                resultObj.setOk(false);
                resultObj.setFilePath(filePath);
                resultObj.setMessage(e.getMessage());
                return resultObj;
            }
        } catch (Exception e) {
            if (logPrint) {
                e.printStackTrace();
            }
            resultObj.setOk(false);
            resultObj.setFilePath(filePath);
            resultObj.setMessage(e.getMessage());
            return resultObj;
        }
        resultObj.setOk(true);
        return resultObj;
    }

    /**
     * 是否为verNum行
     *
     * <p>Author: pengtai
     * <p>Create Time:2025/10/15
     *
     * @param line
     * @return boolean
     */
    private static boolean isVerNumDeclaration(String line) {
        boolean containsVerNum = line.contains("String verNum ") || line.contains("String VERNUM ") || line.contains("String VER_NUM ");
        boolean hasValidModifier = (line.trim().startsWith("private") && line.contains("private static"))
                || (line.trim().startsWith("public") && line.contains("public static"))
                || (line.trim().startsWith("protected") && line.contains("protected static"));

        return containsVerNum && hasValidModifier;
    }

    private static boolean shouldBreakLoop(String line, String vType, boolean find) {
        // 检查是否是verNum声明行
        if (isVerNumDeclaration(line)) {
            return true;
        }

        // 检查其他终止条件
        if (line.contains("@Autowired")
                || (line.contains("define") && !line.contains("defined"))
                || ("3".equals(vType) && find)) {
            return true;
        }

        return false;
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
                //未定位到版本号信息
                resultObj.setMessage(MSG_NOVERSIONNUM);
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }
            //如果为空则直接跳过
            String extType = maxVersionNumAndLine.get("extType");
            if (extType == null || extType.length() == 0) {
                // 无效的文件类型
                resultObj.setMessage(MSG_208TASKTYPE);
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }
            String maxVersionLineNumber = maxVersionNumAndLine.get("maxVersionLineNumber");
            if (maxVersionLineNumber == null || maxVersionLineNumber.length() <= 0 || "0".equals(maxVersionLineNumber)) {
                // 未找到最大版本号所在行
                resultObj.setMessage(MSG_ADMIN_MAXVERLINENUM);
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }
            String maxVersion = maxVersionNumAndLine.get("maxVersion");
            if (maxVersion == null || maxVersion.length() <= 0 || "0".equals(maxVersion)) {
                //未识别出最大版本号
                resultObj.setMessage(MSG_ADMIN_MAXVERNUM);
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }

            String verNumLineNumber = maxVersionNumAndLine.get("verNumLineNumber");
            if ("java".equalsIgnoreCase(extType) && (verNumLineNumber == null || verNumLineNumber.length() <= 0 || "0".equals(verNumLineNumber))) {
                //未识别出String verNum所在行
                resultObj.setMessage(MSG_NOSTRINGNUM);
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }

            // 中间符
            String beforeMsg = maxVersionNumAndLine.get("beforeMsg");
            if (beforeMsg == null || beforeMsg.length() <= 0) {
                // beforeMsg识别出错，请联系管理员！
                resultObj.setMessage(MSG_ADMIN_BEFOREGEN);
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }

            String versionNum = maxVersionNumAndLine.get("versionNum");
            if ("java".equalsIgnoreCase(extType) && (versionNum == null || versionNum.length() <= 0)) {
                //verNum生成失败，请联系管理员检查！
                resultObj.setMessage(MSG_ADMIN_VERNUMGEN);
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }
            String verNumLine = maxVersionNumAndLine.get("verNumLine");
            if ("java".equalsIgnoreCase(extType) && (verNumLine == null || verNumLine.length() <= 0)) {
                resultObj.setMessage(MSG_NOSTRINGNUM);
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }

            // versionNum类型 VXXX-》1、VX.X.X-》2
            String vType = maxVersionNumAndLine.get("vType");
            if (vType == null || vType.length() <= 0) {
                // 无效的文件类型
                resultObj.setMessage(MSG_208TASKTYPE);
                resultObj.setFilePath(filePath);
                resultObj.setOk(false);
                return resultObj;
            }

            String enterLine = System.getProperty("line.separator", "\r\n");
            int targetLineNumber = Integer.valueOf(maxVersionLineNumber) + 1;
            int targetVerNumLineNumber = Integer.valueOf(verNumLineNumber);

            if ("java".equalsIgnoreCase(extType)) {
                if (targetVerNumLineNumber <= targetLineNumber) {
                    // 检测出String verNum所在行小于或等于增加updateNotes注释行，请检查！
                    resultObj.setMessage(MSG_ADMIN_ERRVERNUM);
                    resultObj.setFilePath(filePath);
                    resultObj.setOk(false);
                    return resultObj;
                }
            }

            String newLine = beforeMsg + msg + "," + taskType + "：" + taskNo;
            // 是否包含final
            String containsFinal = maxVersionNumAndLine.get("containsFinal");
            String verNumNewLine = maxVersionNumAndLine.get("verNumLine").replace("(XXXX)", versionNum);

            int totalLines = 0;
            try {
                totalLines = countLinesInFile(filePath);
            } catch (IOException e) {
                if (logPrint) {
                    e.printStackTrace();
                }
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
                if (logPrint) {
                    e.printStackTrace();
                }
                resultObj.setOk(false);
                resultObj.setFilePath(filePath);
                resultObj.setMessage(e.getMessage());
                return resultObj;
            }

            // 将临时文件重命名为原文件，覆盖原文件
            try {
                renameFile("temp_file.txt", filePath);
            } catch (IOException e) {
                if (logPrint) {
                    e.printStackTrace();
                }
                resultObj.setOk(false);
                resultObj.setFilePath(filePath);
                resultObj.setMessage(e.getMessage());
                return resultObj;
            }
        } catch (Exception e) {
            if (logPrint) {
                e.printStackTrace();
            }
            resultObj.setOk(false);
            resultObj.setFilePath(filePath);
            resultObj.setMessage(e.getMessage());
            return resultObj;
        }
        resultObj.setFilePath(filePath);
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

}
