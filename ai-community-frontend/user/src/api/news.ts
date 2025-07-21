import { get } from '@/utils/request';

/**
 * 新闻相关API
 */

// 新闻接口类型定义
export interface NewsItem {
  id: number;
  title: string;
  content: string;
  summary: string;
  coverImage: string;
  images: string;
  source: string;
  sourceUrl: string;
  author: string;
  category: string;
  tags: string;
  publishTime: string;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  isHot: number;
  isTop: number;
  status: number;
  createTime: string;
  updateTime: string;
}

export interface NewsPageResult {
  records: NewsItem[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

export const newsApi = {
  /**
   * 分页获取新闻列表
   */
  getNewsList: async (params: {
    page?: number;
    pageSize?: number;
    category?: string;
  }) => {
    return await get('/user/news/page', params);
  },

  /**
   * 获取新闻详情
   */
  getNewsDetail: async (id: number) => {
    return await get(`/user/news/${id}`);
  },

  /**
   * 获取热点新闻
   */
  getHotNews: async (limit: number = 5) => {
    return await get('/user/news/hot', { limit });
  },

  /**
   * 获取最新新闻
   */
  getLatestNews: async (params: {
    limit?: number;
    category?: string;
  }) => {
    return await get('/news/latest', params);
  }
};

// 虚拟数据 - 用于其他类型的新闻展示
export const mockNewsData = {
  // 科技新闻
  tech: [
    {
      id: 1001,
      title: "人工智能技术在医疗领域的最新突破",
      summary: "最新研究显示，AI技术在疾病诊断方面准确率已达到95%以上，为医疗行业带来革命性变化。",
      coverImage: "https://picsum.photos/300/200?random=1",
      source: "科技日报",
      author: "张科技",
      category: "科技",
      publishTime: "2024-01-20 10:30:00",
      viewCount: 1250,
      isHot: 1
    },
    {
      id: 1002,
      title: "5G网络建设进入新阶段，覆盖率大幅提升",
      summary: "全国5G基站数量突破300万个，网络覆盖率达到85%，为数字经济发展提供强力支撑。",
      coverImage: "https://picsum.photos/300/200?random=2",
      source: "通信世界",
      author: "李网络",
      category: "科技",
      publishTime: "2024-01-20 09:15:00",
      viewCount: 980,
      isHot: 0
    }
  ],
  
  // 体育新闻
  sports: [
    {
      id: 2001,
      title: "中国队在国际赛事中再创佳绩",
      summary: "在刚刚结束的国际体育赛事中，中国代表团表现出色，多个项目取得突破性成绩。",
      coverImage: "https://picsum.photos/300/200?random=3",
      source: "体育周报",
      author: "王体育",
      category: "体育",
      publishTime: "2024-01-20 08:45:00",
      viewCount: 2100,
      isHot: 1
    },
    {
      id: 2002,
      title: "全民健身活动蓬勃发展，参与人数创新高",
      summary: "各地积极开展全民健身活动，参与人数较去年同期增长30%，全民健身意识不断提升。",
      coverImage: "https://picsum.photos/300/200?random=4",
      source: "健康时报",
      author: "赵健身",
      category: "体育",
      publishTime: "2024-01-20 07:20:00",
      viewCount: 756,
      isHot: 0
    }
  ],
  
  // 娱乐新闻
  entertainment: [
    {
      id: 3001,
      title: "国产电影票房创新纪录，文化自信显著增强",
      summary: "今年国产电影表现亮眼，多部作品获得观众好评，票房和口碑双丰收。",
      coverImage: "https://picsum.photos/300/200?random=5",
      source: "娱乐周刊",
      author: "陈娱乐",
      category: "娱乐",
      publishTime: "2024-01-20 06:30:00",
      viewCount: 1800,
      isHot: 1
    }
  ],
  
  // 社会新闻
  society: [
    {
      id: 4001,
      title: "社区志愿服务活动温暖人心",
      summary: "各地社区积极开展志愿服务活动，为居民提供便民服务，构建和谐社区。",
      coverImage: "https://picsum.photos/300/200?random=6",
      source: "社会新闻",
      author: "刘社会",
      category: "社会",
      publishTime: "2024-01-20 05:45:00",
      viewCount: 890,
      isHot: 0
    }
  ]
};

// 新闻分类配置
export const newsCategories = [
  { key: 'all', name: '全部', icon: 'apps-o' },
  { key: '财经', name: '财经', icon: 'gold-coin-o' },
  { key: '科技', name: '科技', icon: 'setting-o' },
  { key: '体育', name: '体育', icon: 'medal-o' },
  { key: '娱乐', name: '娱乐', icon: 'music-o' },
  { key: '社会', name: '社会', icon: 'friends-o' }
];
