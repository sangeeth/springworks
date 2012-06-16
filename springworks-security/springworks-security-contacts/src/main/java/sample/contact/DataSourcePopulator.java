/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sample.contact;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.Random;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.domain.AclImpl;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import sample.contact.hibernate.HibernateUtil;

/**
 * Populates the Contacts in-memory database with contact and ACL information.
 *
 * @author Ben Alex
 */
public class DataSourcePopulator implements InitializingBean {
    //~ Instance fields ================================================================================================

    private String createScript; 
    
    private String hibernatePropertiesFile;
    private boolean useHibernate;
    
    JdbcTemplate template;
    private MutableAclService mutableAclService;
    final Random rnd = new Random();
    TransactionTemplate tt;
    final String[] firstNames = {
            "Bob", "Mary", "James", "Jane", "Kristy", "Kirsty", "Kate", "Jeni", "Angela", "Melanie", "Kent", "William",
            "Geoff", "Jeff", "Adrian", "Amanda", "Lisa", "Elizabeth", "Prue", "Richard", "Darin", "Phillip", "Michael",
            "Belinda", "Samantha", "Brian", "Greg", "Matthew"
        };
    final String[] lastNames = {
            "Smith", "Williams", "Jackson", "Rictor", "Nelson", "Fitzgerald", "McAlpine", "Sutherland", "Abbott", "Hall",
            "Edwards", "Gates", "Black", "Brown", "Gray", "Marwell", "Booch", "Johnson", "McTaggart", "Parklin",
            "Findlay", "Robinson", "Giugni", "Lang", "Chi", "Carmichael"
        };
    private int createEntities = 50;

    //~ Methods ========================================================================================================

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(mutableAclService, "mutableAclService required");
        Assert.notNull(template, "dataSource required");
        Assert.notNull(tt, "platformTransactionManager required");

        // Set a user account that will initially own all the created data
        Authentication authRequest = new UsernamePasswordAuthenticationToken("rod", "koala",
                AuthorityUtils.createAuthorityList("ROLE_IGNORED"));
        SecurityContextHolder.getContext().setAuthentication(authRequest);
        
        dropTables();
        createTables();
        
        /*
           Passwords encoded using MD5, NOT in Base64 format, with null as salt
           Encoded password for rod is "koala"
           Encoded password for dianne is "emu"
           Encoded password for scott is "wombat"
           Encoded password for peter is "opal" (but user is disabled)
           Encoded password for bill is "wombat"
           Encoded password for bob is "wombat"
           Encoded password for jane is "wombat"

         */
        template.execute("INSERT INTO users (USERNAME,PASSWORD,ENABLED) VALUES('rod','a564de63c2d0da68cf47586ee05984d7',TRUE);");
        template.execute("INSERT INTO users (USERNAME,PASSWORD,ENABLED) VALUES('dianne','65d15fe9156f9c4bbffd98085992a44e',TRUE);");
        template.execute("INSERT INTO users (USERNAME,PASSWORD,ENABLED) VALUES('scott','2b58af6dddbd072ed27ffc86725d7d3a',TRUE);");
        template.execute("INSERT INTO users (USERNAME,PASSWORD,ENABLED) VALUES('peter','22b5c9accc6e1ba628cedc63a72d57f8',FALSE);");
        template.execute("INSERT INTO users (USERNAME,PASSWORD,ENABLED) VALUES('bill','2b58af6dddbd072ed27ffc86725d7d3a',TRUE);");
        template.execute("INSERT INTO users (USERNAME,PASSWORD,ENABLED) VALUES('bob','2b58af6dddbd072ed27ffc86725d7d3a',TRUE);");
        template.execute("INSERT INTO users (USERNAME,PASSWORD,ENABLED) VALUES('jane','2b58af6dddbd072ed27ffc86725d7d3a',TRUE);");
        template.execute("INSERT INTO authorities (USERNAME,AUTHORITY) VALUES('rod','ROLE_USER');");
        template.execute("INSERT INTO authorities (USERNAME,AUTHORITY) VALUES('rod','ROLE_SUPERVISOR');");
        template.execute("INSERT INTO authorities (USERNAME,AUTHORITY) VALUES('dianne','ROLE_USER');");
        template.execute("INSERT INTO authorities (USERNAME,AUTHORITY) VALUES('scott','ROLE_USER');");
        template.execute("INSERT INTO authorities (USERNAME,AUTHORITY) VALUES('peter','ROLE_USER');");
        template.execute("INSERT INTO authorities (USERNAME,AUTHORITY) VALUES('bill','ROLE_USER');");
        template.execute("INSERT INTO authorities (USERNAME,AUTHORITY) VALUES('bob','ROLE_USER');");
        template.execute("INSERT INTO authorities (USERNAME,AUTHORITY) VALUES('jane','ROLE_USER');");

        template.execute("INSERT INTO contacts VALUES (1, 'John Smith', 'john@somewhere.com');");
        template.execute("INSERT INTO contacts VALUES (2, 'Michael Citizen', 'michael@xyz.com');");
        template.execute("INSERT INTO contacts VALUES (3, 'Joe Bloggs', 'joe@demo.com');");
        template.execute("INSERT INTO contacts VALUES (4, 'Karen Sutherland', 'karen@sutherland.com');");
        template.execute("INSERT INTO contacts VALUES (5, 'Mitchell Howard', 'mitchell@abcdef.com');");
        template.execute("INSERT INTO contacts VALUES (6, 'Rose Costas', 'rose@xyz.com');");
        template.execute("INSERT INTO contacts VALUES (7, 'Amanda Smith', 'amanda@abcdef.com');");
        template.execute("INSERT INTO contacts VALUES (8, 'Cindy Smith', 'cindy@smith.com');");
        template.execute("INSERT INTO contacts VALUES (9, 'Jonathan Citizen', 'jonathan@xyz.com');");

        for (int i = 10; i < createEntities; i++) {
            String[] person = selectPerson();
            template.execute("INSERT INTO contacts VALUES (" + i + ", '" + person[2] + "', '" + person[0].toLowerCase()
                + "@" + person[1].toLowerCase() + ".com');");
        }

        // Create acl_object_identity rows (and also acl_class rows as needed
        for (int i = 1; i < createEntities; i++) {
            final ObjectIdentity objectIdentity = new ObjectIdentityImpl(Contact.class, new Long(i));
            tt.execute(new TransactionCallback<Object>() {
                    public Object doInTransaction(TransactionStatus arg0) {
                        mutableAclService.createAcl(objectIdentity);

                        return null;
                    }
                });
        }

        // Now grant some permissions
        grantPermissions(1, "rod", BasePermission.ADMINISTRATION);
        grantPermissions(2, "rod", BasePermission.READ);
        grantPermissions(3, "rod", BasePermission.READ);
        grantPermissions(3, "rod", BasePermission.WRITE);
        grantPermissions(3, "rod", BasePermission.DELETE);
        grantPermissions(4, "rod", BasePermission.ADMINISTRATION);
        grantPermissions(4, "dianne", BasePermission.ADMINISTRATION);
        grantPermissions(4, "scott", BasePermission.READ);
        grantPermissions(5, "dianne", BasePermission.ADMINISTRATION);
        grantPermissions(5, "dianne", BasePermission.READ);
        grantPermissions(6, "dianne", BasePermission.READ);
        grantPermissions(6, "dianne", BasePermission.WRITE);
        grantPermissions(6, "dianne", BasePermission.DELETE);
        grantPermissions(6, "scott", BasePermission.READ);
        grantPermissions(7, "scott", BasePermission.ADMINISTRATION);
        grantPermissions(8, "dianne", BasePermission.ADMINISTRATION);
        grantPermissions(8, "dianne", BasePermission.READ);
        grantPermissions(8, "scott", BasePermission.READ);
        grantPermissions(9, "scott", BasePermission.ADMINISTRATION);
        grantPermissions(9, "scott", BasePermission.READ);
        grantPermissions(9, "scott", BasePermission.WRITE);
        grantPermissions(9, "scott", BasePermission.DELETE);

        // Now expressly change the owner of the first ten contacts
        // We have to do this last, because "rod" owns all of them (doing it sooner would prevent ACL updates)
        // Note that ownership has no impact on permissions - they're separate (ownership only allows ACl editing)
        changeOwner(5, "dianne");
        changeOwner(6, "dianne");
        changeOwner(7, "scott");
        changeOwner(8, "dianne");
        changeOwner(9, "scott");

        String[] users = {"bill", "bob", "jane"}; // don't want to mess around with consistent sample data
        Permission[] permissions = {BasePermission.ADMINISTRATION, BasePermission.READ, BasePermission.DELETE};

        for (int i = 10; i < createEntities; i++) {
            String user = users[rnd.nextInt(users.length)];
            Permission permission = permissions[rnd.nextInt(permissions.length)];
            grantPermissions(i, user, permission);

            String user2 = users[rnd.nextInt(users.length)];
            Permission permission2 = permissions[rnd.nextInt(permissions.length)];
            grantPermissions(i, user2, permission2);
        }

        SecurityContextHolder.clearContext();
    }
    
    protected void dropTables() {
        String [] tables = {"contacts","authorities","users","acl_entry","acl_object_identity","acl_class","acl_sid"};
        for(String table:tables) {
            dropTable(table);
        }
    }
    
    private void dropTable(String table) {
        try {
            template.execute(String.format("DROP TABLE %s",table));
        } catch(Exception e) {
            System.out.println("Failed to drop tables: " + e.getMessage());
        }
    }
    
    protected void createTables() throws Exception {
        
        if (this.useHibernate) {
            HibernateUtil.configure(this.hibernatePropertiesFile);
        } else {
            System.out.printf("Creating tables using %s\n", this.createScript);
            
            InputStream in = this.getClass().getResourceAsStream(this.createScript);
                  
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            
            SQLParser parser = new SQLParser(reader);
            
            while(parser.hasMore()) {
               String stmt = parser.nextStatement();
               if (stmt.length()>0) {
                   System.out.printf("Executing %s\n",stmt);
                   template.execute(stmt);
               }
            }
            
            reader.close();  
            in.close();
        }
    }

    private void changeOwner(int contactNumber, String newOwnerUsername) {
        AclImpl acl = (AclImpl) mutableAclService.readAclById(new ObjectIdentityImpl(Contact.class,
                    new Long(contactNumber)));
        acl.setOwner(new PrincipalSid(newOwnerUsername));
        updateAclInTransaction(acl);
    }

    public int getCreateEntities() {
        return createEntities;
    }

    private void grantPermissions(int contactNumber, String recipientUsername, Permission permission) {
        AclImpl acl = (AclImpl) mutableAclService.readAclById(new ObjectIdentityImpl(Contact.class,
                    new Long(contactNumber)));
        acl.insertAce(acl.getEntries().size(), permission, new PrincipalSid(recipientUsername), true);
        updateAclInTransaction(acl);
    }

    private String[] selectPerson() {
        String firstName = firstNames[rnd.nextInt(firstNames.length)];
        String lastName = lastNames[rnd.nextInt(lastNames.length)];

        return new String[] {firstName, lastName, firstName + " " + lastName};
    }

    public void setCreateEntities(int createEntities) {
        this.createEntities = createEntities;
    }

    public void setDataSource(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    public void setMutableAclService(MutableAclService mutableAclService) {
        this.mutableAclService = mutableAclService;
    }
    
    public void setCreateScript(String createScript) {
        this.createScript = createScript;
    }
    
    public void setHibernatePropertiesFile(String hibernatePropertiesFile) {
        this.hibernatePropertiesFile = hibernatePropertiesFile;
    }

    public void setUseHibernate(boolean useHibernate) {
        this.useHibernate = useHibernate;
    }

    public void setPlatformTransactionManager(PlatformTransactionManager platformTransactionManager) {
        this.tt = new TransactionTemplate(platformTransactionManager);
    }

    private void updateAclInTransaction(final MutableAcl acl) {
        tt.execute(new TransactionCallback<Object>() {
                public Object doInTransaction(TransactionStatus arg0) {
                    mutableAclService.updateAcl(acl);

                    return null;
                }
            });
    }
    
    static public class SQLParser {
        private PushbackReader reader;
        private StringBuffer sqlStatement;
        private boolean eof;
        
        public SQLParser(Reader reader) {
            this.reader = new PushbackReader(reader);
            sqlStatement = new StringBuffer();
        }
        
        public boolean hasMore() {
            return !this.eof;
        }
        
        public String nextStatement() throws Exception {
            sqlStatement.setLength(0);
            
            boolean eoln = false;
            boolean comment = false;
            boolean stringLiteral = false;
            int ch;
            int nextCh;
            Object token;
            while((ch=reader.read())!=-1) {
                token = (char)ch;
                
                switch(ch) {
                case '\'' :
                    if (!stringLiteral) {
                        stringLiteral = true;
                    } else {
                        nextCh = reader.read();
                        if (nextCh!='\'') {
                            reader.unread(nextCh);
                            stringLiteral = false;
                        } else {
                            token = "''";
                        }
                    }
                    break;
                case '-':
                    nextCh = reader.read();
                    if (nextCh=='-') {
                        comment = true;
                    } else {
                        // push back
                        reader.unread(nextCh);
                    }
                    break;
                case ';':
                    if (!stringLiteral) {
                        eoln = true;
                    }
                    break;
                case '\n':
                    if (comment) {
                        comment = false;
                    }
                    token = " ";
                }
                
                if (eoln) {
                    break;
                } else {
                    if (!comment) {
                        sqlStatement.append(token);
                    }
                }
            }
            
            eof = (ch==-1);
            
            return sqlStatement.toString().trim();
        }
    }    
}
