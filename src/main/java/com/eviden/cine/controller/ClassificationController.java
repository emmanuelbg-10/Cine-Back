package com.eviden.cine.controller;

import com.eviden.cine.model.Classification;
import com.eviden.cine.service.ClassificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/classifications")
@Tag(name = "Classifications", description = "Operaciones relacionadas con clasificaciones de películas")
public class ClassificationController {

    private final ClassificationService classificationService;

    public ClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @GetMapping
    @Operation(
            summary = "Obtener todas las clasificaciones",
            description = "Retorna una lista de todas las clasificaciones disponibles en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Clasificaciones obtenidas correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<Classification>> getAllClassification() {
        return ResponseEntity.ok(classificationService.getAllClassifications());
    }
}
