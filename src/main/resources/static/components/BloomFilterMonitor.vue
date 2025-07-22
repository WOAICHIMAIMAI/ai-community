<template>
  <div class="bloom-filter-monitor">
    <!-- 顶部状态栏 -->
    <div class="status-bar" :class="statusBarClass">
      <div class="status-indicator">
        <span class="status-icon">{{ statusIcon }}</span>
        <span class="status-text">布隆过滤器: {{ alertLevel }}</span>
        <span class="usage-text">{{ usagePercentage }}%</span>
      </div>
      <div class="last-update">
        {{ lastUpdateText }}
      </div>
    </div>

    <!-- 详细监控面板 -->
    <div v-if="showDetails" class="monitor-panel">
      <div class="monitor-cards">
        <!-- 使用率卡片 -->
        <div class="monitor-card">
          <div class="card-header">
            <h3>📊 使用率监控</h3>
          </div>
          <div class="usage-display">
            <div class="usage-circle" :class="usageCircleClass">
              <div class="usage-number">{{ usagePercentage }}%</div>
              <div class="usage-label">{{ stats.loadedUrlCount || 0 }} / {{ stats.expectedInsertions || 0 }}</div>
            </div>
          </div>
        </div>

        <!-- 告警状态卡片 -->
        <div class="monitor-card">
          <div class="card-header">
            <h3>🔔 告警状态</h3>
          </div>
          <div class="alert-display">
            <div class="alert-level" :class="alertLevelClass">
              {{ alertLevel }}
            </div>
            <div class="alert-description">
              {{ stats.alertLevel?.description || '加载中...' }}
            </div>
          </div>
        </div>

        <!-- 运维建议卡片 -->
        <div class="monitor-card recommendation-card">
          <div class="card-header">
            <h3>💡 运维建议</h3>
          </div>
          <div class="recommendation-content">
            {{ stats.alertRecommendation || '加载中...' }}
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <button @click="manualCheck" :disabled="loading" class="btn btn-primary">
          {{ loading ? '检查中...' : '立即检查' }}
        </button>
        <button @click="rebuildFilter" :disabled="loading" class="btn btn-warning">
          重建过滤器
        </button>
        <button @click="toggleAutoRefresh" class="btn btn-secondary">
          {{ autoRefresh ? '停止自动刷新' : '开启自动刷新' }}
        </button>
      </div>
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
  </div>
</template>

<script>
export default {
  name: 'BloomFilterMonitor',
  props: {
    showDetails: {
      type: Boolean,
      default: false
    },
    autoStart: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      stats: {},
      loading: false,
      error: null,
      lastUpdate: null,
      autoRefresh: true,
      pollTimer: null,
      pollInterval: 5 * 60 * 1000 // 默认5分钟
    }
  },
  computed: {
    alertLevel() {
      return this.stats.alertLevel?.name || '未知';
    },
    usagePercentage() {
      return this.stats.usageRatio ? (this.stats.usageRatio * 100).toFixed(1) : '0.0';
    },
    statusIcon() {
      const icons = {
        '正常': '✅',
        '注意': 'ℹ️', 
        '警告': '⚠️',
        '危险': '🚨'
      };
      return icons[this.alertLevel] || '📊';
    },
    statusBarClass() {
      const classes = {
        '正常': 'status-normal',
        '注意': 'status-info',
        '警告': 'status-warning', 
        '危险': 'status-critical'
      };
      return classes[this.alertLevel] || 'status-normal';
    },
    usageCircleClass() {
      return this.statusBarClass;
    },
    alertLevelClass() {
      return this.statusBarClass;
    },
    lastUpdateText() {
      if (!this.lastUpdate) return '未更新';
      const now = new Date();
      const diff = Math.floor((now - this.lastUpdate) / 1000);
      if (diff < 60) return `${diff}秒前`;
      if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`;
      return this.lastUpdate.toLocaleTimeString();
    }
  },
  mounted() {
    if (this.autoStart) {
      this.startMonitoring();
    }
  },
  beforeUnmount() {
    this.stopMonitoring();
  },
  methods: {
    async fetchStats() {
      try {
        this.loading = true;
        this.error = null;
        
        const response = await fetch('/admin/news/bloom-filter/stats');
        const data = await response.json();
        
        if (data.code === 200) {
          this.stats = data.data;
          this.lastUpdate = new Date();
          this.updatePollInterval();
          this.$emit('stats-updated', this.stats);
        } else {
          throw new Error(data.message || '获取数据失败');
        }
      } catch (error) {
        this.error = error.message;
        this.$emit('error', error);
      } finally {
        this.loading = false;
      }
    },
    
    async manualCheck() {
      try {
        this.loading = true;
        const response = await fetch('/admin/news/bloom-filter/check');
        const data = await response.json();
        
        if (data.code === 200) {
          this.stats = data.data;
          this.lastUpdate = new Date();
          this.$message?.success('检查完成');
        } else {
          throw new Error(data.message || '检查失败');
        }
      } catch (error) {
        this.$message?.error('检查失败: ' + error.message);
      } finally {
        this.loading = false;
      }
    },
    
    async rebuildFilter() {
      if (!confirm('确定要重建布隆过滤器吗？此操作可能需要1-2秒时间。')) {
        return;
      }
      
      try {
        this.loading = true;
        const response = await fetch('/admin/news/bloom-filter/rebuild', {
          method: 'POST'
        });
        const data = await response.json();
        
        if (data.code === 200) {
          this.$message?.success('重建完成');
          // 重建后立即刷新状态
          setTimeout(() => this.fetchStats(), 1000);
        } else {
          throw new Error(data.message || '重建失败');
        }
      } catch (error) {
        this.$message?.error('重建失败: ' + error.message);
      } finally {
        this.loading = false;
      }
    },
    
    updatePollInterval() {
      let newInterval;
      switch(this.alertLevel) {
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
        if (this.autoRefresh) {
          this.restartPolling();
        }
        console.log(`布隆过滤器轮询间隔调整为: ${newInterval/1000/60}分钟`);
      }
    },
    
    startMonitoring() {
      this.fetchStats();
      if (this.autoRefresh) {
        this.startPolling();
      }
    },
    
    stopMonitoring() {
      this.stopPolling();
    },
    
    startPolling() {
      this.stopPolling();
      this.pollTimer = setInterval(() => {
        this.fetchStats();
      }, this.pollInterval);
    },
    
    stopPolling() {
      if (this.pollTimer) {
        clearInterval(this.pollTimer);
        this.pollTimer = null;
      }
    },
    
    restartPolling() {
      if (this.autoRefresh) {
        this.startPolling();
      }
    },
    
    toggleAutoRefresh() {
      this.autoRefresh = !this.autoRefresh;
      if (this.autoRefresh) {
        this.startPolling();
      } else {
        this.stopPolling();
      }
    }
  }
}
</script>

<style scoped>
.bloom-filter-monitor {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.status-normal { background: linear-gradient(135deg, #d4edda, #c3e6cb); color: #155724; }
.status-info { background: linear-gradient(135deg, #d1ecf1, #bee5eb); color: #0c5460; }
.status-warning { background: linear-gradient(135deg, #fff3cd, #ffeaa7); color: #856404; }
.status-critical { background: linear-gradient(135deg, #f8d7da, #f5c6cb); color: #721c24; }

.status-indicator {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-icon {
  font-size: 1.2rem;
}

.status-text {
  font-weight: 600;
}

.usage-text {
  font-weight: bold;
  font-size: 1.1rem;
}

.last-update {
  font-size: 0.9rem;
  opacity: 0.8;
}

.monitor-panel {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.monitor-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.monitor-card {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 20px;
  border: 1px solid #e9ecef;
}

.card-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.1rem;
}

.usage-display {
  text-align: center;
  margin-top: 15px;
}

.usage-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin: 0 auto;
  border: 4px solid;
  transition: all 0.3s ease;
}

.usage-circle.status-normal { border-color: #28a745; background: rgba(40, 167, 69, 0.1); }
.usage-circle.status-info { border-color: #17a2b8; background: rgba(23, 162, 184, 0.1); }
.usage-circle.status-warning { border-color: #ffc107; background: rgba(255, 193, 7, 0.1); }
.usage-circle.status-critical { border-color: #dc3545; background: rgba(220, 53, 69, 0.1); }

.usage-number {
  font-size: 1.8rem;
  font-weight: bold;
}

.usage-label {
  font-size: 0.8rem;
  opacity: 0.8;
  margin-top: 5px;
}

.alert-display {
  text-align: center;
  margin-top: 15px;
}

.alert-level {
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 10px;
}

.alert-description {
  color: #666;
  line-height: 1.4;
}

.recommendation-card {
  grid-column: 1 / -1;
}

.recommendation-content {
  white-space: pre-line;
  line-height: 1.6;
  color: #555;
  background: white;
  padding: 15px;
  border-radius: 8px;
  margin-top: 10px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #0056b3;
}

.btn-warning {
  background: #ffc107;
  color: #212529;
}

.btn-warning:hover:not(:disabled) {
  background: #e0a800;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background: #545b62;
}

.error-message {
  background: #f8d7da;
  color: #721c24;
  padding: 12px;
  border-radius: 6px;
  margin-top: 10px;
  border: 1px solid #f5c6cb;
}
</style>
