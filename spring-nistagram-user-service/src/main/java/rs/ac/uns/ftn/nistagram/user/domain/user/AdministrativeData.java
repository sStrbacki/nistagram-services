package rs.ac.uns.ftn.nistagram.user.domain.user;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Embeddable;

@Data
@ToString
@Embeddable
public class AdministrativeData {

    private boolean verified;
    private boolean banned;

    public AdministrativeData() {
        this.verified = false;
        this.banned = false;
    }

}
