package br.com.dio.persistence.dao;

import br.com.dio.persistence.entity.BoardEntity;
import com.mysql.cj.jdbc.StatementImpl;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe DAO para operações relacionadas a Boards
 */
@AllArgsConstructor
public class BoardDAO {

    private Connection connection;

    /**
     * Insere um novo board no banco de dados
     *
     * @param entity Entidade do board a ser inserida
     * @return Entidade do board com ID atualizado
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    public BoardEntity insert(final BoardEntity entity) throws SQLException {
        var sql = "INSERT INTO BOARDS (name) values (?);";
        try(var statement = connection.prepareStatement(sql)){
            statement.setString(1, entity.getName());
            statement.executeUpdate();
            if (statement instanceof StatementImpl impl){
                entity.setId(impl.getLastInsertID());
            }
        }
        return entity;
    }

    /**
     * Exclui um board pelo ID
     *
     * @param id ID do board a ser excluído
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    public void delete(final Long id) throws SQLException {
        var sql = "DELETE FROM BOARDS WHERE id = ?;";
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    /**
     * Atualiza o nome de um board
     *
     * @param id ID do board a ser atualizado
     * @param newName Novo nome para o board
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    public void updateName(final Long id, final String newName) throws SQLException {
        var sql = "UPDATE BOARDS SET name = ? WHERE id = ?;";
        try(var statement = connection.prepareStatement(sql)){
            statement.setString(1, newName);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }

    /**
     * Busca um board pelo ID
     *
     * @param id ID do board a buscar
     * @return Optional contendo o board ou vazio se não encontrado
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    public Optional<BoardEntity> findById(final Long id) throws SQLException {
        var sql = "SELECT id, name FROM BOARDS WHERE id = ?;";
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, id);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            if (resultSet.next()){
                var entity = new BoardEntity();
                entity.setId(resultSet.getLong("id"));
                entity.setName(resultSet.getString("name"));
                return Optional.of(entity);
            }
            return Optional.empty();
        }
    }

    /**
     * Lista todos os boards
     *
     * @return Lista de todos os boards
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    public List<BoardEntity> findAll() throws SQLException {
        List<BoardEntity> boards = new ArrayList<>();
        var sql = "SELECT id, name FROM BOARDS ORDER BY id;";
        try(var statement = connection.prepareStatement(sql)){
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            while (resultSet.next()){
                var entity = new BoardEntity();
                entity.setId(resultSet.getLong("id"));
                entity.setName(resultSet.getString("name"));
                boards.add(entity);
            }
        }
        return boards;
    }

    /**
     * Verifica se um board existe pelo ID
     *
     * @param id ID do board a verificar
     * @return true se o board existe, false caso contrário
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    public boolean exists(final Long id) throws SQLException {
        var sql = "SELECT 1 FROM BOARDS WHERE id = ?;";
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, id);
            statement.executeQuery();
            return statement.getResultSet().next();
        }
    }

    /**
     * Conta o número total de boards
     *
     * @return Número total de boards
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    public int countAll() throws SQLException {
        var sql = "SELECT COUNT(*) AS total FROM BOARDS;";
        try(var statement = connection.prepareStatement(sql)){
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            if (resultSet.next()){
                return resultSet.getInt("total");
            }
            return 0;
        }
    }

    /**
     * Busca o maior ID de board atualmente em uso
     *
     * @return Maior ID em uso ou 0 se não houver boards
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    public long findMaxId() throws SQLException {
        var sql = "SELECT MAX(id) AS max_id FROM BOARDS;";
        try(var statement = connection.prepareStatement(sql)){
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            if (resultSet.next() && resultSet.getObject("max_id") != null){
                return resultSet.getLong("max_id");
            }
            return 0;
        }
    }

    /**
     * Busca o menor ID disponível (não utilizado) para boards
     *
     * @return Menor ID disponível
     * @throws SQLException se ocorrer erro no acesso ao banco
     */
    public long findMinAvailableId() throws SQLException {
        var sql = """
            SELECT MIN(t.id + 1) AS next_id 
            FROM (SELECT 0 AS id UNION SELECT id FROM BOARDS) t 
            WHERE NOT EXISTS (SELECT 1 FROM BOARDS b WHERE b.id = t.id + 1) 
            AND t.id + 1 > 0;
        """;
        try(var statement = connection.prepareStatement(sql)){
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            if (resultSet.next() && resultSet.getObject("next_id") != null){
                return resultSet.getLong("next_id");
            }
            return 1; // Padrão se não houver lacunas
        }
    }
}