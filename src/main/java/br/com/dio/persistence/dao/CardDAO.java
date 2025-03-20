package br.com.dio.persistence.dao;

import br.com.dio.dto.CardDetailsDTO;
import br.com.dio.persistence.entity.CardEntity;
import com.mysql.cj.jdbc.StatementImpl;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;

import static br.com.dio.persistence.converter.OffsetDateTimeConverter.toOffsetDateTime;
import static java.util.Objects.nonNull;

/**
 * Classe DAO para operações relacionadas a Cards
 */
@AllArgsConstructor
public class CardDAO {

    private Connection connection;

    /**
     * Insere um novo card no banco de dados
     *
     * @param entity Entidade do card a ser inserida
     * @return Entidade do card com ID atualizado
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    public CardEntity insert(final CardEntity entity) throws SQLException {
        var sql = "INSERT INTO CARDS (title, description, board_column_id) values (?, ?, ?);";
        try(var statement = connection.prepareStatement(sql)){
            var i = 1;
            statement.setString(i ++, entity.getTitle());
            statement.setString(i ++, entity.getDescription());
            statement.setLong(i, entity.getBoardColumn().getId());
            statement.executeUpdate();
            if (statement instanceof StatementImpl impl){
                entity.setId(impl.getLastInsertID());
            }

            // Registra a criação do card no histórico (considera como primeira movimentação)
            registerInitialPlacement(entity.getId(), entity.getBoardColumn().getId());
        }
        return entity;
    }

    /**
     * Move um card para outra coluna
     *
     * @param columnId ID da coluna de destino
     * @param cardId ID do card a ser movido
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    public void moveToColumn(final Long columnId, final Long cardId) throws SQLException {
        // Primeiro, obtém a coluna atual do card
        Long currentColumnId = getCurrentColumnId(cardId);

        // Registra a movimentação no histórico
        if (currentColumnId != null) {
            registerMovement(cardId, currentColumnId, columnId);
        }

        // Atualiza a coluna do card
        var sql = "UPDATE CARDS SET board_column_id = ? WHERE id = ?;";
        try(var statement = connection.prepareStatement(sql)){
            var i = 1;
            statement.setLong(i ++, columnId);
            statement.setLong(i, cardId);
            statement.executeUpdate();
        }
    }

    /**
     * Registra o posicionamento inicial do card na primeira coluna
     *
     * @param cardId ID do card
     * @param columnId ID da coluna inicial
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    private void registerInitialPlacement(Long cardId, Long columnId) throws SQLException {
        // Verifica se tabela CARD_HISTORY existe
        if (tableExists("CARD_HISTORY")) {
            var sql = "INSERT INTO CARD_HISTORY (card_id, to_column_id) VALUES (?, ?);";
            try(var statement = connection.prepareStatement(sql)){
                statement.setLong(1, cardId);
                statement.setLong(2, columnId);
                statement.executeUpdate();
            }
        }
    }

    /**
     * Registra uma movimentação do card entre colunas
     *
     * @param cardId ID do card
     * @param fromColumnId ID da coluna de origem
     * @param toColumnId ID da coluna de destino
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    private void registerMovement(Long cardId, Long fromColumnId, Long toColumnId) throws SQLException {
        // Verifica se tabela CARD_HISTORY existe
        if (tableExists("CARD_HISTORY")) {
            var sql = "INSERT INTO CARD_HISTORY (card_id, from_column_id, to_column_id) VALUES (?, ?, ?);";
            try(var statement = connection.prepareStatement(sql)){
                statement.setLong(1, cardId);
                statement.setLong(2, fromColumnId);
                statement.setLong(3, toColumnId);
                statement.executeUpdate();
            }
        }
    }

    /**
     * Obtém o ID da coluna atual do card
     *
     * @param cardId ID do card
     * @return ID da coluna atual ou null se o card não existir
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    private Long getCurrentColumnId(Long cardId) throws SQLException {
        var sql = "SELECT board_column_id FROM CARDS WHERE id = ?";
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, cardId);
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("board_column_id");
            }
            return null;
        }
    }

    /**
     * Verifica se uma tabela existe no banco de dados
     *
     * @param tableName Nome da tabela a verificar
     * @return true se a tabela existir, false caso contrário
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    private boolean tableExists(String tableName) throws SQLException {
        var sql = """
                SELECT COUNT(*) AS table_exists
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                AND table_name = ?
                """;
        try(var statement = connection.prepareStatement(sql)){
            statement.setString(1, tableName);
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("table_exists") > 0;
            }
            return false;
        }
    }

    /**
     * Busca um card pelo ID com detalhes
     *
     * @param id ID do card a buscar
     * @return Optional contendo os detalhes do card ou vazio se não encontrado
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    public Optional<CardDetailsDTO> findById(final Long id) throws SQLException {
        var sql =
                """
                SELECT c.id,
                       c.title,
                       c.description,
                       b.blocked_at,
                       b.block_reason,
                       c.board_column_id,
                       bc.name,
                       (SELECT COUNT(sub_b.id)
                               FROM BLOCKS sub_b
                              WHERE sub_b.card_id = c.id) blocks_amount
                  FROM CARDS c
                  LEFT JOIN BLOCKS b
                    ON c.id = b.card_id
                   AND b.unblocked_at IS NULL
                 INNER JOIN BOARDS_COLUMNS bc
                    ON bc.id = c.board_column_id
                  WHERE c.id = ?;
                """;
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, id);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            if (resultSet.next()){
                var dto = new CardDetailsDTO(
                        resultSet.getLong("c.id"),
                        resultSet.getString("c.title"),
                        resultSet.getString("c.description"),
                        nonNull(resultSet.getString("b.block_reason")),
                        toOffsetDateTime(resultSet.getTimestamp("b.blocked_at")),
                        resultSet.getString("b.block_reason"),
                        resultSet.getInt("blocks_amount"),
                        resultSet.getLong("c.board_column_id"),
                        resultSet.getString("bc.name")
                );
                return Optional.of(dto);
            }
        }
        return Optional.empty();
    }
}