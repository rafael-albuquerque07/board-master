package br.com.dio.ui.util;

import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardColumnKindEnum;

import java.util.List;
import java.util.function.Predicate;

/**
 * Classe utilitária para validações no sistema
 */
public class ValidationUtils {

    /**
     * Valida o título de um card
     *
     * @param title Título a ser validado
     * @throws IllegalArgumentException se o título não for válido
     */
    public static void validateCardTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("O título do card não pode ser vazio.");
        }

        if (title.length() > 100) {
            throw new IllegalArgumentException("O título do card não pode ter mais de 100 caracteres.");
        }
    }

    /**
     * Valida a descrição de um card
     *
     * @param description Descrição a ser validada
     * @throws IllegalArgumentException se a descrição não for válida
     */
    public static void validateCardDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("A descrição do card não pode ser vazia.");
        }

        if (description.length() > 500) {
            throw new IllegalArgumentException("A descrição do card não pode ter mais de 500 caracteres.");
        }
    }

    /**
     * Valida o motivo de bloqueio/desbloqueio
     *
     * @param reason Motivo a ser validado
     * @throws IllegalArgumentException se o motivo não for válido
     */
    public static void validateBlockReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("O motivo não pode ser vazio.");
        }

        if (reason.length() > 255) {
            throw new IllegalArgumentException("O motivo não pode ter mais de 255 caracteres.");
        }
    }

    /**
     * Valida o nome de um board
     *
     * @param name Nome a ser validado
     * @throws IllegalArgumentException se o nome não for válido
     */
    public static void validateBoardName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do board não pode ser vazio.");
        }

        if (name.length() > 100) {
            throw new IllegalArgumentException("O nome do board não pode ter mais de 100 caracteres.");
        }
    }

    /**
     * Valida o nome de uma coluna
     *
     * @param name Nome a ser validado
     * @throws IllegalArgumentException se o nome não for válido
     */
    public static void validateColumnName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da coluna não pode ser vazio.");
        }

        if (name.length() > 100) {
            throw new IllegalArgumentException("O nome da coluna não pode ter mais de 100 caracteres.");
        }
    }

    /**
     * Valida a estrutura de colunas de um board
     *
     * @param columns Lista de colunas a ser validada
     * @throws IllegalArgumentException se a estrutura não for válida
     */
    public static void validateBoardStructure(List<BoardColumnEntity> columns) {
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("O board deve ter pelo menos uma coluna.");
        }

        // Deve ter exatamente uma coluna INITIAL
        validateColumnCount(columns, BoardColumnKindEnum.INITIAL, 1);

        // Deve ter exatamente uma coluna FINAL
        validateColumnCount(columns, BoardColumnKindEnum.FINAL, 1);

        // Deve ter exatamente uma coluna CANCEL
        validateColumnCount(columns, BoardColumnKindEnum.CANCEL, 1);

        // Validar a ordem das colunas
        BoardColumnEntity initialColumn = findColumnByKind(columns, BoardColumnKindEnum.INITIAL);
        BoardColumnEntity finalColumn = findColumnByKind(columns, BoardColumnKindEnum.FINAL);
        BoardColumnEntity cancelColumn = findColumnByKind(columns, BoardColumnKindEnum.CANCEL);

        if (initialColumn.getOrder() != 0) {
            throw new IllegalArgumentException("A coluna inicial deve ser a primeira coluna (ordem 0).");
        }

        int finalOrder = columns.size() - 2;
        if (finalColumn.getOrder() != finalOrder) {
            throw new IllegalArgumentException("A coluna final deve ser a penúltima coluna (ordem " + finalOrder + ").");
        }

        int cancelOrder = columns.size() - 1;
        if (cancelColumn.getOrder() != cancelOrder) {
            throw new IllegalArgumentException("A coluna de cancelamento deve ser a última coluna (ordem " + cancelOrder + ").");
        }
    }

    /**
     * Valida se um card pode ser movido para uma coluna
     *
     * @param cardColumnOrder Ordem da coluna atual do card
     * @param targetColumnOrder Ordem da coluna de destino
     * @param isCancel Se é movimento para cancelamento
     * @throws IllegalArgumentException se o movimento não for válido
     */
    public static void validateCardMovement(int cardColumnOrder, int targetColumnOrder, boolean isCancel) {
        if (isCancel) {
            return; // Movimento para cancelamento é sempre permitido
        }

        if (targetColumnOrder != cardColumnOrder + 1) {
            throw new IllegalArgumentException(
                    "O card só pode ser movido para a próxima coluna na sequência. " +
                            "Coluna atual: " + cardColumnOrder + ", Coluna destino: " + targetColumnOrder
            );
        }
    }

    /**
     * Verifica se uma coluna tem o número esperado de ocorrências na lista
     *
     * @param columns Lista de colunas
     * @param kind Tipo de coluna
     * @param expectedCount Número esperado de ocorrências
     * @throws IllegalArgumentException se o número não corresponder ao esperado
     */
    private static void validateColumnCount(List<BoardColumnEntity> columns, BoardColumnKindEnum kind, int expectedCount) {
        long count = columns.stream()
                .filter(c -> c.getKind() == kind)
                .count();

        if (count != expectedCount) {
            throw new IllegalArgumentException(
                    "O board deve ter exatamente " + expectedCount + " coluna(s) do tipo " + kind +
                            ", mas tem " + count + "."
            );
        }
    }

    /**
     * Encontra uma coluna pelo tipo
     *
     * @param columns Lista de colunas
     * @param kind Tipo de coluna
     * @return A coluna encontrada
     * @throws IllegalArgumentException se a coluna não for encontrada
     */
    private static BoardColumnEntity findColumnByKind(List<BoardColumnEntity> columns, BoardColumnKindEnum kind) {
        return columns.stream()
                .filter(c -> c.getKind() == kind)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Coluna do tipo " + kind + " não encontrada."));
    }

    /**
     * Valida se um ID não é nulo ou negativo
     *
     * @param id ID a ser validado
     * @param entityName Nome da entidade para mensagem de erro
     * @throws IllegalArgumentException se o ID não for válido
     */
    public static void validateId(Long id, String entityName) {
        if (id == null) {
            throw new IllegalArgumentException("O ID do(a) " + entityName + " não pode ser nulo.");
        }

        if (id <= 0) {
            throw new IllegalArgumentException("O ID do(a) " + entityName + " deve ser positivo.");
        }
    }
}