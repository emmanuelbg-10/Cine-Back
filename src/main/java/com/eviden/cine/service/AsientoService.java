package com.eviden.cine.service;

import com.eviden.cine.exception.CustomException;
import com.eviden.cine.model.Asiento;
import com.eviden.cine.repository.AsientoRepository;
import com.eviden.cine.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AsientoService {

    private final AsientoRepository asientoRepository;
    private final RoomRepository roomRepository;

    @Autowired
    public AsientoService(AsientoRepository asientoRepository, RoomRepository roomRepository) {
        this.asientoRepository = asientoRepository;
        this.roomRepository = roomRepository;
    }

    public List<Asiento> obtenerTodosLosAsientos() {
        return asientoRepository.findAll();
    }

    public Optional<Asiento> obtenerAsientoPorId(Long id) {
        return asientoRepository.findById(id);
    }

    public List<Asiento> obtenerAsientosPorroom(Long idRoom) {
        return asientoRepository.findByroomIdroom(idRoom);
    }

    public Asiento guardarAsiento(Asiento asiento) {
        Long idRoom = asiento.getRoom().getIdroom();

        if (!roomRepository.existsById(idRoom)) {
            throw new CustomException("La sala con ID " + idRoom + " no existe.");
        }

        return asientoRepository.save(asiento);
    }

    public void eliminarAsiento(Long id) {
        asientoRepository.deleteById(id);
    }

    public List<Asiento> cambiarDisponibilidad(List<Long> ids) {
        List<Asiento> asientos = asientoRepository.findAllById(ids);

        if (asientos.size() != ids.size()) {
            throw new CustomException("Uno o más asientos no fueron encontrados.");
        }

        asientos.forEach(asiento -> asiento.setDisponible(false));
        return asientoRepository.saveAll(asientos);
    }
    public boolean isAsientoAvailable(List<Long> ids) {
        List<Asiento> asientos = asientoRepository.findAllById(ids);

        if (asientos.size() != ids.size()) {
            throw new IllegalArgumentException("Uno o más asientos no fueron encontrados");
        }

        boolean allAvailable = asientos.stream().allMatch(Asiento::isDisponible);

        if (!allAvailable) {
            throw new IllegalStateException("Uno o más asientos no están disponibles");
        }

        return true;
    }

}
