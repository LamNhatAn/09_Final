package com.utetra.service;

import com.utetra.entity.Branch;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogServiceTest {

    @Test
    void likePatternEscapesWildcards() {
        assertEquals("%tra sua%", CatalogService.likePattern("  tra sua "));
        assertEquals("%100!%%", CatalogService.likePattern("100%"));
        assertEquals("%a!_b%", CatalogService.likePattern("a_b"));
        assertEquals("%![abc]%", CatalogService.likePattern("[abc]"));
        assertEquals("%!!%", CatalogService.likePattern("!"));
        assertEquals("%%", CatalogService.likePattern(null));
    }

    @Test
    void likePatternLimitsLength() {
        assertEquals(102, CatalogService.likePattern("x".repeat(500)).length());
    }

    @Test
    void sortKeyIsWhitelisted() {
        assertEquals("newest", CatalogService.normalizeSort(null));
        assertEquals("newest", CatalogService.normalizeSort("id; drop table users"));
        assertEquals("price_asc", CatalogService.normalizeSort("price_asc"));
        assertEquals(Sort.Direction.DESC, CatalogService.sortOf("price_desc").getOrderFor("basePrice").getDirection());
    }

    @Test
    void branchOpenHours() {
        Branch b = new Branch();
        b.setOpenTime(LocalTime.of(7, 0));
        b.setCloseTime(LocalTime.of(22, 0));
        assertTrue(CatalogService.isOpen(b, LocalTime.of(7, 0)));
        assertTrue(CatalogService.isOpen(b, LocalTime.of(21, 59)));
        assertFalse(CatalogService.isOpen(b, LocalTime.of(22, 0)));
        assertFalse(CatalogService.isOpen(b, LocalTime.of(6, 59)));
    }

    @Test
    void branchOpenOvernight() {
        Branch b = new Branch();
        b.setOpenTime(LocalTime.of(18, 0));
        b.setCloseTime(LocalTime.of(2, 0));
        assertTrue(CatalogService.isOpen(b, LocalTime.of(23, 0)));
        assertTrue(CatalogService.isOpen(b, LocalTime.of(1, 0)));
        assertFalse(CatalogService.isOpen(b, LocalTime.of(12, 0)));
    }

    @Test
    void branchWithoutHoursIsAlwaysOpen() {
        assertTrue(CatalogService.isOpen(new Branch(), LocalTime.of(3, 0)));
    }
}
