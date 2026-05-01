# 记一笔 (AccountBook)

一款基于 Kotlin + Jetpack Compose 开发的 Android 个人记账应用，支持支出/收入记录、月度统计与分类管理。

## 功能展示

| 首页 | 统计 | 记账 |
|------|------|------|
| 按月筛选账单，顶部展示月度支出/收入/结余 | 月度收支总览 + 按分类支出占比 | 金额输入、支出/收入切换、分类选择、备注、日期 |

- **账单管理** — 添加/删除收支记录，支持备注与日期选择
- **分类筛选** — 全部 / 支出 / 收入三种视图切换
- **月度统计** — 左右滑动切换月份，查看当月支出、收入、结余
- **分类占比** — 统计页按分类展示支出金额与占比进度条
- **分类管理** — 支持添加/编辑/删除自定义分类，与默认分类共存
- **动态主题** — 跟随系统深色模式，Android 12+ 支持动态取色

## 项目结构

```
AccountBook/
├── build.gradle.kts              # 根构建配置
├── settings.gradle.kts           # 项目设置
├── app/
│   ├── build.gradle.kts          # 应用构建配置（依赖声明）
│   └── src/main/java/com/example/accountbook/
│       ├── MainActivity.kt       # 入口 Activity + 底部导航
│       ├── viewmodel/
│       │   └── MainViewModel.kt  # 全局状态管理（唯一 ViewModel）
│       ├── data/
│       │   ├── AppDatabase.kt    # Room 数据库定义 + 种子数据
│       │   ├── entity/
│       │   │   ├── Category.kt              # 分类实体
│       │   │   ├── Transaction.kt           # 交易实体
│       │   │   └── TransactionWithCategory.kt # 交易+分类联合查询
│       │   ├── dao/
│       │   │   ├── CategoryDao.kt    # 分类 DAO
│       │   │   └── TransactionDao.kt # 交易 DAO
│       │   └── repository/
│       │       └── AppRepository.kt  # 数据仓库（统一数据访问入口）
│       └── ui/
│           ├── screen/
│           │   ├── HomeScreen.kt             # 首页：账单列表 + 月度摘要
│           │   ├── StatsScreen.kt            # 统计页：分类占比可视化
│           │   ├── AddTransactionScreen.kt   # 记账页：金额/分类/备注/日期
│           │   └── CategoryManageScreen.kt   # 分类管理页：增删改查
│           └── theme/
│               ├── Color.kt   # 色板定义
│               ├── Theme.kt   # Material 3 主题（支持深色 + 动态取色）
│               └── Type.kt    # 字体排版
```

## 核心代码说明

| 文件 | 作用 |
|------|------|
| `MainActivity.kt` | 应用唯一 Activity，搭载底部导航栏（账单 / 统计），管理全屏记账页与分类管理页的路由切换 |
| `MainViewModel.kt` | 单一 ViewModel，集中持有筛选状态、表单状态、交易列表、月度汇总、分类列表，暴露所有增删改操作 |
| `AppDatabase.kt` | Room 数据库单例，`onCreate` 时写入餐饮/交通/购物等 9 个支出分类与工资/奖金/投资等 6 个收入分类的默认数据 |
| `TransactionDao.kt` | 交易表的 Flow 查询：按类型筛选、按日期范围筛选、按月汇总金额 |
| `CategoryDao.kt` | 分类表的 CRUD 操作，支持按类型（支出/收入）查询 |
| `AppRepository.kt` | 对 DAO 层的薄封装，提供统一数据访问给 ViewModel |
| `AddTransactionScreen.kt` | 记账表单：金额输入自动聚焦、支出/收入分段按钮、分类水平滚动选择、日期选择器、保存 |
| `CategoryManageScreen.kt` | 默认分类（锁定）与自定义分类（可编辑/删除）分区展示，支持新增/修改/删除弹窗 |
| `HomeScreen.kt` | 月份切换器 + 筛选芯片 + 账单列表，长按删除确认 |
| `StatsScreen.kt` | 月结余卡片 + 按分类支出金额与进度条 |

## 技术栈

- **语言**：Kotlin
- **UI**：Jetpack Compose + Material 3 (Adaptive Navigation)
- **数据库**：Room (KSP)
- **架构**：MVVM（ViewModel + StateFlow + Compose 收集）
- **最低 SDK**：24 (Android 7.0)
- **目标 SDK**：36

## 构建运行

```bash
# 调试构建
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug
```
