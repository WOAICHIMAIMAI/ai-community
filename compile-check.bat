@echo off
echo 正在检查抢红包功能的编译状态...

echo.
echo 1. 检查注解类编译...
javac -cp "target/classes;target/dependency/*" -d "target/test-classes" "src/main/java/com/zheng/aicommunitybackend/annotation/RateLimit.java" "src/main/java/com/zheng/aicommunitybackend/annotation/RateLimits.java"
if %errorlevel% neq 0 (
    echo 注解类编译失败！
    exit /b 1
) else (
    echo 注解类编译成功！
)

echo.
echo 2. 检查Controller类编译...
javac -cp "target/classes;target/dependency/*" -d "target/test-classes" "src/main/java/com/zheng/aicommunitybackend/controller/user/UserRedPacketController.java"
if %errorlevel% neq 0 (
    echo Controller类编译失败！
    exit /b 1
) else (
    echo Controller类编译成功！
)

echo.
echo 3. 检查测试类编译...
javac -cp "target/classes;target/dependency/*;target/test-classes" -d "target/test-classes" "src/test/java/com/zheng/aicommunitybackend/CompileTest.java"
if %errorlevel% neq 0 (
    echo 测试类编译失败！
    exit /b 1
) else (
    echo 测试类编译成功！
)

echo.
echo ✅ 所有抢红包功能相关类编译成功！
echo 重复注解问题已修复！
