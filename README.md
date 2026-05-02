# 记一笔 (AccountBook)

一款基于 Kotlin + Jetpack Compose 开发的 Android 个人记账应用，支持支出/收入记录、月度统计、分类管理、数据导出导入与小组件自定义。

## 功能展示

| 首页 | 统计 | 记账 | 设置 |
|------|------|------|------|
| 当月账单筛选（全部/支出/收入） | 按分类支出占比 + 进度条 | 金额输入、支出/收入切换、分类选择、备注、日期 | 小组件颜色/文字自定义、账单导出导入 |

- **账单管理** — 添加/删除收支记录，支持备注与日期选择
- **分类筛选** — 全部 / 支出 / 收入三种视图切换，仅显示当月账单
- **月度统计** — 按分类展示支出金额与占比进度条
- **分类管理** — 添加/编辑/删除所有分类，支持长按拖动排序
- **数据导出导入** — 按月份导出账单为 JSON / PDF 文件，支持从 JSON 文件导入
- **小组件** — 桌面小组件一键记账，支持自定义背景颜色和文字
- **动态主题** — 跟随系统深色模式，Android 12+ 支持动态取色
- **侧滑返回** — 所有子页面支持手势侧滑返回上一级

## 项目结构

```
AccountBook/
├── build.gradle.kts              # 根构建配置
├── settings.gradle.kts           # 项目设置
├── app/
│   ├── build.gradle.kts          # 应用构建配置（依赖声明）
│   └── src/main/java/com/example/accountbook/
│       ├── MainActivity.kt       # 入口 Activity + 底部导航 + 路由管理
│       ├── viewmodel/
│       │   └── MainViewModel.kt  # 全局状态管理（唯一 ViewModel）
│       ├── data/
│       │   ├── AppDatabase.kt    # Room 数据库定义 + 种子数据 + 迁移
│       │   ├── entity/
│       │   │   ├── Category.kt              # 分类实体（含排序字段）
│       │   │   ├── Transaction.kt           # 交易实体
│       │   │   └── TransactionWithCategory.kt # 交易+分类联合查询
│       │   ├── dao/
│       │   │   ├── CategoryDao.kt    # 分类 DAO（CRUD + 排序）
│       │   │   └── TransactionDao.kt # 交易 DAO（筛选/汇总/导出）
│       │   └── repository/
│       │       └── AppRepository.kt  # 数据仓库（统一数据访问入口）
│       ├── ui/
│       │   ├── screen/
│       │   │   ├── HomeScreen.kt             # 首页：当月账单列表 + 筛选
│       │   │   ├── StatsScreen.kt            # 统计页：分类占比可视化
│       │   │   ├── AddTransactionScreen.kt   # 记账页：金额/分类/备注/日期
│       │   │   ├── CategoryManageScreen.kt   # 分类管理页：拖动排序 + 增删改
│       │   │   └── SettingsScreen.kt         # 设置页：小组件定制 + 数据导出导入
│       │   └── theme/
│       │       ├── Color.kt   # 色板定义
│       │       ├── Theme.kt   # Material 3 主题（支持深色 + 动态取色）
│       │       └── Type.kt    # 字体排版
│       ├── util/
│       │   ├── WidgetPrefsManager.kt    # 小组件偏好设置管理
│       │   └── ExportImportManager.kt   # JSON/PDF 导出导入逻辑
│       └── widget/
│           └── AccountBookWidgetProvider.kt # 桌面小组件（支持自定义颜色/文字）
```

## 核心代码说明

| 文件 | 作用 |
|------|------|
| `MainActivity.kt` | 应用唯一 Activity，搭载底部导航栏（账单 / 统计），管理全屏页面的路由切换 |
| `MainViewModel.kt` | 单一 ViewModel，集中持有筛选状态、表单状态、当月交易列表、月度汇总、分类列表、小组件偏好、导出导入状态，暴露所有增删改操作 |
| `AppDatabase.kt` | Room 数据库单例，`onCreate` 时写入餐饮/交通/购物等 9 个支出分类与工资/奖金/投资等 6 个收入分类的默认数据，含 v1→v2 迁移 |
| `TransactionDao.kt` | 交易表的 Flow 查询：按类型筛选、按日期范围筛选、按类型+日期组合筛选、按月汇总金额、同步导出查询 |
| `CategoryDao.kt` | 分类表的 CRUD 操作，按排序字段 + ID 排序，支持批量更新排序 |
| `AppRepository.kt` | 对 DAO 层的薄封装，提供统一数据访问给 ViewModel |
| `AddTransactionScreen.kt` | 记账表单：金额输入自动聚焦、支出/收入分段按钮、分类水平滚动选择、日期选择器、保存 |
| `CategoryManageScreen.kt` | 分类管理：统一列表展示，所有分类可编辑/删除，支持长按拖动排序，支出/收入类型切换 |
| `HomeScreen.kt` | 首页：当月账单列表，筛选芯片（全部/支出/收入），长按删除确认 |
| `StatsScreen.kt` | 统计页：按分类展示支出金额与占比进度条，右上角设置入口 |
| `SettingsScreen.kt` | 设置页：小组件颜色选取（10 色预设）、文字自定义、按月份导出 JSON/PDF、从 JSON 导入账单 |
| `AccountBookWidgetProvider.kt` | 桌面小组件：一键打开记账页，支持自定义背景颜色和按钮文字 |
| `WidgetPrefsManager.kt` | SharedPreferences 封装，管理小组件颜色和文字的读写 |
| `ExportImportManager.kt` | JSON/PDF 导出与 JSON 导入解析逻辑 |

## 技术栈

- **语言**：Kotlin
- **UI**：Jetpack Compose + Material 3 (Adaptive Navigation)
- **数据库**：Room (KSP) + Migration
- **架构**：MVVM（ViewModel + StateFlow + Compose 收集）
- **最低 SDK**：24 (Android 7.0)
- **目标 SDK**：36

## 构建运行

```bash
# 调试构建
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug

# 运行测试
./gradlew test
```
