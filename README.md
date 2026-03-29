# SpeakerYou

MicYou 插件 - 将手机作为电脑扬声器，通过 WiFi / 蓝牙 / USB 传输音频。

> [!CAUTION]
> WIP
> 项目处于开发阶段，请勿使用

## 功能

- **三种连接模式**：WiFi、蓝牙、USB
- **跨平台**：支持桌面端和移动端
- **实时状态**：显示连接状态和流媒体信息

## 安装

1. 下载 `SpeakerYou.micyou-plugin.zip`
2. 打开 MicYou 应用
3. 进入设置 → 插件 → 导入插件
4. 选择 zip 文件并启用

## 使用

### 桌面端

1. 点击主界面右上角的插件图标
2. 选择 SpeakerYou 打开主窗口
3. 在手机端也安装并启用插件
4. 选择连接模式（WiFi/蓝牙/USB）
5. 输入电脑 IP 地址
6. 点击 Start 开始串流

### 移动端

> **注意**：当前版本移动端因 DEX 格式兼容问题暂无法启用，后续版本将修复。

## 构建

```bash
# Windows
./gradlew.bat packagePlugin

# 输出位置
# build/distributions/SpeakerYou.micyou-plugin.zip
```

## 作者

WongWingChun

## 许可证

GPLv3