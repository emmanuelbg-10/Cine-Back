package com.eviden.cine.service;

import com.eviden.cine.model.Classification;
import com.eviden.cine.repository.ClassificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClassificationServiceTest {

    @Mock
    private ClassificationRepository classificationRepository;

    @InjectMocks
    private ClassificationService classificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateClassification() {
        Classification classification = new Classification();
        classification.setName("PG-13");

        when(classificationRepository.save(classification)).thenReturn(classification);

        Classification result = classificationService.createClassification(classification);

        assertNotNull(result);
        assertEquals("PG-13", result.getName());
    }

    @Test
    void testGetClassificationByIdFound() {
        Classification classification = new Classification();
        classification.setId(1);
        classification.setName("R");

        when(classificationRepository.findById(1)).thenReturn(Optional.of(classification));

        Optional<Classification> result = classificationService.getClassificationById(1);

        assertTrue(result.isPresent());
        assertEquals("R", result.get().getName());
    }

    @Test
    void testGetClassificationByIdNotFound() {
        when(classificationRepository.findById(99)).thenReturn(Optional.empty());

        Optional<Classification> result = classificationService.getClassificationById(99);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetAllClassifications() {
        Classification c1 = new Classification();
        c1.setName("G");
        Classification c2 = new Classification();
        c2.setName("NC-17");

        when(classificationRepository.findAll()).thenReturn(List.of(c1, c2));

        List<Classification> list = classificationService.getAllClassifications();

        assertEquals(2, list.size());
        assertEquals("G", list.get(0).getName());
        assertEquals("NC-17", list.get(1).getName());
    }

    @Test
    void testDeleteClassification() {
        classificationService.deleteClassification(1);

        verify(classificationRepository).deleteById(1);
    }



}
