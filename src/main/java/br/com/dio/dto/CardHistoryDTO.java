package br.com.dio.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO que representa um registro de movimentação de card entre colunas
 */
public record CardHistoryDTO(
        Long id,
        Long cardId,
        String cardTitle,
        Long fromColumnId,
        String fromColumnName,
        Long toColumnId,
        String toColumnName,
        LocalDateTime movedAt
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Retorna a data de movimentação formatada
     * @return String formatada com data e hora da movimentação
     */
    public String getFormattedMovedAt() {
        return movedAt.format(DATE_FORMATTER);
    }

    /**
     * Verifica se é o posicionamento inicial do card
     * @return true se for a primeira movimentação do card
     */
    public boolean isInitialPlacement() {
        return fromColumnId == null;
    }
}