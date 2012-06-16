package sample.contact.hibernate.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="acl_entry", 
       uniqueConstraints={@UniqueConstraint(name="UNIQUE_CONSTRAINT",
                                            columnNames={"acl_object_identity","ace_order"})})
public class ACLEntryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    
    private ACLObjectIdentityEntity identity;
    
    private int order;
    
    private ACLSIDEntity sid;
    
    private int mask;
    
    private boolean granting;
    
    private boolean auditSuccess;
    
    private boolean auditFailure;

    public ACLEntryEntity() {
        super();
    }

    @Column(name="ID")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JoinColumn(name="acl_object_identity")
    @ManyToOne
    public ACLObjectIdentityEntity getIdentity() {
        return identity;
    }

    public void setIdentity(ACLObjectIdentityEntity identity) {
        this.identity = identity;
    }

    @Column(name="ace_order")
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @ManyToOne
    @JoinColumn(name="sid")
    public ACLSIDEntity getSid() {
        return sid;
    }

    public void setSid(ACLSIDEntity sid) {
        this.sid = sid;
    }

    @Column(name="mask")
    public int getMask() {
        return mask;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

    @Column(name="granting")
    public boolean isGranting() {
        return granting;
    }

    public void setGranting(boolean granting) {
        this.granting = granting;
    }

    @Column(name="audit_success")
    public boolean isAuditSuccess() {
        return auditSuccess;
    }

    public void setAuditSuccess(boolean auditSuccess) {
        this.auditSuccess = auditSuccess;
    }

    @Column(name="audit_failure")
    public boolean isAuditFailure() {
        return auditFailure;
    }

    public void setAuditFailure(boolean auditFailure) {
        this.auditFailure = auditFailure;
    }
}
