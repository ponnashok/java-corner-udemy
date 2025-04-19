package com.javacorner.admin.service.impl;

import com.javacorner.admin.dao.CourseDao;
import com.javacorner.admin.dao.InstructorDao;
import com.javacorner.admin.dao.StudentDao;
import com.javacorner.admin.dto.CourseDTO;
import com.javacorner.admin.entity.Course;
import com.javacorner.admin.entity.Instructor;
import com.javacorner.admin.entity.Student;
import com.javacorner.admin.mapper.CourseMapper;
import com.javacorner.admin.service.CourseService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseDao courseDao;

    private final CourseMapper courseMapper;

    private final InstructorDao instructorDao;

    private final StudentDao studentDao;

    public CourseServiceImpl(CourseDao courseDao, CourseMapper courseMapper, InstructorDao instructorDao, StudentDao studentDao) {
        this.courseDao = courseDao;
        this.courseMapper = courseMapper;
        this.instructorDao = instructorDao;
        this.studentDao = studentDao;
    }

    @Override
    public Course loadCourseById(Long courseId) {
        return courseDao.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course with ID " + courseId + " Not Found"));
    }

    @Override
    public CourseDTO createCourse(CourseDTO courseDTO) {

        Course course = courseMapper.fromCourseDTO(courseDTO);
        Instructor instructor = instructorDao.findById(courseDTO.getInstructor().getInstructorId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Instructor with ID " + courseDTO.getInstructor().getInstructorId() + " Not Found"));
        course.setInstructor(instructor);
        Course savedCourse = courseDao.save(course);
        return courseMapper.fromCourse(savedCourse);
    }

    @Override
    public CourseDTO updateCourse(CourseDTO courseDTO) {
        Course loadedCourse = loadCourseById(courseDTO.getCourseId());
        Instructor instructor = instructorDao.findById(courseDTO.getInstructor().getInstructorId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Instructor with ID " + courseDTO.getInstructor().getInstructorId() + " Not Found"));
        Course course = courseMapper.fromCourseDTO(courseDTO);
        course.setInstructor(instructor);
        course.setStudents(loadedCourse.getStudents());
        Course updatedeCourse = courseDao.save(course);
        return courseMapper.fromCourse(updatedeCourse);
    }

    @Override
    public Page<CourseDTO> findCoursesByCourseName(String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Course> coursePage = courseDao.findCoursesByCourseNameContains(keyword, pageRequest);
        return new PageImpl<>(coursePage
                .getContent().stream().map(courseMapper::fromCourse).collect(Collectors.toList()), pageRequest, coursePage.getTotalElements());
    }

    @Override
    public void assignStudentToCourse(Long courseId, Long studentId) {
        Student student = studentDao.findById(studentId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Student with ID " + studentId + " Not Found"));
        Course course = loadCourseById(courseId);
        course.assignStudentToCourse(student);
    }

    @Override
    public Page<CourseDTO> fetchCoursesForStudent(Long studentId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Course> studentCoursePage = courseDao.getCoursesByStudentId(studentId, pageRequest);

        return new PageImpl<>(studentCoursePage
                .getContent().stream().map(courseMapper::fromCourse).collect(Collectors.toList()), pageRequest, studentCoursePage.getTotalElements());
    }

    @Override
    public Page<CourseDTO> fetchNonEnrolledInCoursesForStudent(Long studentId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Course> nonEnrolledInCoursePage = courseDao.getNonEnrolledCoursesByStudentId(studentId, pageRequest);

        return new PageImpl<>(nonEnrolledInCoursePage
                .getContent().stream().map(courseMapper::fromCourse).collect(Collectors.toList()), pageRequest, nonEnrolledInCoursePage.getTotalElements());

    }

    @Override
    public void removeCourse(Long courseId) {
        courseDao.deleteById(courseId);
    }

    @Override
    public Page<CourseDTO> fetchCoursesForInstructor(Long instructorId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Course> instructorCoursePage = courseDao.getCoursesByInstructorId(instructorId, pageRequest);
        return new PageImpl<>(instructorCoursePage
                .getContent().stream().map(courseMapper::fromCourse).collect(Collectors.toList()), pageRequest, instructorCoursePage.getTotalElements());

    }
}
