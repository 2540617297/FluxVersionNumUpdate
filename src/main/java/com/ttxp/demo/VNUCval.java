package com.ttxp.demo;

/**
 * 常量类
 *
 * <p>
 * 创建时间：2024/11/15
 * <p>
 *
 * <p>
 * 修改时间：2024/11/15
 * <p>
 *
 * @author pengtai
 * @version V1.0.0
 */
public class VNUCval {

    /**
     * label
     */
    /**
     * ----------设置------------
     */
    /**
     * 未选中更新
     */
    public static final String S_UPDATE_ITEM_F_L = "  同步更新UpdateNotes文件";
    /**
     * 选中更新
     */
    public static final String S_UPDATE_ITEM_S_L = "* 同步更新UpdateNotes文件";
    /**
     * 未选中缓存
     */
    public static final String S_CACHE_ITEM_F_L = "  缓存任务号、修改描述";
    /**
     * 选中缓存
     */
    public static final String S_CACHE_ITEM_S_L = "* 缓存任务号、修改描述";
    /**
     * 执行成功复制Notes(git提交)
     */
    public static final String S_CACHE_COPYNOTES_F_L = "  执行成功复制Notes(git提交)";
    /**
     * 执行成功复制Notes(git提交)
     */
    public static final String S_CACHE_COPYNOTES_S_L = "* 执行成功复制Notes(git提交)";
    /**
     * 自动跳过文件夹-否
     */
    public static final String S_CACHE_SKIPPACKAGE_F_L = "  自动跳过文件夹";
    /**
     * 自动跳过文件夹
     */
    public static final String S_CACHE_SKIPPACKAGE_S_L = "* 自动跳过文件夹";


    /**
     * ------------表单-----------
     */
    /**
     * lebel-任务
     */
    public static final String F_TASKTYPE_R_L = "任务";
    /**
     * lebel-客服
     */
    public static final String F_TASKTYPE_K_L = "客服";
    /**
     * lebel-确认按钮
     */
    public static final String F_CONFIRM_K_L = "确认";
    /**
     * lebel-取消按钮
     */
    public static final String F_CANCEL_K_L = "取消";
    /**
     * lebel-修改文件
     */
    public static final String F_UPDATEFILES_K_L = "修改文件：";
    /**
     * title
     */
    public static final String F_TITLE_K_L = "更新VersionNum";
    /**
     * 设置
     */
    public static final String F_SETTINGS_K_L = "设置";
    /**
     * 姓名
     */
    public static final String F_USERNAME_K_L = "姓名：";
    /**
     * 修改描述
     */
    public static final String F_UPDATEMSG_K_L = "修改描述：";
    /**
     * 任务、客服
     */
    public static final String F_RORK_K_L = "任务/客服 ：";
    /**
     * 总文件个数：
     */
    public static final String F_FILESNUM_K_L = "总文件个数：";
    /**
     * 文件夹个数
     */
    public static final String F_DIRECTORY_K_L = "文件夹个数（不支持修改）：";
    /**
     * 文件个数
     */
    public static final String F_FILE_K_L = "文件个数：";


    /**
     * ---------------------常量-----------------------
     */
    /**
     * 同步更新
     */
    public static final String S_UPDATE_KEY = "S_UPDATE_KEY";
    /**
     * 缓存
     */
    public static final String S_CACHE_KEY = "S_CACHE_KEY";
    /**
     * 复制
     */
    public static final String S_COPY_KEY = "S_COPY_KEY";
    /**
     * 跳过
     */
    public static final String S_SKIP_KEY = "S_SKIP_KEY";

    /**
     * ----------------表单------------------
     */
    /**
     * 用户名
     */
    public static final String F_USERNAME = "F_USERNAME";
    /**
     * 任务号
     */
    public static final String F_TASKNO = "F_TASKNO";
    /**
     * 修改描述
     */
    public static final String F_NOTES = "F_NOTES";
    /**
     * 任务类型
     */
    public static final String F_TASKTYPE = "F_TASKTYPE";

    /**
     * --------------消息--------------------------
     */
    public static final String MSG_ISDISTORY = "当前目录为文件夹，暂不支持！";
    public static final String MSG_NOCHECKED = "请选择需要更新的文件！";
    public static final String MSG_MESSGE = "消息";
    public static final String MSG_200USERNAME = "请输入修改人名称！";
    public static final String MSG_200NOTES = "请输入修改描述！";
    public static final String MSG_200RORK = "请输入任务号！";
    public static final String MSG_CONFIRM = "注意！确认是否执行更新！！！";
    public static final String MSG_CONFIRM_UPDATE = "已选择同步更新工程UpdateNotes.txt文件！";
    public static final String MSG_SUCCESS = "执行成功！!请检查！!";
    public static final String MSG_ALLDIRECTORY = "当前选中目录为文件夹！暂不支持！";
    public static final String MSG_SUCCESSFILENUM = "成功文件个数：";
    public static final String MSG_FAILFILENUM = "失败文件个数：";
    public static final String MSG_FAILMSG = "错误信息：";
    public static final String MSG_208TASKTYPE = "无效的文件类型";
    public static final String MSG_208NOTSUPPORT = "暂不支持修改该文件";
    public static final String MSG_NOSTRINGNUM = "未定位到String verNum信息请检查！";
    public static final String MSG_NOVERSIONNUM = "未定位到版本号信息";
    public static final String MSG_ADMIN_MAXVERNUM = "未识别出最大版本号";
    public static final String MSG_ADMIN_MAXVERLINENUM = "未找到最大版本号所在行";
    public static final String MSG_ADMIN_VERNUMGEN = "verNum生成失败，请联系管理员检查！";
    public static final String MSG_ADMIN_PRIERR = "权限符识别失败，请检查（protected/public/private）！";
    public static final String MSG_ADMIN_ERRVERNUM = "检测出String verNum所在行小于或等于增加updateNotes注释行，请检查！";
    public static final String MSG_ADMIN_BEFOREGEN = "beforeMsg识别出错，请联系管理员！";
}
