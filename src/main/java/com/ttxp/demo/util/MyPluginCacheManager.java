package com.ttxp.demo.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 缓存管理器
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
 *
 * @version V1.0.0
 *
 */
@State(
        name = "MyPluginCacheManager",
        storages = {@Storage("myplugin_cache.xml")}
)
public class MyPluginCacheManager implements PersistentStateComponent<MyPluginCacheManager> {
    private Map<String,String> cachedFormValue;

    public Map<String,String> getCachedFormValue() {
        return cachedFormValue;
    }

    public void setCachedFormValue(Map<String,String> value) {
        cachedFormValue = value;
    }

    @Nullable
    @Override
    public MyPluginCacheManager getState() {
        return this;
    }

    @Override
    public void loadState(MyPluginCacheManager state) {
        if (state!= null) {
            cachedFormValue = state.cachedFormValue;
        }
    }

    public static MyPluginCacheManager getInstance() {
        return ApplicationManager.getApplication().getService(MyPluginCacheManager.class);
    }
}