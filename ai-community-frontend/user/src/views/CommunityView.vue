<div class="post-author">
  <van-image
    :src="post.avatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
    round
    width="32"
    height="32"
    class="author-avatar"
    @click="goToUserProfile(post.userId)"
  />
  <div class="author-info">
    <span class="author-name">{{ post.nickname || post.username || '匿名用户' }}</span>
    <span class="post-time">{{ formatDate(post.createTime || post.createdTime) }}</span>
  </div>
</div>