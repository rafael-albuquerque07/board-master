package br.com.dio.persistence.factory;

import br.com.dio.persistence.dao.*;
import lombok.AllArgsConstructor;

import java.sql.Connection;

@AllArgsConstructor
public class DAOFactory {

    private final Connection connection;

    public BoardDAO createBoardDAO() {
        return new BoardDAO(connection);
    }

    public BoardColumnDAO createBoardColumnDAO() {
        return new BoardColumnDAO(connection);
    }

    public CardDAO createCardDAO() {
        return new CardDAO(connection);
    }

    public BlockDAO createBlockDAO() {
        return new BlockDAO(connection);
    }
}