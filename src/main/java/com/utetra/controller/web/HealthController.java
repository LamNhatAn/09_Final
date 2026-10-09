package com.utetra.controller.web;

import com.utetra.repository.BranchRepository;
import com.utetra.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    /** Kiem tra ket noi SQL Server: GET /health */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("app", "UTETra");
        result.put("database", jdbcTemplate.queryForObject("SELECT DB_NAME()", String.class));
        result.put("status", "UP");
        result.put("branches", branchRepository.count());
        result.put("products", productRepository.count());
        return result;
    }
}
