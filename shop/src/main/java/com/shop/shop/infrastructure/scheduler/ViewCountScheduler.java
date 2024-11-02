package com.shop.shop.infrastructure.scheduler;

import com.shop.shop.infrastructure.persistence.product.ProductDetail;
import com.shop.shop.infrastructure.persistence.product.ProductDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountScheduler {

    private final RedisTemplate<String, Long> redisTemplate;
    private final ProductDetailRepository productDetailRepository;

    // 매시간 정각마다 Redis의 조회수를 DB로 쓰기 (write-back)
    @Scheduled(cron = "0 0 * * * *")
    public void writeBackToDatabase() {
        Set<String> keys = redisTemplate.keys("product:view:*");

        if (keys != null) {
            for (String key : keys) {
                try {
                    Long id = parseProductIdFromKey(key);
                    Long viewCount = redisTemplate.opsForValue().get(key);

                    if (viewCount != null) {
                        updateViewCountInDatabase(id, viewCount);
                        redisTemplate.delete(key); // DB에 저장 후 Redis에서 해당 조회수 삭제
                    }
                } catch (Exception e) {
                    log.error("Error processing key {}: {}", key, e.getMessage());
                }
            }
        }
    }

    // Redis 키에서 제품 ID를 추출
    private Long parseProductIdFromKey(String key) {
        return Long.valueOf(key.replace("product:view:", ""));
    }

    // DB의 조회수 업데이트
    private void updateViewCountInDatabase(Long id, Long viewCount) {
        ProductDetail productDetail = productDetailRepository.findById(id).orElse(null);
        if (productDetail != null) {
            productDetail.setViewCount(productDetail.getViewCount() + viewCount);
            productDetailRepository.save(productDetail);
        } else {
            log.warn("ProductDetail not found for ID: {}", id);
        }
    }
}
