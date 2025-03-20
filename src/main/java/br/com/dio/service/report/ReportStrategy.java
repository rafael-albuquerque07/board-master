package br.com.dio.service.report;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface para implementação do padrão Strategy para geração de relatórios.
 * Cada tipo de relatório implementa esta interface.
 *
 * @param <T> Tipo do DTO que será retornado pelo relatório
 */
public interface ReportStrategy<T> {

    /**
     * Gera um relatório para um board específico
     *
     * @param boardId ID do board para gerar o relatório
     * @param connection Conexão com o banco de dados
     * @return Lista de DTOs com os dados do relatório
     * @throws SQLException se houver erro no acesso ao banco
     */
    List<T> generateReport(Long boardId, Connection connection) throws SQLException;

    /**
     * Exibe o relatório no console de forma formatada
     *
     * @param reportData Lista de DTOs com os dados do relatório
     */
    void displayReport(List<T> reportData);

    /**
     * Gera o relatório e exibe no console
     *
     * @param boardId ID do board para gerar o relatório
     * @param boardName Nome do board (para exibição)
     * @param connection Conexão com o banco de dados
     * @throws SQLException se houver erro no acesso ao banco
     */
    default void executeReport(Long boardId, String boardName, Connection connection) throws SQLException {
        List<T> reportData = generateReport(boardId, connection);
        System.out.println("Relatório para o board: " + boardName + " (ID: " + boardId + ")");
        displayReport(reportData);
    }
}