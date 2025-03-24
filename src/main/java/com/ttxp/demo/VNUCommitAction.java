package com.ttxp.demo;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModel;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * idea - commit界面右键
 *
 * <p>
 * 创建时间：2025/2/24
 * <p>
 *
 * <p>
 * 修改时间：2025/2/24
 * <p>
 *
 * @author wangjia
 * @version V1.0.0
 */
public class VNUCommitAction extends AnAction {

    private static Map<Module, List<VirtualFile>> moduleMap = new HashMap<>();
    private static final String TEST_PATH = "/src/test";

    @Override
    public void actionPerformed(AnActionEvent event) {
        // 清空模块映射，用于存储新的变更信息
        moduleMap.clear();

        // 获取当前事件相关的项目
        Project project = event.getProject();
        // 如果项目为空，则直接返回
        if (project == null) {
            return;
        }

        // 获取本地文件系统实例
        LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

        // 获取当前事件的变更列表
        Change[] changes = (Change[])event.getData(VcsDataKeys.CHANGES);

        // 遍历每个变更
        for (Change change : changes) {
            // 如果变更是删除类型，则跳过
            if (change.getType().equals(Change.Type.DELETED)) {
                continue;
            }

            // 获取变更后的修订版本
            ContentRevision afterRevision = change.getAfterRevision();
            // 如果变更后修订版本为空，则跳过
            if (afterRevision == null) {
                continue;
            }
            // 获取文件路径
            FilePath file = afterRevision.getFile();
            // 获取IO文件对象
            File ioFile = file.getIOFile();
            // 在本地文件系统中查找对应的虚拟文件
            VirtualFile virtualFile = localFileSystem.findFileByIoFile(ioFile);
            // 如果虚拟文件为空，则跳过
            if (virtualFile == null) {
                continue;
            }
            // 查找文件所属的模块
            Module module = ModuleUtilCore.findModuleForFile(virtualFile, project);
            // 如果模块不为空
            if (module != null) {
                // 获取模块的根模型
                ModuleRootModel moduleRootModel = ModuleRootManager.getInstance(module);
                // 获取内容根目录数组
                VirtualFile[] contentRoots = moduleRootModel.getContentRoots();
                // 如果内容根目录为空，则跳过
                if (contentRoots == null || contentRoots.length <= 0) {
                    continue;
                }
                // 获取第一个内容根目录的路径
                String modulePath = contentRoots[0].getPath();

                // 获取虚拟文件的规范文件路径
                VirtualFile canonicalFile = virtualFile.getCanonicalFile();
                // 如果规范文件为空，则跳过
                if (canonicalFile == null) {
                    continue;
                }
                // 检查文件路径是否以模块路径加测试路径开头
                if (canonicalFile.getPath().startsWith(modulePath + TEST_PATH)) {
                    // 如果是测试文件夹，跳过处理
                    continue;
                }

                // 如果模块映射中已包含该模块，则添加虚拟文件到对应模块的列表中
                if (moduleMap.containsKey(module)) {
                    moduleMap.get(module).add(virtualFile);
                } else {
                    // 否则，创建新的文件列表，添加虚拟文件，并放入模块映射中
                    ArrayList<VirtualFile> changes1 = new ArrayList<>();
                    changes1.add(virtualFile);
                    moduleMap.put(module, changes1);
                }
            }
        }

        // 如果模块映射为空，则直接返回
        if (moduleMap.isEmpty()) {
            return;
        }
/*
        // 获取模块映射的键集
        Set<Module> modules = moduleMap.keySet();
        // 遍历每个模块
        for (Module module : modules) {
            // 获取模块对应的虚拟文件列表
            List<VirtualFile> virtualFiles = moduleMap.get(module);
            // 检查是否存在名为"UpdateNotes.txt"的文件
            Optional<VirtualFile> optionalVirtualFile = virtualFiles.stream().filter(virtualFile -> virtualFile.getName().equalsIgnoreCase("UpdateNotes.txt")).findAny();
            // 如果不存在
            if (!optionalVirtualFile.isPresent()) {
                // 获取当前模块的根模型
                ModuleRootModel moduleRootModel = ModuleRootManager.getInstance(module);
                // 获取内容根目录数组
                VirtualFile[] contentRoots = moduleRootModel.getContentRoots();
                // 如果内容根目录为空，则跳过
                if (contentRoots == null || contentRoots.length <= 0) {
                    return;
                }
                // 获取第一个内容根目录的路径
                String modulePath = contentRoots[0].getPath();
                // 拼接updateNotes路径
                String updateNotesPath = modulePath + "/src/main/resources/updatenotes/UpdateNotes.txt";
                // 在本地文件系统中查找对应的虚拟文件
                VirtualFile virtualFile = localFileSystem.findFileByPath(updateNotesPath);
                // 如果虚拟文件存在，则添加到虚拟文件列表中
                if (virtualFile != null) {
                    virtualFiles.add(virtualFile);
                }
            }
        }
         */

        // 使用 Stream API 平铺值并收集到一个列表中
        List<VirtualFile> flattenedList = moduleMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // 保存所有文档
        FileDocumentManager.getInstance().saveAllDocuments();
        // 创建版本号更新对象
        VNUGUI handleFile = new VNUGUI();
        // 显示对话框并传递虚拟文件数组
        handleFile.showDialog(event, flattenedList.toArray(new VirtualFile[0]));

    }
}
