@echo off
echo 正在验证抢红包功能编译状态...

echo.
echo 1. 检查DTO类编译...
javac -cp "target/classes;target/dependency/*" -d "target/test-classes" ^
    "src/main/java/com/zheng/aicommunitybackend/domain/dto/RedPacketActivityCreateDTO.java" ^
    "src/main/java/com/zheng/aicommunitybackend/domain/dto/RedPacketGrabDTO.java" ^
    "src/main/java/com/zheng/aicommunitybackend/domain/dto/RedPacketRecordQueryDTO.java" ^
    "src/main/java/com/zheng/aicommunitybackend/domain/dto/RedPacketActivityOperateDTO.java"

if %errorlevel% neq 0 (
    echo ❌ DTO类编译失败！
    exit /b 1
) else (
    echo ✅ DTO类编译成功！
)

echo.
echo 2. 检查VO类编译...
javac -cp "target/classes;target/dependency/*" -d "target/test-classes" ^
    "src/main/java/com/zheng/aicommunitybackend/domain/vo/RedPacketActivityListVO.java"

if %errorlevel% neq 0 (
    echo ❌ VO类编译失败！
    exit /b 1
) else (
    echo ✅ VO类编译成功！
)

echo.
echo 3. 检查Service实现类编译...
javac -cp "target/classes;target/dependency/*" -d "target/test-classes" ^
    "src/main/java/com/zheng/aicommunitybackend/service/impl/RedPacketServiceImpl.java"

if %errorlevel% neq 0 (
    echo ❌ Service实现类编译失败！
    exit /b 1
) else (
    echo ✅ Service实现类编译成功！
)

echo.
echo 4. 检查Controller类编译...
javac -cp "target/classes;target/dependency/*" -d "target/test-classes" ^
    "src/main/java/com/zheng/aicommunitybackend/controller/user/UserRedPacketController.java" ^
    "src/main/java/com/zheng/aicommunitybackend/controller/admin/AdminRedPacketController.java"

if %errorlevel% neq 0 (
    echo ❌ Controller类编译失败！
    exit /b 1
) else (
    echo ✅ Controller类编译成功！
)

echo.
echo 5. 检查测试类编译...
javac -cp "target/classes;target/dependency/*;target/test-classes" -d "target/test-classes" ^
    "src/test/java/com/zheng/aicommunitybackend/CompileFixTest.java"

if %errorlevel% neq 0 (
    echo ❌ 测试类编译失败！
    exit /b 1
) else (
    echo ✅ 测试类编译成功！
)

echo.
echo 🎉 所有抢红包功能相关类编译成功！
echo 📋 修复内容：
echo    - 移除了DTO中不存在的configs字段引用
echo    - 使用UserContext替代JWT手动解析
echo    - 统一使用DTO/VO封装请求和响应
echo    - 修复了重复注解问题
echo    - 优化了接口设计符合项目规范
