package com.studymatch.security;

import com.studymatch.repository.CourseUnitStudentRepository;
import org.springframework.stereotype.Component;

/**
 * R3: o docente só vê alunos inscritos numa UC que lhe está atribuída.
 */
@Component
public class CourseUnitTeachingAccess implements TeachingAccess {

    private final CourseUnitStudentRepository enrolled;

    public CourseUnitTeachingAccess(CourseUnitStudentRepository enrolled) {
        this.enrolled = enrolled;
    }

    @Override
    public boolean teachesStudent(Long teacherId, Long studentId) {
        return teacherId != null && studentId != null && enrolled.teacherHasStudent(teacherId, studentId);
    }
}
