package com.ttxp.demo;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import static com.ttxp.demo.util.VNUCval.*;

/**
 * 版本号更新-主入口(右键菜单)
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
public class VNUMainAction extends AnAction {


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
            Messages.showMessageDialog(e.getProject(), MSG_NOCHECKED, MSG_MESSGE, Messages.getInformationIcon());
            return;
        } else {
            // 判断是否全为文件夹
            boolean isAllDictory = true;
            for (VirtualFile file : files) {
                if (!file.isDirectory()) {
                    isAllDictory = false;
                }
            }
            if (isAllDictory) {
                Messages.showMessageDialog(e.getProject(), MSG_ALLDIRECTORY, MSG_MESSGE, Messages.getInformationIcon());
                return;
            }
        }

        FileDocumentManager.getInstance().saveAllDocuments();
        VNUGUI handleFile = new VNUGUI();
        handleFile.showDialog(e, files);
    }


}
