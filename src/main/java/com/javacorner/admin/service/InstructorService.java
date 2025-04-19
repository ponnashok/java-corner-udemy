package com.javacorner.admin.service;

import com.javacorner.admin.dto.InstructorDTO;
import com.javacorner.admin.entity.Instructor;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InstructorService {

    Instructor loadInstructorById(Long instructorId);

    Page<InstructorDTO> findInstructorByName(String name, int page, int size);

    InstructorDTO loadInstructorByEmail(String email);

    InstructorDTO createInstructor(InstructorDTO instructorDTO);

    InstructorDTO updatedInstructor(InstructorDTO instructorDTO);

    List<InstructorDTO> fetchInstructors();

    void removeInstructor(Long instructorId);
}
