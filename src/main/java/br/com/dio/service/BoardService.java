package br.com.dio.service;

import br.com.dio.persistence.dao.BoardColumnDAO;
import br.com.dio.persistence.dao.BoardDAO;
import br.com.dio.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Serviço responsável pelas operações de negócio relacionadas a boards
 */
@AllArgsConstructor
public class BoardService {

    private final Connection connection;

    /**
     * Insere um novo board no banco de dados com suas colunas
     *
     * @param entity Entidade do board a ser inserida
     * @return Entidade do board com ID atualizado
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    public BoardEntity insert(final BoardEntity entity) throws SQLException {
        // Validações básicas
        if (entity.getName() == null || entity.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do board não pode ser vazio");
        }

        var dao = new BoardDAO(connection);
        var boardColumnDAO = new BoardColumnDAO(connection);
        try {
            dao.insert(entity);
            var columns = entity.getBoardColumns().stream().map(c -> {
                c.setBoard(entity);
                return c;
            }).toList();

            for (var column : columns) {
                boardColumnDAO.insert(column);
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
        return entity;
    }

    /**
     * Exclui um board pelo ID e reseta o contador de auto incremento
     *
     * @param id ID do board a ser excluído
     * @return true se o board foi excluído, false se não existia
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    public boolean delete(final Long id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID do board inválido");
        }

        var dao = new BoardDAO(connection);
        try {
            if (!dao.exists(id)) {
                return false;
            }

            dao.delete(id);

            // Reseta o contador de auto incremento para o menor ID disponível
            resetAutoIncrement();

            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    /**
     * Atualiza o nome de um board existente
     *
     * @param id ID do board a ser atualizado
     * @param newName Novo nome para o board
     * @return true se o board foi atualizado, false se não existia
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    public boolean updateName(final Long id, final String newName) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID do board inválido");
        }

        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do board não pode ser vazio");
        }

        var dao = new BoardDAO(connection);
        try {
            if (!dao.exists(id)) {
                return false;
            }

            dao.updateName(id, newName);
            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    /**
     * Reseta o contador de auto incremento da tabela BOARDS
     * para o menor ID disponível após exclusão
     *
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    private void resetAutoIncrement() throws SQLException {
        // Encontra o valor mínimo disponível para o próximo ID
        String findMinAvailableId = """
            SELECT MIN(t.id + 1) AS next_id 
            FROM (SELECT 0 AS id UNION SELECT id FROM BOARDS) t 
            WHERE NOT EXISTS (SELECT 1 FROM BOARDS b WHERE b.id = t.id + 1) 
            AND t.id + 1 > 0
        """;

        try (var findStatement = connection.prepareStatement(findMinAvailableId)) {
            var resultSet = findStatement.executeQuery();
            if (resultSet.next()) {
                int nextId = resultSet.getInt("next_id");
                if (nextId > 0) {
                    // Ajusta o contador de auto incremento
                    String resetSql = "ALTER TABLE BOARDS AUTO_INCREMENT = " + nextId;
                    try (var resetStatement = connection.prepareStatement(resetSql)) {
                        resetStatement.executeUpdate();
                    }
                }
            }
        }
    }

    /**
     * Conta o número total de boards no sistema
     *
     * @return Número total de boards
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    public int countBoards() throws SQLException {
        var dao = new BoardDAO(connection);
        return dao.countAll();
    }

    /**
     * Lista todos os boards do sistema com informações básicas
     *
     * @return Lista de boards
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    public List<BoardEntity> listAllBoards() throws SQLException {
        var dao = new BoardDAO(connection);
        return dao.findAll();
    }
}