package com.studymatch.controller;

import com.studymatch.dto.CourseUnitResponse;
import com.studymatch.service.CourseUnitService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST das unidades curriculares.
 */
@RestController
@RequestMapping("/api/course-units")
public class CourseUnitController {

    private final CourseUnitService courseUnitService;

    public CourseUnitController(CourseUnitService courseUnitService) {
        this.courseUnitService = courseUnitService;
    }

    /**
     * GET /api/course-units
     *
     * Devolve as unidades curriculares, usadas no registo de inscrições (#49).
     */
    @GetMapping
    public List<CourseUnitResponse> getAllCourseUnits() {
        return courseUnitService.getAllCourseUnits();
    }
}
