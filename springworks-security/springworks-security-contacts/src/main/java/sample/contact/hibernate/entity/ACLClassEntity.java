package sample.contact.hibernate.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/*
CREATE TABLE ACL_CLASS(" +
                    "ID BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 100) NOT NULL PRIMARY KEY," +
                    "CLASS VARCHAR_IGNORECASE(100) NOT NULL," +
                    "CONSTRAINT UNIQUE_UK_2 UNIQUE(CLASS));
 */
@Entity
@Table(name="acl_class", 
       uniqueConstraints={@UniqueConstraint(name="UNIQUE_CONSTRAINT",
                                            columnNames={"class"})})
public class ACLClassEntity {
    private Long id;
    
    private String name;
    
    private List<ACLObjectIdentityEntity> objects;

    public ACLClassEntity() {
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

    @Column(name="class", nullable=false, length=100)
    public String getName() {
        return name;
    }

    public void setName(String className) {
        this.name = className;
    }

    @OneToMany(fetch=FetchType.LAZY,mappedBy="type")
    public List<ACLObjectIdentityEntity> getObjects() {
        return objects;
    }

    public void setObjects(List<ACLObjectIdentityEntity> objects) {
        this.objects = objects;
    }
}
