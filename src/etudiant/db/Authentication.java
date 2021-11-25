package etudiant.db;

import etudiant.model.Etudiant;
import org.springframework.security.crypto.bcrypt.BCrypt;
import java.sql.*;

public class Authentication {
    private final Connection connection;

    public Authentication() throws Exception {
        connection = new DbConnection().connection;
    }

    public Etudiant createAccount(String lName, String fName, String email, String pwd) throws Exception {
        Etudiant etudiant = null;
        try {
            if(alreadyExist(email))
                throw new Exception("Adresse e-mail déjà utilisée");
            else {
                String sel = BCrypt.gensalt();
                String pwdHash = BCrypt.hashpw(pwd, sel);
                PreparedStatement ps = connection.prepareStatement
                        ("INSERT INTO projet_sql.etudiants(nom, prenom, email, mot_de_passe)" +
                                "VALUES (?, ?, ?, ?)");
                ps.setString(1, lName);
                ps.setString(2, fName);
                ps.setString(3, email);
                ps.setString(4, pwdHash);
                ps.execute();
                etudiant = authenticate(email, pwd);
                return etudiant;
            }
        }catch (Exception e){
            throw new Exception("err:email_alredy_used");
        }
    }

    public Etudiant authenticate(String email, String pwd) throws Exception {
        Etudiant etudiant;
        try {
            PreparedStatement ps = connection.prepareStatement
                    ("SELECT id_etudiant, nom, prenom, bloc, email, mot_de_passe " +
                         "FROM projet_sql.etudiants " +
                         "WHERE email = ?;");
            ps.setString(1,email);
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                throw new Exception("NO USER");
            else {
                String dbPwd = rs.getString("mot_de_passe");
                String sel = BCrypt.gensalt();
                if (BCrypt.checkpw(pwd, dbPwd)){
                    etudiant = new Etudiant();
                    etudiant.setId(rs.getInt("id_etudiant"));
                    etudiant.setNom(rs.getString("nom"));
                    etudiant.setPrenom(rs.getString("prenom"));
                    etudiant.setBloc( rs.getInt("bloc"));
                    etudiant.setEmail(rs.getString("email"));
                }
                else{
                    throw new Exception("WRONG PASSWORD");
                }
            }
        } catch (SQLException se) {
            throw new Exception("AUTH_FAIL");
        }
        return etudiant;
    }

    public boolean alreadyExist(String email) throws Exception {
        try {
            PreparedStatement ps = connection.prepareStatement
                    ("SELECT id_etudiant, nom, prenom FROM projet_sql.etudiants WHERE email = ?;");
            ps.setString(1,email);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return true;
            else return false;
        }catch (Exception e){
            throw new Exception("err iconnue");
        }
    }



}