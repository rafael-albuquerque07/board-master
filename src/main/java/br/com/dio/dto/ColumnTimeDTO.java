package br.com.dio.dto;

import java.time.Duration;

/**
 * DTO que representa o tempo gasto por um card em uma coluna
 */
public record ColumnTimeDTO(
        Long columnId,
        String columnName,
        Duration timeSpent
) {
    /**
     * Retorna a duração do tempo na coluna formatada
     * @return String com a duração formatada em dias, horas e minutos
     */
    public String getFormattedDuration() {
        long days = timeSpent.toDays();
        long hours = timeSpent.toHoursPart();
        long minutes = timeSpent.toMinutesPart();

        if (days > 0) {
            return String.format("%d dias, %d horas e %d minutos", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d horas e %d minutos", hours, minutes);
        } else {
            return String.format("%d minutos", minutes);
        }
    }
}