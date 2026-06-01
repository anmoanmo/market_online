package com.buka.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataFixConfig implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        fixProductData();
    }

    private void fixProductData() {
        try {
            log.info("修复商品数据...");
            jdbcTemplate.update("UPDATE product SET title=?, detail=?, cover_img=? WHERE id=1", "简约蓝牙耳机", "高品质蓝牙耳机，支持降噪，续航24小时", "https://picsum.photos/seed/bt-earphone/400/400");
            jdbcTemplate.update("UPDATE product SET title=?, detail=?, cover_img=? WHERE id=2", "机械键盘", "青轴机械键盘，87键，RGB背光", "https://picsum.photos/seed/keyboard/400/400");
            jdbcTemplate.update("UPDATE product SET title=?, detail=?, cover_img=? WHERE id=3", "无线鼠标", "静音无线鼠标，人体工学设计，2.4G连接", "https://picsum.photos/seed/mouse/400/400");
            jdbcTemplate.update("UPDATE product SET title=?, detail=?, cover_img=? WHERE id=4", "智能手表", "智能运动手表，心率监测，IP68防水", "https://picsum.photos/seed/smart-watch/400/400");
            jdbcTemplate.update("UPDATE product SET title=?, detail=?, cover_img=? WHERE id=5", "Type-C扩展坞", "7合1 Type-C扩展坞，HDMI 4K输出", "https://picsum.photos/seed/dock/400/400");
            jdbcTemplate.update("UPDATE product SET title=?, detail=?, cover_img=? WHERE id=6", "笔记本支架", "铝合金笔记本支架，可调节高度，散热快", "https://picsum.photos/seed/laptop-stand/400/400");
            jdbcTemplate.update("UPDATE product SET title=?, detail=?, cover_img=? WHERE id=7", "USB-C充电器", "65W GaN氮化镓充电器，支持PD快充", "https://picsum.photos/seed/charger/400/400");
            jdbcTemplate.update("UPDATE product SET title=?, detail=?, cover_img=? WHERE id=8", "移动电源", "20000mAh大容量移动电源，支持22.5W快充", "https://picsum.photos/seed/powerbank/400/400");
            jdbcTemplate.update("UPDATE product SET title=?, detail=?, cover_img=? WHERE id=9", "显示器挂灯", "屏幕挂灯，无频闪，色温可调", "https://picsum.photos/seed/monitor-light/400/400");
            jdbcTemplate.update("UPDATE product SET title=?, detail=?, cover_img=? WHERE id=10", "桌面收纳盒", "多功能桌面收纳盒，分区设计", "https://picsum.photos/seed/organizer/400/400");
            log.info("商品数据修复完成");
        } catch (Exception e) {
            log.error("修复失败: {}", e.getMessage());
        }
    }
}
