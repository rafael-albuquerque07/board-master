package br.com.dio.ui;
import br.com.dio.ui.util.TerminalColors;

import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.persistence.entity.CardEntity;
import br.com.dio.service.CardService;

import java.sql.SQLException;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public class CardCreationMenu {
    private final BoardEntity entity;
    private final Scanner scanner;

    public CardCreationMenu(BoardEntity entity, Scanner scanner) {
        this.entity = entity;
        this.scanner = scanner;
    }

    public void execute() throws SQLException {
        var card = new CardEntity();
        System.out.println("\n" + TerminalColors.BLUE_BOLD + "===== CRIAR NOVO CARD =====" + TerminalColors.RESET);
        System.out.println("Informe o título do card:");
        card.setTitle(scanner.next());
        System.out.println("Informe a descrição do card:");
        card.setDescription(scanner.next());
        card.setBoardColumn(entity.getInitialColumn());
        try(var connection = getConnection()){
            new CardService(connection).create(card);
            System.out.println(TerminalColors.success("Card criado com sucesso! ID: " + card.getId()));
        }
    }
}