package sample.contact.hibernate.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="authorities")
public class AuthorityEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    
    private String username;
    
    private String authority;
    
    public AuthorityEntity() {
        super();
    }
    
    @Column(name="id")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    @Column(name="USERNAME", length=50,nullable=false)
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    @Column(name="AUTHORITY",length=50,nullable=false)
    public String getAuthority() {
        return authority;
    }
    public void setAuthority(String authority) {
        this.authority = authority;
    }
}