--liquibase formatted sql
--changeset junior:202508191200
--comment: card_history table create

CREATE TABLE CARD_HISTORY (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              card_id BIGINT NOT NULL,
                              from_column_id BIGINT NULL,
                              to_column_id BIGINT NOT NULL,
                              moved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT cards__card_history_fk FOREIGN KEY (card_id) REFERENCES CARDS(id) ON DELETE CASCADE,
                              CONSTRAINT boards_columns_from__card_history_fk FOREIGN KEY (from_column_id) REFERENCES BOARDS_COLUMNS(id) ON DELETE SET NULL,
                              CONSTRAINT boards_columns_to__card_history_fk FOREIGN KEY (to_column_id) REFERENCES BOARDS_COLUMNS(id) ON DELETE CASCADE
) ENGINE=InnoDB;

--rollback DROP TABLE CARD_HISTORY