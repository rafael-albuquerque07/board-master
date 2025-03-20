package br.com.dio.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO que representa informações sobre o tempo de conclusão de um card
 */
public record CardCompletionTimeDTO(
        Long id,
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Duration totalTime
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Retorna a data de início formatada
     * @return String formatada com data e hora de início
     */
    public String getFormattedStartTime() {
        return startTime.format(DATE_FORMATTER);
    }

    /**
     * Retorna a data de conclusão formatada
     * @return String formatada com data e hora de conclusão
     */
    public String getFormattedEndTime() {
        return endTime.format(DATE_FORMATTER);
    }

    /**
     * Retorna a duração total formatada
     * @return String com a duração formatada em dias, horas e minutos
     */
    public String getFormattedDuration() {
        long days = totalTime.toDays();
        long hours = totalTime.toHoursPart();
        long minutes = totalTime.toMinutesPart();

        if (days > 0) {
            return String.format("%d dias, %d horas e %d minutos", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d horas e %d minutos", hours, minutes);
        } else {
            return String.format("%d minutos", minutes);
        }
    }
}