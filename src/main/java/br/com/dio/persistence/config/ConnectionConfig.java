package br.com.dio.persistence.config;

import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static lombok.AccessLevel.PRIVATE;

/**
 * Configuração de conexão com o banco de dados
 * Esta classe fornece acesso ao banco de dados MySQL hospedado no Railway
 */
@NoArgsConstructor(access = PRIVATE)
public final class ConnectionConfig {

    /**
     * Obtém uma conexão com o banco de dados
     *
     * @return Conexão com o banco de dados
     * @throws SQLException se ocorrer erro ao conectar ao banco
     */
    public static Connection getConnection() throws SQLException {
        var url = "jdbc:mysql://shortline.proxy.rlwy.net:56967/railway";
        var user = "root";
        var password = "EVKYcvbbiapdQKkyQkcTUBcCKgQkHllZ";
        var connection = DriverManager.getConnection(url, user, password);
        connection.setAutoCommit(false);
        return connection;
    }

}