-- 添加内容指纹字段
ALTER TABLE hot_news ADD COLUMN content_hash VARCHAR(200) COMMENT '内容指纹，用于相似度检测';

-- 创建索引
CREATE INDEX idx_hot_news_content_hash ON hot_news(content_hash);

-- 注释
COMMENT ON COLUMN hot_news.content_hash IS '内容指纹，用于相似度检测'; 