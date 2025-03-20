package br.com.dio.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO que representa informações detalhadas sobre um bloqueio de card
 */
public record CardBlockingReportDTO(
        Long id,
        String title,
        LocalDateTime blockedAt,
        String blockReason,
        LocalDateTime unblockedAt,
        String unblockReason,
        Duration blockedTime
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Verifica se o card ainda está bloqueado
     * @return true se o card ainda estiver bloqueado
     */
    public boolean isStillBlocked() {
        return unblockedAt == null;
    }

    /**
     * Retorna a data de bloqueio formatada
     * @return String formatada com data e hora do bloqueio
     */
    public String getFormattedBlockedAt() {
        return blockedAt.format(DATE_FORMATTER);
    }

    /**
     * Retorna a data de desbloqueio formatada ou "Ainda bloqueado"
     * @return String formatada com data e hora do desbloqueio ou status
     */
    public String getFormattedUnblockedAt() {
        return unblockedAt != null ? unblockedAt.format(DATE_FORMATTER) : "Ainda bloqueado";
    }

    /**
     * Retorna a duração do bloqueio formatada
     * @return String com a duração formatada em dias, horas e minutos
     */
    public String getFormattedDuration() {
        long days = blockedTime.toDays();
        long hours = blockedTime.toHoursPart();
        long minutes = blockedTime.toMinutesPart();

        if (days > 0) {
            return String.format("%d dias, %d horas e %d minutos", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d horas e %d minutos", hours, minutes);
        } else {
            return String.format("%d minutos", minutes);
        }
    }
}