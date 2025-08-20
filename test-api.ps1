# 测试数据分析API
Write-Host "启动Spring Boot应用..."
Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -NoNewWindow

Write-Host "等待应用启动..."
Start-Sleep -Seconds 30

Write-Host "测试数据分析摘要API..."
try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/analytics/summary?startDate=2024-01-01&endDate=2024-12-31" -Method GET -ContentType "application/json"
    Write-Host "API响应成功:"
    $result | ConvertTo-Json -Depth 10
} catch {
    Write-Host "API调用失败: $($_.Exception.Message)"
    Write-Host "检查应用是否正常启动..."
    Get-Process | Where-Object {$_.ProcessName -like "*java*"}
}
