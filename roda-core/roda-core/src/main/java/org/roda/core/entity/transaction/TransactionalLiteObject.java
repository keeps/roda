package org.roda.core.entity.transaction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Entity
@Table(name = "TX_LITE_OBJECT")
public class TransactionalLiteObject implements Serializable {
    @Serial
    private static final long serialVersionUID = -3676056836840227015L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = false)
    private TransactionLog transactionLog;

    @Column(name = "lite_object", length = 255)
    private String liteObject;

    public TransactionalLiteObject() {
    }

    public TransactionalLiteObject(String liteObject) {
        this.liteObject = liteObject;
    }

    public String getLiteObject() {
        return liteObject;
    }

    public void setTransactionLog(TransactionLog transactionLog) {
        this.transactionLog = transactionLog;
    }
}
