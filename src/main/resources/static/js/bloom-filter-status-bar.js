/**
 * 布隆过滤器状态栏组件
 * 轻量级的状态显示，可嵌入到任何页面
 */
class BloomFilterStatusBar {
    constructor(containerId, options = {}) {
        this.container = document.getElementById(containerId);
        this.options = {
            showDetails: false,
            autoStart: true,
            position: 'top-right', // top-left, top-right, bottom-left, bottom-right
            ...options
        };
        
        this.stats = {};
        this.pollTimer = null;
        this.pollInterval = 5 * 60 * 1000; // 默认5分钟
        this.isVisible = true;
        
        this.init();
    }

    init() {
        this.createStatusBar();
        if (this.options.autoStart) {
            this.startMonitoring();
        }
    }

    createStatusBar() {
        this.container.innerHTML = `
            <div class="bloom-status-bar" id="bloom-status-bar">
                <div class="status-content" onclick="bloomStatusBar.toggleDetails()">
                    <span class="status-icon" id="status-icon">📊</span>
                    <span class="status-text" id="status-text">布隆过滤器</span>
                    <span class="usage-value" id="usage-value">--</span>
                    <span class="toggle-icon" id="toggle-icon">▼</span>
                </div>
                <div class="status-details" id="status-details" style="display: none;">
                    <div class="detail-row">
                        <span>告警级别:</span>
                        <span id="alert-level-detail">--</span>
                    </div>
                    <div class="detail-row">
                        <span>已加载URL:</span>
                        <span id="loaded-count-detail">--</span>
                    </div>
                    <div class="detail-row">
                        <span>总容量:</span>
                        <span id="total-capacity-detail">--</span>
                    </div>
                    <div class="detail-row">
                        <span>最后更新:</span>
                        <span id="last-update-detail">--</span>
                    </div>
                    <div class="detail-actions">
                        <button onclick="bloomStatusBar.manualRefresh()" class="action-btn">刷新</button>
                        <button onclick="bloomStatusBar.openMonitorPage()" class="action-btn">详情</button>
                    </div>
                </div>
                <div class="close-btn" onclick="bloomStatusBar.hide()" title="隐藏">×</div>
            </div>
        `;

        this.addStyles();
        window.bloomStatusBar = this; // 全局访问
    }

    addStyles() {
        if (document.getElementById('bloom-status-bar-styles')) return;

        const styles = `
            <style id="bloom-status-bar-styles">
                .bloom-status-bar {
                    position: fixed;
                    ${this.options.position.includes('top') ? 'top: 20px;' : 'bottom: 20px;'}
                    ${this.options.position.includes('right') ? 'right: 20px;' : 'left: 20px;'}
                    background: white;
                    border-radius: 12px;
                    box-shadow: 0 4px 20px rgba(0,0,0,0.15);
                    border: 1px solid #e0e0e0;
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    font-size: 14px;
                    z-index: 9999;
                    min-width: 200px;
                    max-width: 300px;
                    transition: all 0.3s ease;
                }

                .bloom-status-bar:hover {
                    box-shadow: 0 6px 25px rgba(0,0,0,0.2);
                }

                .status-content {
                    display: flex;
                    align-items: center;
                    padding: 12px 16px;
                    cursor: pointer;
                    gap: 8px;
                    position: relative;
                }

                .status-content:hover {
                    background: #f8f9fa;
                    border-radius: 12px 12px 0 0;
                }

                .status-icon {
                    font-size: 16px;
                }

                .status-text {
                    flex: 1;
                    font-weight: 500;
                    color: #333;
                }

                .usage-value {
                    font-weight: bold;
                    padding: 2px 8px;
                    border-radius: 12px;
                    font-size: 12px;
                }

                .usage-normal { background: #d4edda; color: #155724; }
                .usage-info { background: #d1ecf1; color: #0c5460; }
                .usage-warning { background: #fff3cd; color: #856404; }
                .usage-critical { background: #f8d7da; color: #721c24; }

                .toggle-icon {
                    font-size: 12px;
                    color: #666;
                    transition: transform 0.3s ease;
                }

                .toggle-icon.expanded {
                    transform: rotate(180deg);
                }

                .close-btn {
                    position: absolute;
                    top: 4px;
                    right: 8px;
                    width: 20px;
                    height: 20px;
                    border-radius: 50%;
                    background: #f0f0f0;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    cursor: pointer;
                    font-size: 14px;
                    color: #666;
                    opacity: 0;
                    transition: opacity 0.3s ease;
                }

                .bloom-status-bar:hover .close-btn {
                    opacity: 1;
                }

                .close-btn:hover {
                    background: #e0e0e0;
                    color: #333;
                }

                .status-details {
                    border-top: 1px solid #e0e0e0;
                    padding: 12px 16px;
                    background: #f8f9fa;
                    border-radius: 0 0 12px 12px;
                }

                .detail-row {
                    display: flex;
                    justify-content: space-between;
                    margin-bottom: 8px;
                    font-size: 13px;
                }

                .detail-row:last-of-type {
                    margin-bottom: 12px;
                }

                .detail-row span:first-child {
                    color: #666;
                }

                .detail-row span:last-child {
                    font-weight: 500;
                    color: #333;
                }

                .detail-actions {
                    display: flex;
                    gap: 8px;
                    justify-content: center;
                }

                .action-btn {
                    padding: 6px 12px;
                    border: 1px solid #ddd;
                    background: white;
                    border-radius: 6px;
                    cursor: pointer;
                    font-size: 12px;
                    transition: all 0.3s ease;
                }

                .action-btn:hover {
                    background: #007bff;
                    color: white;
                    border-color: #007bff;
                }

                .bloom-status-bar.updating {
                    opacity: 0.8;
                }

                .bloom-status-bar.updating .status-icon {
                    animation: pulse 1s infinite;
                }

                @keyframes pulse {
                    0%, 100% { opacity: 1; }
                    50% { opacity: 0.5; }
                }

                /* 隐藏状态 */
                .bloom-status-bar.hidden {
                    transform: translateX(${this.options.position.includes('right') ? '100%' : '-100%'});
                    opacity: 0;
                    pointer-events: none;
                }

                /* 响应式设计 */
                @media (max-width: 768px) {
                    .bloom-status-bar {
                        position: fixed;
                        bottom: 20px;
                        left: 20px;
                        right: 20px;
                        top: auto;
                        max-width: none;
                    }
                }
            </style>
        `;
        
        document.head.insertAdjacentHTML('beforeend', styles);
    }

    async fetchStats() {
        try {
            document.getElementById('bloom-status-bar').classList.add('updating');
            
            const response = await fetch('/admin/news/bloom-filter/stats');
            const data = await response.json();
            
            if (data.code === 200) {
                this.stats = data.data;
                this.updateDisplay();
                this.updatePollInterval();
            } else {
                throw new Error(data.message || '获取数据失败');
            }
        } catch (error) {
            console.error('布隆过滤器状态获取失败:', error);
            this.showError();
        } finally {
            document.getElementById('bloom-status-bar').classList.remove('updating');
        }
    }

    updateDisplay() {
        const alertLevel = this.stats.alertLevel?.name || '未知';
        const usageRatio = this.stats.usageRatio ? (this.stats.usageRatio * 100).toFixed(1) : '0.0';
        
        // 状态图标
        const icons = {
            '正常': '✅',
            '注意': 'ℹ️',
            '警告': '⚠️', 
            '危险': '🚨'
        };
        document.getElementById('status-icon').textContent = icons[alertLevel] || '📊';
        
        // 使用率显示
        const usageElement = document.getElementById('usage-value');
        usageElement.textContent = usageRatio + '%';
        
        // 使用率颜色
        const colorClasses = {
            '正常': 'usage-normal',
            '注意': 'usage-info',
            '警告': 'usage-warning',
            '危险': 'usage-critical'
        };
        usageElement.className = `usage-value ${colorClasses[alertLevel] || 'usage-normal'}`;
        
        // 详细信息
        document.getElementById('alert-level-detail').textContent = alertLevel;
        document.getElementById('loaded-count-detail').textContent = 
            (this.stats.loadedUrlCount || 0).toLocaleString();
        document.getElementById('total-capacity-detail').textContent = 
            (this.stats.expectedInsertions || 0).toLocaleString();
        document.getElementById('last-update-detail').textContent = 
            new Date().toLocaleTimeString();
    }

    updatePollInterval() {
        const alertLevel = this.stats.alertLevel?.name;
        let newInterval;
        
        switch(alertLevel) {
            case '危险':
            case '警告':
                newInterval = 1 * 60 * 1000; // 1分钟
                break;
            case '注意':
                newInterval = 2 * 60 * 1000; // 2分钟
                break;
            default:
                newInterval = 5 * 60 * 1000; // 5分钟
        }
        
        if (newInterval !== this.pollInterval) {
            this.pollInterval = newInterval;
            this.restartPolling();
            console.log(`布隆过滤器轮询间隔调整为: ${newInterval/1000/60}分钟`);
        }
    }

    toggleDetails() {
        const details = document.getElementById('status-details');
        const toggleIcon = document.getElementById('toggle-icon');
        
        if (details.style.display === 'none') {
            details.style.display = 'block';
            toggleIcon.classList.add('expanded');
        } else {
            details.style.display = 'none';
            toggleIcon.classList.remove('expanded');
        }
    }

    async manualRefresh() {
        await this.fetchStats();
    }

    openMonitorPage() {
        window.open('/bloom-filter-monitor.html', '_blank');
    }

    hide() {
        document.getElementById('bloom-status-bar').classList.add('hidden');
        this.isVisible = false;
        this.stopPolling();
    }

    show() {
        document.getElementById('bloom-status-bar').classList.remove('hidden');
        this.isVisible = true;
        this.startPolling();
    }

    startMonitoring() {
        this.fetchStats();
        this.startPolling();
    }

    startPolling() {
        this.stopPolling();
        this.pollTimer = setInterval(() => {
            if (this.isVisible) {
                this.fetchStats();
            }
        }, this.pollInterval);
    }

    stopPolling() {
        if (this.pollTimer) {
            clearInterval(this.pollTimer);
            this.pollTimer = null;
        }
    }

    restartPolling() {
        this.startPolling();
    }

    showError() {
        document.getElementById('status-icon').textContent = '❌';
        document.getElementById('usage-value').textContent = 'Error';
        document.getElementById('usage-value').className = 'usage-value usage-critical';
    }

    destroy() {
        this.stopPolling();
        if (this.container) {
            this.container.innerHTML = '';
        }
        delete window.bloomStatusBar;
    }
}

// 使用示例：
// const statusBar = new BloomFilterStatusBar('status-container', {
//     position: 'top-right',
//     autoStart: true
// });
