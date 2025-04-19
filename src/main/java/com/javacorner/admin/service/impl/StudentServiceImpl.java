package com.javacorner.admin.service.impl;

import com.javacorner.admin.dao.StudentDao;
import com.javacorner.admin.dto.StudentDTO;
import com.javacorner.admin.entity.Course;
import com.javacorner.admin.entity.Student;
import com.javacorner.admin.entity.User;
import com.javacorner.admin.mapper.StudentMapper;
import com.javacorner.admin.service.StudentService;
import com.javacorner.admin.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentDao studentDao;

    private final StudentMapper studentMapper;

    private final UserService userService;

    public StudentServiceImpl(StudentDao studentDao, StudentMapper studentMapper, UserService userService) {
        this.studentDao = studentDao;
        this.studentMapper = studentMapper;
        this.userService = userService;
    }

    @Override
    public Student loadStudentById(Long studentId) {
        return studentDao.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student with ID: " + studentId + "not found"));
    }

    @Override
    public Page<StudentDTO> loadStudentByName(String name, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Student> studentsPage = studentDao.findStudentsByName(name, pageRequest);
        return new PageImpl<>(studentsPage.getContent()
                .stream().map(studentMapper::fromStudent).collect(Collectors.toList()), pageRequest, studentsPage.getTotalElements());
    }

    @Override
    public StudentDTO loadStudentByEmail(String email) {
        return studentMapper.fromStudent(studentDao.findStudentByEmail(email));
    }

    @Override
    public StudentDTO createStudent(StudentDTO studentDTO) {
        User user = userService.createUser(studentDTO.getUser().getEmail(), studentDTO.getUser().getPassword());
        userService.assignRoleToUser(user.getEmail(), "Student");
        Student student = studentMapper.fromStudentDTO(studentDTO);
        student.setUser(user);
        Student savedStudent = studentDao.save(student);
        return studentMapper.fromStudent(savedStudent);
    }

    @Override
    public StudentDTO updateStudent(StudentDTO studentDTO) {
        Student loadedStudent = loadStudentById(studentDTO.getStudentId());
        Student student = studentMapper.fromStudentDTO(studentDTO);
        student.setUser(loadedStudent.getUser());
        student.setCourses(loadedStudent.getCourses());
        Student updateStudent = studentDao.save(student);
        return studentMapper.fromStudent(updateStudent);
    }

    @Override
    public void remoceStudent(Long studentId) {
        Student student = loadStudentById(studentId);
        Iterator<Course> courseIterator = student.getCourses().iterator();
        if (courseIterator.hasNext()) {
            Course course = courseIterator.next();
            course.removeStudentFromStudent(student);
        }
        studentDao.deleteById(studentId);
    }
}
