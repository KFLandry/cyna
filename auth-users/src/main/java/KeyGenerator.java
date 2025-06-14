import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Base64;

public class KeyGenerator {
    public static void main(String[] args) {
        // Pour HS256 (256 bits minimum requis)
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println("Generated HS256 Key: " + encodedKey);
        // Pour HS512 (512 bits) - encore plus sûr
        Key key512 = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String encodedKey512 = Base64.getEncoder().encodeToString(key512.getEncoded());
        System.out.println("Generated HS512 Key: " + encodedKey512);
    }
}