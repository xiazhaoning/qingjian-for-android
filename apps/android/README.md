# 青简 Android（实验版）

这是青简项目的 Android IME 适配层。它直接复用仓库中的 `qingjian-core`，Android 层负责输入法服务、键盘 UI 和与应用的文本提交。

当前实验版目标：
- 拼音输入
- 青简 Core 候选生成与排序
- 候选点击上屏
- 删除、空格选首候选、回车
- ARM64/ARMv7/x86/x86_64 原生库

## 云端构建

仓库根目录的 `.github/workflows/android.yml` 会在 GitHub Actions 中自动安装 Android NDK、Rust 和 cargo-ndk，并生成 APK artifact。

工作流完成后，在 Actions → Build Qingjian Android APK → Artifacts 下载 `qingjian-android-debug`，其中的 `app-debug.apk` 即可安装到 Android 手机上测试。

## 注意

这是第一版适配层，不代表已经覆盖桌面版青简的全部功能。双拼、五笔、用户词频持久化、整句模型、翻译、设置项等会在后续迭代中接入。
