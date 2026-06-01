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
        fixData();
    }

    private void fixData() {
        try {
            int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM coupon WHERE coupon_title NOT LIKE '%Ten%'", Integer.class);
            if (count > 0) {
                log.info("Fixing coupon data...");
                jdbcTemplate.execute("UPDATE `coupon` SET `coupon_title`='Ten-Off' WHERE `id`=1");
                jdbcTemplate.execute("UPDATE `coupon` SET `coupon_title`='Twenty-Off-199' WHERE `id`=2");
                jdbcTemplate.execute("UPDATE `coupon` SET `coupon_title`='Fifty-Off-499' WHERE `id`=3");
                jdbcTemplate.execute("UPDATE `coupon_record` SET `coupon_title`='Ten-Off' WHERE `coupon_id`=1");
                jdbcTemplate.execute("UPDATE `coupon_record` SET `coupon_title`='Twenty-Off-199' WHERE `coupon_id`=2");
                jdbcTemplate.execute("UPDATE `coupon_record` SET `use_state`='NEW' WHERE `id`=1");
                jdbcTemplate.execute("UPDATE `coupon` SET `stock`=999 WHERE `id`=1");
                log.info("Coupon data fixed");
            }
        } catch (Exception e) {
            log.error("Fix failed: {}", e.getMessage());
        }
    }
}
