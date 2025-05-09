package com.eviden.cine.component;

import com.eviden.cine.dtos.ReservationRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionDataCache {
    private static final Logger logger = LoggerFactory.getLogger(SessionDataCache.class);
    private final Map<String, ReservationRequestDTO> sessionCache = new ConcurrentHashMap<>();
    private final Map<String, Long> reservaIdCache = new ConcurrentHashMap<>();


    public void saveReservationData(String sessionId, ReservationRequestDTO reservationRequestDTO) {
        sessionCache.put(sessionId, reservationRequestDTO);
    }

    public ReservationRequestDTO getReservationData(String sessionId) {
        return sessionCache.get(sessionId);
    }

    public void removeReservationData(String sessionId) {
        sessionCache.remove(sessionId);
    }

    public void saveReservaId(String sessionId, Long reservaId) {
        logger.info("Guardando reservaId para sessionId: {}", sessionId);
        reservaIdCache.put(sessionId, reservaId);
    }


    public Long getReservaId(String sessionId) {
        return reservaIdCache.get(sessionId);
    }
}

