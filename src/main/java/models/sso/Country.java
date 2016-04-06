package models.sso;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Country entity.
 */
@Entity
@Table(name = "countries", indexes = {
        @Index(name = "iso3_idx", columnList = "iso3", unique = true),
        @Index(name = "niceName_idx", columnList = "niceName", unique = false),
})
@NamedQueries({
        @NamedQuery(name = "Countries.getAllSortedByNiceName",
                query = "SELECT c FROM Country c ORDER BY c.niceName"),
})
public class Country implements Serializable {

    /**
     * ISO code of the country. Unique.
     */
    @Id
    @Column(unique = true, updatable = false)
    @Size(min = 2, max = 2)
    String iso;

    /**
     * ISO-3 code of the country.
     */
    @Size(min = 3, max = 3)
    String iso3;

    /**
     * English name, uppercase.
     */
    @Size(min = 1, max = 80)
    String name;

    /**
     * English name, nice.
     */
    @Size(min = 1, max = 80)
    String niceName;

    /**
     * Name of the country in native language (wiki).
     */
    @Column(nullable = true)
    @Size(max = 150)
    String nativeName;

    /**
     * Optional numeric code.
     */
    @Column(nullable = true)
    Integer numCode;

    /**
     * Phone code.
     */
    int phoneCode;

    /**
     * Default constructor.
     */
    public Country() {
    }

    /**
     * Constructs country by given parameters.
     *
     * @param iso ISO code.
     * @param iso3 ISO-3 code.
     * @param name Name, uppercase.
     * @param niceName Nice name.
     * @param phoneCode Phone code.
     */
    public Country(String iso, String iso3, String name, String niceName, int phoneCode) {
        this.iso = iso.trim().toUpperCase();
        this.iso3 = iso3.trim().toUpperCase();
        this.name = name.toUpperCase();
        this.niceName = niceName.trim();
        this.nativeName = this.niceName;
        this.phoneCode = phoneCode;
    }

    /**
     * Returns ISO code.
     *
     * @return ISO code.
     */
    public String getIso() {
        return iso;
    }

    /**
     * Sets ISO code. It is normalized by trimming and converting to uppercase.
     *
     * @param iso ISO code.
     */
    public void setIso(String iso) {
        this.iso = iso.trim().toUpperCase();
    }

    /**
     * ISO-3 code.
     *
     * @return ISO-3 code.
     */
    public String getIso3() {
        return iso3;
    }

    /**
     * Sets ISO-3 code. It is normalized by trimming and converting to uppercase.
     *
     * @param iso3 ISO-3 code.
     */
    public void setIso3(String iso3) {
        this.iso3 = iso3.trim().toUpperCase();
    }

    /**
     * Returns country name in upper case.
     *
     * @return country name in upper case.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets country name.  It is normalized by trimming and converting to uppercase.
     *
     * @param name Country name to set.
     */
    public void setName(String name) {
        this.name = name.trim().toUpperCase();
    }

    /**
     * Nice name.
     *
     * @return Nice name.
     */
    public String getNiceName() {
        return niceName;
    }

    /**
     * Sets nice country name. It is normalized by trimming.
     *
     * @param niceName Nice name.
     */
    public void setNiceName(String niceName) {
        this.niceName = niceName.trim();
    }

    /**
     * Convenient method that returns native name if it is present or nice name if not.
     *
     * @return Native or nice name.
     */
    public String getNativeOrNiceName() {
        return nativeName != null ? nativeName : niceName;
    }

    /**
     * Native name.
     *
     * @return Native name.
     */
    public String getNativeName() {
        return nativeName;
    }

    /**
     * Sets native country name.
     *
     * @param nativeName Native name.
     */
    public void setNativeName(String nativeName) {
        this.nativeName = nativeName;
    }

    /**
     * Numeric code.
     *
     * @return Numeric code.
     */
    public Integer getNumCode() {
        return numCode;
    }

    /**
     * Sets numeric code.
     *
     * @param numCode Numeric code.
     */
    public void setNumCode(Integer numCode) {
        this.numCode = numCode;
    }

    /**
     * Phone code.
     *
     * @return Phone code.
     */
    public int getPhoneCode() {
        return phoneCode;
    }

    /**
     * Sets phone code.
     *
     * @param phoneCode Phone code.
     */
    public void setPhoneCode(int phoneCode) {
        this.phoneCode = phoneCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Country country = (Country) o;
        return iso.equals(country.iso);

    }

    @Override
    public int hashCode() {
        return iso.hashCode();
    }

    private static final long serialVersionUID = 1L;
}
