package com.eviden.cine.service;

import com.eviden.cine.model.Classification;
import com.eviden.cine.repository.ClassificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClassificationService {

    private final ClassificationRepository classificationRepository;

    public ClassificationService(ClassificationRepository classificationRepository) {
        this.classificationRepository = classificationRepository;
    }

    public Classification createClassification(Classification classification) {
        return classificationRepository.save(classification);
    }

    public Optional<Classification> getClassificationById(int id) {
        return classificationRepository.findById(id);
    }

    public List<Classification> getAllClassifications() {
        return classificationRepository.findAll();
    }

    public void deleteClassification(int id) {
        classificationRepository.deleteById(id);
    }
}
